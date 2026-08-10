package rangeio

// rangeio 包 context 子模块通过私有 contextKey 在 context 中携带 Config，ReadRanges 优先使用注入配置而非 DefaultConfig。

import "context"

type contextKey struct{}

// WithConfig 将 *Config 存入 context，供并行读范围时控制合并与分片参数。
// WithConfig creates a new context that contains the provided [Config]. Calls
// to [ReadRanges] with this context will use the config.
func WithConfig(ctx context.Context, config *Config) context.Context {
	return context.WithValue(ctx, contextKey{}, config)
}

// configFromContext 无配置或类型错误时返回 nil，ReadRanges 回退默认配置。
// configFromContext retrieves the [Config] from the provided context. If the
// context does not contain a config, it returns nil.
func configFromContext(ctx context.Context) *Config {
	config, ok := ctx.Value(contextKey{}).(*Config)
	if !ok {
		return nil
	}
	return config
}
// contextKey 为空 struct，避免与其他包的 WithValue 键发生字符串碰撞。
