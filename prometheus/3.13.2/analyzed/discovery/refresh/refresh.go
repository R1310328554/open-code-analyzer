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

// 通用刷新型服务发现框架：包装任意 refresh 函数，
// 启动时立即执行一次并按 interval 周期性推送 targetgroup 到 channel。

package refresh

import (
	"context"
	"errors"
	"log/slog"
	"time"

	"github.com/prometheus/common/promslog"

	"github.com/prometheus/prometheus/discovery"
	"github.com/prometheus/prometheus/discovery/targetgroup"
)

// Options 配置刷新 Discoverer 的日志、机制名、间隔、回调与指标实例化器。
type Options struct {
	Logger              *slog.Logger
	Mech                string
	SetName             string
	Interval            time.Duration
	RefreshF            func(ctx context.Context) ([]*targetgroup.Group, error)
	MetricsInstantiator discovery.RefreshMetricsInstantiator
}

// Discovery 实现 Discoverer 接口，持有 refresh 回调与 RefreshMetrics。
// Discovery implements the Discoverer interface.
type Discovery struct {
	logger   *slog.Logger
	interval time.Duration
	refreshf func(ctx context.Context) ([]*targetgroup.Group, error)
	metrics  *discovery.RefreshMetrics
}

// NewDiscovery 创建 Discovery：实例化指标并绑定 refresh 回调。
// NewDiscovery returns a Discoverer function that calls a refresh() function at every interval.
func NewDiscovery(opts Options) *Discovery {
	m := opts.MetricsInstantiator.Instantiate(opts.Mech, opts.SetName)

	var logger *slog.Logger
	if opts.Logger == nil {
		logger = promslog.NewNopLogger()
	} else {
		logger = opts.Logger
	}

	d := Discovery{
		logger:   logger,
		interval: opts.Interval,
		refreshf: opts.RefreshF,
		metrics:  m,
	}

	return &d
}

// Run 先立即刷新一次，再按 ticker 周期调用 refresh 并发送到 channel。
// Run implements the Discoverer interface.
func (d *Discovery) Run(ctx context.Context, ch chan<- []*targetgroup.Group) {
	// Get an initial set right away.
	tgs, err := d.refresh(ctx)
	if err != nil {
		if !errors.Is(ctx.Err(), context.Canceled) {
			d.logger.Error("Unable to refresh target groups", "err", err.Error())
		}
	} else {
		select {
		case ch <- tgs:
		case <-ctx.Done():
			return
		}
	}

	ticker := time.NewTicker(d.interval)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			tgs, err := d.refresh(ctx)
			if err != nil {
				if !errors.Is(ctx.Err(), context.Canceled) {
					d.logger.Error("Unable to refresh target groups", "err", err.Error())
				}
				continue
			}

			select {
			case ch <- tgs:
			case <-ctx.Done():
				return
			}
		case <-ctx.Done():
			return
		}
	}
}

// refresh 包装 refreshf：记录耗时直方图，失败时递增 failures 计数。
func (d *Discovery) refresh(ctx context.Context) ([]*targetgroup.Group, error) {
	now := time.Now()
	defer func() {
		d.metrics.Duration.Observe(time.Since(now).Seconds())
		d.metrics.DurationHistogram.Observe(time.Since(now).Seconds())
	}()

	tgs, err := d.refreshf(ctx)
	if err != nil {
		d.metrics.Failures.Inc()
	}
	return tgs, err
}
