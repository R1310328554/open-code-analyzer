package base

// rulerClientsPool 基于 dskit client.Pool 管理到各 ruler 实例的 gRPC 连接，含健康检查与请求耗时直方图。

import (
	"time"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/grpcclient"
	"github.com/grafana/dskit/middleware"
	"github.com/grafana/dskit/ring/client"
	"github.com/grafana/dskit/services"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/health/grpc_health_v1"
)

// ClientsPool 扩展 services.Service，GetClientFor 返回 RulerClient 接口。
// ClientsPool is the interface used to get the client from the pool for a specified address.
type ClientsPool interface {
	services.Service
	// GetClientFor returns the ruler client for the given address.
	GetClientFor(addr string) (RulerClient, error)
}

// rulerClientsPool 包装 client.Pool，类型断言 PoolClient 为 RulerClient。
type rulerClientsPool struct {
	*client.Pool
}

func (p *rulerClientsPool) GetClientFor(addr string) (RulerClient, error) {
	c, err := p.Pool.GetClientFor(addr)
	if err != nil {
		return nil, err
	}
	return c.(RulerClient), nil
}

// newRulerClientPool 默认每分钟 health check，Gauge 上报 ruler_clients 连接数。
func newRulerClientPool(clientCfg grpcclient.Config, logger log.Logger, reg prometheus.Registerer, metricsNamespace string) ClientsPool {
	// We prefer sane defaults instead of exposing further config options.
	poolCfg := client.PoolConfig{
		CheckInterval:      time.Minute,
		HealthCheckEnabled: true,
		HealthCheckTimeout: 10 * time.Second,
	}

	clientsCount := promauto.With(reg).NewGauge(prometheus.GaugeOpts{
		Namespace: metricsNamespace,
		Name:      "ruler_clients",
		Help:      "The current number of ruler clients in the pool.",
	})

	return &rulerClientsPool{
		client.NewPool("ruler", poolCfg, nil, newRulerClientFactory(clientCfg, reg, metricsNamespace), clientsCount, logger),
	}
}

func newRulerClientFactory(clientCfg grpcclient.Config, reg prometheus.Registerer, metricsNamespace string) client.PoolFactory {
	requestDuration := promauto.With(reg).NewHistogramVec(prometheus.HistogramOpts{
		Namespace: metricsNamespace,
		Name:      "ruler_client_request_duration_seconds",
		Help:      "Time spent executing requests to the ruler.",
		Buckets:   prometheus.ExponentialBuckets(0.008, 4, 7),
	}, []string{"operation", "status_code"})

	return client.PoolAddrFunc(func(addr string) (client.PoolClient, error) {
		return dialRulerClient(clientCfg, addr, requestDuration)
	})
}

func newRulerPoolClient(clientCfg grpcclient.Config, reg prometheus.Registerer, metricsNamespace string) func(addr string) (client.PoolClient, error) {
	requestDuration := promauto.With(reg).NewHistogramVec(prometheus.HistogramOpts{
		Namespace: metricsNamespace,
		Name:      "ruler_client_request_duration_seconds",
		Help:      "Time spent executing requests to the ruler.",
		Buckets:   prometheus.ExponentialBuckets(0.008, 4, 7),
	}, []string{"operation", "status_code"})

	return func(addr string) (client.PoolClient, error) {
		return dialRulerClient(clientCfg, addr, requestDuration)
	}
}

// dialRulerClient 注入 grpcclient Instrument 拦截器后 Dial 并包装 extended client。
func dialRulerClient(clientCfg grpcclient.Config, addr string, requestDuration *prometheus.HistogramVec) (*rulerExtendedClient, error) {
	unaryInterceptors, streamInterceptors := grpcclient.Instrument(requestDuration)
	opts, err := clientCfg.DialOption(unaryInterceptors, streamInterceptors, middleware.NoOpInvalidClusterValidationReporter)
	if err != nil {
		return nil, err
	}

	// nolint:staticcheck // grpc.Dial() has been deprecated; we'll address it before upgrading to gRPC 2.
	conn, err := grpc.Dial(addr, opts...)
	if err != nil {
		return nil, errors.Wrapf(err, "failed to dial ruler %s", addr)
	}

	return &rulerExtendedClient{
		RulerClient:  NewRulerClient(conn),
		HealthClient: grpc_health_v1.NewHealthClient(conn),
		conn:         conn,
	}, nil
}

// rulerExtendedClient 组合 RulerClient 与 grpc_health_v1.HealthClient 供 Pool 使用。
type rulerExtendedClient struct {
	RulerClient
	grpc_health_v1.HealthClient
	conn *grpc.ClientConn
}

func (c *rulerExtendedClient) Close() error {
	return c.conn.Close()
}

func (c *rulerExtendedClient) String() string {
	return c.RemoteAddress()
}

func (c *rulerExtendedClient) RemoteAddress() string {
	return c.conn.Target()
}
// Close 关闭底层 conn；RemoteAddress 返回 conn.Target 供 ring 寻址。
