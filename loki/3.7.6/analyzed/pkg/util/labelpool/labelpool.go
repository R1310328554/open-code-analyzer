package labelpool

// labelpool 通过 sync.Pool 复用 labels.ScratchBuilder，减少查询路径上标签构建的堆分配。

import (
	"sync"

	"github.com/prometheus/prometheus/model/labels"
)

var scratchPool = &sync.Pool{
	New: func() any {
		b := labels.NewScratchBuilder(8)
		return &b
	},
}

// Get 从池取 builder 并 Reset 为干净状态，池空时 New 容量为 8 的新实例。
// Get returns a [labels.ScratchBuilder] from the pool, or creates one of if
// the pool is empty. The returned builder is always in a fresh state.
//
// [labels.ScratchBuilder.Overwrite] is only valid to use with pooled builders
// if the all references to the overwritten labels end before the builder is
// returned to the pool.
func Get() *labels.ScratchBuilder {
	b := scratchPool.Get().(*labels.ScratchBuilder)
	b.Reset()
	return b
}

// Put 归还 builder；Overwrite 仅当被覆盖标签引用在归还前全部失效时方可安全使用。
// Put returns a [labels.ScratchBuilder] back to the pool.
func Put(b *labels.ScratchBuilder) { scratchPool.Put(b) }
// scratchPool 全局单例，Get/Put 成对使用可避免 hot path 频繁分配 ScratchBuilder。
