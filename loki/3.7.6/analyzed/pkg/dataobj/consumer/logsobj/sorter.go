package logsobj

// sorter 模块封装对象级日志排序：
// 通过 BuilderFactory 创建临时 Builder 并调用 CopyAndSort 重写 logs section。

import (
	"context"
	"fmt"
	"io"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/dataobj"
)

// Sorter 持有 BuilderFactory 与排序耗时 histogram 指标。
// A Sorter sorts data objects.
type Sorter struct {
	factory  *BuilderFactory
	duration prometheus.Histogram
}

// NewSorter 注册 loki_dataobj_sort_duration_seconds 直方图。
// NewSorter returns a new Sorter.
func NewSorter(factory *BuilderFactory, r prometheus.Registerer) *Sorter {
	return &Sorter{
		factory: factory,
		duration: promauto.With(r).NewHistogram(prometheus.HistogramOpts{
			Name: "loki_dataobj_sort_duration_seconds",
			Help: "Histogram of time spent sorting data objects",

			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: 0,
		}),
	}
}

// Sort 创建 Builder 并调用 CopyAndSort，记录排序耗时后返回新对象。
// Sort takes an existing data object and rewrites the logs sections so they are
// sorted object-wide.
func (s *Sorter) Sort(ctx context.Context, obj *dataobj.Object) (*dataobj.Object, io.Closer, error) {
	b, err := s.factory.NewBuilder(nil)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to create builder: %w", err)
	}
	t := prometheus.NewTimer(s.duration)
	defer t.ObserveDuration()
	// Don't need to reset b as it is discarded after each use.
	return b.CopyAndSort(ctx, obj)
}
