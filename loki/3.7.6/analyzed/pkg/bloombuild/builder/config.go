package builder

// Bloom Builder 配置与租户 Limits 接口：Planner gRPC 地址、退避、临时工作目录，
// Limits 提供 block 编码、大小上限、响应超时及 prefetch 开关等 per-tenant 限制。

import (
	"flag"
	"fmt"
	"time"

	"github.com/grafana/dskit/backoff"
	"github.com/grafana/dskit/grpcclient"
)

// Config 嵌入 grpcclient.Config 与 backoff.Config，WorkingDir 为空则用系统 temp。
// Config configures the bloom-builder component.
type Config struct {
	GrpcConfig     grpcclient.Config `yaml:"grpc_config"`
	PlannerAddress string            `yaml:"planner_address"`
	BackoffConfig  backoff.Config    `yaml:"backoff_config"`
	WorkingDir     string            `yaml:"working_directory" doc:"hidden"`
}

// RegisterFlagsWithPrefix 注册 planner-address、grpc 与 working-directory 等 flag。
// RegisterFlagsWithPrefix registers flags for the bloom-planner configuration.
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.StringVar(&cfg.PlannerAddress, prefix+".planner-address", "", "Hostname (and port) of the bloom planner")
	cfg.GrpcConfig.RegisterFlagsWithPrefix(prefix+".grpc", f)
	cfg.BackoffConfig.RegisterFlagsWithPrefix(prefix+".backoff", f)
	f.StringVar(&cfg.WorkingDir, prefix+".working-directory", "", "Working directory to which blocks are temporarily written to. Empty string defaults to the operating system's temp directory.")
}

// Validate 要求 PlannerAddress 非空且 grpc 配置合法，启动前由模块调用。
func (cfg *Config) Validate() error {
	if cfg.PlannerAddress == "" {
		return fmt.Errorf("planner address is required")
	}

	if err := cfg.GrpcConfig.Validate(); err != nil {
		return fmt.Errorf("grpc config is invalid: %w", err)
	}

	return nil
}

// Limits 抽象 runtime 租户限制，由 limits 包实现并注入 Builder 与 Generator。
type Limits interface {
	BloomBlockEncoding(tenantID string) string
	BloomMaxBlockSize(tenantID string) int
	BloomMaxBloomSize(tenantID string) int
	BuilderResponseTimeout(tenantID string) time.Duration
	PrefetchBloomBlocks(tenantID string) bool
}
