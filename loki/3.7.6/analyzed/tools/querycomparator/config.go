package main

// querycomparator 公共 Config 承载 bucket、org-id、RFC3339 时间窗与 LogQL：parseTimeConfig 校验 start<end，全局 orgID/logger 供 storage 与 HTTP 复用。

import (
	"fmt"
	"os"
	"time"

	"github.com/go-kit/log"
)

// Config 被 compare/metastore/execute 子命令共享，Limit 控制 query_range 返回条数上限。
// Config holds common configuration for all commands
type Config struct {
	Bucket string
	OrgID  string
	Start  string
	End    string
	Query  string
	Limit  int
}

// ParsedConfig holds parsed time values
type ParsedConfig struct {
	Config
	StartTime time.Time
	EndTime   time.Time
}

// parseTimeConfig parses start and end time strings into time.Time values
// parseTimeConfig 将 Start/End 字符串解析为 time.Time，逆序时间窗返回明确错误。
func parseTimeConfig(cfg *Config) (*ParsedConfig, error) {
	start, err := time.Parse(time.RFC3339, cfg.Start)
	if err != nil {
		return nil, fmt.Errorf("parsing start time: %w", err)
	}
	end, err := time.Parse(time.RFC3339, cfg.End)
	if err != nil {
		return nil, fmt.Errorf("parsing end time: %w", err)
	}
	if start.After(end) {
		return nil, fmt.Errorf("start time must be before end time")
	}
	return &ParsedConfig{
		Config:    *cfg,
		StartTime: start,
		EndTime:   end,
	}, nil
}

// orgID 在子命令 Action 中赋值；indexStoragePrefix 影响 V1 索引路径与 metastore 前缀。
// Global variables for bucket and org ID (used by storage functions)
var (
	orgID              string
	indexStoragePrefix string
	logger             log.Logger
)

func init() {
	logger = log.NewLogfmtLogger(os.Stderr)
	logger = log.With(logger, "ts", log.DefaultTimestampUTC)
	logger = log.With(logger, "caller", log.DefaultCaller)
}
// ParsedConfig 嵌入 Config 并附加 StartTime/EndTime，避免各命令重复解析时间 flag。
