package slicegrow

// slicegrow 提供按目标容量扩容的泛型辅助，区别于 slices.Grow 的追加语义。

import "slices"

// GrowToCap 保证 cap 至少为 n，nil 切片时直接 make(0,n)。
// GrowToCap grows the slice to at least n elements total capacity.
// It is an alternative to slices.Grow that increases the capacity of the slice instead of allowing n new appends.
// This is useful when the slice is expected to have nil values.
func GrowToCap[Slice ~[]E, E any](s Slice, n int) Slice {
	if s == nil {
		return make(Slice, 0, n)
	}
	return slices.Grow(s, max(0, n-len(s)))
}

// Copy 扩容 dst 至 len(src) 并复制元素，返回更新后的切片头。
func Copy[Slice ~[]E, E any](dst Slice, src Slice) Slice {
	dst = GrowToCap(dst, len(src))
	dst = dst[:len(src)]
	copy(dst, src)
	return dst
}

func CopyString[Slice ~[]byte](dst Slice, src string) Slice {
	dst = GrowToCap(dst, len(src))
	dst = dst[:len(src)]
	copy(dst, src)
	return dst
}
// 适用于预分配固定槽位、可能含 nil 元素的切片构建场景。
