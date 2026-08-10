package stages

// output 阶段：用 extracted 中指定字段覆盖日志行内容。
// 常用于 regex/json 解析后将结构化字段写回最终 line。

import (
	"errors"
	"reflect"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/mitchellh/mapstructure"
	"github.com/prometheus/common/model"
)

// Config Errors
const (
	ErrEmptyOutputStageConfig = "output stage config cannot be empty"
	ErrOutputSourceRequired   = "output source value is required if output is specified"
)

// output 配置：source 键名指向 extracted 中要写入行的字段。
// OutputConfig configures output value extraction
type OutputConfig struct {
	Source string `mapstructure:"source"`
}

// validateOutput validates the outputStage config
func validateOutputConfig(cfg *OutputConfig) error {
	if cfg == nil {
		return errors.New(ErrEmptyOutputStageConfig)
	}
	if cfg.Source == "" {
		return errors.New(ErrOutputSourceRequired)
	}
	return nil
}

// 解码配置并包装为同步 Processor stage。
// newOutputStage creates a new outputStage
func newOutputStage(logger log.Logger, config interface{}) (Stage, error) {
	cfg := &OutputConfig{}
	err := mapstructure.Decode(config, cfg)
	if err != nil {
		return nil, err
	}
	err = validateOutputConfig(cfg)
	if err != nil {
		return nil, err
	}
	return toStage(&outputStage{
		cfgs:   cfg,
		logger: logger,
	}), nil
}

// output 阶段：将 source 字段字符串化后赋值给 entry 行。
// outputStage will mutate the incoming entry and set it from extracted data
type outputStage struct {
	cfgs   *OutputConfig
	logger log.Logger
}

// 若 extracted 含 source 则替换 *entry，否则 Debug 记录缺失。
// Process implements Stage
func (o *outputStage) Process(_ model.LabelSet, extracted map[string]interface{}, _ *time.Time, entry *string) {
	if o.cfgs == nil {
		return
	}
	if v, ok := extracted[o.cfgs.Source]; ok {
		s, err := getString(v)
		if err != nil {
			if Debug {
				level.Debug(o.logger).Log("msg", "extracted output could not be converted to a string", "err", err, "type", reflect.TypeOf(v))
			}
			return
		}
		*entry = s
	} else {
		if Debug {
			level.Debug(o.logger).Log("msg", "extracted data did not contain output source")
		}
	}
}

// Name implements Stage
func (o *outputStage) Name() string {
	return StageTypeOutput
}
