// memory 包提供 Region 与 Allocator 的 arena 复用机制，供 dataobj 等实验性列式路径使用。
// Package memory provides support for allocating and reusing contiguous
// [Region]s of memory.
//
// An [Allocator] supports reclaiming memory regions for reuse, invaliding
// existing regions. Using a memory region after it has been reclaimed produces
// undefined behavior, so caution must be taken to ensure that the lifetime of
// Memory does not exceed the lifetime of the owning Allocator.
//
// Utility packages are provided to make it easier to work with memory regions:
//
//   - [github.com/grafana/loki/v3/pkg/memory/buffer] for resizable typed buffers.
//   - [github.com/grafana/loki/v3/pkg/memory/bitmap] for bitmaps.
//
// Memory is EXPERIMENTAL and is currently only intended for use by
// [github.com/grafana/loki/v3/pkg/dataobj].
package memory

// Region 是 Allocator 持有的原始 []byte 块，Data() 返回可读写切片视图。
// Region is a contiguous region of memory owned by an [Allocator].
type Region struct {
	// TODO(rfratto): Do we need the Memory type at all?

	data []byte // Raw data.
}

// Data returns the raw data of the memory region.
// Data 暴露 Region 底层字节切片；Reclaim 后继续使用该切片属于 use-after-free。
func (m *Region) Data() []byte { return m.data }
// 配套 buffer/bitmap 子包在 Region 之上提供 typed 缓冲与 LSB 位图抽象。
