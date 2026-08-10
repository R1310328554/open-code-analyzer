package util //nolint:revive

// config 辅助函数将配置对象序列化为 YAML 并输出：LogConfig 逆序打日志便于 Grafana 展示，PrintConfig 按自然顺序写入 writer。

import (
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/go-kit/log/level"
	"github.com/prometheus/common/version"
	"gopkg.in/yaml.v2"

	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

// LogConfig 逆序逐行 Info 日志输出配置，最新条目在 Grafana 中更易置顶显示。
// LogConfig takes a pointer to a config object, marshalls it to YAML and prints each line in REVERSE order
// The reverse order makes display in Grafana in easier which typically sorts newest entries at the top.
func LogConfig(cfg interface{}) error {
	lc, err := yaml.Marshal(cfg)
	if err != nil {
		return err
	}

	cfgStr := string(lc)
	cfgStrs := strings.Split(cfgStr, "\n")
	for i := len(cfgStrs) - 1; i >= 0; i-- {
		level.Info(util_log.Logger).Log("type", "config", "msg", cfgStrs[i])
	}
	return nil
}

// PrintConfig 写入带版本注释的 YAML 文档块，供启动时 stdout 或文件导出使用。
// PrintConfig will takes a pointer to a config object, marshalls it to YAML and prints the result to the provided writer
// unlike LogConfig, PrintConfig prints the object in naturally ocurring order.
func PrintConfig(w io.Writer, config interface{}) error {
	lc, err := yaml.Marshal(config)
	if err != nil {
		return err
	}
	fmt.Fprintf(w, "---\n# Loki Config\n# %s\n%s\n\n", version.Info(), string(lc))
	return nil
}

// IngesterQueryOptions 抽象 querier 查询范围选项，避免 queryrange 与 querier 循环依赖。
// IngesterQueryOptions exists because querier.Config cannot be passed directly to the queryrange package
// due to an import cycle.
type IngesterQueryOptions interface {
	QueryStoreOnly() bool
	QueryIngestersWithin() time.Duration
}
// QueryStoreOnly 与 QueryIngestersWithin 由 ingester 侧实现以控制 store/ingester 查询边界。
