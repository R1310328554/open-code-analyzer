package queryrangebase

// queryrangebase 包 step_align 提供 StepAlignMiddleware：将请求 start/end 毫秒时间戳向下对齐到 step 边界以提升 results cache 命中率。

import (
	"context"
	"time"
)

// StepAlignMiddleware 在链中早于 cache/split 执行，使同 step 查询共享缓存键。
// StepAlignMiddleware aligns the start and end of request to the step to
// improved the cacheability of the query results.
var StepAlignMiddleware = MiddlewareFunc(func(next Handler) Handler {
	return stepAlign{
		next: next,
	}
})

// stepAlign 持有 next Handler，Do 内调用 WithStartEnd 传递对齐后的时间窗。
type stepAlign struct {
	next Handler
}

func (s stepAlign) Do(ctx context.Context, r Request) (Response, error) {
	start := (r.GetStart().UnixMilli() / r.GetStep()) * r.GetStep()
	end := (r.GetEnd().UnixMilli() / r.GetStep()) * r.GetStep()
	return s.next.Do(ctx, r.WithStartEnd(time.UnixMilli(start), time.UnixMilli(end)))
}
// 对齐不改变 query 文本，仅调整时间边界，与 querier.align-querier-with-step 配置联动。
