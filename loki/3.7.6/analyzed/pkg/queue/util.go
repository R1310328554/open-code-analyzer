package queue

// SlicePool 封装 prometheus/util/pool 桶式对象池，为 DequeueMany 批量出队复用 []Request 切片容量。

import "github.com/prometheus/prometheus/util/pool"

// Put 以 buf[0:0] 归还切片，保留底层 array 供下次 Get 复用。
// SlicePool uses a bucket pool and wraps the Get() and Put() functions for
// simpler access.
type SlicePool[T any] struct {
	p *pool.Pool
}

func NewSlicePool[T any](minSize, maxSize int, factor float64) *SlicePool[T] {
	return &SlicePool[T]{
		p: pool.New(minSize, maxSize, factor, func(i int) interface{} {
			return make([]T, 0, i)
		}),
	}
}

func (sp *SlicePool[T]) Get(n int) []T {
	return sp.p.Get(n).([]T)
}

func (sp *SlicePool[T]) Put(buf []T) {
	sp.p.Put(buf[0:0])
}
// RequestQueue.pool 默认桶 [64,128,256,512,1024] 匹配典型批量 dequeue 规模。
