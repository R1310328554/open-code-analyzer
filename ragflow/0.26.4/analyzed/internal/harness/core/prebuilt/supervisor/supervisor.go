// supervisor.go — Supervisor 路由模式：LLM 分析用户请求并将任务转移给专业子 Agent。
// Package supervisor 提供 harness-go 的 Supervisor 路由模式。
// Supervisor 用 LLM 将请求路由到具备专长的子 Agent。
// 各子 Agent 拥有独立工具与领域能力。
package supervisor

import (
	"context"
	"fmt"
	"strings"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// Config Supervisor 配置。
type Config struct {
	Name        string
	Description string
	Model       core.Model[*schema.Message]
	Agents      []AgentSpec // 可用子 Agent 列表
	OutputKey   string      // 最终答案写入会话的键
}

// AgentSpec 定义 Supervisor 可调度的子 Agent。
type AgentSpec struct {
	Name        string
	Description string
	Agent       core.Agent
}

func DefaultConfig() *Config {
	return &Config{
		Name:        "supervisor",
		Description: "A supervisor agent that routes tasks to specialized sub-agents",
	}
}

// New 创建带 transfer 能力的 Flow Agent。
func New(ctx context.Context, cfg *Config) (core.ResumableAgent, error) {
	if cfg == nil {
		cfg = DefaultConfig()
	}
	if cfg.Model == nil {
		return nil, fmt.Errorf("supervisor requires a Model")
	}

	// 构建子 Agent 描述注入系统提示
	agentDescs := buildAgentDescriptions(cfg.Agents)

	instruction := fmt.Sprintf(systemPrompt, agentDescs)

	// Supervisor 本身为 ReActAgent，主要通过 transfer 委派
	sup := core.NewReActAgent(&core.ReActConfig[*schema.Message]{
		Model:       cfg.Model,
		Instruction: instruction,
	})

	supAgent := sup.WithName(cfg.Name).WithDescription(cfg.Description)

	// 包装子 Agent：仅允许 transfer 回 Supervisor。
	// 防止子 Agent 之间随意跳转。
	wrappedSubs := make([]core.Agent, 0, len(cfg.Agents))
	for _, as := range cfg.Agents {
		wrapped := core.AgentWithDeterministicTransfer(ctx, &core.DeterministicTransferConfig{
			Agent:        as.Agent,
			ToAgentNames: []string{cfg.Name},
		})
		wrappedSubs = append(wrappedSubs, wrapped)
	}

	// TODO：统一 tracing 容器以识别 Supervisor。
	// 当前 NewReActAgent 返回具体类型，暂难扩展 GetType。
	// 后续可抽象接口以支持链路追踪。

	flow, err := core.SetSubAgents(ctx, supAgent, wrappedSubs)
	if err != nil {
		return nil, fmt.Errorf("set sub-agents: %w", err)
	}

	return flow, nil
}

func buildAgentDescriptions(agents []AgentSpec) string {
	if len(agents) == 0 {
		return ""
	}
	var sb strings.Builder
	for _, a := range agents {
		sb.WriteString(fmt.Sprintf("- %s: %s\n", a.Name, a.Description))
	}
	return sb.String()
}

const systemPrompt = `You are a supervisor agent. Your job is to understand the user's request and route it to the most appropriate specialist agent.

Available agents:
%s

Instructions:
1. Analyze the user's request carefully
2. Choose the best agent from the list above
3. Use the transfer_to_agent tool to delegate the task to that agent
4. If no agent is suitable, respond directly with your best attempt to help

You should always try to route to a specialist agent when one matches the request domain.`

// ---- 便捷构造函数 ----

// NewWithRouter 纯路由方式创建 Supervisor。
// LLM 选择最合适的 Agent 并 transfer 过去。
func NewWithRouter(ctx context.Context, model core.Model[*schema.Message], agents []AgentSpec) (core.ResumableAgent, error) {
	return New(ctx, &Config{Model: model, Agents: agents})
}

// 子 Agent 经 AgentWithDeterministicTransfer 限制回传路径，保证控制流可预测。
