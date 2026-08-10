// Package canvas — variable reference helpers (re-exports).
//
// The canonical VarRefPattern / ExtractRefs / ResolveTemplate
// implementations live in internal/agent/runtime/template.go so
// components can depend on them without importing canvas. This file
// re-exports the symbols for callers that already use canvas.X.
// variable.go — 变量引用模板辅助函数重导出（runtime/template.go）。

package canvas

import (
	"ragflow/internal/agent/runtime"
)

// VarRefPattern aliases runtime.VarRefPattern.
// VarRefPattern 变量引用正则，别名 runtime.VarRefPattern。
var VarRefPattern = runtime.VarRefPattern

// ExtractRefs re-exports runtime.ExtractRefs.
// ExtractRefs 从字符串提取所有变量引用。
func ExtractRefs(s string) []string {
	return runtime.ExtractRefs(s)
}

// ResolveTemplate re-exports runtime.ResolveTemplate.
// ResolveTemplate 用 CanvasState 替换模板中的变量引用。
func ResolveTemplate(s string, state *CanvasState) (string, error) {
	return runtime.ResolveTemplate(s, state)
}
