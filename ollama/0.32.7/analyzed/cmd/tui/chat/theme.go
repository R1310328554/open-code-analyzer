// Package chat 实现 Ollama Agent 终端聊天界面（Bubble Tea）：会话渲染、输入、工具审批、云端认证与上下文压缩。
package chat

import "github.com/charmbracelet/lipgloss"

// chatAnsi* 为 lipgloss 使用的 ANSI 调色板索引。
const (
	chatAnsiRed         = "1"
	chatAnsiGreen       = "2"
	chatAnsiYellow      = "3"
	chatAnsiBlue        = "4"
	chatAnsiCyan        = "6"
	chatAnsiBrightBlack = "8"
)

// 以下 lipgloss 样式定义聊天 UI 各区域的颜色与强调级别。
var (
	chatHeaderStyle = lipgloss.NewStyle().
			Bold(true)

	chatMetaStyle = lipgloss.NewStyle().
			Faint(true)

	chatFooterStyle = lipgloss.NewStyle().
			Faint(true)

	chatInputBorderStyle = lipgloss.NewStyle().
				Faint(true)

	chatInputPlaceholderStyle = lipgloss.NewStyle().
					Foreground(lipgloss.Color("8"))

	chatCursorStyle = lipgloss.NewStyle().
			Reverse(true)

	chatBlankCursorStyle = lipgloss.NewStyle().
				Faint(true)

	chatNotificationStyle = chatMetaStyle

	chatUserStyle = lipgloss.NewStyle()

	chatUserBlockStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "#777777", Dark: "#8a8a8a"})

	chatToolStyle = lipgloss.NewStyle()

	chatInlineCodeStyle = lipgloss.NewStyle().
				Bold(true)

	chatStrongStyle = lipgloss.NewStyle().
			Bold(true)

	chatCodeBlockStyle = lipgloss.NewStyle()

	chatTableBorderStyle = lipgloss.NewStyle().
				Faint(true)

	chatToolRunningStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color(chatAnsiYellow))

	chatToolDoneStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color(chatAnsiGreen))

	// chatToolMixedStyle 表示工具组部分成功部分失败（琥珀色，区别于绿/红/黄）
	// chatToolMixedStyle marks a tool group with both succeeded and failed
	// calls (partial success). Amber/orange is distinct from green (success),
	// red (failure), and yellow (running).
	chatToolMixedStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color("208"))

	chatToolOutputStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "#666666", Dark: "#a0a0a0"})

	chatDiffMetaStyle = lipgloss.NewStyle().
				Faint(true)

	chatDiffFileStyle = lipgloss.NewStyle().
				Bold(true).
				Foreground(lipgloss.Color(chatAnsiCyan))

	chatDiffHunkStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color(chatAnsiBlue))

	chatDiffAddStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color(chatAnsiGreen))

	chatDiffDeleteStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color(chatAnsiRed))

	chatErrorStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color(chatAnsiRed))

	chatFullAccessStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "#9f5f5f", Dark: "#b87373"})

	chatCommandNameStyle = lipgloss.NewStyle()

	chatPickerTextStyle = lipgloss.NewStyle()

	chatPickerTitleStyle = lipgloss.NewStyle().
				Bold(true)

	chatPickerSelectedStyle = lipgloss.NewStyle().
				Bold(true)

	chatPickerMetaStyle = lipgloss.NewStyle().
				Faint(true)

	chatHistoryTitleStyle = lipgloss.NewStyle().
				Bold(true)

	chatHistorySystemRoleStyle = lipgloss.NewStyle().
					Bold(true).
					Faint(true)

	chatHistoryUserRoleStyle = lipgloss.NewStyle().
					Bold(true).
					Foreground(lipgloss.Color(chatAnsiBlue))

	chatHistoryAssistantRoleStyle = lipgloss.NewStyle().
					Bold(true).
					Foreground(lipgloss.Color(chatAnsiYellow))

	chatHistoryToolRoleStyle = lipgloss.NewStyle().
					Bold(true).
					Foreground(lipgloss.Color(chatAnsiGreen))

	chatHistoryLabelStyle = lipgloss.NewStyle().
				Faint(true)

	chatHistoryTextStyle = lipgloss.NewStyle()
)
