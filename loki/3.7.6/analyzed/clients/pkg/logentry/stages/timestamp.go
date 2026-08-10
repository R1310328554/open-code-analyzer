package stages

// timestamp 阶段：从 extracted 解析时间并覆盖 Entry 时间戳。
// 支持 fallback_formats、时区与失败时 skip/fudge（递增 1ns）策略。

import (
	"errors"
	"fmt"
	"reflect"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	lru "github.com/hashicorp/golang-lru/v2"
	"github.com/mitchellh/mapstructure"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/util"
)

const (
	ErrEmptyTimestampStageConfig = "timestamp stage config cannot be empty"
	ErrTimestampSourceRequired   = "timestamp source value is required if timestamp is specified"
	ErrTimestampFormatRequired   = "timestamp format is required"
	ErrInvalidLocation           = "invalid location specified: %v"
	ErrInvalidActionOnFailure    = "invalid action on failure (supported values are %v)"
	ErrTimestampSourceMissing    = "extracted data did not contain a timestamp"
	ErrTimestampConversionFailed = "failed to convert extracted time to string"
	ErrTimestampParsingFailed    = "failed to parse time"

	TimestampActionOnFailureSkip    = "skip"
	TimestampActionOnFailureFudge   = "fudge"
	TimestampActionOnFailureDefault = TimestampActionOnFailureFudge

	// Maximum number of "streams" for which we keep the last known timestamp
	maxLastKnownTimestampsCacheSize = 10000
)

var (
	TimestampActionOnFailureOptions = []string{TimestampActionOnFailureSkip, TimestampActionOnFailureFudge}
)

// timestamp 配置：source、format、fallback、location 与失败动作。
// TimestampConfig configures timestamp extraction
type TimestampConfig struct {
	Source          string   `mapstructure:"source"`
	Format          string   `mapstructure:"format"`
	FallbackFormats []string `mapstructure:"fallback_formats"`
	Location        *string  `mapstructure:"location"`
	ActionOnFailure *string  `mapstructure:"action_on_failure"`
}

// parser can convert the time string into a time.Time value
// 时间解析函数类型：将字符串按 layout 转为 time.Time。
type parser func(string) (time.Time, error)

// validateTimestampConfig validates a timestampStage configuration
func validateTimestampConfig(cfg *TimestampConfig) (parser, error) {
	if cfg == nil {
		return nil, errors.New(ErrEmptyTimestampStageConfig)
	}
	if cfg.Source == "" {
		return nil, errors.New(ErrTimestampSourceRequired)
	}
	if cfg.Format == "" {
		return nil, errors.New(ErrTimestampFormatRequired)
	}
	var loc *time.Location
	var err error
	if cfg.Location != nil {
		loc, err = time.LoadLocation(*cfg.Location)
		if err != nil {
			return nil, fmt.Errorf(ErrInvalidLocation, err)
		}
	}

	// Validate the action on failure and enforce the default
	if cfg.ActionOnFailure == nil {
		cfg.ActionOnFailure = util.StringRef(TimestampActionOnFailureDefault)
	} else {
		if !util.StringsContain(TimestampActionOnFailureOptions, *cfg.ActionOnFailure) {
			return nil, fmt.Errorf(ErrInvalidActionOnFailure, TimestampActionOnFailureOptions)
		}
	}

	if len(cfg.FallbackFormats) > 0 {
		multiConvertDateLayout := func(input string) (time.Time, error) {
			orignalTime, originalErr := convertDateLayout(cfg.Format, loc)(input)
			if originalErr == nil {
				return orignalTime, originalErr
			}
			for i := 0; i < len(cfg.FallbackFormats); i++ {
				if t, err := convertDateLayout(cfg.FallbackFormats[i], loc)(input); err == nil {
					return t, err
				}
			}
			return orignalTime, originalErr
		}
		return multiConvertDateLayout, nil
	}

	return convertDateLayout(cfg.Format, loc), nil
}

// 创建 timestampStage，fudge 模式时初始化 LRU 末知时间戳缓存。
// newTimestampStage creates a new timestamp extraction pipeline stage.
func newTimestampStage(logger log.Logger, config interface{}) (Stage, error) {
	cfg := &TimestampConfig{}
	err := mapstructure.Decode(config, cfg)
	if err != nil {
		return nil, err
	}
	parser, err := validateTimestampConfig(cfg)
	if err != nil {
		return nil, err
	}

	var lastKnownTimestamps *lru.Cache[string, time.Time]
	if *cfg.ActionOnFailure == TimestampActionOnFailureFudge {
		lastKnownTimestamps, err = lru.New[string, time.Time](maxLastKnownTimestampsCacheSize)
		if err != nil {
			return nil, err
		}
	}

	return toStage(&timestampStage{
		cfg:                 cfg,
		logger:              logger,
		parser:              parser,
		lastKnownTimestamps: lastKnownTimestamps,
	}), nil
}

// timestamp 阶段：parser 成功则写 *t，失败走 action_on_failure。
// timestampStage will set the timestamp using extracted data
type timestampStage struct {
	cfg    *TimestampConfig
	logger log.Logger
	parser parser

	// Stores the last known timestamp for a given "stream id" (guessed, since at this stage
	// there's no reliable way to know it).
	lastKnownTimestamps *lru.Cache[string, time.Time]
}

// Name implements Stage
func (ts *timestampStage) Name() string {
	return StageTypeTimestamp
}

// 解析 extracted[source] 时间；fudge 成功/失败后维护 per-stream LRU。
// Process implements Stage
func (ts *timestampStage) Process(labels model.LabelSet, extracted map[string]interface{}, t *time.Time, _ *string) {
	if ts.cfg == nil {
		return
	}

	parsedTs, err := ts.parseTimestampFromSource(extracted)
	if err != nil {
		ts.processActionOnFailure(labels, t)
		return
	}

	// Update the log entry timestamp with the parsed one
	*t = *parsedTs

	// The timestamp has been correctly parsed, so we should store it in the map
	// containing the last known timestamp used by the "fudge" action on failure.
	if *ts.cfg.ActionOnFailure == TimestampActionOnFailureFudge {
		ts.lastKnownTimestamps.Add(labels.String(), *t)
	}
}

// 从 extracted 取 source 字段并按配置 format 解析为 time.Time。
func (ts *timestampStage) parseTimestampFromSource(extracted map[string]interface{}) (*time.Time, error) {
	// Ensure the extracted data contains the timestamp source
	v, ok := extracted[ts.cfg.Source]
	if !ok {
		if Debug {
			level.Debug(ts.logger).Log("msg", ErrTimestampSourceMissing)
		}

		return nil, errors.New(ErrTimestampSourceMissing)
	}

	// Convert the timestamp source to string (if it's not a string yet)
	s, err := getString(v)
	if err != nil {
		if Debug {
			level.Debug(ts.logger).Log("msg", ErrTimestampConversionFailed, "err", err, "type", reflect.TypeOf(v))
		}

		return nil, errors.New(ErrTimestampConversionFailed)
	}

	// Parse the timestamp source according to the configured format
	parsedTs, err := ts.parser(s)
	if err != nil {
		if Debug {
			level.Debug(ts.logger).Log("msg", ErrTimestampParsingFailed, "err", err, "format", ts.cfg.Format, "value", s)
		}

		return nil, errors.New(ErrTimestampParsingFailed)
	}

	return &parsedTs, nil
}

// 解析失败时分派到 fudge（递增缓存时间）或 skip（保持原值）。
func (ts *timestampStage) processActionOnFailure(labels model.LabelSet, t *time.Time) {
	switch *ts.cfg.ActionOnFailure {
	case TimestampActionOnFailureFudge:
		ts.processActionOnFailureFudge(labels, t)
	case TimestampActionOnFailureSkip:
		// Nothing to do
	}
}

// 用该 label 串上次成功时间 +1ns  fudge，并更新 LRU 缓存。
func (ts *timestampStage) processActionOnFailureFudge(labels model.LabelSet, t *time.Time) {
	labelsStr := labels.String()
	lastTimestamp, ok := ts.lastKnownTimestamps.Get(labelsStr)

	// If the last known timestamp is unknown (ie. has not been successfully parsed yet)
	// there's nothing we can do, so we're going to keep the current timestamp
	if !ok {
		return
	}

	// Fudge the timestamp
	*t = lastTimestamp.Add(1 * time.Nanosecond)

	// Store the fudged timestamp, so that a subsequent fudged timestamp will be 1ns after it
	ts.lastKnownTimestamps.Add(labelsStr, *t)
}
