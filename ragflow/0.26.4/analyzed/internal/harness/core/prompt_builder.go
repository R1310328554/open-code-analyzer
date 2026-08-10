package core

// prompt_builder.go — 系统提示构建器：动态上下文、指令文件发现、字符预算与分段组装（对标 claw-code SystemPromptBuilder）。


import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"sort"
	"strings"
	"time"
)

// PromptBuilder 组装含动态上下文与指令文件的系统提示。
// 支持字符预算与分段截断，设计对标 claw-code。
type PromptBuilder struct {
	buf strings.Builder
}

// NewPromptBuilder 创建空构建器。
func NewPromptBuilder() *PromptBuilder {
	return &PromptBuilder{}
}

// PromptBudget 定义各段落字符上限。
type PromptBudget struct {
	PerFile     int // 单指令文件最大字符（默认 4000）
	TotalFiles  int // 全部指令文件合计上限（默认 12000）
	GitDiff     int // Git diff 段落上限（默认 50000）
	MaxSections int // 最大段落数（默认 20）
}

func (b *PromptBudget) defaults() {
	if b.PerFile <= 0 {
		b.PerFile = 4000
	}
	if b.TotalFiles <= 0 {
		b.TotalFiles = 12000
	}
	if b.GitDiff <= 0 {
		b.GitDiff = 50000
	}
	if b.MaxSections <= 0 {
		b.MaxSections = 20
	}
}

// Build 按预算拼接各 PromptSection 为最终字符串。
func (pb *PromptBuilder) Build(parts []PromptSection, budget *PromptBudget) string {
	budget.defaults()
	pb.buf.Reset()

	for i, p := range parts {
		if i >= budget.MaxSections {
			break
		}
		text := p.Content
		if p.TruncateTo > 0 && len(text) > p.TruncateTo {
			text = text[:p.TruncateTo] + "\n...[truncated]"
		}
		if p.PrependNewline && pb.buf.Len() > 0 {
			pb.buf.WriteString("\n")
		}
		pb.buf.WriteString(text)
		if p.AppendNewline {
			pb.buf.WriteString("\n")
		}
	}

	return pb.buf.String()
}

// PromptSection 具名提示段落。
type PromptSection struct {
	Name           string
	Content        string
	TruncateTo     int // 本段最大字符（0 表示不单独限制）
	AppendNewline  bool
	PrependNewline bool
}

// ---- 指令文件发现 ----

// InstructionFile 已发现的指令文件及其内容。
type InstructionFile struct {
	Path    string
	Content string
}

// DiscoverInstructionFiles 从 startDir 向上找到 git 根并扫描指令文件。
// 查找 CLAUDE.md、CLAW.md、AGENTS.md 及 .claw/rules/*.md
func DiscoverInstructionFiles(startDir string) ([]InstructionFile, error) {
	root := findGitRoot(startDir)
	if root == "" {
		root = startDir
	}

	var files []InstructionFile
	candidates := []string{
		"CLAUDE.md", "CLAW.md", "AGENTS.md",
		".claw/CLAUDE.md", ".claw/instructions.md",
	}

	for _, name := range candidates {
		path := filepath.Join(root, name)
		content, err := os.ReadFile(path)
		if err != nil {
			continue
		}
		files = append(files, InstructionFile{Path: path, Content: string(content)})
	}

	// 加载 .claw/rules 目录下 md/txt
	rulesDir := filepath.Join(root, ".claw", "rules")
	if entries, err := os.ReadDir(rulesDir); err == nil {
		for _, e := range entries {
			if !e.IsDir() && (strings.HasSuffix(e.Name(), ".md") || strings.HasSuffix(e.Name(), ".txt")) {
				path := filepath.Join(rulesDir, e.Name())
				content, err := os.ReadFile(path)
				if err != nil {
					continue
				}
				files = append(files, InstructionFile{Path: path, Content: string(content)})
			}
		}
	}

	return files, nil
}

// InstructionFileSections 将指令文件转为带预算的 PromptSection。
func InstructionFileSections(files []InstructionFile, budget *PromptBudget) []PromptSection {
	budget.defaults()
	var sections []PromptSection
	remaining := budget.TotalFiles

	for _, f := range files {
		if remaining <= 0 {
			break
		}
		content := f.Content
		if len(content) > budget.PerFile {
			content = content[:budget.PerFile] + "\n...[truncated]"
		}
		if len(content) > remaining {
			content = content[:remaining] + "\n...[truncated]"
		}
		remaining -= len(content)

		name := filepath.Base(f.Path)
		sections = append(sections, PromptSection{
			Name:           "Instruction: " + name,
			Content:        content,
			AppendNewline:  true,
			PrependNewline: true,
		})
	}
	return sections
}

// ---- 动态上下文段落 ----

// EnvSection 生成环境信息段（日期、OS、主机、CWD）。
func EnvSection() PromptSection {
	hostname, _ := os.Hostname()
	cwd, _ := os.Getwd()
	now := time.Now().Format(time.RFC3339)

	return PromptSection{
		Name: "Environment",
		Content: fmt.Sprintf("Date: %s\nOS: %s/%s\nHost: %s\nCWD: %s",
			now, runtime.GOOS, runtime.GOARCH, hostname, cwd),
		AppendNewline:  true,
		PrependNewline: true,
	}
}

// GitDiffSection 生成 Git 工作区变更摘要段。
func GitDiffSection(budgetChars int) PromptSection {
	diff, _ := execGitDiff()
	if diff == "" {
		return PromptSection{Name: "GitDiff"}
	}
	if budgetChars > 0 && len(diff) > budgetChars {
		diff = diff[:budgetChars] + "\n...[diff truncated]"
	}
	return PromptSection{
		Name:          "Git Diff",
		Content:       fmt.Sprintf("Working tree changes (git diff):\n%s", diff),
		AppendNewline: true,
	}
}

func execGitDiff() (string, error) {
	cwd, err := os.Getwd()
	if err != nil {
		return "", err
	}
	root := findGitRoot(cwd)
	if root == "" {
		return "", fmt.Errorf("not a git repository")
	}
	data, err := os.ReadFile(filepath.Join(root, ".git", "HEAD"))
	if err != nil {
		return "", err
	}
	ref := strings.TrimSpace(string(data))
	return fmt.Sprintf("HEAD: %s", ref), nil
}

// findGitRoot 向上遍历查找 .git 目录。
func findGitRoot(dir string) string {
	dir, err := filepath.Abs(dir)
	if err != nil {
		return ""
	}
	for {
		if _, err := os.Stat(filepath.Join(dir, ".git")); err == nil {
			return dir
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			return ""
		}
		dir = parent
	}
}

// GroupSectionsWithBudget 在总预算内追加段落。
func GroupSectionsWithBudget(sections []PromptSection, budget int) []PromptSection {
	var result []PromptSection
	remaining := budget
	for _, s := range sections {
		if s.Content == "" {
			continue
		}
		if remaining <= 0 {
			break
		}
		if len(s.Content) > remaining {
			s.Content = s.Content[:remaining] + "\n...[truncated]"
			s.TruncateTo = len(s.Content)
		}
		remaining -= len(s.Content)
		result = append(result, s)
	}
	return result
}

// DeduplicateSections 按内容精确匹配去重。
func DeduplicateSections(sections []PromptSection) []PromptSection {
	seen := make(map[string]bool)
	var result []PromptSection
	for _, s := range sections {
		key := strings.TrimSpace(s.Content)
		if key == "" || seen[key] {
			continue
		}
		seen[key] = true
		result = append(result, s)
	}
	return result
}

// SortSections 将 Environment/Capabilities/Instructions 优先，其余按名称排序。
func SortSections(sections []PromptSection) {
	sort.SliceStable(sections, func(i, j int) bool {
		core := map[string]int{
			"Environment": 0, "Capabilities": 1, "Instructions": 2,
		}
		pi := core[sections[i].Name]
		pj := core[sections[j].Name]
		if pi != pj {
			return pi < pj
		}
		return sections[i].Name < sections[j].Name
	})
}

// execGitDiff 当前仅读取 HEAD ref 作为占位；完整 diff 可后续扩展。
