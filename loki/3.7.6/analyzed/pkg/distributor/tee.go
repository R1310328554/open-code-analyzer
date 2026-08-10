package distributor

// Tee 接口允许 distributor 将日志流复制到额外下游端点，支持注册待完成推送计数。

import (
	"context"
)

// Tee 实现可将同一批 KeyedStream 异步复制到镜像或调试等外部 sink。
// Tee implementations can duplicate the log streams to another endpoint.
type Tee interface {
	Duplicate(ctx context.Context, tenant string, streams []KeyedStream, pushTracker *PushTracker)

// Register 预注册待处理流数量，distributor 在 push 完成前等待对应 doneWithResult 回调。
	// Register is a prehook to allow Tee's to register its pending streams, allowing distributors to wait for them before concluding a push request.
	// If pending streams are registered, one should make sure `pushTracker.doneWithResult` is invoked for the same number of streams added.
	Register(ctx context.Context, tenant string, streams []KeyedStream, pushTracker *PushTracker)
}

// WrapTee wraps a new Tee around an existing Tee.
// WrapTee 将新 Tee 链到已有 Tee 上，已有 multiTee 时直接追加成员。
func WrapTee(existing, newTee Tee) Tee {
	if existing == nil {
		return newTee
	}
	if multi, ok := existing.(*multiTee); ok {
		return &multiTee{append(multi.tees, newTee)}
	}
	return &multiTee{tees: []Tee{existing, newTee}}
}

// multiTee 组合多个 Tee，Duplicate/Register 依次委托给每个子 Tee。
type multiTee struct {
	tees []Tee
}

func (m *multiTee) Duplicate(ctx context.Context, tenant string, streams []KeyedStream, pushTracker *PushTracker) {
	for _, tee := range m.tees {
		tee.Duplicate(ctx, tenant, streams, pushTracker)
	}
}

func (m *multiTee) Register(ctx context.Context, tenant string, streams []KeyedStream, pushTracker *PushTracker) {
	for _, tee := range m.tees {
		tee.Register(ctx, tenant, streams, pushTracker)
	}
}
// nil existing 时 WrapTee 直接返回新 Tee，便于可选启用复制链路。
