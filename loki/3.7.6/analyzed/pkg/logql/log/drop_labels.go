package log

// drop_labels 实现 LogQL drop 标签 Stage：按名称或 Matcher 从 LabelsBuilder 移除指定标签。

import (
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logqlmodel"
)

type NamedLabelMatcher struct {
	Matcher *labels.Matcher
	Name    string
}

func NewNamedLabelMatcher(m *labels.Matcher, n string) NamedLabelMatcher {
	return NamedLabelMatcher{
		Matcher: m,
		Name:    n,
	}
}

// DropLabels 流水线 Stage，遍历配置的匹配项并从当前标签集删除命中项。
type DropLabels struct {
	labels []NamedLabelMatcher
}

func NewDropLabels(labels []NamedLabelMatcher) *DropLabels {
	return &DropLabels{labels: labels}
}

func (dl *DropLabels) Process(_ int64, line []byte, lbls *LabelsBuilder) ([]byte, bool) {
	for _, dropLabel := range dl.labels {
		if dropLabel.Matcher != nil {
			dropLabelMatches(dropLabel.Matcher, lbls)
			continue
		}
		name := dropLabel.Name
		dropLabelNames(name, lbls)
	}
	return line, true
}

func (dl *DropLabels) RequiredLabelNames() []string { return []string{} }

// isErrorLabel 判断是否为 LogQL 内部错误标签 __error__。
func isErrorLabel(name string) bool {
	return name == logqlmodel.ErrorLabel
}

func isErrorDetailsLabel(name string) bool {
	return name == logqlmodel.ErrorDetailsLabel
}

func dropLabelNames(name string, lbls *LabelsBuilder) {
	if isErrorLabel(name) {
		lbls.ResetError()
		return
	}
	if isErrorDetailsLabel(name) {
		lbls.ResetErrorDetails()
		return
	}
	if _, ok := lbls.Get(name); ok {
		lbls.Del(name)
	}
}

// dropLabelMatches 当标签值满足 Matcher 时删除该标签或重置错误状态。
func dropLabelMatches(matcher *labels.Matcher, lbls *LabelsBuilder) {
	var value string
	name := matcher.Name
	if isErrorLabel(name) {
		value = lbls.GetErr()
		if matcher.Matches(value) {
			lbls.ResetError()
		}
		return
	}
	if isErrorDetailsLabel(name) {
		value = lbls.GetErrorDetails()
		if matcher.Matches(value) {
			lbls.ResetErrorDetails()
		}
		return
	}
	value, _ = lbls.Get(name)
	if matcher.Matches(value) {
		lbls.Del(name)
	}
}
// 错误与错误详情标签通过 ResetError/ResetErrorDetails 专用路径处理，避免残留解析失败信息。
