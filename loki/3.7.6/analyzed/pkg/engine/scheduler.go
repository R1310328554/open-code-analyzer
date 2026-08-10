package engine

// Scheduler 是对 internal/scheduler 的公开封装：支持进程内 Local 监听或 HTTP/2 远程传输，向 Worker 分配任务。

import (
	"net"
	"net/http"

	"github.com/go-kit/log"
	"github.com/gorilla/mux"
	"github.com/grafana/dskit/services"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/engine/internal/scheduler"
	"github.com/grafana/loki/v3/pkg/engine/internal/scheduler/wire"
)

// SchedulerParams 配置日志、AdvertiseAddr、Frame 端点路径等调度器启动参数。
type SchedulerParams struct {
	Logger log.Logger // Logger for optional log messages.

	// Address to advertise to workers. Must be set when the scheduler runs in
	// remote transport mode.
	//
	// If nil, the scheduler only listens for in-process connections.
	AdvertiseAddr net.Addr

	// Absolute path of the endpoint where the frame handler is registered.
	// Used for connecting to scheduler and other workers.
	Endpoint string
}

// Scheduler 持有 inner 调度器、endpoint 路径及 wire.Listener/Handler。
// Scheduler is a service that can schedule tasks to connected [Worker]
// instances.
type Scheduler struct {
	// Our public API is a lightweight wrapper around the internal API.

	inner    *scheduler.Scheduler
	endpoint string
	listener wire.Listener
	handler  http.Handler
}

// NewScheduler 根据 AdvertiseAddr 选择 HTTP2Listener 或 LocalScheduler 本地通道。
// NewScheduler creates a new Scheduler. Use [Scheduler.Service] to manage the
// lifecycle of the Scheduler.
func NewScheduler(params SchedulerParams) (*Scheduler, error) {
	if params.Logger == nil {
		params.Logger = log.NewNopLogger()
	}
	if params.Endpoint == "" {
		params.Endpoint = "/api/v2/frame"
	}

	var (
		listener wire.Listener
		handler  http.Handler
	)

	if params.AdvertiseAddr != nil {
		remoteListener := wire.NewHTTP2Listener(
			params.AdvertiseAddr,
			wire.WithHTTP2ListenerLogger(params.Logger),
		)
		listener, handler = remoteListener, remoteListener
	} else {
		listener = &wire.Local{Address: wire.LocalScheduler}
	}

	inner, err := scheduler.New(scheduler.Config{
		Logger:   params.Logger,
		Listener: listener,
	})
	if err != nil {
		return nil, err
	}

	return &Scheduler{
		inner:    inner,
		endpoint: params.Endpoint,
		listener: listener,
		handler:  handler,
	}, nil
}

// RegisterSchedulerServer 在 mux 路由注册 POST frame handler；无远程 handler 时为 no-op。
// RegisterSchedulerServer registers the [wire.Listener] of the inner scheduler
// as http.Handler on the provided router.
//
// RegisterSchedulerServer is a no-op if an advertise address is not provided.
func (s *Scheduler) RegisterSchedulerServer(router *mux.Router) {
	if s.handler == nil {
		return
	}
	router.Path(s.endpoint).Methods("POST").Handler(s.handler)
}

// Service 返回 dskit 生命周期服务，供组件统一启停。
// Service returns the service used to manage the lifecycle of the Scheduler.
func (s *Scheduler) Service() services.Service {
	return s.inner.Service()
}

// RegisterMetrics 将内部调度器指标注册到 Prometheus Registerer。
// RegisterMetrics registers metrics about s to report to reg.
func (s *Scheduler) RegisterMetrics(reg prometheus.Registerer) error {
	return s.inner.RegisterMetrics(reg)
}

// UnregisterMetrics 从采集器移除调度器指标，用于优雅关闭。
// UnregisterMetrics unregisters metrics about s from reg.
func (s *Scheduler) UnregisterMetrics(reg prometheus.Registerer) {
	s.inner.UnregisterMetrics(reg)
}
// 默认 Endpoint 为 /api/v2/frame，与 Worker 侧保持一致。
