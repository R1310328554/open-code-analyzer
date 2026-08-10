package index

// fingerprint 维护 (SeriesRef, Fingerprint) 采样偏移表，供 ShardedPostings 按 fingerprint 分片快速定位 series 偏移范围。

import (
	"math"
	"sort"
)

// FingerprintOffsets 每 1024 条 series 采样一次 fingerprint 与文件偏移。
// (SeriesRef, Fingerprint) tuples
type FingerprintOffsets [][2]uint64

// Range 根据分片 fingerprint 上下界二分查找，返回需扫描的 series 偏移区间。
func (xs FingerprintOffsets) Range(fpFilter FingerprintFilter) (minOffset, maxOffset uint64) {
	from, through := fpFilter.GetFromThrough()
	lower := sort.Search(len(xs), func(i int) bool {
		return xs[i][1] >= uint64(from)
	})

	if lower < len(xs) && lower > 0 {
		// If lower is the first series offset
		// to exist in this shard, we must also check
		// any offsets since the previous sample as well
		minOffset = xs[lower-1][0]
	}

	upper := sort.Search(len(xs), func(i int) bool {
		return xs[i][1] >= uint64(through)
	})

	// If there are no sampled fingerprints greater than this shard,
	// we must check to the end of TSDB series offsets.
	if upper == len(xs) {
		maxOffset = math.MaxUint64
	} else {
		maxOffset = xs[upper][0]
	}

	return
}
// 采样表两端各留缓冲，避免分片边界漏查相邻 series 偏移。
