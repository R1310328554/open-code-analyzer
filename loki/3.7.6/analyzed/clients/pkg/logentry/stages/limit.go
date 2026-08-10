package stages

// limit.go — 令牌桶限速：可全局或按标签值分别限流并可选丢弃。

import (
	"context"
	"fmt"

	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/util"

	"github.com/go-kit/log"
	"github.com/mitchellh/mapstructure"
	"github.com/pkg/errors"
	"golang.org/x/time/rate"
)

const (
	ErrLimitStageInvalidRateOrBurst = "limit stage failed to parse rate or burst"
	ErrLimitStageByLabelMustDrop    = "When ratelimiting by label, drop must be true"
	MinReasonableMaxDistinctLabels  = 10000 // 80bytes per rate.Limiter ~ 1MiB memory
)

var ratelimitDropReason = "ratelimit_drop_stage"

// LimitConfig 定义 rate/burst、是否 drop 及按标签分桶参数。
type LimitConfig struct {
	Rate              float64 `mapstructure:"rate"`
	Burst             int     `mapstructure:"burst"`
	Drop              bool    `mapstructure:"drop"`
	ByLabelName       string  `mapstructure:"by_label_name"`
	MaxDistinctLabels int     `mapstructure:"max_distinct_labels"`
}

// newLimitStage 创建 limitStage，按配置选择全局或分标签 Limiter。
func newLimitStage(logger log.Logger, config interface{}, registerer prometheus.Registerer) (Stage, error) {
	cfg := &LimitConfig{}

	err := mapstructure.WeakDecode(config, cfg)
	if err != nil {
		return nil, err
	}
	err = validateLimitConfig(cfg)
	if err != nil {
		return nil, err
	}

	logger = log.With(logger, "component", "stage", "type", "limit")
	if cfg.ByLabelName != "" && cfg.MaxDistinctLabels < MinReasonableMaxDistinctLabels {
		level.Warn(logger).Log(
			"msg",
			fmt.Sprintf("max_distinct_labels was adjusted up to the minimal reasonable value of %d", MinReasonableMaxDistinctLabels),
		)
		cfg.MaxDistinctLabels = MinReasonableMaxDistinctLabels
	}

	r := &limitStage{
		logger:    logger,
		cfg:       cfg,
		dropCount: getDropCountMetric(registerer),
	}

	if cfg.ByLabelName != "" {
		r.dropCountByLabel = getDropCountByLabelMetric(registerer)
		newRateLimiter := func() *rate.Limiter { return rate.NewLimiter(rate.Limit(cfg.Rate), cfg.Burst) }
		gcCb := func() { r.dropCountByLabel.Reset() }
		r.rateLimiterByLabel = util.NewGenMap[model.LabelValue, *rate.Limiter](cfg.MaxDistinctLabels, newRateLimiter, gcCb)
	} else {
		r.rateLimiter = rate.NewLimiter(rate.Limit(cfg.Rate), cfg.Burst)
	}

	return r, nil
}

// validateLimitConfig 校验 rate/burst 为正；按标签限流时必须 drop。
func validateLimitConfig(cfg *LimitConfig) error {
	if cfg.Rate <= 0 || cfg.Burst <= 0 {
		return errors.Errorf(ErrLimitStageInvalidRateOrBurst)
	}

	if cfg.ByLabelName != "" && !cfg.Drop {
		return errors.Errorf(ErrLimitStageByLabelMustDrop)
	}
	return nil
}

// limitStage 在 shouldThrottle 为 true 时丢弃或阻塞日志。
type limitStage struct {
	logger             log.Logger
	cfg                *LimitConfig
	rateLimiter        *rate.Limiter
	rateLimiterByLabel util.GenerationalMap[model.LabelValue, *rate.Limiter]
	dropCount          *prometheus.CounterVec
	dropCountByLabel   *prometheus.CounterVec
	byLabelName        model.LabelName
}

func (m *limitStage) Run(in chan Entry) chan Entry {
	out := make(chan Entry)
	go func() {
		defer close(out)
		for e := range in {
			if !m.shouldThrottle(e.Labels) {
				out <- e
				continue
			}
		}
	}()
	return out
}

// shouldThrottle 按全局或标签 Limiter 判断是否应限流/丢弃。
func (m *limitStage) shouldThrottle(labels model.LabelSet) bool {
	if m.cfg.ByLabelName != "" {
		labelValue, ok := labels[model.LabelName(m.cfg.ByLabelName)]
		if !ok {
			return false // if no label found, dont ratelimit
		}
		rl := m.rateLimiterByLabel.GetOrCreate(labelValue)
		if rl.Allow() {
			return false
		}
		m.dropCount.WithLabelValues(ratelimitDropReason).Inc()
		m.dropCountByLabel.WithLabelValues(m.cfg.ByLabelName, string(labelValue)).Inc()
		return true
	}

	if m.cfg.Drop {
		if m.rateLimiter.Allow() {
			return false
		}
		m.dropCount.WithLabelValues(ratelimitDropReason).Inc()
		return true
	}
	_ = m.rateLimiter.Wait(context.Background())
	return false
}

// Name implements Stage
func (m *limitStage) Name() string {
	return StageTypeLimit
}

// Cleanup implements Stage.
func (*limitStage) Cleanup() {
	// no-op
}

// getDropCountByLabelMetric 注册按标签维度统计的丢弃计数。
func getDropCountByLabelMetric(registerer prometheus.Registerer) *prometheus.CounterVec {
	return util.RegisterCounterVec(registerer, "logentry", "dropped_lines_by_label_total",
		"A count of all log lines dropped as a result of a pipeline stage",
		[]string{"label_name", "label_value"})
}
