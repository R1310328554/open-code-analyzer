package metric

// Pipeline metrics stage 的 Counter 实现。
// 按日志流标签维护可过期的 Prometheus Counter，支持 inc/add 与字节计数。

import (
	"strings"
	"time"

	"github.com/mitchellh/mapstructure"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"
)

const (
	CounterInc = "inc"
	CounterAdd = "add"

	ErrCounterActionRequired          = "counter action must be defined as either `inc` or `add`"
	ErrCounterInvalidAction           = "action %s is not valid, action must be either `inc` or `add`"
	ErrCounterInvalidMatchAll         = "`match_all: true` cannot be combined with `value`, please remove `match_all` or `value`"
	ErrCounterInvalidCountBytes       = "`count_entry_bytes: true` can only be set with `match_all: true`"
	ErrCounterInvalidCountBytesAction = "`count_entry_bytes: true` can only be used with `action: add`"
)

// Counter stage 配置：match_all、count_entry_bytes、value 与 action。
type CounterConfig struct {
	MatchAll   *bool   `mapstructure:"match_all"`
	CountBytes *bool   `mapstructure:"count_entry_bytes"`
	Value      *string `mapstructure:"value"`
	Action     string  `mapstructure:"action"`
}

func validateCounterConfig(config *CounterConfig) error {
	if config.Action == "" {
		return errors.New(ErrCounterActionRequired)
	}
	config.Action = strings.ToLower(config.Action)
	if config.Action != CounterInc && config.Action != CounterAdd {
		return errors.Errorf(ErrCounterInvalidAction, config.Action)
	}
	if config.MatchAll != nil && *config.MatchAll && config.Value != nil {
		return errors.Errorf(ErrCounterInvalidMatchAll)
	}
	if config.CountBytes != nil && *config.CountBytes && (config.MatchAll == nil || !*config.MatchAll) {
		return errors.New(ErrCounterInvalidCountBytes)
	}
	if config.CountBytes != nil && *config.CountBytes && config.Action != CounterAdd {
		return errors.New(ErrCounterInvalidCountBytesAction)
	}
	return nil
}

func parseCounterConfig(config interface{}) (*CounterConfig, error) {
	cfg := &CounterConfig{}
	err := mapstructure.Decode(config, cfg)
	if err != nil {
		return nil, err
	}
	return cfg, nil
}

// 按日志流标签分片的 Counter 向量，底层为带空闲回收的 metricVec。
// Counters is a vec tor of counters for a each log stream.
type Counters struct {
	*metricVec
	Cfg *CounterConfig
}

// 解析并校验配置后创建 Counter 指标向量。
// NewCounters creates a new counter vec.
func NewCounters(name, help string, config interface{}, maxIdleSec int64) (*Counters, error) {
	cfg, err := parseCounterConfig(config)
	if err != nil {
		return nil, err
	}
	err = validateCounterConfig(cfg)
	if err != nil {
		return nil, err
	}
	return &Counters{
		metricVec: newMetricVec(func(labels map[string]string) prometheus.Metric {
			return &expiringCounter{prometheus.NewCounter(prometheus.CounterOpts{
				Help:        help,
				Name:        name,
				ConstLabels: labels,
			}),
				0,
			}
		}, maxIdleSec),
		Cfg: cfg,
	}, nil
}

// With returns the counter associated with a stream labelset.
func (c *Counters) With(labels model.LabelSet) prometheus.Counter {
	return c.metricVec.With(labels).(prometheus.Counter)
}

// 带最后修改时间戳的 Counter 包装，供 metricVec 空闲过期回收。
type expiringCounter struct {
	prometheus.Counter
	lastModSec int64
}

// Inc increments the counter by 1. Use Add to increment it by arbitrary
// non-negative values.
func (e *expiringCounter) Inc() {
	e.Counter.Inc()
	e.lastModSec = time.Now().Unix()
}

// Add adds the given value to the counter. It panics if the value is <
// 0.
func (e *expiringCounter) Add(val float64) {
	e.Counter.Add(val)
	e.lastModSec = time.Now().Unix()
}

// HasExpired implements Expirable
func (e *expiringCounter) HasExpired(currentTimeSec int64, maxAgeSec int64) bool {
	return currentTimeSec-e.lastModSec >= maxAgeSec
}
