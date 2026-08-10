// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// azure.go — Azure 虚拟机服务发现：通过 ARM API 枚举 VM/VMSS 并解析网卡 IP。

package azure

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"math/rand"
	"net"
	"net/http"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/Azure/azure-sdk-for-go/sdk/azcore"
	"github.com/Azure/azure-sdk-for-go/sdk/azcore/arm"
	"github.com/Azure/azure-sdk-for-go/sdk/azcore/cloud"
	"github.com/Azure/azure-sdk-for-go/sdk/azcore/policy"
	"github.com/Azure/azure-sdk-for-go/sdk/azcore/runtime"
	"github.com/Azure/azure-sdk-for-go/sdk/azcore/to"
	"github.com/Azure/azure-sdk-for-go/sdk/azidentity"
	"github.com/Azure/azure-sdk-for-go/sdk/resourcemanager/compute/armcompute/v5"
	"github.com/Azure/azure-sdk-for-go/sdk/resourcemanager/network/armnetwork/v4"
	cache "github.com/Code-Hex/go-generics-cache"
	"github.com/Code-Hex/go-generics-cache/policy/lru"
	"github.com/prometheus/client_golang/prometheus"
	config_util "github.com/prometheus/common/config"
	"github.com/prometheus/common/model"
	"github.com/prometheus/common/promslog"
	"github.com/prometheus/common/version"

	"github.com/prometheus/prometheus/discovery"
	"github.com/prometheus/prometheus/discovery/refresh"
	"github.com/prometheus/prometheus/discovery/targetgroup"
	"github.com/prometheus/prometheus/util/strutil"
)

const (
	azureLabel                     = model.MetaLabelPrefix + "azure_"
	azureLabelSubscriptionID       = azureLabel + "subscription_id"
	azureLabelTenantID             = azureLabel + "tenant_id"
	azureLabelMachineID            = azureLabel + "machine_id"
	azureLabelMachineResourceGroup = azureLabel + "machine_resource_group"
	azureLabelMachineName          = azureLabel + "machine_name"
	azureLabelMachineComputerName  = azureLabel + "machine_computer_name"
	azureLabelMachineOSType        = azureLabel + "machine_os_type"
	azureLabelMachineLocation      = azureLabel + "machine_location"
	azureLabelMachinePrivateIP     = azureLabel + "machine_private_ip"
	azureLabelMachinePublicIP      = azureLabel + "machine_public_ip"
	azureLabelMachineTag           = azureLabel + "machine_tag_"
	azureLabelMachineScaleSet      = azureLabel + "machine_scale_set"
	azureLabelMachineSize          = azureLabel + "machine_size"

	authMethodOAuth            = "OAuth"
	authMethodSDK              = "SDK"
	authMethodManagedIdentity  = "ManagedIdentity"
	authMethodWorkloadIdentity = "WorkloadIdentity"
)

// DefaultSDConfig 为 Azure 服务发现的默认配置（OAuth 认证、5 分钟刷新）。
var DefaultSDConfig = SDConfig{
	Port:                 80,
	RefreshInterval:      model.Duration(5 * time.Minute),
	Environment:          "AzurePublicCloud",
	AuthenticationMethod: authMethodOAuth,
	HTTPClientConfig:     config_util.DefaultHTTPClientConfig,
}

var environments = map[string]cloud.Configuration{
	"AZURECHINACLOUD":        cloud.AzureChina,
	"AZURECLOUD":             cloud.AzurePublic,
	"AZUREGERMANCLOUD":       cloud.AzurePublic,
	"AZUREPUBLICCLOUD":       cloud.AzurePublic,
	"AZUREUSGOVERNMENT":      cloud.AzureGovernment,
	"AZUREUSGOVERNMENTCLOUD": cloud.AzureGovernment,
}

// CloudConfigurationFromName 根据环境名称（如 AzurePublicCloud）返回 Azure 云配置。
func CloudConfigurationFromName(name string) (cloud.Configuration, error) {
	name = strings.ToUpper(name)
	env, ok := environments[name]
	if !ok {
		return env, fmt.Errorf("there is no cloud configuration matching the name %q", name)
	}

	return env, nil
}

func init() {
	discovery.RegisterConfig(&SDConfig{})
}

// SDConfig 定义基于 Azure Resource Manager 的服务发现配置。
type SDConfig struct {
	Environment          string             `yaml:"environment,omitempty"`
	Port                 int                `yaml:"port"`
	SubscriptionID       string             `yaml:"subscription_id"`
	TenantID             string             `yaml:"tenant_id,omitempty"`
	ClientID             string             `yaml:"client_id,omitempty"`
	ClientSecret         config_util.Secret `yaml:"client_secret,omitempty"`
	RefreshInterval      model.Duration     `yaml:"refresh_interval,omitempty"`
	AuthenticationMethod string             `yaml:"authentication_method,omitempty"`
	ResourceGroup        string             `yaml:"resource_group,omitempty"`

	HTTPClientConfig config_util.HTTPClientConfig `yaml:",inline"`
}

// NewDiscovererMetrics 实现 discovery.Config，返回对应服务的发现器指标。
func (*SDConfig) NewDiscovererMetrics(reg prometheus.Registerer, rmi discovery.RefreshMetricsInstantiator) discovery.DiscovererMetrics {
	return newDiscovererMetrics(reg, rmi)
}

// Name 返回配置机制名称 "azure"。
func (*SDConfig) Name() string { return "azure" }

// NewDiscoverer 根据 SDConfig 创建 Azure Discovery 发现器。
func (c *SDConfig) NewDiscoverer(opts discovery.DiscovererOptions) (discovery.Discoverer, error) {
	return NewDiscovery(c, opts)
}

func validateAuthParam(param, name string) error {
	if param == "" {
		return fmt.Errorf("azure SD configuration requires a %s", name)
	}
	return nil
}

// UnmarshalYAML 解析 YAML 配置并校验订阅 ID 与认证参数。
func (c *SDConfig) UnmarshalYAML(unmarshal func(any) error) error {
	*c = DefaultSDConfig
	type plain SDConfig
	err := unmarshal((*plain)(c))
	if err != nil {
		return err
	}
	if err = validateAuthParam(c.SubscriptionID, "subscription_id"); err != nil {
		return err
	}

	if c.AuthenticationMethod == authMethodOAuth {
		if err = validateAuthParam(c.TenantID, "tenant_id"); err != nil {
			return err
		}
		if err = validateAuthParam(c.ClientID, "client_id"); err != nil {
			return err
		}
		if err = validateAuthParam(string(c.ClientSecret), "client_secret"); err != nil {
			return err
		}
	}

	if c.AuthenticationMethod != authMethodOAuth && c.AuthenticationMethod != authMethodManagedIdentity && c.AuthenticationMethod != authMethodSDK && c.AuthenticationMethod != authMethodWorkloadIdentity {
		return fmt.Errorf("unknown authentication_type %q. Supported types are %q, %q, %q or %q", c.AuthenticationMethod, authMethodOAuth, authMethodManagedIdentity, authMethodSDK, authMethodWorkloadIdentity)
	}

	return c.HTTPClientConfig.Validate()
}

// Discovery 定期执行 Azure 服务发现，实现 Discoverer 接口。
type Discovery struct {
	*refresh.Discovery
	logger  *slog.Logger
	cfg     *SDConfig
	port    int
	cache   *cache.Cache[string, *armnetwork.Interface]
	metrics *azureMetrics
}

// NewDiscovery 创建 Discovery，按 RefreshInterval 周期性刷新 Azure VM 目标。
func NewDiscovery(cfg *SDConfig, opts discovery.DiscovererOptions) (*Discovery, error) {
	m, ok := opts.Metrics.(*azureMetrics)
	if !ok {
		return nil, errors.New("invalid discovery metrics type")
	}

	if opts.Logger == nil {
		opts.Logger = promslog.NewNopLogger()
	}
	l := cache.New(cache.AsLRU[string, *armnetwork.Interface](lru.WithCapacity(5000)))
	d := &Discovery{
		cfg:     cfg,
		port:    cfg.Port,
		logger:  opts.Logger,
		cache:   l,
		metrics: m,
	}

	d.Discovery = refresh.NewDiscovery(
		refresh.Options{
			Logger:              opts.Logger,
			Mech:                "azure",
			SetName:             opts.SetName,
			Interval:            time.Duration(cfg.RefreshInterval),
			RefreshF:            d.refresh,
			MetricsInstantiator: m.refreshMetrics,
		},
	)

	return d, nil
}

type client interface {
	getVMs(ctx context.Context, resourceGroup string) ([]virtualMachine, error)
	getScaleSets(ctx context.Context, resourceGroup string) ([]armcompute.VirtualMachineScaleSet, error)
	getScaleSetVMs(ctx context.Context, scaleSet armcompute.VirtualMachineScaleSet) ([]virtualMachine, error)
	getVMNetworkInterfaceByID(ctx context.Context, networkInterfaceID string) (*armnetwork.Interface, error)
	getVMScaleSetVMNetworkInterfaceByID(ctx context.Context, networkInterfaceID, scaleSetName, instanceID string) (*armnetwork.Interface, error)
}

// azureClient 聚合 Azure 计算与网络 ARM 客户端，供发现流程调用。
type azureClient struct {
	nic    interfacesClientAdapter
	vm     virtualMachinesClientAdapter
	vmss   virtualMachineScaleSetsClientAdapter
	vmssvm virtualMachineScaleSetVMsClientAdapter
	logger *slog.Logger
}

var _ client = &azureClient{}

// 以下 *ClientAdapter 类型仅保留发现所需 ARM 操作，隐藏具体 SDK 客户端以减小二进制。

// virtualMachinesClientAdapter 适配虚拟机 List/ListAll 分页 API。
type virtualMachinesClientAdapter struct {
	newListAllPager func(options *armcompute.VirtualMachinesClientListAllOptions) *runtime.Pager[armcompute.VirtualMachinesClientListAllResponse]
	newListPager    func(resourceGroupName string, options *armcompute.VirtualMachinesClientListOptions) *runtime.Pager[armcompute.VirtualMachinesClientListResponse]
}

func newVirtualMachinesClientAdapter(c *armcompute.VirtualMachinesClient) virtualMachinesClientAdapter {
	return virtualMachinesClientAdapter{
		newListAllPager: c.NewListAllPager,
		newListPager:    c.NewListPager,
	}
}

// NewListAllPager 创建订阅级虚拟机分页迭代器。
func (a virtualMachinesClientAdapter) NewListAllPager(options *armcompute.VirtualMachinesClientListAllOptions) *runtime.Pager[armcompute.VirtualMachinesClientListAllResponse] {
	return a.newListAllPager(options)
}

// NewListPager 创建资源组内虚拟机分页迭代器。
func (a virtualMachinesClientAdapter) NewListPager(resourceGroupName string, options *armcompute.VirtualMachinesClientListOptions) *runtime.Pager[armcompute.VirtualMachinesClientListResponse] {
	return a.newListPager(resourceGroupName, options)
}

// virtualMachineScaleSetsClientAdapter 适配虚拟机规模集 List API。
type virtualMachineScaleSetsClientAdapter struct {
	newListAllPager func(options *armcompute.VirtualMachineScaleSetsClientListAllOptions) *runtime.Pager[armcompute.VirtualMachineScaleSetsClientListAllResponse]
	newListPager    func(resourceGroupName string, options *armcompute.VirtualMachineScaleSetsClientListOptions) *runtime.Pager[armcompute.VirtualMachineScaleSetsClientListResponse]
}

func newVirtualMachineScaleSetsClientAdapter(c *armcompute.VirtualMachineScaleSetsClient) virtualMachineScaleSetsClientAdapter {
	return virtualMachineScaleSetsClientAdapter{
		newListAllPager: c.NewListAllPager,
		newListPager:    c.NewListPager,
	}
}

// NewListAllPager lists all scale sets in the subscription.
func (a virtualMachineScaleSetsClientAdapter) NewListAllPager(options *armcompute.VirtualMachineScaleSetsClientListAllOptions) *runtime.Pager[armcompute.VirtualMachineScaleSetsClientListAllResponse] {
	return a.newListAllPager(options)
}

// NewListPager lists the scale sets in a resource group.
func (a virtualMachineScaleSetsClientAdapter) NewListPager(resourceGroupName string, options *armcompute.VirtualMachineScaleSetsClientListOptions) *runtime.Pager[armcompute.VirtualMachineScaleSetsClientListResponse] {
	return a.newListPager(resourceGroupName, options)
}

// virtualMachineScaleSetVMsClientAdapter 适配规模集内 VM 实例 List API。
type virtualMachineScaleSetVMsClientAdapter struct {
	newListPager func(resourceGroupName, virtualMachineScaleSetName string, options *armcompute.VirtualMachineScaleSetVMsClientListOptions) *runtime.Pager[armcompute.VirtualMachineScaleSetVMsClientListResponse]
}

func newVirtualMachineScaleSetVMsClientAdapter(c *armcompute.VirtualMachineScaleSetVMsClient) virtualMachineScaleSetVMsClientAdapter {
	return virtualMachineScaleSetVMsClientAdapter{
		newListPager: c.NewListPager,
	}
}

// NewListPager lists the virtual machines of a scale set.
func (a virtualMachineScaleSetVMsClientAdapter) NewListPager(resourceGroupName, virtualMachineScaleSetName string, options *armcompute.VirtualMachineScaleSetVMsClientListOptions) *runtime.Pager[armcompute.VirtualMachineScaleSetVMsClientListResponse] {
	return a.newListPager(resourceGroupName, virtualMachineScaleSetName, options)
}

// interfacesClientAdapter 适配网络接口 Get API（含 VMSS 网卡）。
type interfacesClientAdapter struct {
	get func(ctx context.Context, resourceGroupName, networkInterfaceName string, options *armnetwork.InterfacesClientGetOptions) (armnetwork.InterfacesClientGetResponse, error)

	getVirtualMachineScaleSetNetworkInterface func(ctx context.Context, resourceGroupName, virtualMachineScaleSetName, virtualMachineIndex, networkInterfaceName string, options *armnetwork.InterfacesClientGetVirtualMachineScaleSetNetworkInterfaceOptions) (armnetwork.InterfacesClientGetVirtualMachineScaleSetNetworkInterfaceResponse, error)
}

func newInterfacesClientAdapter(c *armnetwork.InterfacesClient) interfacesClientAdapter {
	return interfacesClientAdapter{
		get: c.Get,
		getVirtualMachineScaleSetNetworkInterface: c.GetVirtualMachineScaleSetNetworkInterface,
	}
}

// Get 按资源 ID 获取独立 VM 的网络接口。
func (a interfacesClientAdapter) Get(ctx context.Context, resourceGroupName, networkInterfaceName string, options *armnetwork.InterfacesClientGetOptions) (armnetwork.InterfacesClientGetResponse, error) {
	return a.get(ctx, resourceGroupName, networkInterfaceName, options)
}

// GetVirtualMachineScaleSetNetworkInterface 获取规模集 VM 的网络接口。
func (a interfacesClientAdapter) GetVirtualMachineScaleSetNetworkInterface(ctx context.Context, resourceGroupName, virtualMachineScaleSetName, virtualMachineIndex, networkInterfaceName string, options *armnetwork.InterfacesClientGetVirtualMachineScaleSetNetworkInterfaceOptions) (armnetwork.InterfacesClientGetVirtualMachineScaleSetNetworkInterfaceResponse, error) {
	return a.getVirtualMachineScaleSetNetworkInterface(ctx, resourceGroupName, virtualMachineScaleSetName, virtualMachineIndex, networkInterfaceName, options)
}

// createAzureClient 根据 SDConfig 创建带凭证的 Azure ARM 客户端。
func (d *Discovery) createAzureClient() (client, error) {
	cloudConfiguration, err := CloudConfigurationFromName(d.cfg.Environment)
	if err != nil {
		return &azureClient{}, err
	}

	var c azureClient
	c.logger = d.logger

	telemetry := policy.TelemetryOptions{
		ApplicationID: version.PrometheusUserAgent(),
	}

	credential, err := newCredential(*d.cfg, policy.ClientOptions{
		Cloud:     cloudConfiguration,
		Telemetry: telemetry,
	})
	if err != nil {
		return &azureClient{}, err
	}

	client, err := config_util.NewClientFromConfig(d.cfg.HTTPClientConfig, "azure_sd")
	if err != nil {
		return &azureClient{}, err
	}
	options := &arm.ClientOptions{
		ClientOptions: policy.ClientOptions{
			Transport: client,
			Cloud:     cloudConfiguration,
			Telemetry: telemetry,
		},
	}

	vmClient, err := armcompute.NewVirtualMachinesClient(d.cfg.SubscriptionID, credential, options)
	if err != nil {
		return &azureClient{}, err
	}
	c.vm = newVirtualMachinesClientAdapter(vmClient)

	nicClient, err := armnetwork.NewInterfacesClient(d.cfg.SubscriptionID, credential, options)
	if err != nil {
		return &azureClient{}, err
	}
	c.nic = newInterfacesClientAdapter(nicClient)

	vmssClient, err := armcompute.NewVirtualMachineScaleSetsClient(d.cfg.SubscriptionID, credential, options)
	if err != nil {
		return &azureClient{}, err
	}
	c.vmss = newVirtualMachineScaleSetsClientAdapter(vmssClient)

	vmssvmClient, err := armcompute.NewVirtualMachineScaleSetVMsClient(d.cfg.SubscriptionID, credential, options)
	if err != nil {
		return &azureClient{}, err
	}
	c.vmssvm = newVirtualMachineScaleSetVMsClientAdapter(vmssvmClient)

	return &c, nil
}

func newCredential(cfg SDConfig, policyClientOptions policy.ClientOptions) (azcore.TokenCredential, error) {
	var credential azcore.TokenCredential
	switch cfg.AuthenticationMethod {
	case authMethodWorkloadIdentity:
		options := &azidentity.WorkloadIdentityCredentialOptions{ClientOptions: policyClientOptions}
		workloadIdentityCredential, err := azidentity.NewWorkloadIdentityCredential(options)
		if err != nil {
			return nil, err
		}
		credential = azcore.TokenCredential(workloadIdentityCredential)
	case authMethodManagedIdentity:
		options := &azidentity.ManagedIdentityCredentialOptions{ClientOptions: policyClientOptions}
		if cfg.ClientID != "" {
			options.ID = azidentity.ClientID(cfg.ClientID)
		}
		managedIdentityCredential, err := azidentity.NewManagedIdentityCredential(options)
		if err != nil {
			return nil, err
		}
		credential = azcore.TokenCredential(managedIdentityCredential)
	case authMethodOAuth:
		options := &azidentity.ClientSecretCredentialOptions{ClientOptions: policyClientOptions}
		secretCredential, err := azidentity.NewClientSecretCredential(cfg.TenantID, cfg.ClientID, string(cfg.ClientSecret), options)
		if err != nil {
			return nil, err
		}
		credential = azcore.TokenCredential(secretCredential)
	case authMethodSDK:
		options := &azidentity.DefaultAzureCredentialOptions{ClientOptions: policyClientOptions}
		if cfg.TenantID != "" {
			options.TenantID = cfg.TenantID
		}
		sdkCredential, err := azidentity.NewDefaultAzureCredential(options)
		if err != nil {
			return nil, err
		}
		credential = azcore.TokenCredential(sdkCredential)
	}
	return credential, nil
}

// virtualMachine 表示 Azure 虚拟机（含独立 VM 与规模集实例）。
type virtualMachine struct {
	ID                string
	Name              string
	ComputerName      string
	Type              string
	Location          string
	OsType            string
	ScaleSet          string
	InstanceID        string
	Tags              map[string]*string
	NetworkInterfaces []string
	Size              string
}

// newAzureResource 从 ARM 资源 ID 字符串解析订阅/资源组/资源名。
func newAzureResourceFromID(id string, logger *slog.Logger) (*arm.ResourceID, error) {
	if logger == nil {
		logger = promslog.NewNopLogger()
	}
	resourceID, err := arm.ParseResourceID(id)
	if err != nil {
		err := fmt.Errorf("invalid ID '%s': %w", id, err)
		logger.Error("Failed to parse resource ID", "err", err)
		return &arm.ResourceID{}, err
	}
	return resourceID, nil
}

func (d *Discovery) refreshAzureClient(ctx context.Context, client client) ([]*targetgroup.Group, error) {
	machines, err := client.getVMs(ctx, d.cfg.ResourceGroup)
	if err != nil {
		d.metrics.failuresCount.Inc()
		return nil, fmt.Errorf("could not get virtual machines: %w", err)
	}

	d.logger.Debug("Found virtual machines during Azure discovery.", "count", len(machines))

	// Load the vms managed by scale sets.
	scaleSets, err := client.getScaleSets(ctx, d.cfg.ResourceGroup)
	if err != nil {
		d.metrics.failuresCount.Inc()
		return nil, fmt.Errorf("could not get virtual machine scale sets: %w", err)
	}

	for _, scaleSet := range scaleSets {
		scaleSetVms, err := client.getScaleSetVMs(ctx, scaleSet)
		if err != nil {
			d.metrics.failuresCount.Inc()
			return nil, fmt.Errorf("could not get virtual machine scale set vms: %w", err)
		}
		machines = append(machines, scaleSetVms...)
	}

	// We have the slice of machines. Now turn them into targets.
	// Doing them in go routines because the network interface calls are slow.
	type target struct {
		labelSet model.LabelSet
		err      error
	}

	var wg sync.WaitGroup
	wg.Add(len(machines))
	ch := make(chan target, len(machines))
	for _, vm := range machines {
		go func(vm virtualMachine) {
			defer wg.Done()
			labelSet, err := d.vmToLabelSet(ctx, client, vm)
			ch <- target{labelSet: labelSet, err: err}
		}(vm)
	}

	wg.Wait()
	close(ch)

	var tg targetgroup.Group
	for tgt := range ch {
		if tgt.err != nil {
			d.metrics.failuresCount.Inc()
			return nil, fmt.Errorf("unable to complete Azure service discovery: %w", tgt.err)
		}
		if tgt.labelSet != nil {
			tg.Targets = append(tg.Targets, tgt.labelSet)
		}
	}

	return []*targetgroup.Group{&tg}, nil
}

func (d *Discovery) refresh(ctx context.Context) ([]*targetgroup.Group, error) {
	defer d.logger.Debug("Azure discovery completed")

	client, err := d.createAzureClient()
	if err != nil {
		d.metrics.failuresCount.Inc()
		return nil, fmt.Errorf("could not create Azure client: %w", err)
	}

	return d.refreshAzureClient(ctx, client)
}

func (d *Discovery) vmToLabelSet(ctx context.Context, client client, vm virtualMachine) (model.LabelSet, error) {
	r, err := newAzureResourceFromID(vm.ID, d.logger)
	if err != nil {
		return nil, err
	}

	labels := model.LabelSet{
		azureLabelSubscriptionID:       model.LabelValue(d.cfg.SubscriptionID),
		azureLabelTenantID:             model.LabelValue(d.cfg.TenantID),
		azureLabelMachineID:            model.LabelValue(vm.ID),
		azureLabelMachineName:          model.LabelValue(vm.Name),
		azureLabelMachineComputerName:  model.LabelValue(vm.ComputerName),
		azureLabelMachineOSType:        model.LabelValue(vm.OsType),
		azureLabelMachineLocation:      model.LabelValue(vm.Location),
		azureLabelMachineResourceGroup: model.LabelValue(r.ResourceGroupName),
		azureLabelMachineSize:          model.LabelValue(vm.Size),
	}

	if vm.ScaleSet != "" {
		labels[azureLabelMachineScaleSet] = model.LabelValue(vm.ScaleSet)
	}

	for k, v := range vm.Tags {
		name := strutil.SanitizeLabelName(k)
		labels[azureLabelMachineTag+model.LabelName(name)] = model.LabelValue(*v)
	}

	// Get the IP address information via separate call to the network provider.
	for _, nicID := range vm.NetworkInterfaces {
		var networkInterface *armnetwork.Interface
		if v, ok := d.getFromCache(nicID); ok {
			networkInterface = v
			d.metrics.cacheHitCount.Add(1)
		} else {
			if vm.ScaleSet == "" {
				networkInterface, err = client.getVMNetworkInterfaceByID(ctx, nicID)
			} else {
				networkInterface, err = client.getVMScaleSetVMNetworkInterfaceByID(ctx, nicID, vm.ScaleSet, vm.InstanceID)
			}
			if err != nil {
				if !errors.Is(err, errorNotFound) {
					return nil, err
				}
				d.logger.Warn("Network interface does not exist", "name", nicID, "err", err)
				// Get out of this routine because we cannot continue without a network interface.
				return nil, nil
			}

			// Continue processing with the network interface
			d.addToCache(nicID, networkInterface)
		}

		if networkInterface.Properties == nil {
			continue
		}

		// Unfortunately Azure does not return information on whether a VM is deallocated.
		// This information is available via another API call however the Go SDK does not
		// yet support this. On deallocated machines, this value happens to be nil so it
		// is a cheap and easy way to determine if a machine is allocated or not.
		if networkInterface.Properties.Primary == nil {
			d.logger.Debug("Skipping deallocated virtual machine", "machine", vm.Name)
			return nil, nil
		}

		if *networkInterface.Properties.Primary {
			for _, ip := range networkInterface.Properties.IPConfigurations {
				// IPAddress is a field defined in PublicIPAddressPropertiesFormat,
				// therefore we need to validate that both are not nil.
				if ip.Properties != nil && ip.Properties.PublicIPAddress != nil && ip.Properties.PublicIPAddress.Properties != nil && ip.Properties.PublicIPAddress.Properties.IPAddress != nil {
					labels[azureLabelMachinePublicIP] = model.LabelValue(*ip.Properties.PublicIPAddress.Properties.IPAddress)
				}
				if ip.Properties != nil && ip.Properties.PrivateIPAddress != nil {
					labels[azureLabelMachinePrivateIP] = model.LabelValue(*ip.Properties.PrivateIPAddress)
					address := net.JoinHostPort(*ip.Properties.PrivateIPAddress, strconv.Itoa(d.port))
					labels[model.AddressLabel] = model.LabelValue(address)
					return labels, nil
				}
				// If we made it here, we don't have a private IP which should be impossible.
				// Return an empty target and error to ensure an all or nothing situation.
				return nil, fmt.Errorf("unable to find a private IP for VM %s", vm.Name)
			}
		}
	}
	// TODO: Should we say something at this point?
	return nil, nil
}

func (client *azureClient) getVMs(ctx context.Context, resourceGroup string) ([]virtualMachine, error) {
	var vms []virtualMachine
	if resourceGroup == "" {
		pager := client.vm.NewListAllPager(nil)
		for pager.More() {
			nextResult, err := pager.NextPage(ctx)
			if err != nil {
				return nil, fmt.Errorf("could not list virtual machines: %w", err)
			}
			for _, vm := range nextResult.Value {
				vms = append(vms, mapFromVM(*vm))
			}
		}
	} else {
		pager := client.vm.NewListPager(resourceGroup, nil)
		for pager.More() {
			nextResult, err := pager.NextPage(ctx)
			if err != nil {
				return nil, fmt.Errorf("could not list virtual machines: %w", err)
			}
			for _, vm := range nextResult.Value {
				vms = append(vms, mapFromVM(*vm))
			}
		}
	}
	return vms, nil
}

func (client *azureClient) getScaleSets(ctx context.Context, resourceGroup string) ([]armcompute.VirtualMachineScaleSet, error) {
	var scaleSets []armcompute.VirtualMachineScaleSet
	if resourceGroup == "" {
		pager := client.vmss.NewListAllPager(nil)
		for pager.More() {
			nextResult, err := pager.NextPage(ctx)
			if err != nil {
				return nil, fmt.Errorf("could not list virtual machine scale sets: %w", err)
			}
			for _, vmss := range nextResult.Value {
				scaleSets = append(scaleSets, *vmss)
			}
		}
	} else {
		pager := client.vmss.NewListPager(resourceGroup, nil)
		for pager.More() {
			nextResult, err := pager.NextPage(ctx)
			if err != nil {
				return nil, fmt.Errorf("could not list virtual machine scale sets: %w", err)
			}
			for _, vmss := range nextResult.Value {
				scaleSets = append(scaleSets, *vmss)
			}
		}
	}
	return scaleSets, nil
}

func (client *azureClient) getScaleSetVMs(ctx context.Context, scaleSet armcompute.VirtualMachineScaleSet) ([]virtualMachine, error) {
	var vms []virtualMachine
	// TODO do we really need to fetch the resourcegroup this way?
	r, err := newAzureResourceFromID(*scaleSet.ID, client.logger)
	if err != nil {
		return nil, fmt.Errorf("could not parse scale set ID: %w", err)
	}

	pager := client.vmssvm.NewListPager(r.ResourceGroupName, *(scaleSet.Name), nil)
	for pager.More() {
		nextResult, err := pager.NextPage(ctx)
		if err != nil {
			return nil, fmt.Errorf("could not list virtual machine scale set vms: %w", err)
		}
		for _, vmssvm := range nextResult.Value {
			vms = append(vms, mapFromVMScaleSetVM(*vmssvm, *scaleSet.Name))
		}
	}

	return vms, nil
}

func mapFromVM(vm armcompute.VirtualMachine) virtualMachine {
	var osType string
	tags := map[string]*string{}
	networkInterfaces := []string{}
	var computerName string
	var size string

	if vm.Tags != nil {
		tags = vm.Tags
	}

	if vm.Properties != nil {
		if vm.Properties.StorageProfile != nil &&
			vm.Properties.StorageProfile.OSDisk != nil &&
			vm.Properties.StorageProfile.OSDisk.OSType != nil {
			osType = string(*vm.Properties.StorageProfile.OSDisk.OSType)
		}

		if vm.Properties.NetworkProfile != nil {
			for _, vmNIC := range vm.Properties.NetworkProfile.NetworkInterfaces {
				networkInterfaces = append(networkInterfaces, *vmNIC.ID)
			}
		}
		if vm.Properties.OSProfile != nil && vm.Properties.OSProfile.ComputerName != nil {
			computerName = *(vm.Properties.OSProfile.ComputerName)
		}
		if vm.Properties.HardwareProfile != nil {
			size = string(*vm.Properties.HardwareProfile.VMSize)
		}
	}

	return virtualMachine{
		ID:                *(vm.ID),
		Name:              *(vm.Name),
		ComputerName:      computerName,
		Type:              *(vm.Type),
		Location:          *(vm.Location),
		OsType:            osType,
		ScaleSet:          "",
		Tags:              tags,
		NetworkInterfaces: networkInterfaces,
		Size:              size,
	}
}

func mapFromVMScaleSetVM(vm armcompute.VirtualMachineScaleSetVM, scaleSetName string) virtualMachine {
	var osType string
	tags := map[string]*string{}
	networkInterfaces := []string{}
	var computerName string
	var size string

	if vm.Tags != nil {
		tags = vm.Tags
	}

	if vm.Properties != nil {
		if vm.Properties.StorageProfile != nil &&
			vm.Properties.StorageProfile.OSDisk != nil &&
			vm.Properties.StorageProfile.OSDisk.OSType != nil {
			osType = string(*vm.Properties.StorageProfile.OSDisk.OSType)
		}

		if vm.Properties.NetworkProfile != nil {
			for _, vmNIC := range vm.Properties.NetworkProfile.NetworkInterfaces {
				networkInterfaces = append(networkInterfaces, *vmNIC.ID)
			}
		}
		if vm.Properties.OSProfile != nil && vm.Properties.OSProfile.ComputerName != nil {
			computerName = *(vm.Properties.OSProfile.ComputerName)
		}
		if vm.Properties.HardwareProfile != nil {
			size = string(*vm.Properties.HardwareProfile.VMSize)
		}
	}

	return virtualMachine{
		ID:                *(vm.ID),
		Name:              *(vm.Name),
		ComputerName:      computerName,
		Type:              *(vm.Type),
		Location:          *(vm.Location),
		OsType:            osType,
		ScaleSet:          scaleSetName,
		InstanceID:        *(vm.InstanceID),
		Tags:              tags,
		NetworkInterfaces: networkInterfaces,
		Size:              size,
	}
}

var errorNotFound = errors.New("network interface does not exist")

// getVMNetworkInterfaceByID 获取 VM 网卡；404 时返回 errorNotFound。
func (client *azureClient) getVMNetworkInterfaceByID(ctx context.Context, networkInterfaceID string) (*armnetwork.Interface, error) {
	r, err := newAzureResourceFromID(networkInterfaceID, client.logger)
	if err != nil {
		return nil, fmt.Errorf("could not parse network interface ID: %w", err)
	}

	resp, err := client.nic.Get(ctx, r.ResourceGroupName, r.Name, &armnetwork.InterfacesClientGetOptions{Expand: to.Ptr("IPConfigurations/PublicIPAddress")})
	if err != nil {
		var responseError *azcore.ResponseError
		if errors.As(err, &responseError) && responseError.StatusCode == http.StatusNotFound {
			return nil, errorNotFound
		}
		return nil, fmt.Errorf("failed to retrieve Interface %v with error: %w", networkInterfaceID, err)
	}

	return &resp.Interface, nil
}

// getVMScaleSetVMNetworkInterfaceByID 获取规模集 VM 网卡；404 时返回 errorNotFound。
func (client *azureClient) getVMScaleSetVMNetworkInterfaceByID(ctx context.Context, networkInterfaceID, scaleSetName, instanceID string) (*armnetwork.Interface, error) {
	r, err := newAzureResourceFromID(networkInterfaceID, client.logger)
	if err != nil {
		return nil, fmt.Errorf("could not parse network interface ID: %w", err)
	}

	resp, err := client.nic.GetVirtualMachineScaleSetNetworkInterface(ctx, r.ResourceGroupName, scaleSetName, instanceID, r.Name, &armnetwork.InterfacesClientGetVirtualMachineScaleSetNetworkInterfaceOptions{Expand: to.Ptr("IPConfigurations/PublicIPAddress")})
	if err != nil {
		var responseError *azcore.ResponseError
		if errors.As(err, &responseError) && responseError.StatusCode == http.StatusNotFound {
			return nil, errorNotFound
		}
		return nil, fmt.Errorf("failed to retrieve Interface %v with error: %w", networkInterfaceID, err)
	}

	return &resp.Interface, nil
}

// addToCache will add the network interface information for the specified nicID.
func (d *Discovery) addToCache(nicID string, netInt *armnetwork.Interface) {
	random := rand.Int63n(int64(time.Duration(d.cfg.RefreshInterval * 3).Seconds()))
	rs := time.Duration(random) * time.Second
	exptime := time.Duration(d.cfg.RefreshInterval*10) + rs
	d.cache.Set(nicID, netInt, cache.WithExpiration(exptime))
	d.logger.Debug("Adding nic", "nic", nicID, "time", exptime.Seconds())
}

// getFromCache will get the network Interface for the specified nicID
// 若缓存未启用则跳过写入（用于网卡 IP 查询结果缓存）。
func (d *Discovery) getFromCache(nicID string) (*armnetwork.Interface, bool) {
	net, found := d.cache.Get(nicID)
	return net, found
}
