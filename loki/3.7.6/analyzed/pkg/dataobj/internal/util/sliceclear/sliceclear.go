// sliceclear 清零切片元素并截断长度为 0，释放元素引用以便 GC。
// Package sliceclear provides a way to clear and truncate the length of a
// slice.
package sliceclear

// Clear 调用内置 clear 后返回 s[:0]，保留 cap 供后续 append 复用。
// Clear zeroes out all values in s and returns s[:0]. Clear allows memory of
// previous elements in the slice to be reclained by the garbage collector
// while still allowing the underlying slice memory to be reused.
func Clear[Slice ~[]E, E any](s Slice) Slice {
	clear(s)
	return s[:0]
}
// rangeset.Reset 等场景用其清空切片而不丢弃已分配容量。
