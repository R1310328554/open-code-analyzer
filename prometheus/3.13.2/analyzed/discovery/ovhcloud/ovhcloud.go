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

// OVHcloud 服务发现入口：配置 endpoint/凭证/service 类型，
// 按 dedicated_server 或 vps 创建对应 refresher 并包装为 refresh.Discovery。

// OVHcloud 服务发现入口：配置 endpoint/凭证/service 类型，
// 按 dedicated_server 或 vps 创建对应 refresher 并包装为 refresh.Discovery。

// OVHcloud 服务发现入口：配置 endpoint/凭证/service 类型，
// 按 dedicated_server 或 vps 创建对应 refresher 并包装为 refresh.Discovery。

package ovhcloud

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"net/netip"
	"time"

	"github.com/ovh/go-ovh/ovh"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/config"
	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/discovery"
	"github.com/prometheus/prometheus/discovery/refresh"
	"github.com/prometheus/prometheus/discovery/targetgroup"
)

// metaLabelPrefix 为所有 OVHcloud meta 标签的统一前缀。
// metaLabelPrefix is the meta prefix used for all meta labels in this discovery.
const metaLabelPrefix = model.MetaLabelPrefix + "ovhcloud_"

type refresher interface {
	refresh(context.Context) ([]*targetgroup.Group, error)
}

var DefaultSDConfig = SDConfig{
	Endpoint:        "ovh-eu",
	RefreshInterval: model.Duration(60 * time.Second),
}

// SDConfig 定义 OVHcloud SD 配置：endpoint、应用密钥、consumer key 与服务类型。
// SDConfig defines the Service Discovery struct used for configuration.
type SDConfig struct {
	Endpoint          string         `yaml:"endpoint"`
	ApplicationKey    string         `yaml:"application_key"`
	ApplicationSecret config.Secret  `yaml:"application_secret"`
	ConsumerKey       config.Secret  `yaml:"consumer_key"`
	RefreshInterval   model.Duration `yaml:"refresh_interval"`
	Service           string         `yaml:"service"`
}

// NewDiscovererMetrics implements discovery.Config.
func (*SDConfig) NewDiscovererMetrics(_ prometheus.Registerer, rmi discovery.RefreshMetricsInstantiator) discovery.DiscovererMetrics {
	return &ovhcloudMetrics{
		refreshMetrics: rmi,
	}
}

// Name implements the Discoverer interface.
func (SDConfig) Name() string {
	return "ovhcloud"
}

// UnmarshalYAML implements the yaml.Unmarshaler interface.
// YAML 反序列化：校验 endpoint/密钥非空且 service 为 dedicated_server 或 vps。
func (c *SDConfig) UnmarshalYAML(unmarshal func(any) error) error {
	*c = DefaultSDConfig
	type plain SDConfig
	err := unmarshal((*plain)(c))
	if err != nil {
		return err
	}

	if c.Endpoint == "" {
		return errors.New("endpoint can not be empty")
	}
	if c.ApplicationKey == "" {
		return errors.New("application key can not be empty")
	}
	if c.ApplicationSecret == "" {
		return errors.New("application secret can not be empty")
	}
	if c.ConsumerKey == "" {
		return errors.New("consumer key can not be empty")
	}
	switch c.Service {
	case "dedicated_server", "vps":
		return nil
	default:
		return fmt.Errorf("unknown service: %v", c.Service)
	}
}

// CreateClient creates a new ovh client configured with given credentials.
func createClient(config *SDConfig) (*ovh.Client, error) {
	return ovh.NewClient(config.Endpoint, config.ApplicationKey, string(config.ApplicationSecret), string(config.ConsumerKey))
}

// NewDiscoverer 创建 OVHcloud Discoverer 实例。
// NewDiscoverer returns a Discoverer for the Config.
func (c *SDConfig) NewDiscoverer(opts discovery.DiscovererOptions) (discovery.Discoverer, error) {
	return NewDiscovery(c, opts)
}

func init() {
	discovery.RegisterConfig(&SDConfig{})
}

// parseIPList 解析 OVH 返回的 IP 列表，支持单地址与 /32 前缀格式。
// ParseIPList parses ip list as they can have different formats.
func parseIPList(ipList []string) ([]netip.Addr, error) {
	var ipAddresses []netip.Addr
	for _, ip := range ipList {
		ipAddr, err := netip.ParseAddr(ip)
		if err != nil {
			ipPrefix, err := netip.ParsePrefix(ip)
			if err != nil {
				return nil, errors.New("could not parse IP addresses from list")
			}
			if ipPrefix.IsValid() {
				netmask := ipPrefix.Bits()
				if netmask != 32 {
					continue
				}
				ipAddr = ipPrefix.Addr()
			}
		}
		if ipAddr.IsValid() && !ipAddr.IsUnspecified() {
			ipAddresses = append(ipAddresses, ipAddr)
		}
	}

	if len(ipAddresses) == 0 {
		return nil, errors.New("could not parse IP addresses from list")
	}
	return ipAddresses, nil
}

// 按 service 字段选择 VPS 或独立服务器 refresher 实现。
func newRefresher(conf *SDConfig, logger *slog.Logger) (refresher, error) {
	switch conf.Service {
	case "vps":
		return newVpsDiscovery(conf, logger), nil
	case "dedicated_server":
		return newDedicatedServerDiscovery(conf, logger), nil
	}
	return nil, fmt.Errorf("unknown OVHcloud discovery service '%s'", conf.Service)
}

// NewDiscovery 创建周期性刷新的 OVHcloud 服务发现 Discoverer。
// NewDiscovery returns a new OVHcloud Discoverer which periodically refreshes its targets.
func NewDiscovery(conf *SDConfig, opts discovery.DiscovererOptions) (*refresh.Discovery, error) {
	m, ok := opts.Metrics.(*ovhcloudMetrics)
	if !ok {
		return nil, errors.New("invalid discovery metrics type")
	}

	r, err := newRefresher(conf, opts.Logger)
	if err != nil {
		return nil, err
	}

	return refresh.NewDiscovery(
		refresh.Options{
			Logger:              opts.Logger,
			Mech:                "ovhcloud",
			SetName:             opts.SetName,
			Interval:            time.Duration(conf.RefreshInterval),
			RefreshF:            r.refresh,
			MetricsInstantiator: m.refreshMetrics,
		},
	), nil
}
