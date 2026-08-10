package querytee

// querytee 包 InstrumentationServer 在独立端口暴露 Prometheus 指标，供 query-tee 代理进程自监控。

import (
	"fmt"
	"net"
	"net/http"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/gorilla/mux"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

// InstrumentationServer 持有 metrics registry 与 HTTP 服务，监听 /metrics。
type InstrumentationServer struct {
	port     int
	registry *prometheus.Registry
	srv      *http.Server
	logger   log.Logger
}

// NewInstrumentationServer 构造指标服务器，端口与 registry 由调用方指定。
// NewInstrumentationServer returns a server exposing Prometheus metrics.
func NewInstrumentationServer(port int, registry *prometheus.Registry, logger log.Logger) *InstrumentationServer {
	return &InstrumentationServer{
		port:     port,
		registry: registry,
		logger:   logger,
	}
}

// Start 先 Listen 端口失败则早退，后台 goroutine Serve 暴露 /metrics。
// Start the instrumentation server.
func (s *InstrumentationServer) Start() error {
	// Setup listener first, so we can fail early if the port is in use.
	listener, err := net.Listen("tcp", fmt.Sprintf(":%d", s.port))
	if err != nil {
		return err
	}

	router := mux.NewRouter()
	router.Handle("/metrics", promhttp.HandlerFor(s.registry, promhttp.HandlerOpts{}))

	s.srv = &http.Server{
		Handler: router,
	}

	go func() {
		if err := s.srv.Serve(listener); err != nil {
			level.Error(s.logger).Log("msg", "metrics server terminated", "err", err)
		}
	}()

	return nil
}

// Stop 关闭 HTTP 服务并清空 srv 指针。
// Stop closes the instrumentation server.
func (s *InstrumentationServer) Stop() {
	if s.srv != nil {
		s.srv.Close()
		s.srv = nil
	}
}
// 与 query-tee 主代理服务端口分离，避免 metrics 与查询流量混用。
