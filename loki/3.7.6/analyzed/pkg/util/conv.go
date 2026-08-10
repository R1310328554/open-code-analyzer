package util //nolint:revive

// util 包 conv 子模块提供 Prometheus 标签集合与 map 的零拷贝转换，以及毫秒精度时间舍入与 Labels→Metric 辅助函数。

import (
	"time"
	"unsafe"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"
)

// ModelLabelSetToMap 通过 unsafe 指针将 LabelSet 视为 map，调用方不得修改返回值。
// ModelLabelSetToMap convert a model.LabelSet to a map[string]string
func ModelLabelSetToMap(m model.LabelSet) map[string]string {
	if len(m) == 0 {
		return map[string]string{}
	}
	return *(*map[string]string)(unsafe.Pointer(&m)) // #nosec G103 -- we know the string is not mutated -- nosemgrep: use-of-unsafe-block
}

// MapToModelLabelSet 为 ModelLabelSetToMap 的逆操作，空 map 返回空 LabelSet。
// MapToModelLabelSet converts a map into a model.LabelSet
func MapToModelLabelSet(m map[string]string) model.LabelSet {
	if len(m) == 0 {
		return model.LabelSet{}
	}
	return *(*map[model.LabelName]model.LabelValue)(unsafe.Pointer(&m)) // #nosec G103 -- we know the string is not mutated -- nosemgrep: use-of-unsafe-block
}

// RoundToMilliseconds 将 from 向下、through 向上舍入到毫秒，扩展查询窗口边界。
// RoundToMilliseconds returns milliseconds precision time from nanoseconds.
// from will be rounded down to the nearest milliseconds while through is rounded up.
func RoundToMilliseconds(from, through time.Time) (model.Time, model.Time) {
	fromMs := from.UnixNano() / int64(time.Millisecond)
	throughMs := through.UnixNano() / int64(time.Millisecond)

	// add a millisecond to the through time if the nanosecond offset within the second is not a multiple of milliseconds
	if int64(through.Nanosecond())%int64(time.Millisecond) != 0 {
		throughMs++
	}

	return model.Time(fromMs), model.Time(throughMs)
}

// LabelsToMetric 逐标签拷贝为 model.Metric，勿在热路径频繁调用。
// LabelsToMetric converts a Labels to Metric
// Don't do this on any performance sensitive paths.
func LabelsToMetric(ls labels.Labels) model.Metric {
	m := make(model.Metric, ls.Len())
	ls.Range(func(l labels.Label) {
		m[model.LabelName(l.Name)] = model.LabelValue(l.Value)
	})
	return m
}
// unsafe 转换依赖 Go map 与 LabelSet 内存布局相同，仅只读场景安全。
