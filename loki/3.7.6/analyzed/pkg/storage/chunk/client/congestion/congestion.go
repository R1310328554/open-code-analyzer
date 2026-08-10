package congestion

// congestion 根据配置组装 Controller：解析 strategy 字符串实例化 AIMD 或 noop 控制器，并注入 Retrier、Hedger 与 Metrics 装饰链。

import (
	"strings"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
)

// NewController 为日志添加 component=congestion_control 并链式挂载重试与 hedge 组件。
func NewController(cfg Config, logger log.Logger, metrics *Metrics) Controller {
	logger = log.With(logger, "component", "congestion_control")

	return newController(cfg, logger).
		withRetrier(newRetrier(cfg, logger)).
		withHedger(newHedger(cfg, logger)).
		withMetrics(metrics)
}

// newController 识别 aimd 策略，未知策略降级为 NoopController 并打 warn 日志。
func newController(cfg Config, logger log.Logger) Controller {
	start := strings.ToLower(cfg.Controller.Strategy)
	switch start {
	case "aimd":
		return NewAIMDController(cfg).withLogger(logger)
	default:
		level.Warn(logger).Log("msg", "unrecognized congestion control strategy in config, using noop", "strategy", start)
		return NewNoopController(cfg).withLogger(logger)
	}
}

// newRetrier 支持 limited 有限重试，否则使用 NoopRetrier 透传单次调用。
func newRetrier(cfg Config, logger log.Logger) Retrier {
	start := strings.ToLower(cfg.Retry.Strategy)
	switch start {
	case "limited":
		return NewLimitedRetrier(cfg).withLogger(logger)
	default:
		level.Warn(logger).Log("msg", "unrecognized retried strategy in config, using noop", "strategy", start)
		return NewNoopRetrier(cfg).withLogger(logger)
	}
}

func newHedger(cfg Config, logger log.Logger) Hedger {
	start := strings.ToLower(cfg.Hedge.Strategy)
	switch start {
	default:
		level.Warn(logger).Log("msg", "unrecognized hedging strategy in config, using noop", "strategy", start)
		return NewNoopHedger(cfg).withLogger(logger)
	}
}
// newHedger 当前所有 hedge 策略均降级为 noop，预留 limited 等未来实现。
