// rangeset 维护半开区间集合，用于表示大量有效或无效行号范围。
// Package rangeset implements a half-open interval set [start, end). It is
// intended to be used for working with many large ranges of valid or invalid
// rows.
package rangeset

import (
	"cmp"
	"fmt"
	"iter"
	"math"
	"slices"
	"sort"

	"github.com/grafana/loki/v3/pkg/dataobj/internal/util/sliceclear"
)

// Range 表示半开区间，不变式为 Start 严格小于 End。
// Range is a half-open interval [Start, End).
//
// Invariant: Start < End
type Range struct {
	Start uint64
	End   uint64
}

// String returns r in a human-readable format "[r.Start,r.End)".
func (r Range) String() string { return fmt.Sprintf("[%d,%d)", r.Start, r.End) }

// Len returns the size of r.
func (r Range) Len() uint64 { return r.End - r.Start }

// IsValid returns true if r is valid.
func (r Range) IsValid() bool { return r.Start < r.End }

// Contains returns true if a value is inside r.
func (r Range) Contains(row uint64) bool { return row >= r.Start && row < r.End }

// ContainsRange returns true if all rows in other are inside r.
func (r Range) ContainsRange(other Range) bool {
	return r.Start <= other.Start && r.End >= other.End
}

// Overlaps returns true if r overlaps with other.
func (r Range) Overlaps(other Range) bool {
	return r.Start < other.End && other.Start < r.End
}

// Set 持有有序且不重叠的 Range 切片，零值即可直接使用。
// Set holds a sorted list of non-overlapping [Range]s. The zero value is ready
// for use.
type Set struct{ ranges []Range }

// From 过滤无效区间、排序后 normalize，得到规范化的 Set。
// From creates a new Set from a slice of ranges. Invalid ranges (those of
// length zero or negative length) are ignored.
func From(ranges ...Range) Set {
	s := Set{ranges: make([]Range, 0, len(ranges))}

	for _, r := range ranges {
		if !r.IsValid() {
			continue
		}
		s.ranges = append(s.ranges, r)
	}

	// We need to sort the ranges before normalizing them.
	slices.SortFunc(s.ranges, func(a, b Range) int {
		return cmp.Compare(a.Start, b.Start)
	})

	s.normalize()
	return s
}

// Add 按起点二分插入并扩展重叠区间，最后 normalize 合并相邻段。
// Add inserts r into s. If r overlaps with any existing ranges in s, the
// ranges are merged.
func (s *Set) Add(r Range) {
	if len(s.ranges) == 0 {
		s.ranges = append(s.ranges, r)
		return
	}

	// Find the index to insert r at.
	insertIndex := sort.Search(len(s.ranges), func(i int) bool {
		return s.ranges[i].Start >= r.Start
	})

	switch {
	case insertIndex < len(s.ranges) && s.ranges[insertIndex].Start == r.Start:
		// An existing range starts at the same position; we'll merge it with
		// the existing one and then fix overlaps below.
		s.ranges[insertIndex].End = max(s.ranges[insertIndex].End, r.End)

	case insertIndex > 0 && s.ranges[insertIndex-1].End >= r.Start:
		// The new range overlaps with the previous range. Similar to above, we'll
		// expand the previous range then fix overlaps below.
		s.ranges[insertIndex-1].End = max(s.ranges[insertIndex-1].End, r.End)

	default:
		// Insert the new range; this may still cause overlaps that will get
		// fixed in the loop below.
		s.ranges = slices.Insert(s.ranges, insertIndex, r)
	}

	s.normalize()
}

// normalize 原地合并重叠或相邻区间，压缩 ranges 切片长度。
// normalize merges overlapping and adjacent ranges in-place.
func (s *Set) normalize() {
	out := s.ranges[:0]

	for i := 0; i < len(s.ranges); {
		next := s.ranges[i]
		i++

		// Keep merging for each subsequent overlapping or adjacent range.
		for i < len(s.ranges) && next.End >= s.ranges[i].Start {
			next.End = max(next.End, s.ranges[i].End)
			i++
		}

		out = append(out, next)
	}

	s.ranges = out
}

// RangeFor 二分查找包含 value 的区间，不存在则返回零长 Range。
// RangeFor returns the range containing the value, or an invalid range of length
// zero if no such range exists.
func (s *Set) RangeFor(value uint64) Range {
	i := sort.Search(len(s.ranges), func(i int) bool {
		return s.ranges[i].End > value
	})
	if i < len(s.ranges) && value >= s.ranges[i].Start {
		return s.ranges[i]
	}
	return Range{}
}

// IncludesValue returns true if the set contains the value.
func (s *Set) IncludesValue(value uint64) bool { return s.RangeFor(value).IsValid() }

// IncludesRange returns true if the range r is fully contained inside s.
func (s *Set) IncludesRange(r Range) bool {
	i := sort.Search(len(s.ranges), func(i int) bool {
		return s.ranges[i].End > r.Start
	})
	if i == len(s.ranges) {
		return false
	}
	return s.ranges[i].ContainsRange(r)
}

// Overlaps returns true if r overlaps with s, false if no value in r is found
// in s.
func (s *Set) Overlaps(r Range) bool {
	i := sort.Search(len(s.ranges), func(i int) bool {
		return s.ranges[i].End > r.Start
	})
	if i == len(s.ranges) {
		return false
	}
	return s.ranges[i].Start < r.End
}

// Next 返回严格大于 value 的下一个集合内行号，无后继则 false。
// Next returns the next value in s after the given value. If there are no more
// values in s after value, Next returns [math.MaxUint64] and false.
func (s *Set) Next(value uint64) (uint64, bool) {
	nextValue := value + 1

	i := sort.Search(len(s.ranges), func(i int) bool {
		return s.ranges[i].End > nextValue
	})
	if i == len(s.ranges) {
		return math.MaxUint64, false
	}

	// Clamp the next value to the start of the range.
	return max(nextValue, s.ranges[i].Start), true
}

// Iter returns an iterator over the ranges in s.
func (s *Set) Iter() iter.Seq[Range] {
	return func(yield func(Range) bool) {
		for _, r := range s.ranges {
			if !yield(r) {
				return
			}
		}
	}
}

// Len returns the number of values covered by all ranges in s.
func (s *Set) Len() int {
	var total int
	for _, r := range s.ranges {
		total += int(r.Len())
	}
	return total
}

// Reset 清空区间但保留底层切片容量，便于对象池复用 Set。
// Reset removes all ranges from the set but retains underlying memory.
func (s *Set) Reset() {
	s.ranges = sliceclear.Clear(s.ranges)
}
// IncludesValue、Overlaps 等均基于有序区间二分，适合稀疏大行号集合。
