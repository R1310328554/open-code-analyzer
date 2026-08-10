// compute 包对 columnar.Datum 执行无状态列式计算，结果通过传入的 memory.Allocator 分配；当前为实验性 API，仅供 dataobj 使用。
// Package compute implements stateless computational operations over columnar
// data.
//
// Compute functions operate on one or more [columnar.Datum] values, which is
// an opaque representation of either an array or a scalar value of a specific
// value kind.
//
// Unless otherwise specified, all compute functions return data allocated with
// the [memory.Allocator] provided to the compute function.
//
// Package compute is EXPERIMENTAL and currently only intended to be used by
// [github.com/grafana/loki/v3/pkg/dataobj].
package compute

import (
	_ "github.com/grafana/loki/v3/pkg/columnar" // blank import for package doc comment
	_ "github.com/grafana/loki/v3/pkg/memory"   // blank import for package doc comment
)
