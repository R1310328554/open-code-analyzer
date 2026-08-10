package pattern

// ring_client 封装 pattern ingester 的 hash ring 与 gRPC 连接池：供 tee/querier 按地址获取 PatternClient。

import (
	"context"
	"fmt"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/ring"
	ring_client "github.com/grafana/dskit/ring/client"
	"github.com/grafana/dskit/services"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/pattern/clientpool"
)

// RingClient 组合 Service 生命周期、ReadRing 查询与按地址取 PoolClient。
type RingClient interface {
	services.Service
	Ring() ring.ReadRing
	GetClientFor(addr string) (ring_client.PoolClient, error)
}

type ringClient struct {
	cfg    Config
	logger log.Logger

	services.Service
	subservices        *services.Manager
	subservicesWatcher *services.FailureWatcher
	ring               *ring.Ring
	pool               *ring_client.Pool
}

// NewRingClient 创建 pattern-ingester ring、clientpool 并注册 subservices。
func NewRingClient(
	cfg Config,
	metricsNamespace string,
	registerer prometheus.Registerer,
	logger log.Logger,
) (RingClient, error) {
	var err error
	registerer = prometheus.WrapRegistererWithPrefix(metricsNamespace+"_", registerer)
	ringClient := &ringClient{
		logger: log.With(logger, "component", "pattern-ring-client"),
		cfg:    cfg,
	}
	ringClient.ring, err = ring.New(cfg.LifecyclerConfig.RingConfig, "pattern-ingester", "pattern-ring", ringClient.logger, registerer)
	if err != nil {
		return nil, err
	}
	factory := cfg.factory
	if factory == nil {
		factory = ring_client.PoolAddrFunc(func(addr string) (ring_client.PoolClient, error) {
			return clientpool.NewClient(cfg.ClientConfig, addr)
		})
	}
	ringClient.pool = clientpool.NewPool("pattern-ingester", cfg.ClientConfig.PoolConfig, ringClient.ring, factory, logger, metricsNamespace)

	ringClient.subservices, err = services.NewManager(ringClient.pool, ringClient.ring)
	if err != nil {
		return nil, fmt.Errorf("services manager: %w", err)
	}
	ringClient.subservicesWatcher = services.NewFailureWatcher()
	ringClient.subservicesWatcher.WatchManager(ringClient.subservices)
	ringClient.Service = services.NewBasicService(ringClient.starting, ringClient.running, ringClient.stopping)

	return ringClient, nil
}

func (r *ringClient) starting(ctx context.Context) error {
	return services.StartManagerAndAwaitHealthy(ctx, r.subservices)
}

func (r *ringClient) running(ctx context.Context) error {
	select {
	case <-ctx.Done():
		return nil
	case err := <-r.subservicesWatcher.Chan():
		return fmt.Errorf("pattern tee subservices failed: %w", err)
	}
}

func (r *ringClient) stopping(_ error) error {
	return services.StopManagerAndAwaitStopped(context.Background(), r.subservices)
}

func (r *ringClient) Ring() ring.ReadRing {
	return r.ring
}

func (r *ringClient) StartAsync(ctx context.Context) error {
	return r.ring.StartAsync(ctx)
}

func (r *ringClient) AwaitRunning(ctx context.Context) error {
	return r.ring.AwaitRunning(ctx)
}

func (r *ringClient) StopAsync() {
	r.ring.StopAsync()
}

func (r *ringClient) AwaitTerminated(ctx context.Context) error {
	return r.ring.AwaitTerminated(ctx)
}

func (r *ringClient) FailureCase() error {
	return r.ring.FailureCase()
}

func (r *ringClient) State() services.State {
	return r.ring.State()
}

func (r *ringClient) AddListener(listener services.Listener) func() {
	return r.ring.AddListener(listener)
}

// GetClientFor 从连接池返回指定 ingester 地址的 gRPC 客户端。
func (r *ringClient) GetClientFor(addr string) (ring_client.PoolClient, error) {
	return r.pool.GetClientFor(addr)
}
// running 阻塞监听 subservicesWatcher，任一子服务失败则 pattern ring client 退出。
