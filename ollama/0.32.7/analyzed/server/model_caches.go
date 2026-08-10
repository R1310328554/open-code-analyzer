// 模型相关内存缓存聚合：推荐、show 与列表三类 cache 的生命周期。
package server

import "context"

// modelCaches 持有 recommendations/show/modelList 三类服务端缓存。
type modelCaches struct {
	recommendations *modelRecommendationsCache
	show            *modelShowCache
	modelList       *modelListCache
}

// newModelCaches 构造并初始化各子 cache。
func newModelCaches() *modelCaches {
	return &modelCaches{
		recommendations: newModelRecommendationsCache(),
		show:            newModelShowCache(),
		modelList:       newModelListCache(),
	}
}

// Start 在后台启动各 cache 的 hydrate/刷新 goroutine。
func (c *modelCaches) Start(ctx context.Context) {
	if c == nil {
		return
	}
	if c.recommendations != nil {
		c.recommendations.Start(ctx)
	}
	if c.show != nil {
		c.show.Start(ctx)
	}
	if c.modelList != nil {
		c.modelList.Start(ctx)
	}
}
