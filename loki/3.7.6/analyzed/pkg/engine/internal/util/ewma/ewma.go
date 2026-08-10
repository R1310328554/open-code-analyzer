// Package ewma 实现可导出为 Prometheus 指标的指数加权移动平均。
// Package ewma provides an implementation of an exponentially weighted moving
// average (EWMA) that can be reported as a Prometheus metric.
package ewma

import (
	"context"
	"errors"
	"sync"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"go.uber.org/atomic"
)

// Source 抽象采样值来源，可由函数或自定义类型实现。
// Source is an interface that provides a value for an EWMA calculation.
type Source interface {
	// Get returns the current value for the EWMA calculation.
	Get() float64
}

// SourceFunc is a [Source] that provides its value from a function.
type SourceFunc func() float64

// Get returns the value provided by the SourceFunc.
func (sf SourceFunc) Get() float64 { return sf() }

// Options 配置指标名、更新周期及多个时间窗口（如 1m/5m/15m）。
// Options provides configuration options for an EWMA.
type Options struct {
	// Name of the EWMA metric.
	Name string

	// Help text for the EWMA metric.
	Help string

	// UpdateFrequency is the frequency at which the EWMA is updated from the
	// Source. A typical value is 5s.
	UpdateFrequency time.Duration

	// Windows is a list of time windows for which averages will be calculated;
	// such as 1m, 5m, and 15m averages.
	Windows []time.Duration
}

// EWMA 后台定时从 Source 拉取样本并更新各 window，实现 Collector 接口。
// EWMA provides an exponentially weighted moving average (EWMA). EWMA
// implements [prometheus.Collector].
type EWMA struct {
	source     Source
	metric     *prometheus.Desc
	updateFreq time.Duration

	running atomic.Bool

	mut     sync.RWMutex
	windows []*window
}

var _ prometheus.Collector = (*EWMA)(nil)

// New 校验 source 与 UpdateFrequency，为每个窗口分配独立 state。
// New creates a new EWMA with the given options and source. The returned EWMA
// must be started by calling [EWMA.Monitor].
func New(opts Options, source Source) (*EWMA, error) {
	if source == nil {
		return nil, errors.New("source must not be nil")
	} else if opts.UpdateFrequency <= 0 {
		return nil, errors.New("UpdateFrequency must be greater than zero")
	}

	windows := make([]*window, 0, len(opts.Windows))
	for _, size := range opts.Windows {
		if size <= 0 {
			return nil, errors.New("window size must be greater than zero")
		}
		windows = append(windows, &window{Size: size})
	}

	return &EWMA{
		source:     source,
		metric:     prometheus.NewDesc(opts.Name, opts.Help, []string{"window"}, nil),
		updateFreq: opts.UpdateFrequency,

		windows: windows,
	}, nil
}

// MustNew calls [New] and panics on error.
func MustNew(opts Options, source Source) *EWMA {
	ewma, err := New(opts, source)
	if err != nil {
		panic(err)
	}
	return ewma
}

// Monitor 在 goroutine 中按 ticker 更新，ctx 取消后退出；重复启动会报错。
// Monitor starts the EWMA, polling values from the source at the configured
// update frequency.
//
// Monitor runs until the provided context is canceled. Monitor returns an error
// if a Monitor is already running.
func (ewma *EWMA) Monitor(ctx context.Context) error {
	if !ewma.running.CompareAndSwap(false, true) {
		return errors.New("EWMA is already running")
	}
	defer ewma.running.Store(false)

	ticker := time.NewTicker(ewma.updateFreq)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return nil
		case <-ticker.C:
			ewma.updateWindows()
		}
	}
}

// updateWindows 用同一时刻样本更新所有窗口，保证多窗口指标一致。
func (ewma *EWMA) updateWindows() {
	ewma.mut.Lock()
	defer ewma.mut.Unlock()

	// Make sure to use the same sample and "now" time for all windows for
	// consistency.
	var (
		sample = ewma.source.Get()
		now    = time.Now()
	)

	for _, w := range ewma.windows {
		w.Observe(sample, now)
	}
}

// Collect 仅在 Monitor 运行中上报，避免静止值误导监控。
// Collect writes the EWMA metrics to the given channel. Metrics are only
// written when the EWMA is running via [EWMA.Monitor].
func (ewma *EWMA) Collect(ch chan<- prometheus.Metric) {
	if !ewma.running.Load() {
		// Don't report any metrics unless the EWMA is running. Not running the
		// EWMA is a bug and reporting its values would be misleading since
		// they're not changing.
		return
	}

	ewma.mut.RLock()
	defer ewma.mut.RUnlock()

	for _, w := range ewma.windows {
		ch <- prometheus.MustNewConstMetric(ewma.metric, prometheus.GaugeValue, w.Value(), w.Name())
	}
}

// Describe describes the metrics emitted by EWMA.
func (ewma *EWMA) Describe(ch chan<- *prometheus.Desc) {
	ch <- ewma.metric
}
// Describe 向 Prometheus 注册带 window 标签的 Gauge 描述符。
