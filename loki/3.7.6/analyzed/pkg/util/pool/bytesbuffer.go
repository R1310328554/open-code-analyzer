package pool

// pool 包 BufferPool 提供按尺寸分桶的 bytes.Buffer 对象池：minSize 至 maxSize 按 factor 几何递增，Get 时 Reset 后复用底层 cap。

import (
	"bytes"
	"sync"
)

// BufferPool 每桶独立 sync.Pool，Put 时按 Cap 归入首个足够大的桶。
// BufferPool is a bucketed pool for variably bytes buffers.
type BufferPool struct {
	buckets []sync.Pool
	sizes   []int
}

// NewBuffer 参数非法时 panic；sizes 切片记录各桶容量上界供 Get/Put 匹配。
// NewBuffer a new Pool with size buckets for minSize to maxSize
// increasing by the given factor.
func NewBuffer(minSize, maxSize int, factor float64) *BufferPool {
	if minSize < 1 {
		panic("invalid minimum pool size")
	}
	if maxSize < 1 {
		panic("invalid maximum pool size")
	}
	if factor < 1 {
		panic("invalid factor")
	}

	var sizes []int

	for s := minSize; s <= maxSize; s = int(float64(s) * factor) {
		sizes = append(sizes, s)
	}

	return &BufferPool{
		buckets: make([]sync.Pool, len(sizes)),
		sizes:   sizes,
	}
}

// Get 找不到合适桶时直接分配 cap=sz 的新 Buffer，超大缓冲不入池。
// Get returns a byte buffer that fits the given size.
func (p *BufferPool) Get(sz int) *bytes.Buffer {
	for i, bktSize := range p.sizes {
		if sz > bktSize {
			continue
		}
		b := p.buckets[i].Get()
		if b == nil {
			b = bytes.NewBuffer(make([]byte, 0, bktSize))
		}
		buf := b.(*bytes.Buffer)
		buf.Reset()
		return b.(*bytes.Buffer)
	}
	return bytes.NewBuffer(make([]byte, 0, sz))
}

// Put adds a byte buffer to the right bucket in the pool.
func (p *BufferPool) Put(s *bytes.Buffer) {
	if s == nil {
		return
	}
	capt := s.Cap()
	for i, size := range p.sizes {
		if capt > size {
			continue
		}
		p.buckets[i].Put(s)
		return
	}
}
// Put 对 nil 缓冲 no-op；超过 maxSize 的 Buffer 丢弃不复用，防止池无限膨胀。
