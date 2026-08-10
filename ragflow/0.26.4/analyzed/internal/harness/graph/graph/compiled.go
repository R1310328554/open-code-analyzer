// compiled.go — CompiledStateGraph：子图嵌套、命名空间与检查点迁移。

// Package graph 提供支持子图的 CompiledStateGraph 实现。
package graph

import (
	"context"
	"fmt"
	"sync"

	"ragflow/internal/harness/graph/constants"
	"ragflow/internal/harness/graph/types"

	"github.com/google/uuid"
)

// CompiledStateGraph 已编译状态图，支持嵌套子图与检查点命名空间。
// This corresponds to Python's CompiledStateGraph in graph/state.py
type CompiledStateGraph struct {
	*compiledGraph

	// subgraphs 子图名 → 已编译 CompiledStateGraph
	subgraphs map[string]*CompiledStateGraph

	// parent 父图指针，根图为 nil
	parent *CompiledStateGraph

	// namespace 本图检查点命名空间
	namespace string

	// checkpointMap 父子检查点 ID 双向映射
	checkpointMap map[string]string

	mu sync.RWMutex
}

// NewCompiledStateGraph 从 compiledGraph 或自身包装构造 CSG。
func NewCompiledStateGraph(base types.CompiledGraph) *CompiledStateGraph {
	switch v := base.(type) {
	case *compiledGraph:
		return &CompiledStateGraph{
			compiledGraph: v,
			subgraphs:     make(map[string]*CompiledStateGraph),
			parent:        nil,
			namespace:     "",
			checkpointMap: make(map[string]string),
		}
	case *CompiledStateGraph:
		return v
	default:
		panic(fmt.Sprintf("NewCompiledStateGraph requires *compiledGraph or *CompiledStateGraph, got %T", base))
	}
}

// AddSubgraph 编译并注册命名子图，构建层级 namespace。
func (c *CompiledStateGraph) AddSubgraph(name string, subgraph types.StateGraph) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if _, exists := c.subgraphs[name]; exists {
		return fmt.Errorf("subgraph '%s' already exists", name)
	}

	sg, ok := subgraph.(*stateGraph)
	if !ok {
		return fmt.Errorf("subgraph type %T does not support Compile", subgraph)
	}
	compiled, err := sg.Compile()
	if err != nil {
		return fmt.Errorf("failed to compile subgraph '%s': %w", name, err)
	}

	// Wrap in CompiledStateGraph
	subgraphCSG := &CompiledStateGraph{
		compiledGraph: compiled.(*compiledGraph),
		subgraphs:     make(map[string]*CompiledStateGraph),
		parent:        c,
		namespace:     buildSubgraphNamespace(c.namespace, name),
		checkpointMap: make(map[string]string),
	}

	c.subgraphs[name] = subgraphCSG
	return nil
}

// GetSubgraph 按名称获取子图。
func (c *CompiledStateGraph) GetSubgraph(name string) (*CompiledStateGraph, bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	subgraph, exists := c.subgraphs[name]
	return subgraph, exists
}

// GetSubgraphs 返回子图映射副本。
func (c *CompiledStateGraph) GetSubgraphs() map[string]*CompiledStateGraph {
	c.mu.RLock()
	defer c.mu.RUnlock()

	// Return a copy
	result := make(map[string]*CompiledStateGraph, len(c.subgraphs))
	for name, subgraph := range c.subgraphs {
		result[name] = subgraph
	}
	return result
}

// Invoke 注入 checkpoint_ns 后委托底层 compiledGraph.Invoke。
func (c *CompiledStateGraph) Invoke(ctx context.Context, input interface{}, config ...*types.RunnableConfig) (interface{}, error) {
	// Set up namespace in config
	rc := &types.RunnableConfig{}
	if len(config) > 0 && config[0] != nil {
		rc = config[0]
	}

	// Add checkpoint namespace
	if rc.Configurable == nil {
		rc.Configurable = make(map[string]interface{})
	}
	rc.Configurable[constants.ConfigKeyCheckpointNS] = c.namespace

	// Invoke base graph
	return c.compiledGraph.Invoke(ctx, input, rc)
}

// Stream 带命名空间的流式执行入口。
func (c *CompiledStateGraph) Stream(ctx context.Context, input interface{}, mode types.StreamMode, config ...*types.RunnableConfig) (<-chan interface{}, <-chan error) {
	// Set up namespace in config
	rc := &types.RunnableConfig{}
	if len(config) > 0 && config[0] != nil {
		rc = config[0]
	}
	if rc.Configurable == nil {
		rc.Configurable = make(map[string]interface{})
	}
	rc.Configurable[constants.ConfigKeyCheckpointNS] = c.namespace

	// Stream from base graph
	return c.compiledGraph.Stream(ctx, input, mode, rc)
}

// MigrateCheckpoint 在父图与子图间迁移/映射检查点 ID。
func (c *CompiledStateGraph) MigrateCheckpoint(
	ctx context.Context,
	threadID string,
	checkpointID string,
	toSubgraph string,
) (string, error) {
	c.mu.Lock()
	defer c.mu.Unlock()

	if toSubgraph == "" {
		// Migrate to parent
		if c.parent == nil {
			return "", fmt.Errorf("no parent graph to migrate to")
		}

		// Get parent checkpoint ID from map
		parentCheckpointID, exists := c.checkpointMap[checkpointID]
		if !exists {
			return "", fmt.Errorf("no parent checkpoint mapping found for %s", checkpointID)
		}

		return parentCheckpointID, nil
	}

	// Migrate to subgraph
	subgraph, exists := c.subgraphs[toSubgraph]
	if !exists {
		return "", fmt.Errorf("subgraph '%s' not found", toSubgraph)
	}

	// Create new checkpoint ID for subgraph
	newCheckpointID := generateCheckpointID()

	// Store mapping
	subgraph.checkpointMap[newCheckpointID] = checkpointID
	c.checkpointMap[checkpointID] = newCheckpointID

	return newCheckpointID, nil
}

// GetNamespace 返回本图检查点命名空间。
func (c *CompiledStateGraph) GetNamespace() string {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.namespace
}

// GetParent 返回父 CompiledStateGraph。
func (c *CompiledStateGraph) GetParent() *CompiledStateGraph {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.parent
}

// IsRoot 是否为根图（无父图）。
func (c *CompiledStateGraph) IsRoot() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.parent == nil
}

// GetCheckpointMap 返回检查点 ID 映射副本。
func (c *CompiledStateGraph) GetCheckpointMap() map[string]string {
	c.mu.RLock()
	defer c.mu.RUnlock()

	// Return a copy
	result := make(map[string]string, len(c.checkpointMap))
	for k, v := range c.checkpointMap {
		result[k] = v
	}
	return result
}

// buildSubgraphNamespace 用 NSSep 拼接父子命名空间。
func buildSubgraphNamespace(parentNS, subgraphName string) string {
	if parentNS == "" {
		return subgraphName
	}
	return parentNS + constants.NSSep + subgraphName
}

// buildTaskPath 构建任务路径供检查点迁移使用。
func buildTaskPath(namespace, subgraphName string) string {
	if namespace == "" {
		return subgraphName + string(constants.NSEnd)
	}
	return namespace + string(constants.NSSep) + subgraphName + string(constants.NSEnd)
}

// generateCheckpointID 生成带 ckp_ 前缀的 UUID 检查点 ID。
func generateCheckpointID() string {
	return "ckp_" + uuid.New().String()
}

// 对应 Python CompiledStateGraph；子图共享父 checkpointer 但 namespace 隔离。
