package mempool

// mempool 包为 bloom querier 等热路径提供字节切片分配抽象：Allocator 接口统一 Get/Put 语义，在堆分配与 sync.Pool 复用间切换。

import (
	"github.com/prometheus/prometheus/util/pool"
)

// Allocator 管理可变长 []byte 生命周期，Put 返回 false 表示无法回收该缓冲。
// Allocator handles byte slices for bloom queriers.
// It exists to reduce the cost of allocations and allows to re-use already allocated memory.
type Allocator interface {
	Get(size int) ([]byte, error)
	Put([]byte) bool
}

// SimpleHeapAllocator 每次 Get 直接 make，适合测试或无需复用的场景。
// SimpleHeapAllocator allocates a new byte slice every time and does not re-cycle buffers.
type SimpleHeapAllocator struct{}

func (a *SimpleHeapAllocator) Get(size int) ([]byte, error) {
	return make([]byte, size), nil
}

func (a *SimpleHeapAllocator) Put([]byte) bool {
	return true
}

// BytePool uses a sync.Pool to re-cycle already allocated buffers.
// BytePool 基于 Prometheus util/pool 按尺寸分桶缓存 []byte，降低 GC 压力。
type BytePool struct {
	pool *pool.Pool
}

func NewBytePoolAllocator(minSize, maxSize int, factor float64) *BytePool {
	return &BytePool{
		pool: pool.New(
			minSize, maxSize, factor,
			func(size int) interface{} {
				return make([]byte, size)
			}),
	}
}

// BytePool.Get 从对应桶取出缓冲并截断到请求长度，容量仍保留桶上限。
// Get implements Allocator
func (p *BytePool) Get(size int) ([]byte, error) {
	return p.pool.Get(size).([]byte)[:size], nil
}

// Put implements Allocator
func (p *BytePool) Put(b []byte) bool {
	p.pool.Put(b)
	return true
}
// NewBytePoolAllocator 的 factor 控制相邻桶容量倍率，与 Prometheus 池算法一致。
