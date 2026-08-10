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

// HTTP 服务发现：周期性 GET 配置的 URL，解析 JSON 格式的 targetgroup 列表，支持源消失时发送空更新。

package http

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"time"

	"github.com/grafana/regexp"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/config"
	"github.com/prometheus/common/model"
	"github.com/prometheus/common/promslog"
	"github.com/prometheus/common/version"

	"github.com/prometheus/prometheus/discovery"
	"github.com/prometheus/prometheus/discovery/refresh"
	"github.com/prometheus/prometheus/discovery/targetgroup"
)

var (
	// HTTP SD 默认配置（60s 刷新、默认 HTTP 客户端）。
// DefaultSDConfig is the default HTTP SD configuration.
	DefaultSDConfig = SDConfig{
		HTTPClientConfig: config.DefaultHTTPClientConfig,
		RefreshInterval:  model.Duration(60 * time.Second),
	}
	userAgent        = version.PrometheusUserAgent()
	matchContentType = regexp.MustCompile(`^(?i:application\/json(;\s*charset=("utf-8"|utf-8))?)$`)
)

func init() {
	discovery.RegisterConfig(&SDConfig{})
}

// HTTP SD 配置：目标 URL、刷新间隔与 HTTP 客户端选项。
// SDConfig is the configuration for HTTP based discovery.
type SDConfig struct {
	HTTPClientConfig config.HTTPClientConfig `yaml:",inline"`
	RefreshInterval  model.Duration          `yaml:"refresh_interval,omitempty"`
	URL              string                  `yaml:"url"`
}

// NewDiscovererMetrics implements discovery.Config.
func (*SDConfig) NewDiscovererMetrics(reg prometheus.Registerer, rmi discovery.RefreshMetricsInstantiator) discovery.DiscovererMetrics {
	return newDiscovererMetrics(reg, rmi)
}

// Name returns the name of the Config.
func (*SDConfig) Name() string { return "http" }

// NewDiscoverer returns a Discoverer for the Config.
func (c *SDConfig) NewDiscoverer(opts discovery.DiscovererOptions) (discovery.Discoverer, error) {
	return NewDiscovery(c, opts)
}

// SetDirectory joins any relative file paths with dir.
func (c *SDConfig) SetDirectory(dir string) {
	c.HTTPClientConfig.SetDirectory(dir)
}

// UnmarshalYAML implements the yaml.Unmarshaler interface.
func (c *SDConfig) UnmarshalYAML(unmarshal func(any) error) error {
	*c = DefaultSDConfig
	type plain SDConfig
	err := unmarshal((*plain)(c))
	if err != nil {
		return err
	}
	if c.URL == "" {
		return errors.New("URL is missing")
	}
	parsedURL, err := url.Parse(c.URL)
	if err != nil {
		return err
	}
	if parsedURL.Scheme != "http" && parsedURL.Scheme != "https" {
		return errors.New("URL scheme must be 'http' or 'https'")
	}
	if parsedURL.Host == "" {
		return errors.New("host is missing in URL")
	}
	return c.HTTPClientConfig.Validate()
}

const httpSDURLLabel = model.MetaLabelPrefix + "url"

// Discovery 实现基于 HTTP JSON 端点的服务发现。
// Discovery provides service discovery functionality based
// on HTTP endpoints that return target groups in JSON format.
type Discovery struct {
	*refresh.Discovery
	url             string
	client          *http.Client
	refreshInterval time.Duration
	tgLastLength    int
	metrics         *httpMetrics
}

// NewDiscovery returns a new HTTP discovery for the given config.
// 创建 HTTP Discovery：配置客户端并包装 refresh.Discovery。
func NewDiscovery(conf *SDConfig, opts discovery.DiscovererOptions) (*Discovery, error) {
	m, ok := opts.Metrics.(*httpMetrics)
	if !ok {
		return nil, errors.New("invalid discovery metrics type")
	}

	if opts.Logger == nil {
		opts.Logger = promslog.NewNopLogger()
	}

	client, err := config.NewClientFromConfig(conf.HTTPClientConfig, "http", opts.HTTPClientOptions...)
	if err != nil {
		return nil, err
	}
	client.Timeout = time.Duration(conf.RefreshInterval)

	d := &Discovery{
		url:             conf.URL,
		client:          client,
		refreshInterval: time.Duration(conf.RefreshInterval), // Stored to be sent as headers.
		metrics:         m,
	}

	d.Discovery = refresh.NewDiscovery(
		refresh.Options{
			Logger:              opts.Logger,
			Mech:                "http",
			SetName:             opts.SetName,
			Interval:            time.Duration(conf.RefreshInterval),
			RefreshF:            d.Refresh,
			MetricsInstantiator: m.refreshMetrics,
		},
	)
	return d, nil
}

// 拉取 URL、校验 Content-Type/状态码，反序列化 targetgroup 并补全 meta 标签。
func (d *Discovery) Refresh(ctx context.Context) ([]*targetgroup.Group, error) {
	req, err := http.NewRequest(http.MethodGet, d.url, http.NoBody)
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", userAgent)
	req.Header.Set("Accept", "application/json")
	req.Header.Set("X-Prometheus-Refresh-Interval-Seconds", strconv.FormatFloat(d.refreshInterval.Seconds(), 'f', -1, 64))

	resp, err := d.client.Do(req.WithContext(ctx))
	if err != nil {
		d.metrics.failuresCount.Inc()
		return nil, err
	}
	defer func() {
		io.Copy(io.Discard, resp.Body)
		resp.Body.Close()
	}()

	if resp.StatusCode != http.StatusOK {
		d.metrics.failuresCount.Inc()
		return nil, fmt.Errorf("server returned HTTP status %s", resp.Status)
	}

	if !matchContentType.MatchString(strings.TrimSpace(resp.Header.Get("Content-Type"))) {
		d.metrics.failuresCount.Inc()
		return nil, fmt.Errorf("unsupported content type %q", resp.Header.Get("Content-Type"))
	}

	b, err := io.ReadAll(resp.Body)
	if err != nil {
		d.metrics.failuresCount.Inc()
		return nil, err
	}

	var targetGroups []*targetgroup.Group

	if err := json.Unmarshal(b, &targetGroups); err != nil {
		d.metrics.failuresCount.Inc()
		return nil, err
	}

	for i, tg := range targetGroups {
		if tg == nil {
			d.metrics.failuresCount.Inc()
			err = errors.New("nil target group item found")
			return nil, err
		}

		tg.Source = urlSource(d.url, i)
		if tg.Labels == nil {
			tg.Labels = model.LabelSet{}
		}
		tg.Labels[httpSDURLLabel] = model.LabelValue(d.url)
	}

	// Generate empty updates for sources that disappeared.
	l := len(targetGroups)
	for i := l; i < d.tgLastLength; i++ {
		targetGroups = append(targetGroups, &targetgroup.Group{Source: urlSource(d.url, i)})
	}
	d.tgLastLength = l

	return targetGroups, nil
}

// urlSource returns a source ID for the i-th target group per URL.
// 为同一 URL 下第 i 个 target group 生成唯一 source ID。
func urlSource(url string, i int) string {
	return fmt.Sprintf("%s:%d", url, i)
}
