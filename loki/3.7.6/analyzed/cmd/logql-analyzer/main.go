package main

// logql-analyzer 独立 HTTP 服务：暴露 /api/logql-analyze 供前端
// 提交 LogQL 进行语法分析，附带 CORS 与 /ready 健康检查端点。

import (
	"flag"
	"net/http"

	"github.com/go-kit/log/level"
	"github.com/gorilla/mux"
	"github.com/grafana/dskit/server"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/logqlanalyzer"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

// 解析 dskit server 配置、初始化日志并启动 LogQL 分析 HTTP 服务。
func main() {
	cfg := getConfig()
	util_log.InitLogger(&server.Config{
		LogLevel: cfg.LogLevel,
	}, prometheus.DefaultRegisterer, false)
	s, err := createServer(cfg)
	if err != nil {
		level.Error(util_log.Logger).Log("msg", "error while creating the server", "err", err)
	}

	err = s.Run()
	defer s.Shutdown()
	if err != nil {
		level.Error(util_log.Logger).Log("msg", "error while running the server", "err", err)
	}
}

// 从标准 flag 包注册并解析 server.Config 监听地址与日志级别等。
func getConfig() server.Config {
	cfg := server.Config{}
	cfg.RegisterFlags(flag.CommandLine)
	flag.Parse()
	return cfg
}

func createServer(cfg server.Config) (*server.Server, error) {
	s, err := server.New(cfg)
	if err != nil {
		return nil, err
	}
	s.HTTP.Use(mux.CORSMethodMiddleware(s.HTTP))
	s.HTTP.Use(logqlanalyzer.CorsMiddleware())
	s.HTTP.Handle("/api/logql-analyze", &logqlanalyzer.LogQLAnalyzeHandler{}).Methods(http.MethodPost, http.MethodOptions)

	s.HTTP.HandleFunc("/ready", func(w http.ResponseWriter, _ *http.Request) {
		http.Error(w, "ready", http.StatusOK)
	}).Methods(http.MethodGet)
	return s, err
}
