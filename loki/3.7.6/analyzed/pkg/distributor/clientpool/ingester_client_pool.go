package clientpool

// ingester gRPC 客户端连接池：基于 ring 发现实例，定期清理失效连接并可选健康检查。

import (
	"flag"
	"time"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/ring"
	ring_client "github.com/grafana/dskit/ring/client"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

var clients prometheus.Gauge

// PoolConfig 定义连接清理周期、健康检查开关与远程探测超时。
// PoolConfig is config for creating a Pool.
type PoolConfig struct {
	ClientCleanupPeriod  time.Duration `yaml:"client_cleanup_period"`
	HealthCheckIngesters bool          `yaml:"health_check_ingesters"`
	RemoteTimeout        time.Duration `yaml:"remote_timeout"`
}

// RegisterFlagsWithPrefix 将 client-cleanup-period 等参数绑定到 FlagSet。
// RegisterFlags adds the flags required to config this to the given FlagSet.
func (cfg *PoolConfig) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.DurationVar(&cfg.ClientCleanupPeriod, prefix+"client-cleanup-period", 15*time.Second, "How frequently to clean up clients for ingesters that have gone away.")
	f.BoolVar(&cfg.HealthCheckIngesters, prefix+"health-check-ingesters", true, "Run a health check on each ingester client during periodic cleanup.")
	f.DurationVar(&cfg.RemoteTimeout, prefix+"remote-timeout", 1*time.Second, "Timeout for the health check.")
}

// NewPool 构造 ring 感知的 ingester 客户端池，并注册 distributor_ingester_clients 指标。
func NewPool(name string, cfg PoolConfig, ring ring.ReadRing, factory ring_client.PoolFactory, logger log.Logger, metricsNamespace string) *ring_client.Pool {
	poolCfg := ring_client.PoolConfig{
		CheckInterval:      cfg.ClientCleanupPeriod,
		HealthCheckEnabled: cfg.HealthCheckIngesters,
		HealthCheckTimeout: cfg.RemoteTimeout,
	}

	if clients == nil {
		clients = promauto.NewGauge(prometheus.GaugeOpts{
			Namespace: metricsNamespace,
			Name:      "distributor_ingester_clients",
			Help:      "The current number of ingester clients.",
		})
	}
	// TODO(chaudum): Allow configuration of metric name by the caller.
	return ring_client.NewPool(name, poolCfg, ring_client.NewRingServiceDiscovery(ring), factory, clients, logger)
}
// 底层使用 dskit ring_client.Pool 实现服务发现与连接生命周期管理。
