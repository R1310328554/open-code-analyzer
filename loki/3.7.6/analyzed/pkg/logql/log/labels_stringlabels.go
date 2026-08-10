//go:build !slicelabels && !dedupelabels

package log

// labels_stringlabels 在默认 Prometheus labels 实现下提供流标签哈希器，供 log pipeline 按标签组合缓存 per-stream 提取器。

import "github.com/prometheus/prometheus/model/labels"

type hasher struct{}

// newHasher 创建用于 BaseLabelsBuilder 的标签哈希实例。
// newHasher returns a hasher that computes hashes for labels.
func newHasher() *hasher {
	return &hasher{}
}

// Hash 对 labels.Labels 计算 uint64 哈希；跨进程/版本不保证稳定。
// Hash computes a hash of lbs.
// It is not guaranteed to be stable across different Loki processes or versions.
func (h *hasher) Hash(lbs labels.Labels) uint64 {
	// We use Hash() here because there's no performance advantage to using HashWithoutLabels() with stringlabels.
	// The results from Hash(l) and HashWithoutLabels(l, []string{}) are different with stringlabels, so using Hash
	// here also simplifies our tests.
	return labels.StableHash(lbs)
}
// 此处选用 Hash 而非 HashWithoutLabels，以便与 stringlabels 变体测试对齐。
