package writefailures

// Manager 按租户限流记录 distributor 写入失败，尊重租户级 LimitedLogPushErrors 开关。

import (
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/limiter"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/runtime"
)

// Manager 组合 RateLimiter、租户配置、Prometheus 指标与带 path=write 的 logger。
type Manager struct {
	limiter    *limiter.RateLimiter
	logger     log.Logger
	tenantCfgs *runtime.TenantConfigs
	metrics    *metrics
}

func NewManager(logger log.Logger, reg prometheus.Registerer, cfg Cfg, tenants *runtime.TenantConfigs, subsystem string) *Manager {
	logger = log.With(logger, "path", "write")
	if cfg.AddInsightsLabel {
		logger = log.With(logger, "insight", "true")
	}

	strategy := newStrategy(cfg.LogRate.Val(), float64(cfg.LogRate.Val()))

	return &Manager{
		limiter:    limiter.NewRateLimiter(strategy, time.Minute),
		logger:     logger,
		tenantCfgs: tenants,
		metrics:    newMetrics(reg, subsystem),
	}
}

// Log 在租户启用失败日志或重复流信息时，按消息长度消耗令牌并输出或丢弃。
func (m *Manager) Log(tenantID string, err error) {
	if m == nil {
		return
	}

	if !m.tenantCfgs.LimitedLogPushErrors(tenantID) && !m.tenantCfgs.LogDuplicateStreamInfo(tenantID) {
		return
	}

	errMsg := err.Error()
	if m.limiter.AllowN(time.Now(), tenantID, len(errMsg)) {
		m.metrics.loggedCount.WithLabelValues(tenantID).Inc()
		level.Error(m.logger).Log("msg", "write operation failed", "details", errMsg, "org_id", tenantID)
		return
	}

	m.metrics.discardedCount.WithLabelValues(tenantID).Inc()
}
// 限流拒绝时递增 write_failures_discarded_total，成功输出则递增 logged_total。
