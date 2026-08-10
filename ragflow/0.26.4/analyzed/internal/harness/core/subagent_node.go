// subagent_node.go — SubAgentNode：将 Agent 包装为 StateGraph 节点，支持字段映射与自定义提取/收集。

// Package core 提供可复用 SubAgentNode：将 Agent 包装为 StateGraph 节点，
// 支持字段级输入/输出映射与检查点/中断传播。
//
// Usage:
//
//	// Create a graph with a sub-agent as a node
//	sg := graph.NewStateGraph(MyState{})
//	node := NewSubAgentNode(myAgent, WithSubAgentInput("query", "input"))
//	sg.AddNode("sub_agent", node)
//	sg.AddEdge("__start__", "sub_agent")
//	sg.AddEdge("sub_agent", "__end__")
//
// SubAgentNode supports:
//   - Field-level input/output mapping via FieldMapping
//   - Checkpoint/interrupt propagation from the sub-agent
//   - Integration with graph.StatePre/StatePost handlers
package core

import (
	"context"
	"fmt"

	"ragflow/internal/harness/core/schema"
	"ragflow/internal/harness/graph/types"
)

// SubAgentNodeOption SubAgentNode 配置选项函数。
type SubAgentNodeOption func(*SubAgentNodeConfig)

// SubAgentNodeConfig 子智能体节点的完整配置。
type SubAgentNodeConfig struct {
	// InputMapping 图状态字段到智能体输入字段的映射。
	// Format: types.FieldMapping{From: "state_field", To: "agent_input_field"}
	InputMapping []types.FieldMapping
	// OutputMapping 智能体输出到图状态字段的映射。
	// Format: types.FieldMapping{From: "agent_output_field", To: "state_field"}
	OutputMapping []types.FieldMapping
	// InputExtractor 自定义从图状态提取 AgentInput。
	// If nil, the entire state is passed as the input messages.
	InputExtractor func(ctx context.Context, state interface{}) (*AgentInput, error)
	// OutputCollector 自定义将智能体输出写回图状态。
	// If nil, messages from the agent output are appended to state.
	OutputCollector func(ctx context.Context, state interface{}, messages []*schema.Message) (interface{}, error)
	// NodeName 图中该子智能体节点的名称。
	NodeName string
}

// WithSubAgentInput 配置状态字段到智能体输入的映射。
// The 'from' path is in the graph state, 'to' path is in the agent's input.
func WithSubAgentInput(from, to string) SubAgentNodeOption {
	return func(cfg *SubAgentNodeConfig) {
		cfg.InputMapping = append(cfg.InputMapping, types.FieldMapping{From: from, To: to})
	}
}

// WithSubAgentOutput 配置智能体输出到图状态的映射。
// The 'from' path is in the agent's output, 'to' path is in the graph state.
func WithSubAgentOutput(from, to string) SubAgentNodeOption {
	return func(cfg *SubAgentNodeConfig) {
		cfg.OutputMapping = append(cfg.OutputMapping, types.FieldMapping{From: from, To: to})
	}
}

// WithSubAgentExtractor 设置自定义输入提取器。
func WithSubAgentExtractor(fn func(ctx context.Context, state interface{}) (*AgentInput, error)) SubAgentNodeOption {
	return func(cfg *SubAgentNodeConfig) {
		cfg.InputExtractor = fn
	}
}

// WithSubAgentCollector 设置自定义输出收集器。
func WithSubAgentCollector(fn func(ctx context.Context, state interface{}, messages []*schema.Message) (interface{}, error)) SubAgentNodeOption {
	return func(cfg *SubAgentNodeConfig) {
		cfg.OutputCollector = fn
	}
}

// WithSubAgentName 设置子智能体节点名称。
func WithSubAgentName(name string) SubAgentNodeOption {
	return func(cfg *SubAgentNodeConfig) {
		cfg.NodeName = name
	}
}

// NewSubAgentNode 创建 StateGraph 兼容节点函数，
// 流程：提取输入 → 运行智能体 → 合并输出到图状态。
// as a first-class graph node with field-level data projection.
//
// The sub-agent node:
//  1. Extracts input from the graph state (via InputExtractor or FieldMapping)
//  2. Runs the agent
//  3. Merges agent output back into the graph state (via OutputCollector or FieldMapping)
//
// This enables composable, reusable agent nodes in any StateGraph.
func NewSubAgentNode(agent Agent, opts ...SubAgentNodeOption) func(ctx context.Context, state interface{}) (interface{}, error) {
	cfg := &SubAgentNodeConfig{
		NodeName: agent.Name(context.Background()),
	}
	for _, opt := range opts {
		opt(cfg)
	}

	return func(ctx context.Context, state interface{}) (interface{}, error) {
		// Step 1: Extract input from graph state
		input, err := subAgentExtractInput(cfg, ctx, state)
		if err != nil {
			return nil, fmt.Errorf("sub-agent %s: extract input: %w", cfg.NodeName, err)
		}

		// Step 2: Run the agent
		output, err := subAgentRunAgent(ctx, agent, input)
		if err != nil {
			return nil, fmt.Errorf("sub-agent %s: %w", cfg.NodeName, err)
		}

		// Step 3: Collect output back into graph state
		return subAgentCollectOutput(cfg, ctx, state, output)
	}
}

// subAgentExtractInput 按配置从图状态构建 AgentInput。
// extractor or FieldMapping.
func subAgentExtractInput(cfg *SubAgentNodeConfig, ctx context.Context, state interface{}) (*AgentInput, error) {
	// Custom extractor takes precedence
	if cfg.InputExtractor != nil {
		return cfg.InputExtractor(ctx, state)
	}

	st, ok := state.(map[string]interface{})
	if !ok {
		return &AgentInput{}, nil
	}

	// FieldMapping takes precedence over default "Messages" field.
	if len(cfg.InputMapping) > 0 {
		input := &AgentInput{}
		for _, m := range cfg.InputMapping {
			if val, exists := st[m.From]; exists {
				if str, ok := val.(string); ok && str != "" {
					input.Messages = append(input.Messages, schema.UserMessage(str))
				}
			}
		}
		if len(input.Messages) > 0 {
			return input, nil
		}
		// Fall through to default if no mapping values were found.
	}

	// Default: pass state messages as agent input
	input := &AgentInput{}
	if msgs, ok := st["Messages"]; ok {
		if msgList, ok := msgs.([]*schema.Message); ok {
			input.Messages = msgList
		} else if rawList, ok := msgs.([]interface{}); ok {
			for _, raw := range rawList {
				if msg, ok := raw.(*schema.Message); ok {
					input.Messages = append(input.Messages, msg)
				}
			}
		}
	}
	return input, nil
}

// subAgentRunAgent 执行智能体并收集非流式输出消息。
func subAgentRunAgent(ctx context.Context, agent Agent, input *AgentInput) ([]*schema.Message, error) {
	iter := agent.Run(ctx, input)
	var messages []*schema.Message
	for {
		ev, ok := iter.Next()
		if !ok {
			break
		}
		if ev.Err != nil {
			return nil, ev.Err
		}
		if ev.Output != nil && ev.Output.MessageOutput != nil &&
			!ev.Output.MessageOutput.IsStreaming &&
			ev.Output.MessageOutput.Message != nil {
			messages = append(messages, ev.Output.MessageOutput.Message)
		}
	}
	return messages, nil
}

// subAgentCollectOutput 将输出消息合并回图状态（默认追加到 Messages）。
// NOTE: Agent output messages are stored as []interface{} (not []*schema.Message)
// in the state map. Callers reading st["Messages"] back must handle []interface{}
// with type assertions, or use the default extractor which already does this.
func subAgentCollectOutput(cfg *SubAgentNodeConfig, ctx context.Context, state interface{}, messages []*schema.Message) (interface{}, error) {
	// Custom collector takes precedence
	if cfg.OutputCollector != nil {
		return cfg.OutputCollector(ctx, state, messages)
	}

	st, ok := state.(map[string]interface{})
	if !ok {
		return state, nil
	}

	// FieldMapping: project agent output to state fields.
	if len(cfg.OutputMapping) > 0 && len(messages) > 0 {
		// Use the last assistant message content as the output value.
		var lastContent string
		for i := len(messages) - 1; i >= 0; i-- {
			if messages[i].Role == schema.RoleAssistant {
				lastContent = messages[i].Content
				break
			}
		}
		for _, m := range cfg.OutputMapping {
			if lastContent != "" {
				st[m.To] = lastContent
			}
		}
		return st, nil
	}

	// Default: append messages to state
	if len(messages) > 0 {
		existing, _ := st["Messages"].([]interface{})
		for _, msg := range messages {
			existing = append(existing, msg)
		}
		st["Messages"] = existing
	}
	return st, nil
}
