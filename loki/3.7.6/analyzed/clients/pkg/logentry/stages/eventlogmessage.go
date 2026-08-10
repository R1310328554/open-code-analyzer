package stages

// eventlogmessage.go — 解析 Windows 事件日志 message 字段中的 key:value 行。

import (
	"fmt"
	"strings"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/mitchellh/mapstructure"
	"github.com/pkg/errors"
	"github.com/prometheus/common/model"
)

const (
	defaultSource                = "message"
	ErrEmptyEvtLogMsgStageConfig = "empty event log message stage configuration"
)

// EventLogMessageConfig 控制 message 来源键与标签冲突策略。
type EventLogMessageConfig struct {
	Source            *string `mapstructure:"source"`
	DropInvalidLabels bool    `mapstructure:"drop_invalid_labels"`
	OverwriteExisting bool    `mapstructure:"overwrite_existing"`
}

// eventLogMessageStage 将 message 内多行键值对写入 extracted 映射。
type eventLogMessageStage struct {
	cfg    *EventLogMessageConfig
	logger log.Logger
}

// newEventLogMessageStage 创建 event_log_message 阶段并校验 source 标签名。, including validating any supplied configuration
func newEventLogMessageStage(logger log.Logger, config interface{}) (Stage, error) {
	cfg, err := parseEventLogMessageConfig(config)
	if err != nil {
		return nil, err
	}
	// validate config (i.e., check that source is a valid log label)
	err = validateEventLogMessageConfig(cfg)
	if err != nil {
		return nil, err
	}
	return &eventLogMessageStage{
		cfg:    cfg,
		logger: log.With(logger, "component", "stage", "type", "event_log_message"),
	}, nil
}

// parseEventLogMessageConfig 解码阶段配置。, creating a default configuration struct otherwise
func parseEventLogMessageConfig(config interface{}) (*EventLogMessageConfig, error) {
	cfg := &EventLogMessageConfig{}
	err := mapstructure.Decode(config, cfg)
	if err != nil {
		return nil, err
	}
	return cfg, nil
}

// validateEventLogMessageConfig 校验配置非空且 source 为合法标签名。, checking that any specified source
// is a valid label name, and setting default values if a nil config object is provided
func validateEventLogMessageConfig(c *EventLogMessageConfig) error {
	if c == nil {
		return errors.New(ErrEmptyEvtLogMsgStageConfig)
	}
	if c.Source != nil && !model.LegacyValidation.IsValidLabelName(*c.Source) {
		return fmt.Errorf(ErrInvalidLabelName, *c.Source)
	}
	return nil
}

func (m *eventLogMessageStage) Run(in chan Entry) chan Entry {
	out := make(chan Entry)
	key := defaultSource
	if m.cfg.Source != nil {
		key = *m.cfg.Source
	}
	go func() {
		defer close(out)
		for e := range in {
			err := m.processEntry(e.Extracted, key)
			if err != nil {
				continue
			}
			out <- e
		}
	}()
	return out
}

// processEntry 按行解析 message，提取合法标签键值到 extracted。 with the specified key, adding additional
// entries into the extracted map
func (m *eventLogMessageStage) processEntry(extracted map[string]interface{}, key string) error {
	value, ok := extracted[key]
	if !ok {
		if Debug {
			level.Debug(m.logger).Log("msg", "source not in the extracted values", "source", key)
		}
		return nil
	}
	s, err := getString(value)
	if err != nil {
		level.Warn(m.logger).Log("msg", "invalid label value parsed", "value", value)
		return err
	}
	lines := strings.Split(s, "
")
	for _, line := range lines {
		parts := strings.SplitN(line, ":", 2)
		if len(parts) < 2 {
			level.Warn(m.logger).Log("msg", "invalid line parsed from message", "line", line)
			continue
		}
		mkey := parts[0]
		if !model.LegacyValidation.IsValidLabelName(mkey) {
			if m.cfg.DropInvalidLabels {
				if Debug {
					level.Debug(m.logger).Log("msg", "invalid label parsed from message", "key", mkey)
				}
				continue
			}
			mkey = SanitizeFullLabelName(mkey)
		}
		if _, ok := extracted[mkey]; ok && !m.cfg.OverwriteExisting {
			level.Info(m.logger).Log("msg", "extracted key that already existed, appending _extracted to key",
				"key", mkey)
			mkey += "_extracted"
		}
		mval := strings.TrimSpace(parts[1])
		if !model.LabelValue(mval).IsValid() {
			if Debug {
				level.Debug(m.logger).Log("msg", "invalid value parsed from message", "value", mval)
			}
			continue
		}
		extracted[mkey] = mval
	}
	if Debug {
		level.Debug(m.logger).Log("msg", "extracted data debug in event_log_message stage",
			"extracted data", fmt.Sprintf("%v", extracted))
	}
	return nil
}

func (m *eventLogMessageStage) Name() string {
	return StageTypeEventLogMessage
}

// Cleanup implements Stage.
func (*eventLogMessageStage) Cleanup() {
	// no-op
}

// SanitizeFullLabelName 将任意字符串规整为合法 Prometheus 标签名。
// TODO: switch to prometheus/prometheus/util/strutil/SanitizeFullLabelName
func SanitizeFullLabelName(input string) string {
	if len(input) == 0 {
		return "_"
	}
	var validSb strings.Builder
	for i, b := range input {
		if (b < 'a' || b > 'z') && (b < 'A' || b > 'Z') && b != '_' && (b < '0' || b > '9' || i <= 0) {
			validSb.WriteRune('_')
		} else {
			validSb.WriteRune(b)
		}
	}
	return validSb.String()
}
