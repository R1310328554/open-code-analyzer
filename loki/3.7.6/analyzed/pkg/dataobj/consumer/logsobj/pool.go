package logsobj

// pool 模块实现固定容量的 Builder 对象池：
// 通过 channel 非阻塞 Get 或阻塞 Wait 复用 Builder，Put 归还实例。

import (
	"context"
)

// SizedBuilderPool 用带缓冲 channel 管理固定数量 Builder 实例。
// A SizedBuilderPool implements a fixed-size pool of builders.
type SizedBuilderPool struct {
	builders chan *Builder
}

// NewSizedBuilderPool 预填充 channel，容量等于传入 builders 数量。
// NewSizedBuilderPool returns a new SizedBuilderPool.
func NewSizedBuilderPool(builders []*Builder) *SizedBuilderPool {
	p := &SizedBuilderPool{builders: make(chan *Builder, len(builders))}
	for _, b := range builders {
		p.builders <- b
	}
	return p
}

// Get 非阻塞取 Builder，池空时返回 nil。
// Get returns the next builder in the pool. If there are no builders available,
// because the pool is currently empty, it returns nil instead.
func (p *SizedBuilderPool) Get() *Builder {
	select {
	case res := <-p.builders:
		return res
	default:
		return nil
	}
}

// Wait 阻塞等待可用 Builder 或 context 取消。
// Wait returns the next builder in the pool. If there are no builders available,
// because the pool is currently empty, it blocks until either a builder becomes
// available or the context is canceled.
func (p *SizedBuilderPool) Wait(ctx context.Context) (*Builder, error) {
	select {
	case res := <-p.builders:
		return res, nil
	case <-ctx.Done():
		return nil, ctx.Err()
	}
}

// Put 将 Builder 归还对象池供后续复用。
// Put returns the builder to the pool.
func (p *SizedBuilderPool) Put(b *Builder) {
	p.builders <- b
}
