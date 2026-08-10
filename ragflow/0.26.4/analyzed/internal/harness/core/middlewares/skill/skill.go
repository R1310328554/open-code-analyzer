// skill.go — 技能中间件：Inline 注入指令，Fork/ForkWithContext 注册为工具。

// Package skill 提供技能加载与执行中间件。
// 技能定义在带 YAML frontmatter 的 SKILL.md 中。
package skill

import (
	"context"
	"fmt"
	"strings"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// ExecMode 定义技能执行方式。
type ExecMode int

const (
	ModeInline          ExecMode = iota // 技能内容注入系统指令
	ModeFork                            // 通过工具调用加载技能
	ModeForkWithContext                 // 带父上下文通过工具加载
)

// FileSystemBackend 从文件系统读取技能定义。
type FileSystemBackend interface {
	Read(path string) (string, error)
	List() ([]string, error)
}

// Config 描述单个技能。
type Config struct {
	Name          string
	Description   string
	Content       string
	ExecutionMode ExecMode
	Model         string // Fork 模式使用的模型名
	Agent         string // Fork 模式使用的 Agent 名
}

// TypedConfig 配置技能中间件。
type TypedConfig[M core.MessageType] struct {
	Skills             []Config
	Backend            FileSystemBackend
	CustomSystemPrompt func(name, desc string) string
	CustomToolParams   func(name string) string
	BuildContent       func(ctx context.Context, cfg Config) (string, error)
	BuildForkMessages  func(ctx context.Context, cfg Config, request string) (string, error)
	FormatForkResult   func(ctx context.Context, result string) (string, error)
}

type middleware[M core.MessageType] struct {
	core.BaseMiddleware[M]
	cfg *TypedConfig[M]
}

// NewTyped 创建技能中间件。
func NewTyped[M core.MessageType](cfg *TypedConfig[M]) core.TypedReActMiddleware[M] {
	return &middleware[M]{cfg: cfg}
}

func New(cfg *TypedConfig[*schema.Message]) core.TypedReActMiddleware[*schema.Message] {
	return NewTyped[*schema.Message](cfg)
}

// ContributeTools 为 Fork 模式技能注册 skill_* 工具。
func (m *middleware[M]) ContributeTools(ctx context.Context) []core.Tool {
	if m.cfg == nil {
		return nil
	}
	var tools []core.Tool
	for _, s := range m.loadSkills() {
		switch s.ExecutionMode {
		case ModeFork, ModeForkWithContext:
			tools = append(tools, m.newSkillTool(s))
		}
	}
	return tools
}

func (m *middleware[M]) ContributeToolInfos(ctx context.Context) []*schema.ToolInfo   { return nil }
func (m *middleware[M]) ContributeReturnDirectly(ctx context.Context) map[string]bool { return nil }

// BeforeAgent 将 Inline 技能追加到系统指令。
func (m *middleware[M]) BeforeAgent(ctx context.Context, rc *core.ReActAgentContext) (context.Context, *core.ReActAgentContext, error) {
	if m.cfg == nil {
		return ctx, rc, nil
	}
	for _, s := range m.loadSkills() {
		if s.ExecutionMode == ModeInline {
			rc.Instruction = applyCustomInstruction(rc.Instruction, s, m.cfg.CustomSystemPrompt)
		}
	}
	return ctx, rc, nil
}

// loadSkills 合并配置与后端扫描的技能列表。
func (m *middleware[M]) loadSkills() []Config {
	if m.cfg == nil {
		return nil
	}
	skills := m.cfg.Skills
	if len(skills) == 0 && m.cfg.Backend != nil {
		names, err := m.cfg.Backend.List()
		if err == nil {
			for _, name := range names {
				content, err := m.cfg.Backend.Read(name)
				if err != nil {
					continue
				}
				parsed := parseSkill(content)
				if parsed != nil {
					skills = append(skills, *parsed)
				}
			}
		}
	}
	return skills
}

func (m *middleware[M]) newSkillTool(s Config) core.Tool {
	return core.NewBaseTool("skill_"+s.Name,
		fmt.Sprintf("Execute the '%s' skill. %s", s.Name, s.Description),
		func(ctx context.Context, args string) (string, error) {
			if m.cfg.BuildContent != nil {
				content, err := m.cfg.BuildContent(ctx, s)
				if err != nil {
					return "", err
				}
				if m.cfg.FormatForkResult != nil {
					return m.cfg.FormatForkResult(ctx, content)
				}
				return content, nil
			}
			content := s.Content
			if content == "" && m.cfg.Backend != nil {
				loaded, err := m.cfg.Backend.Read(s.Name)
				if err == nil {
					content = loaded
				}
			}
			if m.cfg.BuildForkMessages != nil {
				result, err := m.cfg.BuildForkMessages(ctx, s, args)
				if err != nil {
					return "", err
				}
				return result, nil
			}
			if m.cfg.FormatForkResult != nil {
				return m.cfg.FormatForkResult(ctx, content)
			}
			return fmt.Sprintf("### Skill: %s\n\n%s\n\nArgs: %s", s.Name, truncate(content, 2000), args), nil
		})
}

// ---- 辅助函数 ----

// parseSkill 解析 frontmatter 与正文为 Config。
func parseSkill(content string) *Config {
	cfg := &Config{ExecutionMode: ModeInline}
	content = strings.TrimSpace(content)

	// 解析 YAML 风格 frontmatter
	if strings.HasPrefix(content, "---") {
		parts := strings.SplitN(content[3:], "---", 2)
		if len(parts) == 2 {
			front := strings.TrimSpace(parts[0])
			body := strings.TrimSpace(parts[1])
			for _, line := range strings.Split(front, "\n") {
				line = strings.TrimSpace(line)
				if strings.HasPrefix(line, "name:") {
					cfg.Name = strings.TrimSpace(line[5:])
				} else if strings.HasPrefix(line, "description:") {
					cfg.Description = strings.TrimSpace(line[12:])
				} else if strings.HasPrefix(line, "model:") {
					cfg.Model = strings.TrimSpace(line[6:])
				}
			}
			cfg.Content = body
			return cfg
		}
	}
	// 无 frontmatter 时全文作为技能内容
	cfg.Content = content
	return cfg
}

func applyCustomInstruction(instruction string, s Config, customFn func(name, desc string) string) string {
	if customFn != nil {
		return instruction + "\n\n" + customFn(s.Name, s.Description)
	}
	return instruction + "\n\n## Skill: " + s.Name + "\n" + truncate(s.Content, 4000)
}

func truncate(s string, n int) string {
	if len(s) <= n {
		return s
	}
	return s[:n] + "\n...(truncated)"
}

// BuildContent/BuildForkMessages/FormatForkResult 可定制 Fork 执行与结果格式化。
