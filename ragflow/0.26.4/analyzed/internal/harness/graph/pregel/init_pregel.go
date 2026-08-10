// Package pregel 通过 init 注册 Pregel 运行函数，
// 使 types.CompiledGraph.Invoke 走 Pregel 执行引擎。
package pregel

import (
	"context"

	"ragflow/internal/harness/graph/checkpoint"
	"ragflow/internal/harness/graph/types"
)

func init() {
	types.SetPregelRunFunc(runCompiledGraph)
}

// runCompiledGraph 将 CompiledGraph 编译结果桥接到 Pregel Engine.RunSync。
func runCompiledGraph(
	ctx context.Context,
	cg types.CompiledGraph,
	input interface{},
	config *types.RunnableConfig,
	streamMode types.StreamMode,
) (interface{}, error) {
	interruptKeys := make([]string, 0, len(cg.GetInterrupts()))
	for k := range cg.GetInterrupts() {
		interruptKeys = append(interruptKeys, k)
	}
	interruptAfterKeys := make([]string, 0, len(cg.GetInterruptsAfter()))
	for k := range cg.GetInterruptsAfter() {
		interruptAfterKeys = append(interruptAfterKeys, k)
	}

	cp, _ := cg.GetCheckpointer().(checkpoint.BaseCheckpointer)
	engine := NewEngine(cg.GetGraph(),
		WithCheckpointer(cp),
		WithInterrupts(interruptKeys...),
		WithInterruptsAfter(interruptAfterKeys...),
		WithRecursionLimit(cg.GetRecursionLimit()),
		WithDebug(cg.IsDebug()),
		WithConfig(config),
	)
	return engine.RunSync(ctx, input)
}
