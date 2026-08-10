package util //nolint:revive

// util 包 merger 提供 Prometheus model.SamplePair 有序归并：双路线性合并与分治 k 路归并，相同时间戳只保留一路样本。

import "github.com/prometheus/common/model"

// MergeSampleSets 要求输入已按 Timestamp 升序，相等时间戳去重保留 a 侧。
// MergeSampleSets merges and dedupes two sets of already sorted sample pairs.
func MergeSampleSets(a, b []model.SamplePair) []model.SamplePair {
	result := make([]model.SamplePair, 0, len(a)+len(b))
	i, j := 0, 0
	for i < len(a) && j < len(b) {
		if a[i].Timestamp < b[j].Timestamp {
			result = append(result, a[i])
			i++
		} else if a[i].Timestamp > b[j].Timestamp {
			result = append(result, b[j])
			j++
		} else {
			result = append(result, a[i])
			i++
			j++
		}
	}
	// Add the rest of a or b. One of them is empty now.
	result = append(result, a[i:]...)
	result = append(result, b[j:]...)
	return result
}

// MergeNSampleSets 递归对半分治，0/1 路边界快速返回，适合 querier 合并分片。
// MergeNSampleSets merges and dedupes n sets of already sorted sample pairs.
func MergeNSampleSets(sampleSets ...[]model.SamplePair) []model.SamplePair {
	l := len(sampleSets)
	switch l {
	case 0:
		return []model.SamplePair{}
	case 1:
		return sampleSets[0]
	}

	n := l / 2
	left := MergeNSampleSets(sampleSets[:n]...)
	right := MergeNSampleSets(sampleSets[n:]...)
	return MergeSampleSets(left, right)
}
// 归并结果预分配 len(a)+len(b) 容量，避免多次 append 触发切片扩容。
