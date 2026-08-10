package tui

import (
	"fmt"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/ollama/ollama/cmd/launch"
)

var (
	confirmActiveStyle = lipgloss.NewStyle().
				Bold(true).
				Background(lipgloss.AdaptiveColor{Light: "254", Dark: "236"})

	confirmInactiveStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"})
)

// confirm 提供 bubbletea 是/否确认对话框。
// confirmModel 是确认对话框的 bubbletea 模型。
type confirmModel struct {
	prompt    string
	yesLabel  string
	noLabel   string
	yes       bool
	confirmed bool
	cancelled bool
	width     int
}

// ConfirmOptions 复用 launch 包的确认选项类型。
type ConfirmOptions = launch.ConfirmOptions

// Init 无需异步命令。
func (m confirmModel) Init() tea.Cmd {
	return nil
}

// Update 处理窗口尺寸与左右/确认/取消键。
func (m confirmModel) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		wasSet := m.width > 0
		m.width = msg.Width
		if wasSet {
			return m, tea.ClearScreen
		}
		return m, nil

	case tea.KeyMsg:
		switch msg.String() {
		case "ctrl+c", "esc":
			m.cancelled = true
			return m, tea.Quit
		case "enter":
			m.confirmed = true
			return m, tea.Quit
		case "left":
			m.yes = true
		case "right":
			m.yes = false
		}
	}

	return m, nil
}

// View 渲染提示语与是/否按钮。
func (m confirmModel) View() string {
	if m.confirmed || m.cancelled {
		return ""
	}

	var yesBtn, noBtn string
	yesLabel := m.yesLabel
	if yesLabel == "" {
		yesLabel = "Yes"
	}
	noLabel := m.noLabel
	if noLabel == "" {
		noLabel = "No"
	}
	if m.yes {
		yesBtn = confirmActiveStyle.Render(" " + yesLabel + " ")
		noBtn = confirmInactiveStyle.Render(" " + noLabel + " ")
	} else {
		yesBtn = confirmInactiveStyle.Render(" " + yesLabel + " ")
		noBtn = confirmActiveStyle.Render(" " + noLabel + " ")
	}

	s := selectorTitleStyle.Render(m.prompt) + "\n\n"
	s += "  " + yesBtn + "  " + noBtn + "\n\n"
	s += selectorHelpStyle.Render("←/→ navigate • enter confirm • esc cancel")

	if m.width > 0 {
		return lipgloss.NewStyle().MaxWidth(m.width).Render(s)
	}
	return s
}

// RunConfirm 显示 bubbletea 是/否确认框；确认返回 true，取消返回 false。
// Returns true if the user confirmed, false if cancelled.
func RunConfirm(prompt string) (bool, error) {
	return RunConfirmWithOptions(prompt, ConfirmOptions{})
}

// RunConfirmWithOptions 支持自定义按钮标签的是/否确认框。
// optional custom button labels.
func RunConfirmWithOptions(prompt string, options ConfirmOptions) (bool, error) {
	yesLabel := options.YesLabel
	if yesLabel == "" {
		yesLabel = "Yes"
	}
	noLabel := options.NoLabel
	if noLabel == "" {
		noLabel = "No"
	}

	m := confirmModel{
		prompt:   prompt,
		yesLabel: yesLabel,
		noLabel:  noLabel,
		yes:      options.Default != launch.ConfirmDefaultNo,
	}

	p := tea.NewProgram(m)
	finalModel, err := p.Run()
	if err != nil {
		return false, fmt.Errorf("error running confirm: %w", err)
	}

	fm := finalModel.(confirmModel)
	if fm.cancelled {
		return false, ErrCancelled
	}

	return fm.yes, nil
}
