// coding.go — 开箱即用编码 Agent：文件系统、Shell 白名单、Git 安全规则与可选子 Agent。

// Package coding 基于 agentcore ReAct 与 profile 提供生产级编码助手。
// ReAct agent, middleware stack, and profile system.
//
// 等价于 deepagents-code：文件操作、Shell 安全、Git 防护与可选子 Agent。
// assistant with file operations, shell security, Git safety, and optional sub-agents.
//
// Quick start:
//
//	agent := coding.New(&coding.Config{
//	    Model: myModel,
//	})
//	runner := core.NewTypedRunner(core.RunnerConfig[*schema.Message]{Agent: agent})
//	iter := runner.Run(ctx, []*schema.Message{schema.UserMessage("fix this bug")})
//
// With the profile system:
//
//	coding.RegisterHarnessProfile()
//	agent, _ := profile.NewAgent(ctx, &profile.AgentConfig{
//	    ModelSpec: "anthropic:claude-sonnet-4-6",
//	    HarnessProfileName: "coding-agent",
//	})
package coding

import (
	"context"
	"fmt"
	"os"
	"os/exec"
	"strings"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/middlewares/filesystem"
	"ragflow/internal/harness/core/middlewares/subagent"
	"ragflow/internal/harness/core/profile"
	"ragflow/internal/harness/core/schema"
)

// Config 配置编码 Agent。
type Config struct {
	// Name Agent 名称，默认 coding_agent。
	Name string

	// Model 聊天模型（使用 profile 时可省略）。
	Model core.Model[*schema.Message]

	// Tools 除内置编码工具外额外注册的工具。
	Tools []core.Tool

	// Instruction 覆盖默认编码系统 prompt。
	Instruction string

	// MaxIterations ReAct 循环上限，默认 30。
	MaxIterations int

	// EnableShell 为 true 时启用本地 Shell 执行（默认只读文件操作）。
	// Default: false (read-only file operations).
	EnableShell bool

	// ShellBackend is the filesystem backend to use for shell execution.
	// When nil and EnableShell is true, a default local shell backend is created.
	ShellBackend filesystem.Backend

	// ShellAllowList configures which shell commands are allowed.
	// When nil, the default allow-list is used (if EnableShell is true).
	// When EnableShell is false, this field is ignored.
	ShellAllowList *ShellAllowListConfig

	// FilesystemBackend is the filesystem backend for file operations.
	// When nil, an InMemoryBackend is used (read-only from agent perspective).
	// Use a LocalFilesystemBackend for real file system access.
	FilesystemBackend filesystem.Backend

	// SubAgentSpecs 声明用于任务委托的子 Agent。
	SubAgentSpecs []subagent.SubAgentSpec

	// SubAgentConfig configures the SubAgentMiddleware (recursion depth, events).
	SubAgentConfig *subagent.Config

	// RegisterHarness 为 true 时同时注册 coding-agent harness profile。
	// Default: false.
	RegisterHarness bool
}

// DefaultConfig 返回带合理默认值的 Config。
func DefaultConfig() *Config {
	return &Config{
		Name:          "coding_agent",
		MaxIterations: 30,
		EnableShell:   false,
	}
}

// New 组装文件系统、Shell 白名单、SubAgent 中间件并创建 ReActAgent。
//
// 默认栈包含：
//   - 编码优化系统 prompt（含 Git 安全规则）
//   - 文件系统中间件（read/write/edit/ls/glob/grep）
//   - Shell 命令白名单中间件（EnableShell 时）
//   - SubAgentMiddleware（SubAgentSpecs 非空时）
//   - Optional "coding-agent" harness profile registration
// New 按 Config 构建中间件栈并返回 ReActAgent。
func New(cfg *Config) *core.ReActAgent[*schema.Message] {
	if cfg == nil {
		cfg = DefaultConfig()
	}
	if cfg.MaxIterations <= 0 {
		cfg.MaxIterations = 30
	}
	if cfg.Name == "" {
		cfg.Name = "coding_agent"
	}
	instruction := cfg.Instruction
	if instruction == "" {
		instruction = systemPrompt
	}

	// Build middleware stack.
	var middlewares []core.ReActMiddleware

	// 1. Shell 白名单（先于 filesystem，拦截 execute 调用）。
	if cfg.EnableShell {
		shellCfg := cfg.ShellAllowList
		if shellCfg == nil {
			shellCfg = &ShellAllowListConfig{
				AllowedCommands: DefaultShellAllowList(),
				BlockedCommands: DefaultBlockedCommands(),
			}
		}
		middlewares = append(middlewares, NewShellAllowList(shellCfg))
	}

	// 2. 文件系统中间件（提供读写与 execute）。
	fsCfg := &filesystem.Config{
		Backend: cfg.FilesystemBackend,
	}
	if cfg.EnableShell && cfg.ShellBackend != nil {
		fsCfg.Backend = cfg.ShellBackend
	} else if cfg.EnableShell && cfg.FilesystemBackend == nil {
		// Create default local shell backend.
		fsCfg.Backend = &localShellBackend{}
	}
	middlewares = append(middlewares, filesystem.New(fsCfg))

	// 3. SubAgentMiddleware（声明子 Agent 时 Init 并注入工具）。
	if len(cfg.SubAgentSpecs) > 0 {
		saCfg := cfg.SubAgentConfig
		if saCfg == nil {
			saCfg = &subagent.Config{MaxDepth: 5}
		}
		saMW := subagent.New(cfg.SubAgentSpecs, saCfg)
		middlewares = append(middlewares, saMW)

		// Build react config — Init() prepares the sub-agent for middleware
		// inheritance, while ToolContributor handles automatic tool injection.
		reactCfg := &core.ReActConfig[*schema.Message]{
			Model:         cfg.Model,
			Instruction:   instruction,
			MaxIterations: cfg.MaxIterations,
			Middlewares:   middlewares,
			Tools:         cfg.Tools,
		}
		saMW.Init(context.Background(), reactCfg)
		return core.NewReActAgent(reactCfg)
	}

	// Build react config without sub-agents.
	reactCfg := &core.ReActConfig[*schema.Message]{
		Model:         cfg.Model,
		Instruction:   instruction,
		MaxIterations: cfg.MaxIterations,
		Middlewares:   middlewares,
		Tools:         cfg.Tools,
	}
	if cfg.EnableShell || cfg.FilesystemBackend != nil {
		// Must have at least one tool for ReAct loop.
		if len(reactCfg.Tools) == 0 {
			// Filesystem middleware adds tools via BeforeAgent, but we need
			// at least one tool to trigger the ReAct loop.
			reactCfg.Tools = append(reactCfg.Tools, &execTool{})
		}
	}
	return core.NewReActAgent(reactCfg)
}

// ---- 本地 Shell 后端 ----

// localShellBackend 用本地 OS 实现 filesystem.Backend 与 Shell 执行。
type localShellBackend struct{}

func (b *localShellBackend) Read(path string) (string, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

func (b *localShellBackend) Write(path, content string) error {
	return os.WriteFile(path, []byte(content), 0644)
}

func (b *localShellBackend) Edit(path, old, new string) error {
	data, err := os.ReadFile(path)
	if err != nil {
		return err
	}
	content := string(data)
	if !strings.Contains(content, old) {
		return fmt.Errorf("edit_file: string %q not found in %s", old, path)
	}
	content = strings.Replace(content, old, new, 1)
	return os.WriteFile(path, []byte(content), 0644)
}

func (b *localShellBackend) Ls(path string) ([]string, error) {
	entries, err := os.ReadDir(path)
	if err != nil {
		return nil, err
	}
	names := make([]string, 0, len(entries))
	for _, e := range entries {
		names = append(names, e.Name())
	}
	return names, nil
}

func (b *localShellBackend) Glob(pattern string) ([]string, error) {
	// Simple glob via the shell.
	cmd := exec.Command("sh", "-c", fmt.Sprintf("ls -d %s 2>/dev/null", pattern))
	out, err := cmd.Output()
	if err != nil {
		return nil, nil
	}
	lines := strings.TrimSpace(string(out))
	if lines == "" {
		return nil, nil
	}
	return strings.Split(lines, "\n"), nil
}

func (b *localShellBackend) Grep(pattern, path string) (string, error) {
	cmd := exec.Command("grep", "-rn", pattern, path)
	out, err := cmd.Output()
	if err != nil {
		// grep returns exit code 1 when no matches.
		return "", nil
	}
	return string(out), nil
}

func (b *localShellBackend) Execute(command string) (string, error) {
	cmd := exec.Command("sh", "-c", command)
	out, err := cmd.CombinedOutput()
	if err != nil {
		return string(out), fmt.Errorf("execute: %w\n%s", err, string(out))
	}
	return string(out), nil
}

// ---- 引导 ReAct 循环的占位工具 ----

type execTool struct{}

func (t *execTool) Name() string        { return "_bootstrap_tool" }
func (t *execTool) Description() string { return "Internal bootstrap tool" }
func (t *execTool) Invoke(ctx context.Context, args string, opts ...core.ToolOption) (string, error) {
	return "", nil
}
func (t *execTool) Stream(ctx context.Context, args string, opts ...core.ToolOption) (*schema.StreamReader[string], error) {
	return schema.StreamReaderFromArray([]string{""}), nil
}

// ---- Harness profile 注册 ----

// HarnessProfile 返回 coding-agent 预配置 HarnessProfile。
// It registers the standard coding agent middleware stack.
func HarnessProfile() *profile.HarnessProfile {
	return &profile.HarnessProfile{
		Name:             "coding-agent",
		BaseSystemPrompt: strPtr(systemPrompt),
		MaxIterations:    30,
		RecursionDepth:   5,
	}
}

// RegisterHarnessProfile 全局注册 coding-agent profile。
// After calling this, users can create coding agents via profile.NewAgent:
//
//	agent, _ := profile.NewAgent(ctx, &profile.AgentConfig{
//	    ModelSpec: "anthropic:claude-sonnet-4-6",
//	    HarnessProfileName: "coding-agent",
//	})
func RegisterHarnessProfile() {
	if profile.LookupHarness("coding-agent") != nil {
		return // already registered
	}
	profile.RegisterHarness(HarnessProfile())
}

func strPtr(s string) *string { return &s }

// 无子 Agent 且启用 Shell/文件系统时注入 _bootstrap_tool 以触发 ReAct 循环。
