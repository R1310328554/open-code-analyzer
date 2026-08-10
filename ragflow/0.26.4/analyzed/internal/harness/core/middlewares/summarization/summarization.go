// summarization.go — 会话摘要中间件：超 token/消息阈值时压缩历史，可选 supersede 与优先级摘要压缩。

// Package summarization 在超阈值时自动摘要对话历史。
// 使用 Compactor 安全切分、Supersede 移除陈旧文件操作。
// Uses Compactor for safe message splitting, Supersede for removing stale
// file operations, and priority-based summary compression.
package summarization

import (
	"context"
	"fmt"
	"strings"
	"time"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// TriggerCondition 定义摘要触发条件（token 或消息数）。
type TriggerCondition struct {
	MaxTokens   int // Trigger when estimated tokens exceed this
	MaxMessages int // Trigger when message count exceeds this (0 = no limit)
}

// TypedConfig 配置摘要中间件与 Compactor/压缩预算。
type TypedConfig[M core.MessageType] struct {
	Model              core.Model[M]
	Trigger            *TriggerCondition
	TokenCounter       func(ctx context.Context, msgs []M) (int, error)
	GenModelInput      func(ctx context.Context, instruction string, msgs []M) ([]M, error)
	Finalize           func(ctx context.Context, original, summary []M) ([]M, error)
	Callback           func(ctx context.Context, before, after core.TypedReActAgentState[M]) error
	RetryConfig        *core.TypedModelRetryConfig[M]
	EmitInternalEvents bool
	MaxRetries         int
	MaxTokens          int
	SummaryLang        string
	// EnableSupersede 启用陈旧文件读操作的 supersede 移除。
	EnableSupersede bool
	// EnableCompression 启用基于优先级的摘要行压缩。
	EnableCompression bool
	// CompactionCfg configures the Compactor (token threshold, preserve count).
	CompactionCfg *CompactionConfig
	// SummaryBudget configures summary compression budget.
	SummaryBudget *SummaryBudget
}

type Config = TypedConfig[*schema.Message]

type middleware[M core.MessageType] struct {
	core.BaseMiddleware[M]
	cfg       *TypedConfig[M]
	compactor *Compactor
}

// NewTyped 创建摘要中间件并填充默认 Trigger 与 Compactor 配置。
func NewTyped[M core.MessageType](cfg *TypedConfig[M]) core.TypedReActMiddleware[M] {
	if cfg == nil {
		cfg = &TypedConfig[M]{MaxTokens: 160000}
	}
	if cfg.Trigger == nil {
		cfg.Trigger = &TriggerCondition{MaxTokens: 160000}
	}
	if cfg.MaxTokens <= 0 {
		cfg.MaxTokens = 160000
	}
	if cfg.Trigger.MaxTokens <= 0 {
		cfg.Trigger.MaxTokens = cfg.MaxTokens
	}
	if cfg.TokenCounter == nil {
		cfg.TokenCounter = defaultTokenCounter[M]
	}
	if cfg.CompactionCfg == nil {
		cfg.CompactionCfg = &CompactionConfig{
			TriggerTokens:  100000,
			PreserveRecent: 4,
		}
	}
	return &middleware[M]{cfg: cfg, compactor: &Compactor{}}
}

func New(cfg *Config) core.TypedReActMiddleware[*schema.Message] {
	return NewTyped[*schema.Message](cfg)
}

// BeforeModelRewrite 执行 supersede → 阈值判断 → 压缩 → 可选压缩摘要。
func (m *middleware[M]) BeforeModelRewrite(ctx context.Context, state *core.TypedReActAgentState[M], mc *core.TypedModelContext[M]) (context.Context, *core.TypedReActAgentState[M], error) {
	// 阶段 1：Supersede — 阈值检查前移除 superseded 文件操作。
	if m.cfg.EnableSupersede {
		msgs := typedToSchemaMessages(state.Messages)
		filtered := ApplySupersede(msgs)
		state.Messages = schemaMessagesToTyped[M](filtered)
	}

	// 阶段 2：判断是否需要压缩。
	if !m.shouldCompact(ctx, state) {
		return ctx, state, nil
	}

	// Fire before event if enabled
	if m.cfg.EmitInternalEvents {
		ev := &core.TypedAgentEvent[M]{
			Output: &core.TypedAgentOutput[M]{},
		}
		_ = core.TypedSendEvent(ctx, ev)
	}

	// 阶段 3：调用 Compactor 生成摘要消息。
	msgs := typedToSchemaMessages(state.Messages)
	compactCfg := m.cfg.CompactionCfg
	summarizer := func(ctx context.Context, msgs []*schema.Message) (string, error) {
		return m.generateSummary(ctx, schemaMessagesToTyped[M](msgs))
	}

	compacted, err := m.compactor.Compact(msgs, compactCfg, summarizer)
	if err != nil {
		return ctx, state, nil
	}

	// 阶段 4：可选对 <summary> 内容做优先级压缩。
	if m.cfg.EnableCompression && len(compacted) > 0 && compacted[0].Role == schema.RoleSystem &&
		strings.Contains(compacted[0].Content, "<summary>") {
		compacted[0].Content = compressSummaryContent(compacted[0].Content, m.cfg.SummaryBudget)
	}

	// Apply finalizer
	typedCompacted := schemaMessagesToTyped[M](compacted)
	if m.cfg.Finalize != nil {
		var err error
		typedCompacted, err = m.cfg.Finalize(ctx, state.Messages, typedCompacted)
		if err != nil {
			return ctx, state, nil
		}
	}

	// Callback
	if m.cfg.Callback != nil {
		before := *state
		state.Messages = typedCompacted
		_ = m.cfg.Callback(ctx, before, *state)
		return ctx, state, nil
	}

	state.Messages = typedCompacted
	return ctx, state, nil
}

func (m *middleware[M]) shouldCompact(ctx context.Context, state *core.TypedReActAgentState[M]) bool {
	if m.cfg.Trigger.MaxMessages > 0 && len(state.Messages) > m.cfg.Trigger.MaxMessages {
		return true
	}
	if m.cfg.TokenCounter != nil && len(state.Messages) > 0 {
		tokens, err := m.cfg.TokenCounter(ctx, state.Messages)
		if err == nil && tokens > m.cfg.Trigger.MaxTokens {
			return true
		}
	}
	return false
}

// generateSummary 调用 Model 生成摘要，含重试与内部事件。
func (m *middleware[M]) generateSummary(ctx context.Context, msgs []M) (string, error) {
	if m.cfg.Model == nil {
		return fmt.Sprintf("(%d messages)", len(msgs)), nil
	}

	instruction := getSummaryInstruction(m.cfg.SummaryLang)
	var promptMsgs []M
	if m.cfg.GenModelInput != nil {
		var err error
		promptMsgs, err = m.cfg.GenModelInput(ctx, instruction, msgs)
		if err != nil {
			return "", err
		}
	} else {
		var builder strings.Builder
		builder.WriteString(instruction)
		builder.WriteString("\n\nConversation:\n")
		for i, msg := range msgs {
			text := extractText(msg)
			if text != "" {
				builder.WriteString(fmt.Sprintf("[%d]: %s\n", i+1, truncateText(text, 500)))
			}
			if i > 200 {
				builder.WriteString("...[truncated]")
				break
			}
		}
		promptMsgs = []M{buildSummaryPrompt[M](builder.String())}
	}

	var lastErr error
	maxAttempts := m.cfg.MaxRetries
	if maxAttempts <= 0 {
		maxAttempts = 1
		if m.cfg.RetryConfig != nil && m.cfg.RetryConfig.MaxRetries > 0 {
			maxAttempts = 1 + m.cfg.RetryConfig.MaxRetries
		}
	}
	for attempt := 0; attempt < maxAttempts; attempt++ {
		resp, err := m.cfg.Model.Generate(ctx, promptMsgs)
		if err == nil {
			if m.cfg.EmitInternalEvents {
				ev := &core.TypedAgentEvent[M]{
					Output: &core.TypedAgentOutput[M]{},
				}
				_ = core.TypedSendEvent(ctx, ev)
			}
			return extractText(resp), nil
		}
		lastErr = err
		if attempt < maxAttempts {
			time.Sleep(time.Duration(100*(1<<uint(attempt))) * time.Millisecond)
		}
	}
	return fmt.Sprintf("(%d messages)", len(msgs)), lastErr
}

func compressSummaryContent(content string, budget *SummaryBudget) string {
	start := strings.Index(content, "<summary>")
	end := strings.LastIndex(content, "</summary>")
	if start < 0 || end <= start {
		return content
	}
	inner := content[start+9 : end]
	compressed := CompressSummary(inner, budget)
	return content[:start+9] + "\n" + compressed + "\n" + content[end:]
}

// ---- 辅助函数 ----

func typedToSchemaMessages[M core.MessageType](msgs []M) []*schema.Message {
	result := make([]*schema.Message, 0, len(msgs))
	for _, m := range msgs {
		if msg, ok := any(m).(*schema.Message); ok {
			result = append(result, msg)
		}
	}
	return result
}

func schemaMessagesToTyped[M core.MessageType](msgs []*schema.Message) []M {
	result := make([]M, 0, len(msgs))
	for _, m := range msgs {
		if v, ok := any(m).(M); ok {
			result = append(result, v)
		}
	}
	return result
}

func getSummaryInstruction(lang string) string {
	if lang == "zh" {
		return `你是一个对话摘要助手。请总结以下对话,保留关键上下文、决定和待办事项。
要求:
1. 保持客观,不要添加对话中没有的信息
2. 保留重要的决策、结论和行动项
3. 使用与原对话相同的语言
4. 摘要应当简明扼要`
	}
	return `You are a conversation summarizer. Summarize the following conversation, preserving key context, decisions, and action items.
Requirements:
1. Stay objective, do not add information not in the conversation
2. Preserve important decisions, conclusions, and action items
3. Use the same language as the original conversation
4. Keep the summary concise`
}

func extractText[M core.MessageType](msg M) string {
	switch v := any(msg).(type) {
	case *schema.Message:
		return v.Content
	case *schema.AgenticMessage:
		return v.Content
	}
	return ""
}

func truncateText(s string, maxLen int) string {
	if len(s) <= maxLen {
		return s
	}
	// 按 rune 边界截断对话片段，避免摘要 prompt 中出现乱码。
	// multi-byte UTF-8 character, producing invalid output in the summary.
	runes := []rune(s)
	if maxLen > len(runes) {
		return s
	}
	return string(runes[:maxLen]) + "..."
}

func buildSummaryPrompt[M core.MessageType](content string) M {
	var zero M
	switch any(zero).(type) {
	case *schema.AgenticMessage:
		return any(schema.UserAgenticMessage(content)).(M)
	default:
		return any(schema.UserMessage(content)).(M)
	}
}

func defaultTokenCounter[M core.MessageType](ctx context.Context, msgs []M) (int, error) {
	total := 0
	for _, msg := range msgs {
		text := extractText(msg)
		total += len([]rune(text)) * 4 / 3
	}
	return total, nil
}

// FinalizerBuilder 构建保留最近 N 条消息的 Finalize 函数。
type FinalizerBuilder struct {
	Lang       string
	KeepLatest int
}

// Build 返回 Finalize：摘要消息 + 原始列表尾部 keep 条。
func (b *FinalizerBuilder) Build() func(ctx context.Context, original, summary []*schema.Message) ([]*schema.Message, error) {
	keep := b.KeepLatest
	if keep <= 0 {
		keep = DefaultKeepMessages
	}
	return func(ctx context.Context, original, summary []*schema.Message) ([]*schema.Message, error) {
		if len(original) <= keep {
			return summary, nil
		}
		result := make([]*schema.Message, 0, len(summary)+keep)
		result = append(result, summary...)
		result = append(result, original[len(original)-keep:]...)
		return result, nil
	}
}

// SummaryLang 为 zh 时使用中文摘要指令；Callback 可观测压缩前后状态。
