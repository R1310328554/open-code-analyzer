// bufpool 按指数分桶复用 bytes.Buffer，避免大缓冲归还后永久占用内存。
// Package bufpool offers a pool of [*bytes.Buffer] objects that are placed
// into exponentially sized buckets.
//
// Bucketing prevents the memory cost of a pool from permanently increasing
// when a large buffer is placed into the pool.
package bufpool

import (
	"bytes"
)

// Get 从对应容量桶取出缓冲并重置，保证 Cap 至少为 size。
// Get returns a buffer from the pool for the given size. Returned buffers are
// reset and ready for writes.
//
// The capacity of the returned buffer is guaranteed to be at least size.
func Get(size int) *bytes.Buffer {
	if size < 0 {
		size = 0
	}

	b := findBucket(uint64(size))

	buf := b.pool.Get().(*bytes.Buffer)
	buf.Reset()
	buf.Grow(size)
	return buf
}

// Put 按当前 Cap 选桶归还缓冲；nil 或超大缓冲会被忽略。
// Put returns a buffer to the pool. The buffer is placed into an appropriate
// bucket based on its current capacity.
func Put(buf *bytes.Buffer) {
	if buf == nil {
		return
	}

	b := findBucket(uint64(buf.Cap()))
	if b == nil {
		return
	}
	b.pool.Put(buf)
}

// GetUnsized 从无分桶池取缓冲，适合容量不可预测的场景。
// GetUnsized returns a buffer from the unsized pool. Returned buffers are
// reset and ready for writes.
//
// Buffers retrieved by GetUnsized should be returned to the pool with
// [PutUnsized].
func GetUnsized() *bytes.Buffer {
	buf := unsizedPool.Get().(*bytes.Buffer)
	buf.Reset()
	return buf
}

// PutUnsized 仅用于归还 GetUnsized 取得的缓冲，勿与 Put 混用。
// PutUnsized returns a buffer to the unsized pool. PutUnsized should only be
// used for buffers retrieved by [GetUnsized].
func PutUnsized(buf *bytes.Buffer) {
	if buf == nil {
		return
	}

	unsizedPool.Put(buf)
}
// 分桶与无尺寸池并存，兼顾可预测大小与通用复用两种用法。
