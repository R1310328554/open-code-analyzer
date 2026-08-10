package views

// views 包共享 Bubble Tea 视图类型：ViewID、SwitchViewMsg、RunConfig 与 lipgloss 样式。

import (
	tea "charm.land/bubbletea/v2"
	"charm.land/lipgloss/v2"
)

// globalProgram 供后台 goroutine 向 TUI 发送 BenchmarkOutputMsg 等异步消息。
// Global program reference for sending messages
var globalProgram *tea.Program

// SetProgram 在 main 创建 tea.Program 后注入，供 RunView 基准输出回调使用。
// SetProgram sets the global program reference
func SetProgram(p *tea.Program) {
	globalProgram = p
}

// ViewID 区分列表选择与运行配置两个全屏视图。
// ViewID identifies different views in the application
type ViewID int

// View identifiers
const (
	ListID ViewID = iota
	RunID
)

// Shared messages
type (
	// SwitchViewMsg is sent when switching between views
	SwitchViewMsg struct {
		View ViewID
	}

// BenchmarkOutputMsg 携带 go test  stdout/stderr 单行文本供 viewport 追加。
	// BenchmarkOutputMsg is sent when new benchmark output is available
	BenchmarkOutputMsg string

	// BenchmarkFinishedMsg is sent when a benchmark run completes
	BenchmarkFinishedMsg struct{}
)

// Shared styles
var (
	HeaderStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("12")).
			Padding(0, 1)

	ConfigStyle = lipgloss.NewStyle().
			Padding(0, 2)

	ControlStyle = lipgloss.NewStyle().
			Padding(0, 2).
			Foreground(lipgloss.Color("241"))

	ViewportStyle = lipgloss.NewStyle().
			BorderStyle(lipgloss.RoundedBorder()).
			BorderForeground(lipgloss.Color("62"))
)

// StatusText 用 lipgloss 着色 Running/Ready 状态字符串。
// StatusText returns a styled status string based on running state
func StatusText(running bool) string {
	if running {
		return lipgloss.NewStyle().
			Foreground(lipgloss.Color("42")).
			Render("Running")
	}
	return lipgloss.NewStyle().
		Foreground(lipgloss.Color("243")).
		Render("Ready")
}

// Model 接口统一 Init/Update/View，与 Bubble Tea 框架契约一致。
// Model interface that all views must implement
type Model interface {
	Init() tea.Cmd
	Update(msg tea.Msg) (Model, tea.Cmd)
	View() string
}

// RunConfig 记录重复次数、trace 开关、选中基准名与存储类型过滤。
// RunConfig holds the configuration for running benchmarks
type RunConfig struct {
	Count        int
	TraceEnabled bool
	Selected     []string
	StorageType  string // Storage type to use: "dataobj", "chunk", or "both"
}

// ViewportConfig holds the configuration for the viewport
type ViewportConfig struct {
	Width  int
	Height int
}
// ViewportStyle 为输出区域设置圆角边框，HeaderStyle/ControlStyle 统一 TUI 视觉层次。
