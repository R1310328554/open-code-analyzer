package main

// Docker Loki 日志驱动插件主入口。
// 在 Unix socket 上启动插件 HTTP 服务，可选开启 pprof 调试端口。

import (
	"fmt"
	"net/http"
	_ "net/http/pprof"
	"os"

	"github.com/docker/go-plugins-helpers/sdk"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	dslog "github.com/grafana/dskit/log"
	"github.com/prometheus/common/version"

	_ "github.com/grafana/loki/v3/pkg/util/build"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

// Docker 插件默认 Unix domain socket 路径。
const socketAddress = "/run/docker/plugins/loki.sock"

var logLevel dslog.Level

// 初始化日志级别、注册 handler 并监听插件 socket。
func main() {
	levelVal := os.Getenv("LOG_LEVEL")
	if levelVal == "" {
		levelVal = "info"
	}

	if err := logLevel.Set(levelVal); err != nil {
		fmt.Fprintln(os.Stdout, "invalid log level: ", levelVal)
		os.Exit(1)
	}
	logger := newLogger(logLevel)
	level.Info(util_log.Logger).Log("msg", "Starting docker-plugin", "version", version.Info())

	h := sdk.NewHandler(`{"Implements": ["LoggingDriver"]}`)

	handlers(&h, newDriver(logger))

	pprofPort := os.Getenv("PPROF_PORT")
	if pprofPort != "" {
		go func() {
			err := http.ListenAndServe(fmt.Sprintf(":%s", pprofPort), nil) //#nosec G114 -- This is a debug feature that must be intentionally enabled and is not used in prod, DOS is not a concern.
			logger.Log("msg", "http server stopped", "err", err)
		}()
	}

	if err := h.ServeUnix(socketAddress, 0); err != nil {
		panic(err)
	}
}

// 构造输出到 stdout 的 logfmt 日志器（Docker 插件要求）。
func newLogger(lvl dslog.Level) log.Logger {
	// plugin logs must be stdout to appear.
	logger := log.NewLogfmtLogger(log.NewSyncWriter(os.Stdout))
	logger = level.NewFilter(logger, lvl.Option)
	logger = log.With(logger, "ts", log.DefaultTimestampUTC)
	logger = log.With(logger, "caller", log.Caller(3))
	return logger
}
