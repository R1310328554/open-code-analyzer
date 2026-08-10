package serverutils

// HTTP/gRPC server 配置合并工具：将用户配置与 dskit server 默认值合并，
// 供 lokipush/syslog 等 push target 构建独立 HTTP 服务时使用。

import (
	"flag"

	"dario.cat/mergo"
	"github.com/grafana/dskit/server"
)

// 先 RegisterFlags 获取默认值，mergo.WithOverride 覆盖；端口 0 保留随机端口语义。
// MergeWithDefaults applies server.Config defaults to a given and different server.Config.
func MergeWithDefaults(config server.Config) (server.Config, error) {
	// Bit of a chicken and egg problem trying to register the defaults and apply overrides from the loaded config.
	// First create an empty config and set defaults.
	mergee := server.Config{}
// 通过空 FlagSet 注册 dskit server 全部默认 flag 值作为 merge 基线。
	mergee.RegisterFlags(flag.NewFlagSet("empty", flag.ContinueOnError))
	// Then apply any config values loaded as overrides to the defaults.
	if err := mergo.Merge(&mergee, config, mergo.WithOverride); err != nil {
		return server.Config{}, err
	}
	// The merge won't overwrite with a zero value but in the case of ports 0 value
	// indicates the desire for a random port so reset these to zero if the incoming config val is 0
// mergo 不会用零值覆盖，显式重置为 0 以启用随机 HTTP 端口分配。
	if config.HTTPListenPort == 0 {
		mergee.HTTPListenPort = 0
	}
	if config.GRPCListenPort == 0 {
		mergee.GRPCListenPort = 0
	}
	return mergee, nil
}
