// memalign 提供 64 字节对齐工具，匹配现代 CPU cache line 与 Arrow buffer 推荐。
// Package memalign provides utilities for aligning memory.
package memalign

// Align 将 int 长度向上取整到 64 的倍数，用于 bitmap/buffer 分配 padding。
// Align rounds up n to the next multiple of 64. 64-byte alignment is used to
// match modern CPU cache line sizes.
func Align(n int) int {
	return (n + 63) &^ 63
}

// Align64 is like [Align] but for uint64 values.
// Align64 与 Align 相同逻辑，用于 uintptr 地址计算时的 64 字节对齐。
func Align64(n uint64) uint64 {
	return (n + 63) &^ 63
}
// 对齐公式 (n+63)&^63 通过位运算避免除法，保证分配起始地址落在 cache line 边界。
