package coding

// shell.go — Shell 命令白名单中间件：在 execute 工具调用前按前缀匹配允许/禁止列表拦截危险命令。


import (
	"context"
	"fmt"
	"strings"
	"sync"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// ShellAllowListConfig 配置 Shell 白名单中间件。
type ShellAllowListConfig struct {
	// AllowedCommands 允许的命令前缀列表；命令以任一前缀开头即放行。
	// 例如 "git"、"go" 可匹配 "git commit"、"go build"。
	// Example: "git", "go", "npm" allows "git commit", "go build", "npm install".
	// 为空时若未启用 Passthrough，则使用 DefaultShellAllowList。
	AllowedCommands []string

	// BlockedCommands 显式禁止的前缀，优先级高于 AllowedCommands。
	// 例如 "git push --force" 或 "rm -rf"。
	// Example: "git push --force" or "rm -rf".
	BlockedCommands []string

	// DenyMessage 命令被拦截时返回给 LLM 的提示文本。
	DenyMessage string

	// Passthrough 为 true 时跳过过滤，全部放行。
	// 默认 false。
	Passthrough bool
}

// DefaultShellAllowList 返回编码场景常用命令白名单。
func DefaultShellAllowList() []string {
	return []string{
		"git", "go", "make", "npm", "npx", "yarn", "pnpm",
		"cargo", "rustc", "python", "python3", "pip", "pip3",
		"ls", "cat", "head", "tail", "wc", "sort", "uniq",
		"grep", "find", "which", "type", "file", "du", "df",
		"echo", "printf", "env", "pwd", "date",
		"ps", "top", "htop", "kill", "killall",
		"curl", "wget", "ping", "nslookup", "dig",
		"diff", "patch", "cmp", "tar", "gzip", "gunzip", "zip", "unzip",
		"docker", "docker-compose",
		"sed", "awk", "xargs",
		"ssh", "scp", "rsync",
		"goctl", "mockgen", "protoc",
	}
}

// DefaultBlockedCommands 返回始终应拦截的危险命令。
func DefaultBlockedCommands() []string {
	return []string{
		"rm -rf /", "rm -rf ~", "rm -rf .",
		"chmod -R", "chown -R",
		"dd if=", "mkfs", "fdisk",
		"> /dev/", "> /etc/", "> /boot/",
		":(){ :|:& };:", // fork 炸弹
	}
}

// ShellAllowListMiddleware 在工具执行前过滤 Shell 命令。
// 应放在文件系统中间件之前，以拦截 execute 工具调用。
// 对普通与 Enhanced 两种工具调用路径均生效。
type ShellAllowListMiddleware struct {
	core.BaseMiddleware[*schema.Message]
	cfg    *ShellAllowListConfig
	once   sync.Once
	parsed struct {
		allowed []string
		blocked []string
	}
}

// NewShellAllowList 创建白名单中间件；cfg 为 nil 或 Passthrough 时全部放行。
// nil 配置等价于 Passthrough 模式。
func NewShellAllowList(cfg *ShellAllowListConfig) *ShellAllowListMiddleware {
	if cfg == nil {
		cfg = &ShellAllowListConfig{Passthrough: true}
	}
	if cfg.DenyMessage == "" {
		cfg.DenyMessage = "Error: command blocked by security policy. Use allowed commands only."
	}
	m := &ShellAllowListMiddleware{cfg: cfg}
	m.once.Do(m.init)
	return m
}

func (m *ShellAllowListMiddleware) init() {
	if m.cfg.AllowedCommands == nil {
		m.parsed.allowed = DefaultShellAllowList()
	} else {
		m.parsed.allowed = normalizeCommands(m.cfg.AllowedCommands)
	}
	if m.cfg.BlockedCommands == nil {
		m.parsed.blocked = DefaultBlockedCommands()
	} else {
		m.parsed.blocked = normalizeCommands(m.cfg.BlockedCommands)
	}
}

// WrapToolInvoke 拦截名为 execute 的工具调用并校验命令。
// 先查 BlockedCommands，再查 AllowedCommands。
func (m *ShellAllowListMiddleware) WrapToolInvoke(ctx context.Context, ep core.InvokableToolEndpoint, tc *core.ToolContext) (core.InvokableToolEndpoint, error) {
	if tc.Name != "execute" || m.cfg.Passthrough {
		return ep, nil
	}
	m.once.Do(m.init)

	return func(ctx context.Context, args string, opts ...core.ToolOption) (string, error) {
		cmd := strings.TrimSpace(args)
		if cmd == "" {
			return ep(ctx, args, opts...)
		}

		// 优先检查禁止列表。
		for _, blocked := range m.parsed.blocked {
			if strings.HasPrefix(cmd, blocked) {
				return m.cfg.DenyMessage, nil
			}
		}

		// 无白名单则放行。
		if len(m.parsed.allowed) == 0 {
			return ep(ctx, args, opts...)
		}

		// 检查白名单前缀匹配。
		for _, allowed := range m.parsed.allowed {
			if strings.HasPrefix(cmd, allowed) {
				return ep(ctx, args, opts...)
			}
		}

		return fmt.Sprintf("Error: command %q is not in the allowed list.", strings.Split(cmd, " ")[0]), nil
	}, nil
}

// WrapEnhancedInvokableToolCall 同样拦截增强工具路径。
func (m *ShellAllowListMiddleware) WrapEnhancedInvokableToolCall(ctx context.Context, ep core.EnhancedInvokableToolEndpoint, tc *core.ToolContext) (core.EnhancedInvokableToolEndpoint, error) {
	if tc.Name != "execute" || m.cfg.Passthrough {
		return ep, nil
	}
	m.once.Do(m.init)

	return func(ctx context.Context, args *schema.ToolArgument, opts ...core.ToolOption) (*schema.ToolResult, error) {
		cmd := strings.TrimSpace(args.Arguments)
		if cmd == "" {
			return ep(ctx, args, opts...)
		}

		for _, blocked := range m.parsed.blocked {
			if strings.HasPrefix(cmd, blocked) {
				return &schema.ToolResult{
					Name:    args.Name,
					Content: m.cfg.DenyMessage,
				}, nil
			}
		}

		if len(m.parsed.allowed) == 0 {
			return ep(ctx, args, opts...)
		}

		for _, allowed := range m.parsed.allowed {
			if strings.HasPrefix(cmd, allowed) {
				return ep(ctx, args, opts...)
			}
		}

		return &schema.ToolResult{
			Name:    args.Name,
			Content: fmt.Sprintf("Error: command %q is not in the allowed list.", strings.Split(cmd, " ")[0]),
		}, nil
	}, nil
}

// ---- 辅助函数 ----

func normalizeCommands(cmds []string) []string {
	out := make([]string, 0, len(cmds))
	for _, c := range cmds {
		c = strings.TrimSpace(c)
		if c != "" {
			out = append(out, c)
		}
	}
	return out
}

// 拦截时不抛错，而是将 DenyMessage 或错误说明作为工具结果返回给模型。
