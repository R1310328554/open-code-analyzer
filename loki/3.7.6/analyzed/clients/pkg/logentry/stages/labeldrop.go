package stages

// labeldrop.go — 按名称列表从 labels 中删除指定标签。

import (
	"time"

	"github.com/mitchellh/mapstructure"
	"github.com/pkg/errors"
	"github.com/prometheus/common/model"
)

const (
	// ErrEmptyLabelDropStageConfig error returned if config is empty
	ErrEmptyLabelDropStageConfig = "labeldrop stage config cannot be empty"
)

// LabelDropConfig 为需要删除的标签名列表。
type LabelDropConfig []string

// validateLabelDropConfig 要求至少配置一个待删标签。
func validateLabelDropConfig(c LabelDropConfig) error {
	if len(c) < 1 {
		return errors.New(ErrEmptyLabelDropStageConfig)
	}

	return nil
}

// newLabelDropStage 解析配置并创建 labelDropStage。
func newLabelDropStage(configs interface{}) (Stage, error) {
	cfgs := &LabelDropConfig{}
	err := mapstructure.Decode(configs, cfgs)
	if err != nil {
		return nil, err
	}

	err = validateLabelDropConfig(*cfgs)
	if err != nil {
		return nil, err
	}

	return toStage(&labelDropStage{
		cfgs: *cfgs,
	}), nil
}

// labelDropStage 按配置名逐一 delete labels。
type labelDropStage struct {
	cfgs LabelDropConfig
}

// Process 删除配置列出的每个 label。
func (l *labelDropStage) Process(labels model.LabelSet, _ map[string]interface{}, _ *time.Time, _ *string) {
	for _, label := range l.cfgs {
		delete(labels, model.LabelName(label))
	}
}

// Name implements Stage
func (l *labelDropStage) Name() string {
	return StageTypeLabelDrop
}
