// unsafecast 提供无拷贝的类型重解释工具，供 memory 包在 []byte 与 typed 切片间转换。
// Package unsafecast provides utilties for performing unsafe type casts.
package unsafecast

import "unsafe"

// Sizeof returns the size of T in bytes.
func Sizeof[T any]() uintptr {
	var zero T
	return unsafe.Sizeof(zero)
}

// Slice 按 fromSize/toSize 比例缩放 len/cap，直接复用底层内存不做元素级转换。
// Slice reinterprets a slice of one type as a slice of another type. Slice
// does not perform any type conversion or validation; it simply reinterprets
// the underlying memory.
//
// The length and capacity of the output slice are scaled according to the
// sizes of the From and To types.
func Slice[From, To any](in []From) []To {
	var (
		fromSize = int(Sizeof[From]())
		toSize   = int(Sizeof[To]())

		toLen = len(in) * fromSize / toSize
		toCap = cap(in) * fromSize / toSize
	)

	outPointer := (*To)(unsafe.Pointer(unsafe.SliceData(in)))
	return unsafe.Slice(outPointer, toCap)[:toLen]
}
// 调用方须保证类型大小整除关系与对齐要求，否则 reinterpret 可能产生错误视图或未定义行为。
