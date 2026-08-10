// Package canvas 实现 RAGFlow Agent Canvas 的 Go 移植层。
//
// Shared runtime contracts (CanvasState, Component, ComponentFactory,
// state context plumbing, template helpers) live in
// internal/agent/runtime. Canvas re-exports them through thin aliases
// so existing call sites keep working while breaking the historic
// canvas <-> component import cycle.
package canvas

import (
	"ragflow/internal/agent/runtime"
	"ragflow/internal/common"

	"go.uber.org/zap"
)

// legacyNoOpNames DSL v1 兼容的遗留组件名，编译时映射为 no-op。
// recognises for DSL v1 compatibility but does not ship a real
// implementation for. Encountering one of these in a DSL is mapped to
// the same no-op echo lambda used for placeholder bodies by the
// BuildWorkflow in scheduler.go. New DSLs should not use these names —
// they exist only so v1 DSLs that reference Python-era sentinel
// components ("ExitLoop") still compile and run in the Go port.
//
// Membership semantics inside a Loop's sub-graph: legacy names that
// appear as descendants of a Loop are absorbed as no-op members of the
// sub-graph; they do not contribute to loop control. Termination is
// driven by the Loop's loop_termination_condition predicate, not by
// reaching an ExitLoop node.
var legacyNoOpNames = map[string]bool{
	"exitloop": true,
}

// CanvasState aliases runtime.CanvasState so existing canvas callers
// (and component tests that still import the canvas package) keep
// compiling without changes. The canonical definition lives in
// internal/agent/runtime/state.go.
type CanvasState = runtime.CanvasState

// NewCanvasState re-exports runtime.NewCanvasState.
func NewCanvasState(runID, taskID string) *CanvasState {
	return runtime.NewCanvasState(runID, taskID)
}

// Canvas 内存中的 Canvas DSL，由 user_canvas 行解码，供 compile.go 编译。
// It is the input to compile.go which builds the eino Workflow.
type Canvas struct {
	Components map[string]CanvasComponent `json:"components"`
	Path       []string                   `json:"path"`
	History    []map[string]any           `json:"history,omitempty"`
	Retrieval  map[string]any             `json:"retrieval,omitempty"`
	Globals    map[string]any             `json:"globals,omitempty"`
	// NodeParents preserves the front-end graph's grouping metadata
	// (graph.nodes[*].parentId) for runtime-only subgraph expansion.
	// The backend treats the incoming DSL as read-only; this is a
	// decoder-side mirror used only to decide which nodes belong to a
	// Loop / Parallel body during compilation.
	NodeParents map[string]string `json:"-"`
}

// CanvasComponent DSL 节点，Obj.ComponentName 对应 Python 组件类名。
// matches agent/component/<name>.py's class name (case-insensitive,
// per Python v1 DSL semantics).
type CanvasComponent struct {
	Obj        CanvasComponentObj `json:"obj"`
	Downstream []string           `json:"downstream"`
	Upstream   []string           `json:"upstream"`
}

type CanvasComponentObj struct {
	ComponentName string         `json:"component_name"`
	Params        map[string]any `json:"params"`
}

// Close 释放 DSL 中组件持有的资源（MCP 会话、HTTP 连接等）。
// DSL. It walks every component's params map and calls Close() on any
// value that implements a Close() method (MCPToolAdapters, HTTP
// clients, etc.). Mirrors Python's Graph.close() in agent/canvas.py.
//
// In Go's architecture MCP sessions are per-invocation and auto-torn
// down; Close() is a best-effort hook that ensures idle HTTP
// connections are released even when adapters outlive a single call.
func (c *Canvas) Close() {
	if c == nil {
		return
	}
	seen := make(map[any]bool)
	for _, comp := range c.Components {
		for _, v := range comp.Obj.Params {
			walkAndClose(v, seen)
		}
	}
}

// walkAndClose recursively walks a value and calls Close() on any
// objects that implement a Close() method. Maps, slices, and pointers
// are recursed into; other types are skipped. Already-seen objects
// (by interface identity) are skipped to avoid double-close.
func walkAndClose(v any, seen map[any]bool) {
	if v == nil {
		return
	}
	if closer, ok := v.(interface{ Close() }); ok {
		if !seen[closer] {
			seen[closer] = true
			safeClose(closer)
		}
		return
	}
	switch val := v.(type) {
	case map[string]any:
		for _, child := range val {
			walkAndClose(child, seen)
		}
	case []any:
		for _, child := range val {
			walkAndClose(child, seen)
		}
	}
}

// safeClose calls Close() on a closer value, swallowing panics so a
// misbehaving resource doesn't crash the canvas tear-down path.
func safeClose(closer interface{ Close() }) {
	defer func() {
		if rec := recover(); rec != nil {
			common.Warn("canvas: Close() panicked", zap.Any("recover", rec))
		}
	}()
	closer.Close()
}

// Component 别名 runtime.Component，供 BuildWorkflow 构建子图时使用。
// surface BuildWorkflow needs at sub-graph build time. The canonical
// definition (and the SetDefaultFactory / DefaultFactory plumbing)
// lives in internal/agent/runtime/component.go.
type Component = runtime.Component

// ComponentFactory aliases runtime.ComponentFactory.
type ComponentFactory = runtime.ComponentFactory

// SetDefaultFactory re-exports runtime.SetDefaultFactory. The
// orchestrator's main.go can call either entry point; new code
// should prefer the runtime package directly.
func SetDefaultFactory(f ComponentFactory) {
	runtime.SetDefaultFactory(f)
}
