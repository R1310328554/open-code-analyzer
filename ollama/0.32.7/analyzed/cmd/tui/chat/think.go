package chat

import (
	"fmt"
	"strings"

	tea "github.com/charmbracelet/bubbletea"

	"github.com/ollama/ollama/api"
)

// think 为聊天 TUI 提供 /think 命令与思考模式选择器。
// chatThinkOption 表示思考模式列表中的一项。
type chatThinkOption struct {
	value       string
	label       string
	description string
}

// chatThinkPicker 是 bubbletea 思考模式单选器状态。
type chatThinkPicker struct {
	options []chatThinkOption
	cursor  int
}

// chatThinkOptions 为 /think 命令与选择器共用的预设选项。
var chatThinkOptions = []chatThinkOption{
	{value: "auto", label: "auto", description: "use the model default"},
	{value: "on", label: "on", description: "enable thinking"},
	{value: "off", label: "off", description: "disable thinking"},
	{value: "low", label: "low", description: "use low thinking effort"},
	{value: "medium", label: "medium", description: "use medium thinking effort"},
	{value: "high", label: "high", description: "use high thinking effort"},
	{value: "max", label: "max", description: "use maximum thinking effort"},
}

// openThinkPicker 打开思考模式选择器并将状态设为 think。
func (m *chatModel) openThinkPicker() (tea.Model, tea.Cmd) {
	m.thinkPicker = newChatThinkPicker(m.opts.Think)
	m.status = "think"
	return *m, nil
}

// newChatThinkPicker 根据当前 Think 值初始化光标位置。
func newChatThinkPicker(current *api.ThinkValue) *chatThinkPicker {
	picker := &chatThinkPicker{options: append([]chatThinkOption(nil), chatThinkOptions...)}
	currentValue := thinkValueLabel(current)
	for i, option := range picker.options {
		if option.value == currentValue {
			picker.cursor = i
			break
		}
	}
	return picker
}

// updateThinkPicker 处理选择器内的键盘输入。
func (m chatModel) updateThinkPicker(msg tea.KeyMsg) (tea.Model, tea.Cmd) {
	switch msg.Type {
	case tea.KeyCtrlC, tea.KeyEsc:
		m.thinkPicker = nil
		m.status = "ready"
	case tea.KeyEnter:
		return m.selectThinkOption()
	case tea.KeyUp:
		m.thinkPicker.move(-1)
	case tea.KeyDown:
		m.thinkPicker.move(1)
	}
	return m, nil
}

// move 在选项列表中移动光标。
func (p *chatThinkPicker) move(delta int) {
	if p == nil || len(p.options) == 0 || delta == 0 {
		return
	}
	p.cursor = clamp(p.cursor+delta, 0, len(p.options)-1)
}

// selected 返回当前高亮选项。
func (p *chatThinkPicker) selected() (chatThinkOption, bool) {
	if p == nil || len(p.options) == 0 {
		return chatThinkOption{}, false
	}
	return p.options[clamp(p.cursor, 0, len(p.options)-1)], true
}

// selectThinkOption 应用用户选中的思考模式。
func (m chatModel) selectThinkOption() (tea.Model, tea.Cmd) {
	option, ok := m.thinkPicker.selected()
	if !ok {
		return m, nil
	}
	m.thinkPicker = nil
	return m.applyThinkValue(option.value)
}

// handleThinkCommand 处理 /think 斜杠命令。
func (m *chatModel) handleThinkCommand(value string) (tea.Model, tea.Cmd) {
	return m.applyThinkValue(value)
}

// applyThinkValue 解析并写入 opts.Think，更新状态栏标签。
func (m *chatModel) applyThinkValue(value string) (tea.Model, tea.Cmd) {
	think, label, err := parseThinkValue(value)
	if err != nil {
		m.entries = append(m.entries, newChatEntry(chatEntry{role: "error", content: err.Error(), err: err.Error()}))
		m.status = "error"
		return *m, nil
	}
	m.opts.Think = think
	m.status = "think " + label
	return *m, nil
}

// parseThinkValue 将用户输入解析为 api.ThinkValue 与显示标签。
func parseThinkValue(value string) (*api.ThinkValue, string, error) {
	switch strings.ToLower(strings.TrimSpace(value)) {
	case "", "auto", "default", "unset":
		return nil, "auto", nil
	case "on", "true", "think", "thinking":
		return &api.ThinkValue{Value: true}, "on", nil
	case "off", "false", "nothink", "no-think":
		return &api.ThinkValue{Value: false}, "off", nil
	case "low", "medium", "high", "max":
		value = strings.ToLower(strings.TrimSpace(value))
		return &api.ThinkValue{Value: value}, value, nil
	default:
		return nil, "", fmt.Errorf("Usage: /think [auto|on|off|low|medium|high|max]")
	}
}

// thinkValueLabel 将 ThinkValue 转为选择器用的字符串标签。
func thinkValueLabel(value *api.ThinkValue) string {
	if value == nil || value.Value == nil {
		return "auto"
	}
	switch v := value.Value.(type) {
	case bool:
		if v {
			return "on"
		}
		return "off"
	case string:
		return strings.ToLower(v)
	default:
		return "auto"
	}
}

// renderThinkPicker 渲染思考模式选择器界面。
func (m chatModel) renderThinkPicker(width int) string {
	picker := m.thinkPicker
	if picker == nil {
		return ""
	}

	var b strings.Builder
	b.WriteString(chatPickerTitleStyle.Render("Thinking mode"))
	b.WriteString("\n\n")
	for i, option := range picker.options {
		selected := i == picker.cursor
		if selected {
			b.WriteString(chatPickerSelectedStyle.Render("› " + option.label))
		} else {
			b.WriteString("  ")
			b.WriteString(chatPickerTextStyle.Render(option.label))
		}
		b.WriteByte('\n')
		b.WriteString(chatPickerMetaStyle.Render("  " + option.description))
		b.WriteByte('\n')
		if i < len(picker.options)-1 {
			b.WriteByte('\n')
		}
	}

	b.WriteString("\n")
	b.WriteString(chatPickerMetaStyle.Render("↑/↓ navigate • enter select • esc cancel"))
	return b.String()
}
