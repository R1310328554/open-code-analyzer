// agent 包实现 Ollama 智能体会话、工具调用、用户审批与上下文压缩等核心逻辑。
package agent

import (
	"context"
	"strings"
	"sync"
)

// ApprovalRequest 描述一次待审批的工具调用批次。
type ApprovalRequest struct {
	WorkingDir string
	Calls      []ApprovalToolCall
}

// AddToolCall 向审批请求追加一条工具调用。
func (r *ApprovalRequest) AddToolCall(id, name, scope string, args map[string]any) {
	r.Calls = append(r.Calls, ApprovalToolCall{
		ToolCallID:    id,
		ToolName:      name,
		Args:          args,
		ApprovalScope: scope,
	})
}

// ApprovalToolCall 表示单条待审批的工具调用。
type ApprovalToolCall struct {
	ToolCallID    string
	ToolName      string
	Args          map[string]any
	ApprovalScope string
}

// Approval 表示用户对工具执行的审批结果。
type Approval struct {
	Allow       bool
	AllowAll    bool
	AllowScopes []string
	Reason      string
}

// ApprovalPrompter 负责向用户展示审批提示并收集决定。
type ApprovalPrompter interface {
	PromptApproval(context.Context, ApprovalRequest) (Approval, error)
}

// ApprovalState 在会话内累积已授予的审批范围。
type ApprovalState struct {
	mu       sync.RWMutex
	allowAll bool
	scopes   map[string]bool
}

// Set 重置全局允许标志与范围映射。
func (s *ApprovalState) Set(allowAll bool, scopes map[string]bool) {
	if s == nil {
		return
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	s.allowAll = allowAll
	s.scopes = cloneApprovalScopes(scopes)
}

// GrantAll grants blanket approval for all future tool calls.
func (s *ApprovalState) GrantAll() {
	// GrantAll 授予对所有后续工具调用的 blanket 批准。
	if s == nil {
		return
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	s.allowAll = true
}

// AllGranted reports whether blanket approval has been granted.
func (s *ApprovalState) AllGranted() bool {
	// AllGranted 报告是否已授予 blanket 批准。
	if s == nil {
		return false
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.allowAll
}

// Allows 检查给定审批范围是否已被允许。
func (s *ApprovalState) Allows(scope string) bool {
	if s == nil {
		return false
	}
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.allowAll || s.scopes[scope]
}

// Apply merges an approval's scopes and allow-all flag into the state. It
// returns true if the approval grants permission (allow-all or at least one
// scope). It does not mutate the approval; the caller sets Allow based on the
// returned value.
func (s *ApprovalState) Apply(result *Approval) bool {
	// Apply 将审批结果合并进状态；若授予 allow-all 或至少一个范围则返回 true。
	if s == nil || result == nil {
		return false
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	granted := false
	if result.AllowAll {
		s.allowAll = true
		granted = true
	}
	if len(result.AllowScopes) > 0 {
		granted = true
		s.grantScopesLocked(result.AllowScopes)
	}
	return granted
}

// GrantScopes merges the given scopes into the state.
func (s *ApprovalState) GrantScopes(scopes []string) {
	// GrantScopes 将给定范围合并进审批状态。
	if s == nil {
		return
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	s.grantScopesLocked(scopes)
}

// grantScopesLocked adds trimmed, non-empty scopes to the state. Caller must
// hold s.mu.
func (s *ApprovalState) grantScopesLocked(scopes []string) {
	// grantScopesLocked 添加修剪后的非空范围；调用方须持有 s.mu。
	if s.scopes == nil {
		s.scopes = make(map[string]bool, len(scopes))
	}
	for _, scope := range scopes {
		scope = strings.TrimSpace(scope)
		if scope != "" {
			s.scopes[scope] = true
		}
	}
}

// cloneApprovalScopes 深拷贝审批范围映射。
func cloneApprovalScopes(src map[string]bool) map[string]bool {
	if len(src) == 0 {
		return nil
	}
	dst := make(map[string]bool, len(src))
	for scope, allowed := range src {
		if allowed {
			dst[scope] = true
		}
	}
	return dst
}

// needsApproval 判断工具调用是否仍需用户审批。
func (s *Session) needsApproval(tool Tool, name string, args map[string]any) bool {
	return ToolRequiresApproval(tool, args) && !s.allows(toolApprovalScope(tool, name, args))
}

// allows reports whether scope is permitted by the session's accumulated approval state.
func (s *Session) allows(scope string) bool {
	// allows 报告会话累积审批状态是否允许该范围。
	if s == nil || s.ApprovalState == nil {
		return false
	}
	return s.ApprovalState.Allows(scope)
}

// applyApproval merges an approval result into the session's state and marks
// the result as allowed when scopes or allow-all were granted.
func (s *Session) applyApproval(result *Approval) {
	// applyApproval 将审批结果合并进会话状态并标记 Allow。
	if s == nil || result == nil {
		return
	}
	if s.ApprovalState == nil {
		s.ApprovalState = &ApprovalState{}
	}
	if s.ApprovalState.Apply(result) {
		result.Allow = true
	}
}

// authorizeToolCalls 批量请求用户审批并更新会话状态。
func (s *Session) authorizeToolCalls(ctx context.Context, req ApprovalRequest) (Approval, error) {
	if s == nil || len(req.Calls) == 0 || (s.ApprovalState != nil && s.ApprovalState.AllGranted()) {
		return Approval{Allow: true}, nil
	}
	if s.ApprovalPrompter == nil {
		return Approval{
			Reason: "Tool execution requires approval, but no approval prompter is available.",
		}, nil
	}

	result, err := s.ApprovalPrompter.PromptApproval(ctx, req)
	if err != nil {
		return Approval{}, err
	}
	s.applyApproval(&result)
	return result, nil
}

// toolApprovalScope returns the approval scope key for a tool invocation.
// If the tool implements ScopedTool, its ApprovalScope method determines the
// scope (e.g. shell tools scope to "<tool>\x00<command>"). Otherwise the scope
// is the trimmed tool name.
func toolApprovalScope(tool Tool, toolName string, args map[string]any) string {
	// toolApprovalScope 返回工具调用的审批范围键。
	if scoped, ok := tool.(ScopedTool); ok {
		return scoped.ApprovalScope(args)
	}
	return strings.TrimSpace(toolName)
}
