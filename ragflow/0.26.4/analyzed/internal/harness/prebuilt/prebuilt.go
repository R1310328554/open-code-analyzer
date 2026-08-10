// Package prebuilt 提供常见 Agent Harness 模式的预置组件。
//
// 包含 ReAct Agent、ToolNode、ValidationNode、ConditionalNode 与 TransformNode。
package prebuilt

import (
	"context"
	"fmt"

	"ragflow/internal/harness/graph/runnable"
)

// ReactAgentConfig ReAct Agent 配置。
type ReactAgentConfig struct {
	// Tools Agent 可用工具列表
	Tools []Tool
	// Model 使用的 LLM 模型
	Model LLM
	// SystemPrompt 系统提示词
	SystemPrompt string
	// MaxIterations 最大迭代次数
	MaxIterations int
	// StopCondition 停止条件回调
	StopCondition func(*ReActState) bool
}

// ReActState ReAct Agent 运行时状态。
type ReActState struct {
	// Input 用户输入
	Input string
	// Thought 当前思考
	Thought string
	// Action 当前动作
	Action string
	// Observation 动作观察结果
	Observation string
	// Answer 最终答案
	Answer string
	// Iteration 迭代计数
	Iteration int
	// ToolCalls 工具调用历史
	ToolCalls []ToolCall
}

// Tool Agent 可调用的工具定义。
type Tool struct {
	Name        string
	Description string
	Function    func(context.Context, map[string]interface{}) (interface{}, error)
	Schema      map[string]interface{}
}

// ToolCall 单次工具调用记录。
type ToolCall struct {
	ToolName string
	Input    map[string]interface{}
	Output   interface{}
	Error    error
}

// LLM 语言模型接口。
type LLM interface {
	Generate(ctx context.Context, messages []map[string]interface{}) (string, error)
	GenerateStream(ctx context.Context, messages []map[string]interface{}) (<-chan string, error)
}

// NewReactAgent 创建 ReAct（推理+行动）Agent。
func NewReactAgent(config ReactAgentConfig) (runnable.Runnable[map[string]interface{}, map[string]interface{}], error) {
	if len(config.Tools) == 0 {
		return nil, fmt.Errorf("at least one tool is required")
	}
	if config.Model == nil {
		return nil, fmt.Errorf("model is required")
	}
	if config.MaxIterations <= 0 {
		config.MaxIterations = 10
	}

	// 将 Agent 包装为 Runnable
	agent := runnable.NewRunnableFunc(
		func(ctx context.Context, input map[string]interface{}) (map[string]interface{}, error) {
			state := &ReActState{
				Input:     fmt.Sprintf("%v", input["input"]),
				Iteration: 0,
				ToolCalls: make([]ToolCall, 0),
			}

			for state.Iteration < config.MaxIterations {
				// 检查停止条件
				if config.StopCondition != nil && config.StopCondition(state) {
					break
				}

				// 生成思考
				thought, err := config.Model.Generate(ctx, buildMessages(state, config.SystemPrompt))
				if err != nil {
					return nil, fmt.Errorf("failed to generate thought: %w", err)
				}
				state.Thought = thought

				// 从思考中解析动作（简化版）
				action := parseAction(thought)
				state.Action = action

				if action == "ANSWER" {
					// 提取答案
					state.Answer = extractAnswer(thought)
					break
				}

				// 执行工具
				toolOutput, err := executeTool(ctx, action, input, config.Tools)
				state.Observation = fmt.Sprintf("%v", toolOutput)
				state.ToolCalls = append(state.ToolCalls, ToolCall{
					ToolName: action,
					Input:    input,
					Output:   toolOutput,
					Error:    err,
				})

				if err != nil {
					state.Observation = fmt.Sprintf("Tool error: %v", err)
				}

				state.Iteration++
			}

			return map[string]interface{}{
				"output":      state.Answer,
				"thoughts":    state.Thought,
				"iterations":  state.Iteration,
				"tool_calls":  state.ToolCalls,
				"final_state": state,
			}, nil
		},
		runnable.WithName[map[string]interface{}, map[string]interface{}]("react_agent"),
		runnable.WithDescription[map[string]interface{}, map[string]interface{}]("ReAct agent with tools"),
	)

	return agent, nil
}

// ToolNode 创建执行工具的节点。
func ToolNode(tool Tool) runnable.Runnable[map[string]interface{}, map[string]interface{}] {
	return runnable.NewRunnableFunc(
		func(ctx context.Context, input map[string]interface{}) (map[string]interface{}, error) {
			output, err := tool.Function(ctx, input)
			if err != nil {
				return nil, fmt.Errorf("tool %s failed: %w", tool.Name, err)
			}

			return map[string]interface{}{
				"tool":     tool.Name,
				"input":    input,
				"output":   output,
				"success":  true,
				"metadata": map[string]interface{}{"tool_schema": tool.Schema},
			}, nil
		},
		runnable.WithName[map[string]interface{}, map[string]interface{}](fmt.Sprintf("tool_%s", tool.Name)),
		runnable.WithDescription[map[string]interface{}, map[string]interface{}](tool.Description),
	)
}

// ValidationNode 创建校验输入的节点。
func ValidationNode(
	validateFunc func(map[string]interface{}) error,
	errorMessage string,
) runnable.Runnable[map[string]interface{}, map[string]interface{}] {
	return runnable.NewRunnableFunc(
		func(ctx context.Context, input map[string]interface{}) (map[string]interface{}, error) {
			if err := validateFunc(input); err != nil {
				return nil, fmt.Errorf("%s: %w", errorMessage, err)
			}
			// 校验通过后原样透传输入
			return input, nil
		},
		runnable.WithName[map[string]interface{}, map[string]interface{}]("validation_node"),
		runnable.WithDescription[map[string]interface{}, map[string]interface{}]("Input validation node"),
	)
}

// ConditionalNode 创建按条件路由的节点。
func ConditionalNode(
	condition func(map[string]interface{}) string,
	branches map[string]runnable.Runnable[map[string]interface{}, map[string]interface{}],
	defaultBranch string,
) runnable.Runnable[map[string]interface{}, map[string]interface{}] {
	return runnable.NewRunnableFunc(
		func(ctx context.Context, input map[string]interface{}) (map[string]interface{}, error) {
			branchName := condition(input)
			branch, exists := branches[branchName]
			if !exists {
				if defaultBranch == "" {
					return nil, fmt.Errorf("no branch for condition '%s' and no default branch", branchName)
				}
				branch = branches[defaultBranch]
				if branch == nil {
					return nil, fmt.Errorf("default branch '%s' not found", defaultBranch)
				}
			}

			return branch.Invoke(ctx, input)
		},
		runnable.WithName[map[string]interface{}, map[string]interface{}]("conditional_node"),
		runnable.WithDescription[map[string]interface{}, map[string]interface{}]("Conditional routing node"),
	)
}

// TransformNode 创建变换输入的节点。
func TransformNode(
	transformFunc func(map[string]interface{}) (map[string]interface{}, error),
) runnable.Runnable[map[string]interface{}, map[string]interface{}] {
	return runnable.NewRunnableFunc(
		func(ctx context.Context, input map[string]interface{}) (map[string]interface{}, error) {
			return transformFunc(input)
		},
		runnable.WithName[map[string]interface{}, map[string]interface{}]("transform_node"),
		runnable.WithDescription[map[string]interface{}, map[string]interface{}]("Input transformation node"),
	)
}

// 辅助函数

func buildMessages(state *ReActState, systemPrompt string) []map[string]interface{} {
	messages := make([]map[string]interface{}, 0)

	if systemPrompt != "" {
		messages = append(messages, map[string]interface{}{
			"role":    "system",
			"content": systemPrompt,
		})
	}

	messages = append(messages, map[string]interface{}{
		"role":    "user",
		"content": state.Input,
	})

	if state.Thought != "" {
		messages = append(messages, map[string]interface{}{
			"role":    "assistant",
			"content": state.Thought,
		})
	}

	if state.Observation != "" {
		messages = append(messages, map[string]interface{}{
			"role":    "system",
			"content": state.Observation,
		})
	}

	return messages
}

func parseAction(thought string) string {
	// 简化解析——生产环境应使用更完善的解析器
	if len(thought) > 10 && thought[:5] == "THINK" {
		return "THINK"
	}
	if len(thought) > 10 && thought[:6] == "ACTION" {
		// Extract tool name
		return "TOOL_CALL"
	}
	if len(thought) > 10 && thought[:6] == "ANSWER" {
		return "ANSWER"
	}
	return "THINK"
}

func extractAnswer(thought string) string {
	// 简化提取
	return thought
}

func executeTool(ctx context.Context, action string, input map[string]interface{}, tools []Tool) (interface{}, error) {
	// 查找匹配工具
	for _, tool := range tools {
		if tool.Name == action {
			return tool.Function(ctx, input)
		}
	}
	return nil, fmt.Errorf("tool not found: %s", action)
}
