package stages

// labelallow.go — 白名单模式：仅保留配置中允许的标签。

import (
	"time"

	"github.com/mitchellh/mapstructure"
	"github.com/pkg/errors"
	"github.com/prometheus/common/model"
)

const (
	// ErrEmptyLabelAllowStageConfig error returned if config is empty
	ErrEmptyLabelAllowStageConfig = "labelallow stage config cannot be empty"
)

// LabelAllowConfig 为允许保留的标签名列表。
type LabelAllowConfig []string

// validateLabelAllowConfig 要求至少配置一个标签名。
func validateLabelAllowConfig(c LabelAllowConfig) error {
	if len(c) < 1 {
		return errors.New(ErrEmptyLabelAllowStageConfig)
	}

	return nil
}

// newLabelAllowStage 将列表转为 set 并包装为 Stage。
func newLabelAllowStage(configs interface{}) (Stage, error) {
	cfgs := &LabelAllowConfig{}
	err := mapstructure.Decode(configs, cfgs)
	if err != nil {
		return nil, err
	}

	err = validateLabelAllowConfig(*cfgs)
	if err != nil {
		return nil, err
	}

	labelMap := make(map[string]struct{})
	for _, label := range *cfgs {
		labelMap[label] = struct{}{}
	}

	return toStage(&labelAllowStage{
		labels: labelMap,
	}), nil
}

// labelAllowStage 删除不在白名单内的所有 labels 键。
type labelAllowStage struct {
	labels map[string]struct{}
}

// Process 遍历 labels，移除非白名单标签。
func (l *labelAllowStage) Process(labels model.LabelSet, _ map[string]interface{}, _ *time.Time, _ *string) {
	for label := range labels {
		if _, ok := l.labels[string(label)]; !ok {
			delete(labels, label)
		}
	}
}

// Name implements Stage
func (l *labelAllowStage) Name() string {
	return StageTypeLabelAllow
}
