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

// Hetzner 服务发现入口：统一 SDConfig 按 role 分发到 Robot 专用服务器或 Hetzner Cloud API 子发现器。

package hetzner

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"time"

	"github.com/hetznercloud/hcloud-go/v2/hcloud"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/config"
	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/discovery"
	"github.com/prometheus/prometheus/discovery/refresh"
	"github.com/prometheus/prometheus/discovery/targetgroup"
)

const (
	hetznerLabelPrefix            = model.MetaLabelPrefix + "hetzner_"
	hetznerLabelRole              = hetznerLabelPrefix + "role"
	hetznerLabelServerID          = hetznerLabelPrefix + "server_id"
	hetznerLabelServerName        = hetznerLabelPrefix + "server_name"
	hetznerLabelServerStatus      = hetznerLabelPrefix + "server_status"
	hetznerLabelDatacenter        = hetznerLabelPrefix + "datacenter" // Label name kept for backward compatibility
	hetznerLabelPublicIPv4        = hetznerLabelPrefix + "public_ipv4"
	hetznerLabelPublicIPv6Network = hetznerLabelPrefix + "public_ipv6_network"
)

// Hetzner SD 默认配置（80 端口、60s 刷新间隔）。
// DefaultSDConfig is the default Hetzner SD configuration.
var DefaultSDConfig = SDConfig{
	Port:             80,
	RefreshInterval:  model.Duration(60 * time.Second),
	HTTPClientConfig: config.DefaultHTTPClientConfig,
}

func init() {
	discovery.RegisterConfig(&SDConfig{})
}

// Hetzner SD 配置：HTTP 客户端、刷新间隔、端口、role 与 label 选择器。
// SDConfig is the configuration for Hetzner based service discovery.
type SDConfig struct {
	HTTPClientConfig config.HTTPClientConfig `yaml:",inline"`

	RefreshInterval model.Duration `yaml:"refresh_interval"`
	Port            int            `yaml:"port"`
	Role            Role           `yaml:"role"`

	LabelSelector string `yaml:"label_selector,omitempty"`

	hcloudEndpoint string // For tests only.
	robotEndpoint  string // For tests only.
}

// NewDiscovererMetrics implements discovery.Config.
func (*SDConfig) NewDiscovererMetrics(_ prometheus.Registerer, rmi discovery.RefreshMetricsInstantiator) discovery.DiscovererMetrics {
	return &hetznerMetrics{
		refreshMetrics: rmi,
	}
}

// Name returns the name of the Config.
func (*SDConfig) Name() string { return "hetzner" }

// NewDiscoverer returns a Discoverer for the Config.
// 按 SDConfig 构造 Hetzner Discovery 实例。
func (c *SDConfig) NewDiscoverer(opts discovery.DiscovererOptions) (discovery.Discoverer, error) {
	return NewDiscovery(c, opts)
}

type refresher interface {
	refresh(context.Context) ([]*targetgroup.Group, error)
}

// Hetzner 角色枚举：robot（专用服务器）或 hcloud（云主机）。
// Role is the Role of the target within the Hetzner Ecosystem.
type Role string

// The valid options for role.
const (
	// Hetzner Robot Role (Dedicated Server)
	// https://robot.hetzner.com
	HetznerRoleRobot Role = "robot"
	// Hetzner Cloud Role
	// https://console.hetzner.cloud
	HetznerRoleHcloud Role = "hcloud"
)

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (c *Role) UnmarshalYAML(unmarshal func(any) error) error {
	if err := unmarshal((*string)(c)); err != nil {
		return err
	}
	switch *c {
	case HetznerRoleRobot, HetznerRoleHcloud:
		return nil
	default:
		return fmt.Errorf("unknown role %q", *c)
	}
}

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (c *SDConfig) UnmarshalYAML(unmarshal func(any) error) error {
	*c = DefaultSDConfig
	type plain SDConfig
	err := unmarshal((*plain)(c))
	if err != nil {
		return err
	}

	if c.Role == "" {
		return errors.New("role missing (one of: robot, hcloud)")
	}
	return c.HTTPClientConfig.Validate()
}

// SetDirectory joins any relative file paths with dir.
func (c *SDConfig) SetDirectory(dir string) {
	c.HTTPClientConfig.SetDirectory(dir)
}

// Discovery periodically performs Hetzner requests. It implements
// the Discoverer interface.
type Discovery struct {
	*refresh.Discovery
}

// NewDiscovery returns a new Discovery which periodically refreshes its targets.
// 包装 refresh.Discovery，注入 hetzner 刷新函数与指标。
func NewDiscovery(conf *SDConfig, opts discovery.DiscovererOptions) (*refresh.Discovery, error) {
	m, ok := opts.Metrics.(*hetznerMetrics)
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
			Mech:                "hetzner",
			SetName:             opts.SetName,
			Interval:            time.Duration(conf.RefreshInterval),
			RefreshF:            r.refresh,
			MetricsInstantiator: m.refreshMetrics,
		},
	), nil
}

// 按 role 选择 hcloud 或 robot 具体 refresher 实现。
func newRefresher(conf *SDConfig, l *slog.Logger) (refresher, error) {
	switch conf.Role {
	case HetznerRoleHcloud:
		if conf.hcloudEndpoint == "" {
			conf.hcloudEndpoint = hcloud.Endpoint
		}
		return newHcloudDiscovery(conf, l)
	case HetznerRoleRobot:
		if conf.robotEndpoint == "" {
			conf.robotEndpoint = "https://robot-ws.your-server.de"
		}
		return newRobotDiscovery(conf, l)
	}
	return nil, errors.New("unknown Hetzner discovery role")
}
