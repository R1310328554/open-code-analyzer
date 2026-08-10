package clientpool

// clientpool 包 ingester_client_pool 基于 ring 发现维护 pattern ingester gRPC 连接池。

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

// PoolConfig 控制客户端清理周期、是否 health check 及远程超时。
// PoolConfig is config for creating a Pool.
type PoolConfig struct {
	ClientCleanupPeriod  time.Duration `yaml:"client_cleanup_period"`
	HealthCheckIngesters bool          `yaml:"health_check_ingesters"`
	RemoteTimeout        time.Duration `yaml:"remote_timeout"`
}

// RegisterFlags adds the flags required to config this to the given FlagSet.
func (cfg *PoolConfig) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.DurationVar(&cfg.ClientCleanupPeriod, prefix+"client-cleanup-period", 15*time.Second, "How frequently to clean up clients for ingesters that have gone away.")
	f.BoolVar(&cfg.HealthCheckIngesters, prefix+"health-check-ingesters", true, "Run a health check on each ingester client during periodic cleanup.")
	f.DurationVar(&cfg.RemoteTimeout, prefix+"remote-timeout", 1*time.Second, "Timeout for the health check.")
}

// NewPool 用 ring 服务发现创建 dskit Pool，并注册当前活跃 client 数量 Gauge。
func NewPool(name string, cfg PoolConfig, ring ring.ReadRing, factory ring_client.PoolFactory, logger log.Logger, metricsNamespace string) *ring_client.Pool {
	poolCfg := ring_client.PoolConfig{
		CheckInterval:      cfg.ClientCleanupPeriod,
		HealthCheckEnabled: cfg.HealthCheckIngesters,
		HealthCheckTimeout: cfg.RemoteTimeout,
	}

	if clients == nil {
		clients = promauto.NewGauge(prometheus.GaugeOpts{
			Namespace: metricsNamespace,
			Name:      "pattern_ingester_clients",
			Help:      "The current number of pattern ingester clients.",
		})
	}
	// TODO(chaudum): Allow configuration of metric name by the caller.
	return ring_client.NewPool(name, poolCfg, ring_client.NewRingServiceDiscovery(ring), factory, clients, logger)
}
// HealthCheckIngesters 为 true 时在清理周期内对失联 ingester 做 gRPC health 探测。
