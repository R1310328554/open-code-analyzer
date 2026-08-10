// deep.go — DeepAgent 深度优先任务分解智能体：ReAct 循环 + 内置任务管理、可选 Shell 与子 Agent 委托。
// Package deep 提供 DeepAgent —— 深度优先任务分解与执行智能体。
// 结合 ReAct 循环、任务管理、文件系统访问与可选 Shell 执行。
// 适用于生产级编码/运维场景。
package deep

import (
	"context"
	"encoding/json"
	"fmt"
	"os/exec"
	"strings"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// SubAgentSpec 定义可委托任务的子智能体。
type SubAgentSpec struct {
	Name        string
	Description string
	Agent       core.Agent
}

// Config Deep Agent 配置项。
type Config struct {
	Name          string
	Description   string
	Model         core.Model[*schema.Message]
	Tools         []core.Tool
	MaxIterations int
	Instruction   string                      // 自定义系统提示（覆盖默认）
	EnableShell   bool                        // 是否启用 Shell 执行工具
	SubAgents     []SubAgentSpec              // 子智能体列表，用于任务委托
	FailoverModel core.Model[*schema.Message] // 故障转移备用模型
	OutputKey     string                      // 会话输出存储键
}

func DefaultConfig() *Config {
	return &Config{
		Name:          "deep_agent",
		Description:   "A depth-first task decomposition and execution agent",
		MaxIterations: 20,
		EnableShell:   false,
	}
}

// NewTyped 创建 TypedReActAgent 形式的 DeepAgent。
func NewTyped(cfg *Config) *core.ReActAgent[*schema.Message] {
	if cfg == nil {
		cfg = DefaultConfig()
	}
	if cfg.MaxIterations <= 0 {
		cfg.MaxIterations = 20
	}
	if cfg.Name == "" {
		cfg.Name = "deep_agent"
	}

	instruction := cfg.Instruction
	if instruction == "" {
		instruction = systemPrompt
	}

	// 若配置 OutputKey，在系统提示末尾追加存储指引
	if cfg.OutputKey != "" {
		instruction += "\n\nStore the final answer in the session under key {" + cfg.OutputKey + "}."
	}

	// 组装工具集：用户工具 + 任务管理工具
	tools := make([]core.Tool, 0, len(cfg.Tools)+8)
	tools = append(tools, cfg.Tools...)

	// 任务管理（write_todos / list / update）
	taskMgr := NewTaskManager()
	tools = append(tools,
		TaskCreateTool(taskMgr),
		TaskListTool(taskMgr),
		TaskUpdateTool(taskMgr),
	)

	// 可选 Shell 工具
	if cfg.EnableShell {
		tools = append(tools, ShellTool("."))
	}

	chatCfg := &core.ReActConfig[*schema.Message]{
		Model:         cfg.Model,
		Tools:         tools,
		Instruction:   instruction,
		MaxIterations: cfg.MaxIterations,
		OutputKey:     cfg.OutputKey,
	}

	// 配置 FailoverModel 时启用故障转移
	if cfg.FailoverModel != nil {
		chatCfg.FailoverConfig = &core.FailoverConfig[*schema.Message]{
			Models: []core.Model[*schema.Message]{cfg.FailoverModel},
		}
	}

	a := core.NewReActAgent(chatCfg)
	return a.WithName(cfg.Name).WithDescription(cfg.Description)
}

// NewWithSubAgents 创建支持子 Agent 委托的 DeepAgent。
// 可将任务转移给子 Agent 并接收结果。
// 无子 Agent 时返回普通 Deep Agent。
func NewWithSubAgents(ctx context.Context, cfg *Config) (core.ResumableAgent, error) {
	if cfg == nil {
		cfg = DefaultConfig()
	}
	deep := NewTyped(cfg)
	if cfg == nil || len(cfg.SubAgents) == 0 {
		return deep, nil
	}
	subs := make([]core.Agent, 0, len(cfg.SubAgents))
	for _, sa := range cfg.SubAgents {
		subs = append(subs, sa.Agent)
	}
	return core.SetSubAgents(ctx, deep, subs)
}

// New 返回通用 Agent 接口。
func New(cfg *Config) core.Agent { return NewTyped(cfg) }

// Prompt 返回默认系统提示词。
func Prompt() string { return systemPrompt }

// ---- Shell 工具 ----

// ShellTool 创建执行 Shell 命令的工具。
// 警告：仅在可信环境启用，存在任意代码执行风险。
func ShellTool(workDir string) core.Tool {
	return core.NewBaseTool(
		"shell",
		"Execute a shell command and return its output. Args: {\"command\":\"ls -la\"}",
		func(ctx context.Context, args string) (string, error) {
			var in struct {
				Command string `json:"command"`
			}
			if err := json.Unmarshal([]byte(args), &in); err != nil {
				return "", err
			}

			cmd := exec.CommandContext(ctx, "sh", "-c", in.Command)
			if workDir != "" {
				cmd.Dir = workDir
			}
			output, err := cmd.CombinedOutput()
			result := shellResult{
				Command:  in.Command,
				Output:   string(output),
				ExitCode: 0,
			}
			if exitErr, ok := err.(*exec.ExitError); ok {
				result.ExitCode = exitErr.ExitCode()
			} else if err != nil {
				return "", fmt.Errorf("shell exec: %w", err)
			}
			b, _ := json.Marshal(result)
			return string(b), nil
		},
	)
}

type shellResult struct {
	Command  string `json:"command"`
	Output   string `json:"output"`
	ExitCode int    `json:"exit_code"`
}

// StreamingShellTool 流式 Shell 执行工具。
func StreamingShellTool(workDir string) core.Tool {
	return core.NewBaseTool(
		"streaming_shell",
		"Execute a shell command with streaming output. Args: {\"command\":\"tail -f log.txt\"}",
		func(ctx context.Context, args string) (string, error) {
			var in struct {
				Command string `json:"command"`
			}
			json.Unmarshal([]byte(args), &in) // ignore error

			cmd := exec.CommandContext(ctx, "sh", "-c", in.Command)
			if workDir != "" {
				cmd.Dir = workDir
			}
			output, err := cmd.CombinedOutput()
			if err != nil {
				exitCode := -1
				if ee, ok := err.(*exec.ExitError); ok {
					exitCode = ee.ExitCode()
				}
				return fmt.Sprintf(`{"exit_code":%d,"error":"%s","output":"%s"}`, exitCode, err, escapeShell(string(output))), nil
			}
			return fmt.Sprintf(`{"exit_code":0,"output":%q}`, escapeShell(string(output))), nil
		},
	)
}

func escapeShell(s string) string {
	s = strings.ReplaceAll(s, "\n", "\\n")
	s = strings.ReplaceAll(s, "\r", "")
	s = strings.ReplaceAll(s, `\`, `\\`)
	s = strings.ReplaceAll(s, `"`, `\"`)
	return s
}

// ---- 任务管理器（内嵌于 Deep Agent）----

// TaskState 子任务生命周期状态。
type TaskState string

const (
	TaskPending   TaskState = "pending"
	TaskRunning   TaskState = "running"
	TaskCompleted TaskState = "completed"
	TaskFailed    TaskState = "failed"
)

// Task 由任务管理器跟踪的工作单元。
type Task struct {
	ID           string    `json:"id"`
	Description  string    `json:"description"`
	State        TaskState `json:"state"`
	Result       string    `json:"result,omitempty"`
	Error        string    `json:"error,omitempty"`
	Dependencies []string  `json:"dependencies,omitempty"`
}

// TaskManager 在会话内跟踪子任务列表。
type TaskManager struct{ tasks []*Task }

func NewTaskManager() *TaskManager { return &TaskManager{} }

func (m *TaskManager) Create(desc string, deps ...string) *Task {
	t := &Task{
		ID:          fmt.Sprintf("task_%d", len(m.tasks)+1),
		Description: desc, State: TaskPending,
		Dependencies: deps,
	}
	m.tasks = append(m.tasks, t)
	return t
}

func (m *TaskManager) List() []*Task { return m.tasks }
func (m *TaskManager) Get(id string) (*Task, error) {
	for _, t := range m.tasks {
		if t.ID == id {
			return t, nil
		}
	}
	return nil, fmt.Errorf("task %q not found", id)
}

func (m *TaskManager) Update(id, result string, state TaskState) error {
	t, err := m.Get(id)
	if err != nil {
		return err
	}
	t.Result = result
	t.State = state
	return nil
}

// TaskCreateTool 返回创建子任务（write_todos）的工具。
func TaskCreateTool(m *TaskManager) core.Tool {
	return core.NewBaseTool(
		"write_todos",
		"Create a todo/sub-task. Args: {\"todos\":[{\"desc\":\"...\",\"deps\":[]}]}",
		func(ctx context.Context, args string) (string, error) {
			var in struct {
				Todos []struct {
					Desc    string   `json:"desc"`
					Depends []string `json:"deps,omitempty"`
				} `json:"todos"`
			}
			if err := json.Unmarshal([]byte(args), &in); err != nil {
				return "", err
			}
			var created []*Task
			for _, td := range in.Todos {
				t := m.Create(td.Desc, td.Depends...)
				created = append(created, t)
			}
			b, _ := json.Marshal(created)
			return string(b), nil
		},
	)
}

// TaskListTool 返回列出全部子任务的工具。
func TaskListTool(m *TaskManager) core.Tool {
	return core.NewBaseTool(
		"list_todos",
		"List all sub-tasks and their status.",
		func(ctx context.Context, args string) (string, error) {
			b, _ := json.Marshal(m.List())
			return string(b), nil
		},
	)
}

// TaskUpdateTool 返回更新子任务状态的工具。
func TaskUpdateTool(m *TaskManager) core.Tool {
	return core.NewBaseTool(
		"update_todo",
		"Update a sub-task status. Args: {\"id\":\"task_1\",\"result\":\"done!\",\"status\":\"completed\"}",
		func(ctx context.Context, args string) (string, error) {
			var in struct {
				ID     string `json:"id"`
				Result string `json:"result,omitempty"`
				Status string `json:"status"`
			}
			if err := json.Unmarshal([]byte(args), &in); err != nil {
				return "", err
			}
			if err := m.Update(in.ID, in.Result, TaskState(in.Status)); err != nil {
				return "", err
			}
			b, _ := json.Marshal(map[string]string{"updated": in.ID})
			return string(b), nil
		},
	)
}

// ---- 多语言提示词 ----

const systemPrompt = `You are a Deep Agent — a depth-first task decomposition and execution agent.

Your role:
1. Break down complex tasks into specific, actionable sub-steps
2. Execute each step, verifying results before proceeding
3. Track sub-task completion using the write_todos / update_todo tools
4. Read files before editing them; test changes when appropriate
5. Report final results clearly when all tasks are complete

Guidelines:
- Verify actions before executing
- Read files before editing  
- Test changes when appropriate
- Track sub-tasks and their completion status
- Each sub-task should be specific, actionable, ordered logically
- After completing each sub-task, verify the output is correct`

var prompts = map[string]struct{ System, TaskPrompt, VerifyPrompt, TransferDesc string }{
	"en": {systemPrompt, "Each sub-task should be specific, actionable, ordered logically.", "After completing each sub-task, verify the output is correct.", "Transfer the question to another agent."},
	"zh": {`你是一个深度代理 —— 一个深度优先的任务分解和执行代理。

你的角色：
1. 将复杂任务分解为具体的、可执行的子步骤
2. 执行每个步骤，在继续之前验证结果
3. 使用 write_todos / update_todo 工具跟踪子任务完成情况
4. 在编辑文件前先阅读文件；适当时候测试变更
5. 所有任务完成后清晰报告最终结果

准则：
- 执行前验证操作
- 编辑前先阅读文件
- 适当时测试变更
- 跟踪子任务及其完成状态
- 每个子任务应具体、可操作、逻辑有序
- 完成每个子任务后，验证输出是否正确`, "每个子任务应该是具体的、可操作的、逻辑有序的。", "完成每个子任务后，验证输出是否正确。", "将问题移交给其他代理。"},
}

func SelectPrompt(lang string) string {
	if p, ok := prompts[lang]; ok {
		return p.System
	}
	return systemPrompt
}

// SelectPrompt 按语言代码选择系统提示；deep 包已内置中英文 prompts 映射。
