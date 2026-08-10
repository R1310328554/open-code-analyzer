package helpers

// tsdb helpers Setup：解析 Loki 配置并初始化 TSDB shipper 只读模式，要求环境变量 BUCKET（表号）与 DIR（本地缓存目录）。

import (
	"flag"
	"fmt"
	"os"
	"path/filepath"

	"github.com/grafana/dskit/server"
	"github.com/grafana/dskit/services"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/collectors"
	"github.com/prometheus/client_golang/prometheus/collectors/version"

	"github.com/grafana/loki/v3/pkg/loki"
	"github.com/grafana/loki/v3/pkg/storage"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/util"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper"
	"github.com/grafana/loki/v3/pkg/util/cfg"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
	"github.com/grafana/loki/v3/pkg/validation"
)

// Setup 设置 ActiveIndexDirectory/CacheLocation 并返回 moduleManager HTTP 服务。
func Setup() (loki.Config, services.Service, string, error) {
	var c loki.ConfigWrapper
	if err := cfg.DynamicUnmarshal(&c, os.Args[1:], flag.CommandLine); err != nil {
		fmt.Fprintf(os.Stderr, "failed parsing config: %v\n", err)
		os.Exit(1)
	}

	bucket := os.Getenv("BUCKET")
	dir := os.Getenv("DIR")

	if bucket == "" {
		return c.Config, nil, "", fmt.Errorf("$BUCKET must be specified")
	}

	if dir == "" {
		return c.Config, nil, "", fmt.Errorf("$DIR must be specified")
	}

	if err := util.EnsureDirectory(dir); err != nil {
		return c.Config, nil, "", fmt.Errorf("failed to ensure directory %s: %w", dir, err)
	}

	c.StorageConfig.TSDBShipperConfig.Mode = indexshipper.ModeReadOnly
	util_log.InitLogger(&c.Server, prometheus.DefaultRegisterer, false)

	c.StorageConfig.TSDBShipperConfig.ActiveIndexDirectory = filepath.Join(dir, "tsdb-active")
	c.StorageConfig.TSDBShipperConfig.CacheLocation = filepath.Join(dir, "tsdb-cache")

	svc, err := moduleManager(&c.Server)
	if err != nil {
		return c.Config, nil, "", err
	}

	return c.Config, svc, bucket, nil
}

// moduleManager 注册 version/go runtime 指标并启动 dskit server 包装 Loki 空模块。
func moduleManager(cfg *server.Config) (services.Service, error) {
	prometheus.MustRegister(version.NewCollector("loki"))
	// unregister default go collector
	prometheus.Unregister(collectors.NewGoCollector())
	// register collector with additional metrics
	prometheus.MustRegister(collectors.NewGoCollector(
		collectors.WithGoCollectorRuntimeMetrics(collectors.MetricsAll),
	))

	if cfg.HTTPListenPort == 0 {
		cfg.HTTPListenPort = 8080
	}

	serv, err := server.New(*cfg)
	if err != nil {
		return nil, err
	}

	s := loki.NewServerService(serv, func() []services.Service { return nil })

	return s, nil
}

// DefaultConfigs 为 index-analyzer 等工具提供默认 chunk store 与 limits overrides。
func DefaultConfigs() (config.ChunkStoreConfig, *validation.Overrides, storage.ClientMetrics) {
	var (
		chunkStoreConfig config.ChunkStoreConfig
		limits           validation.Limits
		clientMetrics    storage.ClientMetrics
	)
	chunkStoreConfig.RegisterFlags(flag.NewFlagSet("chunk-store", flag.PanicOnError))
	limits.RegisterFlags(flag.NewFlagSet("limits", flag.PanicOnError))
	overrides, _ := validation.NewOverrides(limits, nil)
	return chunkStoreConfig, overrides, clientMetrics
}

func ExitErr(during string, err error) {
	if err != nil {
		fmt.Fprintf(os.Stderr, "encountered error during %s: %v\n", during, err)
		os.Exit(1)
	}

}
// ExitErr 在 CLI 工具中统一打印 during 阶段错误并以 exit 1 终止进程。
