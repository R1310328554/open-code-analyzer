package log

// keep_labels 实现 LogQL keep 标签 Stage：仅保留配置列表中的标签，其余非特殊标签一律删除。

import (
	"github.com/grafana/loki/v3/pkg/logqlmodel"
)

type KeepLabels struct {
	labels []NamedLabelMatcher
}

func NewKeepLabels(labels []NamedLabelMatcher) *KeepLabels {
	return &KeepLabels{labels: labels}
}

// Process 遍历当前未排序标签，未命中 keep 规则且非特殊标签时调用 Del 移除。
func (kl *KeepLabels) Process(_ int64, line []byte, lbls *LabelsBuilder) ([]byte, bool) {
	if len(kl.labels) == 0 {
		return line, true
	}

	// TODO: Reuse buf?
	for _, lb := range lbls.UnsortedLabels(nil) {
		if isSpecialLabel(lb.Name) {
			continue
		}

		var keep bool
		for _, keepLabel := range kl.labels {
			if keepLabel.Matcher != nil && keepLabel.Matcher.Name == lb.Name && keepLabel.Matcher.Matches(lb.Value) {
				keep = true
				break
			}

			if keepLabel.Name == lb.Name {
				keep = true
				break
			}
		}

		if !keep {
			lbls.Del(lb.Name)
		}
	}

	return line, true
}

func (kl *KeepLabels) RequiredLabelNames() []string {
	return []string{}
}

// isSpecialLabel 保护 __error__、__error_details__ 与 preserve 类内部标签不被 keep 误删。
func isSpecialLabel(lblName string) bool {
	switch lblName {
	case logqlmodel.ErrorLabel, logqlmodel.ErrorDetailsLabel, logqlmodel.PreserveErrorLabel:
		return true
	}

	return false
}
// keep 支持按名称或 Matcher 匹配；Matcher 需名称与值同时满足才保留对应标签。
