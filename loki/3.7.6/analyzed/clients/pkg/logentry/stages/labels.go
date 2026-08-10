package stages

// labels.go — 将 extracted 中的字段提升为 Prometheus 流标签。

import (
	"fmt"
	"reflect"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/mitchellh/mapstructure"
	"github.com/pkg/errors"
	"github.com/prometheus/common/model"
)

const (
	ErrEmptyLabelStageConfig = "label stage config cannot be empty"
	ErrInvalidLabelName      = "invalid label name: %s"
)

// LabelsConfig 映射目标标签名到 extracted 源字段（空值则同名）。
type LabelsConfig map[string]*string

// validateLabelsConfig 校验标签名合法并为空 source 填默认值。
func validateLabelsConfig(c LabelsConfig) error {
	if c == nil {
		return errors.New(ErrEmptyLabelStageConfig)
	}
	for labelName, labelSrc := range c {
		if !model.LegacyValidation.IsValidLabelName(labelName) {
			return fmt.Errorf(ErrInvalidLabelName, labelName)
		}
		// If no label source was specified, use the key name
		if labelSrc == nil || *labelSrc == "" {
			lName := labelName
			c[labelName] = &lName
		}
	}
	return nil
}

// newLabelStage 创建 labelStage，从 extracted 写入 labels。
func newLabelStage(logger log.Logger, configs interface{}) (Stage, error) {
	cfgs := &LabelsConfig{}
	err := mapstructure.Decode(configs, cfgs)
	if err != nil {
		return nil, err
	}
	err = validateLabelsConfig(*cfgs)
	if err != nil {
		return nil, err
	}
	return toStage(&labelStage{
		cfgs:   *cfgs,
		logger: logger,
	}), nil
}

// labelStage 从 extracted 取值并校验后写入 labels。
type labelStage struct {
	cfgs   LabelsConfig
	logger log.Logger
}

// Process 将 extracted 中匹配字段设为 stream labels。
func (l *labelStage) Process(labels model.LabelSet, extracted map[string]interface{}, _ *time.Time, _ *string) {
	processLabelsConfigs(l.logger, extracted, l.cfgs, func(_, labelName model.LabelName, labelValue model.LabelValue) {
		labels[labelName] = labelValue
	})
}

// labelsConsumer 回调：将 source 字段值写入目标 labelName。
type labelsConsumer func(source, labelName model.LabelName, labelValue model.LabelValue)

// processLabelsConfigs 遍历配置，转换并校验 extracted 值后调用 consumer。
func processLabelsConfigs(logger log.Logger, extracted map[string]interface{}, configs LabelsConfig, consumer labelsConsumer) {
	for lName, lSrc := range configs {
		source := *lSrc
		if lValue, ok := extracted[source]; ok {
			s, err := getString(lValue)
			if err != nil {
				if Debug {
					level.Debug(logger).Log("msg", "failed to convert extracted label value to string", "err", err, "type", reflect.TypeOf(lValue))
				}
				continue
			}
			labelValue := model.LabelValue(s)
			if !labelValue.IsValid() {
				if Debug {
					level.Debug(logger).Log("msg", "invalid label value parsed", "value", labelValue)
				}
				continue
			}
			consumer(model.LabelName(source), model.LabelName(lName), labelValue)
		}
	}
}

// Name implements Stage
func (l *labelStage) Name() string {
	return StageTypeLabel
}
