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

// IONOS Cloud 服务发现入口：按 datacenter_id 发现数据中心内虚拟机，通过官方 SDK 拉取服务器列表并映射为抓取目标。

// IONOS Cloud 服务发现入口：按 datacenter_id 发现数据中心内虚拟机，通过官方 SDK 拉取服务器列表并映射为抓取目标。

package ionos

import (
	"errors"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/config"
	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/discovery"
	"github.com/prometheus/prometheus/discovery/refresh"
)

const (
	// metaLabelPrefix is the meta prefix used for all meta labels in this
	// discovery.
	metaLabelPrefix    = model.MetaLabelPrefix + "ionos_"
	metaLabelSeparator = ","
)

func init() {
	discovery.RegisterConfig(&SDConfig{})
}

// Discovery 包装 refresh.Discovery，委托 serverDiscovery 执行刷新。
// Discovery periodically performs IONOS Cloud target discovery. It implements
// the Discoverer interface.
type Discovery struct{}

// NewDiscovery returns a new refresh.Discovery for IONOS Cloud.
// 构造 IONOS refresh.Discovery 并绑定 server refresher。
func NewDiscovery(conf *SDConfig, opts discovery.DiscovererOptions) (*refresh.Discovery, error) {
	m, ok := opts.Metrics.(*ionosMetrics)
	if !ok {
		return nil, errors.New("invalid discovery metrics type")
	}

	if conf.ionosEndpoint == "" {
		conf.ionosEndpoint = "https://api.ionos.com"
	}

	d, err := newServerDiscovery(conf, opts.Logger)
	if err != nil {
		return nil, err
	}

	return refresh.NewDiscovery(
		refresh.Options{
			Logger:              opts.Logger,
			Mech:                "ionos",
			SetName:             opts.SetName,
			Interval:            time.Duration(conf.RefreshInterval),
			RefreshF:            d.refresh,
			MetricsInstantiator: m.refreshMetrics,
		},
	), nil
}

// IONOS SD 默认配置（80 端口、60s 刷新）。
// DefaultSDConfig is the default IONOS Cloud service discovery configuration.
var DefaultSDConfig = SDConfig{
	HTTPClientConfig: config.DefaultHTTPClientConfig,
	RefreshInterval:  model.Duration(60 * time.Second),
	Port:             80,
}

// IONOS SD 配置：datacenter_id、HTTP 客户端与刷新间隔。
// SDConfig configuration to use for IONOS Cloud Discovery.
type SDConfig struct {
	// DatacenterID: IONOS Cloud data center ID used to discover targets.
	DatacenterID string `yaml:"datacenter_id"`

	HTTPClientConfig config.HTTPClientConfig `yaml:",inline"`

	RefreshInterval model.Duration `yaml:"refresh_interval"`
	Port            int            `yaml:"port"`

	ionosEndpoint string // For tests only.
}

// NewDiscovererMetrics implements discovery.Config.
func (*SDConfig) NewDiscovererMetrics(_ prometheus.Registerer, rmi discovery.RefreshMetricsInstantiator) discovery.DiscovererMetrics {
	return &ionosMetrics{
		refreshMetrics: rmi,
	}
}

// Name returns the name of the IONOS Cloud service discovery.
func (SDConfig) Name() string {
	return "ionos"
}

// NewDiscoverer returns a new discovery.Discoverer for IONOS Cloud.
func (c SDConfig) NewDiscoverer(opts discovery.DiscovererOptions) (discovery.Discoverer, error) {
	return NewDiscovery(&c, opts)
}

// UnmarshalYAML implements the yaml.Unmarshaler interface.
// YAML 解析：校验 datacenter_id 非空并验证 HTTP 客户端配置。
func (c *SDConfig) UnmarshalYAML(unmarshal func(any) error) error {
	*c = DefaultSDConfig
	type plain SDConfig
	err := unmarshal((*plain)(c))
	if err != nil {
		return err
	}

	if c.DatacenterID == "" {
		return errors.New("datacenter id can't be empty")
	}

	return c.HTTPClientConfig.Validate()
}

// SetDirectory joins any relative file paths with dir.
func (c *SDConfig) SetDirectory(dir string) {
	c.HTTPClientConfig.SetDirectory(dir)
}
