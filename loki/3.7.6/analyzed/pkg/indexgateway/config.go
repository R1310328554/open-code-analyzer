package indexgateway

// config 定义 Index Gateway 服务端运行模式与 ring 配置：simple 单实例处理全部租户，ring 模式通过 KV ring 做租户 shuffle sharding。

import (
	"flag"
	"fmt"

	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/util/ring"
)

const (
	NumTokens         = 128
	ReplicationFactor = 3
)

// Mode 枚举 simple（默认）与 ring 两种 Index Gateway 部署模式。
// Mode represents in which mode an Index Gateway instance is running.
//
// Right now, two modes are supported: simple mode (default) and ring mode.
type Mode string

// Set 解析命令行/配置中的 mode 字符串并校验合法性。
// Set implements a flag interface, and is necessary to use the IndexGatewayClientMode as a flag.
func (i Mode) Set(v string) error {
	switch v {
	case string(SimpleMode):
		// nolint:ineffassign
		i = SimpleMode
	case string(RingMode):
		// nolint:ineffassign
		i = RingMode
	default:
		return fmt.Errorf("mode %s not supported. list of supported modes: simple (default), ring", v)
	}
	return nil
}

// String 返回当前 Mode 的字符串表示，供 flag 默认值展示。
// String implements a flag interface, and is necessary to use the IndexGatewayClientMode as a flag.
func (i Mode) String() string {
	switch i {
	case RingMode:
		return string(RingMode)
	default:
		return string(SimpleMode)
	}
}

const (
	// SimpleMode 下单实例负责所有租户的全部索引读写。
// SimpleMode is a mode where an Index Gateway instance solely handle all the work.
	SimpleMode Mode = "simple"

	// RingMode 通过 ring 将租户子集分配给不同网关实例，需 KV 存储维护 ring 状态。
// RingMode is a mode where different Index Gateway instances are assigned to handle different tenants.
	//
	// It is more horizontally scalable than the simple mode, but requires running a key-value store ring.
	RingMode Mode = "ring"
)

// Config 包含 Mode 与 Ring 配置；Ring 可继承公共 ring 段默认值。
// Config configures an Index Gateway server.
type Config struct {
	// Mode configures in which mode the client will be running when querying and communicating with an Index Gateway instance.
	Mode Mode `yaml:"mode"`

	// Ring configures the ring key-value store used to save and retrieve the different Index Gateway instances.
	//
	// In case it isn't explicitly set, it follows the same behavior of the other rings (ex: using the common configuration
	// section and the ingester configuration by default).
	Ring ring.RingConfig `yaml:"ring,omitempty" doc:"description=Defines the ring to be used by the index gateway servers and clients in case the servers are configured to run in 'ring' mode. In case this isn't configured, this block supports inheriting configuration from the common ring section."`
}

// RegisterFlags 注册 mode、ring 前缀 flag；num-tokens 固定为 NumTokens 不可改。
// RegisterFlags register all IndexGatewayClientConfig flags and all the flags of its subconfigs but with a prefix (ex: shipper).
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	f.StringVar((*string)(&cfg.Mode), "index-gateway.mode", SimpleMode.String(), "Defines in which mode the index gateway server will operate (default to 'simple'). It supports two modes:\n- 'simple': an index gateway server instance is responsible for handling, storing and returning requests for all indices for all tenants.\n- 'ring': an index gateway server instance is responsible for a subset of tenants instead of all tenants.")

	// Ring
	skipFlags := []string{
		"index-gateway.ring.num-tokens",
		"index-gateway.ring.replication-factor",
	}
	cfg.Ring.RegisterFlagsWithPrefix("index-gateway.", "collectors/", f, skipFlags...)
	f.IntVar(&cfg.Ring.NumTokens, "index-gateway.ring.num-tokens", NumTokens, fmt.Sprintf("IGNORED: Num tokens is fixed to %d", NumTokens))
	// ReplicationFactor defines how many Index Gateway instances are assigned to each tenant.
	//
	// Whenever the store queries the ring key-value store for the Index Gateway instance responsible for tenant X,
	// multiple Index Gateway instances are expected to be returned as Index Gateway might be busy/locked for specific
	// reasons (this is assured by the spikey behavior of Index Gateway latencies).
	f.IntVar(&cfg.Ring.ReplicationFactor, "replication-factor", ReplicationFactor, "Deprecated: How many index gateway instances are assigned to each tenant. Use -index-gateway.shard-size instead. The shard size is also a per-tenant setting.")
}

func (cfg *Config) Validate() error {
	if cfg.Ring.NumTokens != NumTokens {
		return errors.New("Num tokens must not be changed as it will not take effect")
	}
	return nil
}
// Validate 禁止修改 NumTokens，因 ring 令牌数已在代码中硬编码。
