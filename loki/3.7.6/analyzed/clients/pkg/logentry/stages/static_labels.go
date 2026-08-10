package stages

// static_labels 阶段：为每条日志附加固定键值标签。
// 标签名须符合 Prometheus UTF8 规范，值为配置字面量。

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
	// ErrEmptyStaticLabelStageConfig error returned if config is empty
	ErrEmptyStaticLabelStageConfig = "static_labels stage config cannot be empty"
)

// 静态标签配置：标签名到固定字符串值的映射。
// StaticLabelConfig is a slice of static-labels to be included
type StaticLabelConfig map[string]*string

func validateLabelStaticConfig(c StaticLabelConfig) error {
	if c == nil {
		return errors.New(ErrEmptyStaticLabelStageConfig)
	}
	for labelName := range c {
		if !model.UTF8Validation.IsValidLabelName(labelName) {
			return fmt.Errorf(ErrInvalidLabelName, labelName)
		}
	}
	return nil
}

// 解码 static_labels 配置并返回 StaticLabelStage。
func newStaticLabelsStage(logger log.Logger, configs interface{}) (Stage, error) {
	cfgs := &StaticLabelConfig{}
	err := mapstructure.Decode(configs, cfgs)
	if err != nil {
		return nil, err
	}

	err = validateLabelStaticConfig(*cfgs)
	if err != nil {
		return nil, err
	}

	return toStage(&StaticLabelStage{
		cfgs:   *cfgs,
		logger: logger,
	}), nil
}

// static_labels 阶段：持有标签映射与 logger。
type StaticLabelStage struct {
	cfgs   StaticLabelConfig
	logger log.Logger
}

// 遍历配置将非空静态值写入 labels（跳过非法值）。
// Process implements Stage
func (l *StaticLabelStage) Process(labels model.LabelSet, _ map[string]interface{}, _ *time.Time, _ *string) {

	for lName, lSrc := range l.cfgs {
		if lSrc == nil || *lSrc == "" {
			continue
		}
		s, err := getString(*lSrc)
		if err != nil {
			if Debug {
				level.Debug(l.logger).Log("msg", "failed to convert static label value to string", "err", err, "type", reflect.TypeOf(lSrc))
			}
			continue
		}
		lvalue := model.LabelValue(s)
		if !lvalue.IsValid() {
			if Debug {
				level.Debug(l.logger).Log("msg", "invalid label value parsed", "value", lvalue)
			}
			continue
		}
		lname := model.LabelName(lName)
		labels[lname] = lvalue
	}
}

// Name implements Stage
func (l *StaticLabelStage) Name() string {
	return StageTypeStaticLabels
}
