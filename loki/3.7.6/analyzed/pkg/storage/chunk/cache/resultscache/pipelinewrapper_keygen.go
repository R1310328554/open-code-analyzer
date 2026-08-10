package resultscache

// pipelinewrapper_keygen 在缓存键中区分是否禁用 pipeline wrapper：请求头 LokiDisablePipelineWrappers 为 true 时前缀 pipeline-disabled。

import (
	"context"

	"github.com/grafana/loki/v3/pkg/util/httpreq"
)

type PipelineWrapperKeyGenerator struct {
	inner KeyGenerator
}

// NewPipelineWrapperKeygen 包装已有键生成器，Do 路径经 NewResultsCache 自动应用。
func NewPipelineWrapperKeygen(inner KeyGenerator) KeyGenerator {
	return &PipelineWrapperKeyGenerator{inner: inner}
}

func (kg *PipelineWrapperKeyGenerator) GenerateCacheKey(ctx context.Context, userID string, r Request) string {
	innerKey := kg.inner.GenerateCacheKey(ctx, userID, r)

	if httpreq.ExtractHeader(ctx, httpreq.LokiDisablePipelineWrappersHeader) == "true" {
		return "pipeline-disabled:" + innerKey
	}
	return innerKey
}
// GenerateCacheKey 读取 httpreq context header，确保启用与禁用 pipeline wrapper 的相同查询不会错误共享缓存条目。
