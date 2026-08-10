// reduction.go — 工具输出缩减中间件：BeforeModelRewrite 分两阶段截断过长输出并清理陈旧 tool 调用。

// Package reduction 提供工具输出缩减中间件。
// 两阶段设计：即时截断 → 模型重写前清理。
package reduction

import (
	"context"
	"sync"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// Backend 持久化溢出内容的后端接口。
type Backend interface {
	Store(key string, content string) error
	Load(key string) (string, error)
}

// ToolConfig 单工具缩减策略。
type ToolConfig struct {
	SkipTruncation bool
	SkipClear      bool
}

// TypedConfig 配置缩减中间件。
type TypedConfig[M core.MessageType] struct {
	Backend           Backend
	MaxToolOutputLen  int // 超出此长度截断 tool 输出（0 表示不截断）
	MaxToolCalls      int // 超出此数量清理 tool 调用（0 表示不清理）
	MaxTokensForClear int // 总 token 超过此值触发清理
	ClearAtLeast      int // 每次清理至少释放的 token 数
	ToolConfigs       map[string]*ToolConfig
	ExcludeTools      map[string]bool
}

type Config = TypedConfig[*schema.Message]

type middleware[M core.MessageType] struct {
	core.BaseMiddleware[M]
	cfg        *TypedConfig[M]
	mu         sync.Mutex
	keyCounter int
}

// NewTyped 创建泛型 reduction 中间件并填充默认阈值。
func NewTyped[M core.MessageType](cfg *TypedConfig[M]) core.TypedReActMiddleware[M] {
	if cfg == nil {
		cfg = &TypedConfig[M]{}
	}
	if cfg.MaxToolOutputLen <= 0 {
		cfg.MaxToolOutputLen = 2000
	}
	if cfg.MaxToolCalls <= 0 {
		cfg.MaxToolCalls = 20
	}
	if cfg.MaxTokensForClear <= 0 {
		cfg.MaxTokensForClear = 100000
	}
	return &middleware[M]{cfg: cfg}
}

func New(cfg *Config) core.TypedReActMiddleware[*schema.Message] {
	return NewTyped[*schema.Message](cfg)
}

// ---- 清理阶段（BeforeModelRewrite）----

// BeforeModelRewrite 截断输出并按 token 预算清理旧 tool 消息。
func (mw *middleware[M]) BeforeModelRewrite(ctx context.Context, state *core.TypedReActAgentState[M], mc *core.TypedModelContext[M]) (context.Context, *core.TypedReActAgentState[M], error) {
	// 阶段 1：截断过长 tool 输出
	mw.truncateToolOutputs(state)

	// 阶段 2：token 超阈值时清理旧 tool 调用
	if mw.cfg.MaxTokensForClear > 0 {
		totalTokens := mw.estimateTokens(state.Messages)
		if totalTokens > mw.cfg.MaxTokensForClear {
			mw.clearOldToolCalls(state, totalTokens)
		}
	}

	return ctx, state, nil
}

// truncateToolOutputs 遍历 tool 消息，超长度或超数量则截断/占位。
func (mw *middleware[M]) truncateToolOutputs(state *core.TypedReActAgentState[M]) {
	toolCount := 0
	for i, msg := range state.Messages {
		m, ok := any(msg).(*schema.Message)
		if !ok || m == nil || m.Role != schema.RoleTool {
			continue
		}
		toolCount++
		if mw.cfg.MaxToolCalls > 0 && toolCount > mw.cfg.MaxToolCalls {
			m.Content = "..."
			m.Extra = nil
			state.Messages[i] = any(m).(M)
			continue
		}
		if mw.cfg.MaxToolOutputLen > 0 && len(m.Content) > mw.cfg.MaxToolOutputLen {
			if !mw.isExcluded(m.ToolName) {
				m.Content = m.Content[:mw.cfg.MaxToolOutputLen] + "\n...(truncated)"
				state.Messages[i] = any(m).(M)
			}
		}
	}
}

// clearOldToolCalls 从最早 tool 消息起释放 token 直至达标。
func (mw *middleware[M]) clearOldToolCalls(state *core.TypedReActAgentState[M], totalTokens int) {
	if mw.cfg.ClearAtLeast <= 0 {
		return
	}
	targetTokens := mw.cfg.MaxTokensForClear - mw.cfg.ClearAtLeast
	if totalTokens <= targetTokens {
		return
	}

	freed := 0
	toolCount := 0
	for i, msg := range state.Messages {
		m, ok := any(msg).(*schema.Message)
		if !ok || m == nil || m.Role != schema.RoleTool {
			continue
		}
		toolCount++
		if mw.cfg.MaxToolCalls > 0 && toolCount > mw.cfg.MaxToolCalls {
			before := len([]rune(m.Content))
			m.Content = "..."
			freed += before - 3
			state.Messages[i] = any(m).(M)
			if totalTokens-freed <= targetTokens {
				break
			}
		}
	}
}

// estimateTokens 启发式估算消息列表 token 数。
func (mw *middleware[M]) estimateTokens(msgs []M) int {
	total := 0
	for _, msg := range msgs {
		switch v := any(msg).(type) {
		case *schema.Message:
			total += len([]rune(v.Content)) * 4 / 3
			for _, tc := range v.ToolCalls {
				total += len([]rune(tc.Function.Arguments)) * 4 / 3
			}
		case *schema.AgenticMessage:
			total += len([]rune(v.Content)) * 4 / 3
		}
	}
	return total
}

func (mw *middleware[M]) isExcluded(name string) bool {
	if mw.cfg.ExcludeTools == nil {
		return false
	}
	return mw.cfg.ExcludeTools[name]
}

func (mw *middleware[M]) nextKey() int {
	mw.mu.Lock()
	defer mw.mu.Unlock()
	mw.keyCounter++
	return mw.keyCounter
}

func truncateText(s string, maxLen int) string {
	if len(s) <= maxLen {
		return s
	}
	// 按 rune 边界截断；[:maxLen] 按字节索引可能切断 UTF-8 多字节字符，
	// 与 TruncateToolResult 的防护逻辑一致。
	runes := []rune(s)
	if maxLen > len(runes) {
		return s
	}
	return string(runes[:maxLen])
}

// ExcludeTools 中的工具跳过截断；Backend 可用于将溢出内容外存。
