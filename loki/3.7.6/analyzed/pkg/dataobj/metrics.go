package dataobj

// dataobj 编码阶段 Prometheus 指标：section 数量与元数据大小分布。

import (
	"errors"
	"fmt"
	"time"

	"github.com/gogo/protobuf/proto"
	"github.com/prometheus/client_golang/prometheus"
)

// Metrics 使用原生直方图限制 builder 侧时间序列数量。
// Metrics instruments encoded data objects.
type Metrics struct {
	sectionsCount       prometheus.Histogram
	fileMetadataSize    prometheus.Histogram
	sectionMetadataSize *prometheus.HistogramVec
}

// NewMetrics 创建 sections_count、file_metadata_size 与按 section 标签的 vec。
// NewMetrics creates a new set of metrics for encoding.
func NewMetrics() *Metrics {
	// To limit the number of time series per data object builder, these metrics
	// are only available as classic histograms, otherwise we would have 10x the
	// total number of metrics.

	return &Metrics{
		sectionsCount: newNativeHistogram(prometheus.HistogramOpts{
			Namespace: "loki_dataobj",
			Subsystem: "encoding",
			Name:      "sections_count",
			Help:      "Distribution of sections per encoded data object.",
		}),

		fileMetadataSize: newNativeHistogram(prometheus.HistogramOpts{
			Namespace: "loki_dataobj",
			Subsystem: "encoding",
			Name:      "file_metadata_size",
			Help:      "Distribution of metadata size per encoded data object.",
		}),

		sectionMetadataSize: newNativeHistogramVec(prometheus.HistogramOpts{
			Namespace: "loki_dataobj",
			Subsystem: "encoding",
			Name:      "section_metadata_size",
			Help:      "Distribution of metadata size per encoded section.",
		}, []string{"section"}),
	}
}

// newNativeHistogram 统一设置 1.1 桶因子与每小时最小重置间隔。
func newNativeHistogram(opts prometheus.HistogramOpts) prometheus.Histogram {
	opts.NativeHistogramBucketFactor = 1.1
	opts.NativeHistogramMaxBucketNumber = 100
	opts.NativeHistogramMinResetDuration = time.Hour

	return prometheus.NewHistogram(opts)
}

func newNativeHistogramVec(opts prometheus.HistogramOpts, labels []string) *prometheus.HistogramVec {
	opts.NativeHistogramBucketFactor = 1.1
	opts.NativeHistogramMaxBucketNumber = 100
	opts.NativeHistogramMinResetDuration = time.Hour

	return prometheus.NewHistogramVec(opts, labels)
}

// Register registers metrics to report to reg.
func (m *Metrics) Register(reg prometheus.Registerer) error {
	var errs []error
	errs = append(errs, reg.Register(m.sectionsCount))
	errs = append(errs, reg.Register(m.fileMetadataSize))
	errs = append(errs, reg.Register(m.sectionMetadataSize))
	return errors.Join(errs...)
}

// Unregister unregisters metrics from the provided Registerer.
func (m *Metrics) Unregister(reg prometheus.Registerer) {
	reg.Unregister(m.sectionsCount)
	reg.Unregister(m.fileMetadataSize)
	reg.Unregister(m.sectionMetadataSize)
}

// Observe 遍历对象 metadata 中各 section 并记录元数据字节数。
// Observe updates metrics with statistics about the given [Object].
func (m *Metrics) Observe(obj *Object) error {
	m.sectionsCount.Observe(float64(len(obj.sections)))
	m.fileMetadataSize.Observe(float64(proto.Size(obj.metadata)))

	var errs []error

	for _, section := range obj.metadata.Sections {
		typ, err := getSectionType(obj.metadata, section)
		if err != nil {
			errs = append(errs, fmt.Errorf("getting section type: %w", err))
			continue
		}

		metadataSize := section.GetLayout().GetMetadata().GetLength()
		m.sectionMetadataSize.WithLabelValues(typ.String()).Observe(float64(metadataSize))
	}

	return errors.Join(errs...)
}
// section_metadata_size 按 section 类型标签区分各段元数据体积。
