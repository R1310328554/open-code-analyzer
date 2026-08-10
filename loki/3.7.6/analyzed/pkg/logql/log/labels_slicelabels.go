//go:build slicelabels

package log

// labels_slicelabels 在 slicelabels 构建标签下提供标签哈希器，复用缓冲区避免 Hash 时额外分配。

import "github.com/prometheus/prometheus/model/labels"

type hasher struct {
	buf []byte // buffer for computing hash without bytes slice allocation.
}

// newHasher 预分配 1024 字节容量，在 LabelsBuilder 生命周期内反复用于 resultCache 键。
// newHasher returns a hasher that computes hashes for labels by reusing the same buffer.
func newHasher() *hasher {
	return &hasher{
		buf: make([]byte, 0, 1024),
	}
}

// Hash 调用 HashWithoutLabels 并返回 uint64；跨进程或版本不保证稳定，仅用于进程内缓存。
// Hash computes a hash of lbs.
// It is not guaranteed to be stable across different Loki processes or versions.
func (h *hasher) Hash(lbs labels.Labels) uint64 {
	var hash uint64
	hash, h.buf = lbs.HashWithoutLabels(h.buf, []string(nil)...)
	return hash
}
// 本文件由 //go:build slicelabels 约束，与默认 labels 构建路径下的 hasher 实现互斥编译。
