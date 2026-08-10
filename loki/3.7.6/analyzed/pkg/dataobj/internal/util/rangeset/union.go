package rangeset

// union 合并两个区间集：拼接后排序再 normalize，得到并集的标准形式。

import (
	"cmp"
	"slices"
)

// Union 若一侧为空则返回另一侧；否则拼接、排序并归并重叠区间。
// Union returns a new set, holding the union of a and b.
func Union(a, b Set) Set {
	var out Set

	// Simple cases: if either set is empty, the union is the other set.
	switch {
	case len(a.ranges) == 0:
		out.ranges = append(out.ranges, b.ranges...)
		return out
	case len(b.ranges) == 0:
		out.ranges = append(out.ranges, a.ranges...)
		return out
	}

	// Do a union by appending a and b, then fixing overlaps.
	out.ranges = slices.Grow(out.ranges, len(a.ranges)+len(b.ranges))
	out.ranges = append(out.ranges, a.ranges...)
	out.ranges = append(out.ranges, b.ranges...)

	slices.SortFunc(out.ranges, func(a, b Range) int {
		return cmp.Compare(a.Start, b.Start)
	})

	out.normalize()
	return out
}
// 与 Intersect 对称，均假设输入 Set 已规范化且区间有序。
