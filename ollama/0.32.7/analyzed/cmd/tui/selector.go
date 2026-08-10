package tui

import (
	"fmt"
	"sort"
	"strings"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/ollama/ollama/cmd/launch"
)

var (
	selectorTitleStyle = lipgloss.NewStyle().
				Bold(true)

	selectorItemStyle = lipgloss.NewStyle().
				PaddingLeft(4)

	selectorSelectedItemStyle = lipgloss.NewStyle().
					PaddingLeft(2).
					Bold(true).
					Background(lipgloss.AdaptiveColor{Light: "254", Dark: "236"})

	selectorDescStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"})

	selectorDescLineStyle = selectorDescStyle.
				PaddingLeft(6)

	selectorFilterStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"}).
				Italic(true)

	selectorInputStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "235", Dark: "252"})

	selectorDefaultTagStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"})

	selectorHelpStyle = lipgloss.NewStyle().
				Foreground(lipgloss.AdaptiveColor{Light: "244", Dark: "244"})

	selectorMoreStyle = lipgloss.NewStyle().
				PaddingLeft(6).
				Foreground(lipgloss.AdaptiveColor{Light: "242", Dark: "246"}).
				Italic(true)

	sectionHeaderStyle = lipgloss.NewStyle().
				PaddingLeft(2).
				Bold(true).
				Foreground(lipgloss.AdaptiveColor{Light: "240", Dark: "249"})
)

// selector 提供单选与多选 bubbletea 列表，供 launch 与根 TUI 复用。
const maxSelectorItems = 10

// ErrCancelled 表示用户取消选择操作。
var ErrCancelled = launch.ErrCancelled

// SelectItem 表示选择器中的一行（名称、描述、推荐标记等）。
type SelectItem struct {
	Name              string
	Description       string
	Recommended       bool
	AvailabilityBadge string
}

// SelectorModel 为 selectorModel 的导出别名，供 TUI 模态嵌入。
type SelectorModel = selectorModel

type selectorItemsUpdatedMsg struct {
	items []SelectItem
}

// waitForSelectorItems 将异步列表更新转为 bubbletea 消息。
func waitForSelectorItems(updates <-chan []SelectItem) tea.Cmd {
	if updates == nil {
		return nil
	}
	return func() tea.Msg {
		items, ok := <-updates
		if !ok {
			return nil
		}
		return selectorItemsUpdatedMsg{items: items}
	}
}

// ConvertItems 将 launch.SelectionItem 转为 SelectItem。
func ConvertItems(items []launch.SelectionItem) []SelectItem {
	out := make([]SelectItem, len(items))
	for i, item := range items {
		out[i] = SelectItem{
			Name:              item.Name,
			Description:       item.Description,
			Recommended:       item.Recommended,
			AvailabilityBadge: item.AvailabilityBadge,
		}
	}
	return out
}

// ReorderItems 将推荐项排在前面，组内保持原序，与 Recommended/More 分区一致。
// preserving relative order within each group. This ensures the data order matches
// the visual section layout (Recommended / More).
func ReorderItems(items []SelectItem) []SelectItem {
	var rec, other []SelectItem
	for _, item := range items {
		if item.Recommended {
			rec = append(rec, item)
		} else {
			other = append(other, item)
		}
	}
	return append(rec, other...)
}

// selectorModel 是单选列表的 bubbletea 模型。
type selectorModel struct {
	title        string
	items        []SelectItem
	updates      <-chan []SelectItem
	filter       string
	cursor       int
	scrollOffset int
	selected     string
	cancelled    bool
	helpText     string
	width        int
	rankFiltered bool
}

func selectorModelWithCurrent(title string, items []SelectItem, current string) selectorModel {
	m := selectorModel{
		title:  title,
		items:  items,
		cursor: cursorForCurrent(items, current),
	}
	m.updateScroll(m.otherStart())
	return m
}

// NewSelectorModel 创建带当前项高亮的单选器。
func NewSelectorModel(title string, items []SelectItem, current string) SelectorModel {
	return selectorModelWithCurrent(title, items, current)
}

// NewModelSelectorModel 创建带初始过滤与排序的单选器。
func NewModelSelectorModel(title string, items []SelectItem, current, filter string) SelectorModel {
	m := selectorModelWithCurrent(title, items, current)
	m.filter = strings.TrimSpace(filter)
	m.rankFiltered = true
	if m.filter != "" {
		m.cursor = 0
		m.scrollOffset = 0
	}
	return m
}

func currentItemName(items []SelectItem, cursor int) string {
	if cursor < 0 || cursor >= len(items) {
		return ""
	}
	return items[cursor].Name
}

func indexOfItemName(items []SelectItem, name string) int {
	for i, item := range items {
		if item.Name == name {
			return i
		}
	}
	return -1
}

func cursorForItemName(items []SelectItem, name string, fallback int) int {
	if len(items) == 0 {
		return 0
	}
	if name != "" {
		if i := indexOfItemName(items, name); i >= 0 {
			return i
		}
	}
	if fallback < 0 {
		return 0
	}
	if fallback >= len(items) {
		return len(items) - 1
	}
	return fallback
}

func (m selectorModel) filteredItems() []SelectItem {
	if m.filter == "" {
		return m.items
	}
	filterLower := strings.ToLower(m.filter)
	var result []SelectItem
	for _, item := range m.items {
		if m.rankFiltered {
			if selectItemMatchScore(item, filterLower).ok {
				result = append(result, item)
			}
			continue
		}
		if strings.Contains(strings.ToLower(item.Name), filterLower) {
			result = append(result, item)
		}
	}
	if m.rankFiltered {
		sortSelectItemsForFilter(result, filterLower)
	}
	return result
}

func (m selectorModel) Init() tea.Cmd {
	return waitForSelectorItems(m.updates)
}

// otherStart 返回过滤列表中首个非推荐项索引；过滤模式下为 0。
// When filtering, all items scroll together so this returns 0.
func (m selectorModel) otherStart() int {
	if m.filter != "" {
		return 0
	}
	filtered := m.filteredItems()
	for i, item := range filtered {
		if !item.Recommended {
			return i
		}
	}
	return len(filtered)
}

// updateNavigation 处理上下翻页与过滤输入，不处理 Enter/Esc。
// It does NOT handle Enter, Esc, or CtrlC. This is used by both the standalone
// selector and the TUI modal (which intercepts Enter/Esc for its own logic).
func (m *selectorModel) updateNavigation(msg tea.KeyMsg) {
	filtered := m.filteredItems()
	otherStart := m.otherStart()

	switch msg.Type {
	case tea.KeyUp:
		if m.cursor > 0 {
			m.cursor--
			m.updateScroll(otherStart)
		}

	case tea.KeyDown:
		if m.cursor < len(filtered)-1 {
			m.cursor++
			m.updateScroll(otherStart)
		}

	case tea.KeyPgUp:
		m.cursor -= maxSelectorItems
		if m.cursor < 0 {
			m.cursor = 0
		}
		m.updateScroll(otherStart)

	case tea.KeyPgDown:
		m.cursor += maxSelectorItems
		if m.cursor >= len(filtered) {
			m.cursor = len(filtered) - 1
		}
		m.updateScroll(otherStart)

	case tea.KeyBackspace:
		if len(m.filter) > 0 {
			m.filter = m.filter[:len(m.filter)-1]
			m.cursor = 0
			m.scrollOffset = 0
		}

	case tea.KeyRunes:
		m.filter += string(msg.Runes)
		m.cursor = 0
		m.scrollOffset = 0
	}
}

// UpdateNavigation 供 TUI 模态复用的导航入口。
func (m *selectorModel) UpdateNavigation(msg tea.KeyMsg) {
	m.updateNavigation(msg)
}

// Move 按 delta 移动光标并更新滚动。
func (m *selectorModel) Move(delta int) {
	if delta == 0 {
		return
	}
	filtered := m.filteredItems()
	if len(filtered) == 0 {
		m.cursor = 0
		m.scrollOffset = 0
		return
	}
	m.cursor += delta
	if m.cursor < 0 {
		m.cursor = 0
	}
	if m.cursor >= len(filtered) {
		m.cursor = len(filtered) - 1
	}
	m.updateScroll(m.otherStart())
}

// SetHelpText 覆盖底部帮助文案。
func (m *selectorModel) SetHelpText(help string) {
	m.helpText = help
}

// Filter 返回当前过滤字符串。
func (m selectorModel) Filter() string {
	return m.filter
}

// FilteredItems 返回过滤后的选项副本。
func (m selectorModel) FilteredItems() []SelectItem {
	return append([]SelectItem(nil), m.filteredItems()...)
}

// SelectedItem 返回光标处的选项。
func (m selectorModel) SelectedItem() (SelectItem, bool) {
	filtered := m.filteredItems()
	if len(filtered) == 0 || m.cursor < 0 || m.cursor >= len(filtered) {
		return SelectItem{}, false
	}
	return filtered[m.cursor], true
}

// RenderContent 渲染列表内容，供嵌入模态使用。
func (m selectorModel) RenderContent() string {
	return m.renderContent()
}

// updateScroll 按光标位置调整 scrollOffset；非过滤时 More 区独立滚动。
// When not filtering, scrollOffset is relative to the "More" (non-recommended) section.
// When filtering, it's relative to the full filtered list.
func (m *selectorModel) updateScroll(otherStart int) {
	if m.filter != "" {
		if m.cursor < m.scrollOffset {
			m.scrollOffset = m.cursor
		}
		if m.cursor >= m.scrollOffset+maxSelectorItems {
			m.scrollOffset = m.cursor - maxSelectorItems + 1
		}
		return
	}

	// Cursor is in recommended section — reset "More" scroll to top
	if m.cursor < otherStart {
		m.scrollOffset = 0
		return
	}

	// Cursor is in "More" section — scroll relative to others
	posInOthers := m.cursor - otherStart
	maxOthers := maxSelectorItems - otherStart
	if maxOthers < 3 {
		maxOthers = 3
	}
	if posInOthers < m.scrollOffset {
		m.scrollOffset = posInOthers
	}
	if posInOthers >= m.scrollOffset+maxOthers {
		m.scrollOffset = posInOthers - maxOthers + 1
	}
}

func (m selectorModel) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		wasSet := m.width > 0
		m.width = msg.Width
		if wasSet {
			return m, tea.ClearScreen
		}
		return m, nil

	case selectorItemsUpdatedMsg:
		current := currentItemName(m.filteredItems(), m.cursor)
		m.items = msg.items
		m.cursor = cursorForItemName(m.filteredItems(), current, m.cursor)
		m.updateScroll(m.otherStart())
		return m, waitForSelectorItems(m.updates)

	case tea.KeyMsg:
		switch msg.Type {
		case tea.KeyCtrlC, tea.KeyEsc:
			m.cancelled = true
			return m, tea.Quit

		case tea.KeyLeft:
			m.cancelled = true
			return m, tea.Quit

		case tea.KeyEnter:
			filtered := m.filteredItems()
			if len(filtered) > 0 && m.cursor < len(filtered) {
				m.selected = filtered[m.cursor].Name
			}
			return m, tea.Quit

		default:
			m.updateNavigation(msg)
		}
	}

	return m, nil
}

func cursorItemSuffix(item SelectItem) string {
	if item.AvailabilityBadge == "" {
		return ""
	}
	return " " + selectorDefaultTagStyle.Render("("+item.AvailabilityBadge+")")
}

func (m selectorModel) renderItem(s *strings.Builder, item SelectItem, idx int) {
	if idx == m.cursor {
		s.WriteString(selectorSelectedItemStyle.Render("▸ " + item.Name))
		s.WriteString(cursorItemSuffix(item))
	} else {
		s.WriteString(selectorItemStyle.Render(item.Name))
	}
	s.WriteString("\n")
	if item.Description != "" {
		s.WriteString(selectorDescLineStyle.Render(item.Description))
		s.WriteString("\n")
	}
}

func (m selectorModel) renderCompactItem(s *strings.Builder, item SelectItem, idx int) {
	if idx == m.cursor {
		s.WriteString(selectorSelectedItemStyle.Render("▸ " + item.Name))
		s.WriteString(cursorItemSuffix(item))
	} else {
		s.WriteString(selectorItemStyle.Render(item.Name))
	}
	s.WriteString("\n")
}

// renderContent 渲染标题、列表与帮助，不检查 cancelled/selected 状态。
// checking the cancelled/selected state. This is used by both View() (standalone mode)
// and by the TUI modal which embeds a selectorModel.
func (m selectorModel) renderContent() string {
	var s strings.Builder

	s.WriteString(selectorTitleStyle.Render(m.title))
	s.WriteString(" ")
	if m.filter == "" {
		s.WriteString(selectorFilterStyle.Render("Type to filter..."))
	} else {
		s.WriteString(selectorInputStyle.Render(m.filter))
	}
	s.WriteString("\n\n")

	filtered := m.filteredItems()

	if len(filtered) == 0 {
		s.WriteString(selectorItemStyle.Render(selectorDescStyle.Render("(no matches)")))
		s.WriteString("\n")
	} else if m.filter != "" {
		s.WriteString(sectionHeaderStyle.Render("Top Results"))
		s.WriteString("\n")

		displayCount := min(len(filtered), maxSelectorItems)
		for i := range displayCount {
			idx := m.scrollOffset + i
			if idx >= len(filtered) {
				break
			}
			m.renderItem(&s, filtered[idx], idx)
		}

		if remaining := len(filtered) - m.scrollOffset - displayCount; remaining > 0 {
			s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)))
			s.WriteString("\n")
		}
	} else {
		// 分为固定的推荐区与可滚动的 More 区
		// Split into pinned recommended and scrollable others
		var recItems, otherItems []int
		for i, item := range filtered {
			if item.Recommended {
				recItems = append(recItems, i)
			} else {
				otherItems = append(otherItems, i)
			}
		}

		// Always render all recommended items (pinned)
		if len(recItems) > 0 {
			s.WriteString(sectionHeaderStyle.Render("Recommended"))
			s.WriteString("\n")
			for _, idx := range recItems {
				m.renderItem(&s, filtered[idx], idx)
			}
		}

		if len(otherItems) > 0 {
			s.WriteString("\n")
			s.WriteString(sectionHeaderStyle.Render("More"))
			s.WriteString("\n")

			maxOthers := maxSelectorItems - len(recItems)
			if maxOthers < 3 {
				maxOthers = 3
			}
			displayCount := min(len(otherItems), maxOthers)

			for i := range displayCount {
				idx := m.scrollOffset + i
				if idx >= len(otherItems) {
					break
				}
				m.renderItem(&s, filtered[otherItems[idx]], otherItems[idx])
			}

			if remaining := len(otherItems) - m.scrollOffset - displayCount; remaining > 0 {
				s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)))
				s.WriteString("\n")
			}
		}
	}

	s.WriteString("\n")
	help := "↑/↓ navigate • enter select • ← back"
	if m.helpText != "" {
		help = m.helpText
	}
	s.WriteString(selectorHelpStyle.Render(help))

	return s.String()
}

// RenderCompactContent 渲染紧凑列表，用于嵌套模态。
func (m selectorModel) RenderCompactContent(maxItems int) string {
	var s strings.Builder

	s.WriteString(selectorTitleStyle.Render(m.title))
	s.WriteString(" ")
	if m.filter == "" {
		s.WriteString(selectorFilterStyle.Render("Type to filter..."))
	} else {
		s.WriteString(selectorInputStyle.Render(m.filter))
	}
	s.WriteString("\n")

	filtered := m.filteredItems()
	if len(filtered) == 0 {
		s.WriteString(selectorItemStyle.Render(selectorDescStyle.Render("(no matches)")))
		s.WriteString("\n")
	} else {
		maxItems = max(1, maxItems)
		start := 0
		if len(filtered) > maxItems {
			start = m.cursor - maxItems/2
			if start < 0 {
				start = 0
			}
			if maxStart := len(filtered) - maxItems; start > maxStart {
				start = maxStart
			}
		}
		end := min(len(filtered), start+maxItems)
		if start > 0 {
			s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... %d more above", start)))
			s.WriteString("\n")
		}
		for idx := start; idx < end; idx++ {
			m.renderCompactItem(&s, filtered[idx], idx)
		}
		if remaining := len(filtered) - end; remaining > 0 {
			s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)))
			s.WriteString("\n")
		}
	}

	help := "↑/↓ navigate • enter select • esc cancel"
	if m.helpText != "" {
		help = m.helpText
	}
	s.WriteString(selectorHelpStyle.Render(help))

	return s.String()
}

func (m selectorModel) View() string {
	if m.cancelled || m.selected != "" {
		return ""
	}

	s := m.renderContent()
	if m.width > 0 {
		return lipgloss.NewStyle().MaxWidth(m.width).Render(s)
	}
	return s
}

type selectItemScore struct {
	ok          bool
	rank        int
	index       int
	lengthDelta int
	recommended int
	name        string
}

func sortSelectItemsForFilter(items []SelectItem, filter string) {
	filter = strings.ToLower(strings.TrimSpace(filter))
	sort.SliceStable(items, func(i, j int) bool {
		return compareSelectItemsForFilter(items[i], items[j], filter) < 0
	})
}

func compareSelectItemsForFilter(a, b SelectItem, filter string) int {
	aScore := selectItemMatchScore(a, filter)
	bScore := selectItemMatchScore(b, filter)
	for _, cmp := range []int{
		compareSelectorInt(aScore.rank, bScore.rank),
		compareSelectorInt(aScore.index, bScore.index),
		compareSelectorInt(aScore.lengthDelta, bScore.lengthDelta),
		compareSelectorInt(aScore.recommended, bScore.recommended),
		strings.Compare(aScore.name, bScore.name),
	} {
		if cmp != 0 {
			return cmp
		}
	}
	return 0
}

func selectItemMatchScore(item SelectItem, filter string) selectItemScore {
	filter = strings.ToLower(strings.TrimSpace(filter))
	name := strings.ToLower(strings.TrimSpace(item.Name))
	description := strings.ToLower(strings.TrimSpace(item.Description))
	score := selectItemScore{
		rank:        4,
		index:       1 << 20,
		lengthDelta: 1 << 20,
		name:        name,
	}
	if item.Recommended {
		score.recommended = -1
	}
	if filter == "" {
		score.ok = true
		return score
	}
	nameRunes := len([]rune(name))
	filterRunes := len([]rune(filter))
	if name == filter {
		score.ok = true
		score.rank = 0
		score.index = 0
		score.lengthDelta = 0
		return score
	}
	if strings.HasPrefix(name, filter) {
		score.ok = true
		score.rank = 1
		score.index = 0
		score.lengthDelta = max(0, nameRunes-filterRunes)
		return score
	}
	if index := strings.Index(name, filter); index >= 0 {
		score.ok = true
		score.rank = 2
		score.index = len([]rune(name[:index]))
		score.lengthDelta = max(0, nameRunes-filterRunes)
		return score
	}
	if index := strings.Index(description, filter); index >= 0 {
		score.ok = true
		score.rank = 3
		score.index = len([]rune(description[:index]))
		score.lengthDelta = max(0, nameRunes-filterRunes)
	}
	return score
}

func compareSelectorInt(a, b int) int {
	switch {
	case a < b:
		return -1
	case a > b:
		return 1
	default:
		return 0
	}
}

// cursorForCurrent 匹配 current 名称或 tag 前缀，未找到则返回 0。
func cursorForCurrent(items []SelectItem, current string) int {
	if current == "" {
		return 0
	}

	// Prefer exact name matches before tag-prefix fallback so "qwen3.5" does not
	// incorrectly select "qwen3.5:cloud" (and vice versa) based on list order.
	if i := indexOfItemName(items, current); i >= 0 {
		return i
	}

	for i, item := range items {
		if strings.HasPrefix(item.Name, current+":") || strings.HasPrefix(current, item.Name+":") {
			return i
		}
	}

	return 0
}

// SelectSingle 运行独立单选 TUI 并返回选中名称。
func SelectSingle(title string, items []SelectItem, current string) (string, error) {
	return SelectSingleWithUpdates(title, items, current, nil)
}

// SelectSingleWithUpdates 支持打开期间接收列表刷新的单选 TUI。
func SelectSingleWithUpdates(title string, items []SelectItem, current string, updates <-chan []SelectItem) (string, error) {
	if len(items) == 0 {
		return "", fmt.Errorf("no items to select from")
	}

	m := selectorModelWithCurrent(title, items, current)
	m.updates = updates

	p := tea.NewProgram(m)
	finalModel, err := p.Run()
	if err != nil {
		return "", fmt.Errorf("error running selector: %w", err)
	}

	fm := finalModel.(selectorModel)
	if fm.cancelled {
		return "", ErrCancelled
	}

	return fm.selected, nil
}

// multiSelectorModel 是多选列表的 bubbletea 模型。
type multiSelectorModel struct {
	title        string
	items        []SelectItem
	updates      <-chan []SelectItem
	itemIndex    map[string]int
	filter       string
	cursor       int
	scrollOffset int
	checked      map[int]bool
	checkOrder   []int
	cancelled    bool
	confirmed    bool
	width        int

	// multi enables full multi-select editing mode. The zero value (false)
	// shows a single-select picker where Enter adds the chosen model to
	// the existing list. Tab toggles between modes.
	multi     bool
	singleAdd string // model picked in single mode
}

func newMultiSelectorModel(title string, items []SelectItem, preChecked []string) multiSelectorModel {
	m := multiSelectorModel{
		title:     title,
		items:     items,
		itemIndex: make(map[string]int, len(items)),
		checked:   make(map[int]bool),
	}

	for i, item := range items {
		m.itemIndex[item.Name] = i
	}

	// Reverse order so preChecked[0] (the current default) ends up last
	// in checkOrder, matching the "last checked = default" convention.
	for i := len(preChecked) - 1; i >= 0; i-- {
		if idx, ok := m.itemIndex[preChecked[i]]; ok {
			m.checked[idx] = true
			m.checkOrder = append(m.checkOrder, idx)
		}
	}

	// Position cursor on the current default model
	if len(preChecked) > 0 {
		if idx, ok := m.itemIndex[preChecked[0]]; ok {
			m.cursor = idx
			m.updateScroll(m.otherStart())
		}
	}

	return m
}

func (m *multiSelectorModel) rebuildItemIndex() {
	m.itemIndex = make(map[string]int, len(m.items))
	for i, item := range m.items {
		m.itemIndex[item.Name] = i
	}
}

func (m *multiSelectorModel) replaceItems(items []SelectItem) {
	current := currentItemName(m.filteredItems(), m.cursor)
	checkedNames := make([]string, 0, len(m.checkOrder))
	for _, idx := range m.checkOrder {
		if idx >= 0 && idx < len(m.items) {
			checkedNames = append(checkedNames, m.items[idx].Name)
		}
	}

	m.items = items
	m.rebuildItemIndex()
	m.checked = make(map[int]bool, len(checkedNames))
	m.checkOrder = nil
	for _, name := range checkedNames {
		if idx, ok := m.itemIndex[name]; ok {
			m.checked[idx] = true
			m.checkOrder = append(m.checkOrder, idx)
		}
	}
	m.cursor = cursorForItemName(m.filteredItems(), current, m.cursor)
	m.updateScroll(m.otherStart())
}

func (m multiSelectorModel) filteredItems() []SelectItem {
	if m.filter == "" {
		return m.items
	}
	filterLower := strings.ToLower(m.filter)
	var result []SelectItem
	for _, item := range m.items {
		if strings.Contains(strings.ToLower(item.Name), filterLower) {
			result = append(result, item)
		}
	}
	return result
}

// otherStart returns the index of the first non-recommended item in the filtered list.
func (m multiSelectorModel) otherStart() int {
	if m.filter != "" {
		return 0
	}
	filtered := m.filteredItems()
	for i, item := range filtered {
		if !item.Recommended {
			return i
		}
	}
	return len(filtered)
}

// updateScroll adjusts scrollOffset for section-based scrolling (matches single-select).
func (m *multiSelectorModel) updateScroll(otherStart int) {
	if m.filter != "" {
		if m.cursor < m.scrollOffset {
			m.scrollOffset = m.cursor
		}
		if m.cursor >= m.scrollOffset+maxSelectorItems {
			m.scrollOffset = m.cursor - maxSelectorItems + 1
		}
		return
	}

	if m.cursor < otherStart {
		m.scrollOffset = 0
		return
	}

	posInOthers := m.cursor - otherStart
	maxOthers := maxSelectorItems - otherStart
	if maxOthers < 3 {
		maxOthers = 3
	}
	if posInOthers < m.scrollOffset {
		m.scrollOffset = posInOthers
	}
	if posInOthers >= m.scrollOffset+maxOthers {
		m.scrollOffset = posInOthers - maxOthers + 1
	}
}

func (m *multiSelectorModel) toggleItem() {
	filtered := m.filteredItems()
	if len(filtered) == 0 || m.cursor >= len(filtered) {
		return
	}

	item := filtered[m.cursor]
	origIdx := m.itemIndex[item.Name]

	if m.checked[origIdx] {
		wasDefault := len(m.checkOrder) > 0 && m.checkOrder[len(m.checkOrder)-1] == origIdx
		delete(m.checked, origIdx)
		for i, idx := range m.checkOrder {
			if idx == origIdx {
				m.checkOrder = append(m.checkOrder[:i], m.checkOrder[i+1:]...)
				break
			}
		}
		if wasDefault {
			// When removing the default, pick the nearest checked model above it
			// (or below if none above) so default fallback follows list order.
			newDefault := -1
			for i := origIdx - 1; i >= 0; i-- {
				if m.checked[i] {
					newDefault = i
					break
				}
			}
			if newDefault == -1 {
				for i := origIdx + 1; i < len(m.items); i++ {
					if m.checked[i] {
						newDefault = i
						break
					}
				}
			}
			if newDefault != -1 {
				for i, idx := range m.checkOrder {
					if idx == newDefault {
						m.checkOrder = append(m.checkOrder[:i], m.checkOrder[i+1:]...)
						break
					}
				}
				m.checkOrder = append(m.checkOrder, newDefault)
			}
		}
	} else {
		m.checked[origIdx] = true
		m.checkOrder = append(m.checkOrder, origIdx)
	}
}

func (m multiSelectorModel) selectedCount() int {
	return len(m.checkOrder)
}

func (m multiSelectorModel) Init() tea.Cmd {
	return waitForSelectorItems(m.updates)
}

func (m multiSelectorModel) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		wasSet := m.width > 0
		m.width = msg.Width
		if wasSet {
			return m, tea.ClearScreen
		}
		return m, nil

	case selectorItemsUpdatedMsg:
		m.replaceItems(msg.items)
		return m, waitForSelectorItems(m.updates)

	case tea.KeyMsg:
		filtered := m.filteredItems()

		switch msg.Type {
		case tea.KeyCtrlC, tea.KeyEsc:
			m.cancelled = true
			return m, tea.Quit

		case tea.KeyLeft:
			m.cancelled = true
			return m, tea.Quit

		case tea.KeyTab:
			m.multi = !m.multi

		case tea.KeyEnter:
			if !m.multi {
				if len(filtered) > 0 && m.cursor < len(filtered) {
					m.singleAdd = filtered[m.cursor].Name
					m.confirmed = true
					return m, tea.Quit
				}
			} else if len(m.checkOrder) > 0 {
				m.confirmed = true
				return m, tea.Quit
			}

		case tea.KeySpace:
			if m.multi {
				m.toggleItem()
			}

		case tea.KeyUp:
			if m.cursor > 0 {
				m.cursor--
				m.updateScroll(m.otherStart())
			}

		case tea.KeyDown:
			if m.cursor < len(filtered)-1 {
				m.cursor++
				m.updateScroll(m.otherStart())
			}

		case tea.KeyPgUp:
			m.cursor -= maxSelectorItems
			if m.cursor < 0 {
				m.cursor = 0
			}
			m.updateScroll(m.otherStart())

		case tea.KeyPgDown:
			m.cursor += maxSelectorItems
			if m.cursor >= len(filtered) {
				m.cursor = len(filtered) - 1
			}
			m.updateScroll(m.otherStart())

		case tea.KeyBackspace:
			if len(m.filter) > 0 {
				m.filter = m.filter[:len(m.filter)-1]
				m.cursor = 0
				m.scrollOffset = 0
			}

		case tea.KeyRunes:
			// 部分终端（如 Windows PowerShell）将空格作为 KeyRunes 送达
			// On some terminals (e.g. Windows PowerShell), space arrives as
			// KeyRunes instead of KeySpace. Intercept it so toggle still works.
			if len(msg.Runes) == 1 && msg.Runes[0] == ' ' {
				if m.multi {
					m.toggleItem()
				}
			} else {
				m.filter += string(msg.Runes)
				m.cursor = 0
				m.scrollOffset = 0
			}
		}
	}

	return m, nil
}

func (m multiSelectorModel) renderSingleItem(s *strings.Builder, item SelectItem, idx int) {
	if idx == m.cursor {
		s.WriteString(selectorSelectedItemStyle.Render("▸ " + item.Name))
		s.WriteString(cursorItemSuffix(item))
	} else {
		s.WriteString(selectorItemStyle.Render(item.Name))
	}
	s.WriteString("\n")
	if item.Description != "" {
		s.WriteString(selectorDescLineStyle.Render(item.Description))
		s.WriteString("\n")
	}
}

func (m multiSelectorModel) renderMultiItem(s *strings.Builder, item SelectItem, idx int) {
	origIdx := m.itemIndex[item.Name]

	var check string
	if m.checked[origIdx] {
		check = "[x] "
	} else {
		check = "[ ] "
	}

	suffix := ""
	if len(m.checkOrder) > 0 && m.checkOrder[len(m.checkOrder)-1] == origIdx {
		suffix = " " + selectorDefaultTagStyle.Render("(default)")
	}

	if idx == m.cursor {
		s.WriteString(selectorSelectedItemStyle.Render("▸ " + check + item.Name))
		s.WriteString(cursorItemSuffix(item))
	} else {
		s.WriteString(selectorItemStyle.Render(check + item.Name))
	}
	s.WriteString(suffix)
	s.WriteString("\n")
	if item.Description != "" {
		s.WriteString(selectorDescLineStyle.Render(item.Description))
		s.WriteString("\n")
	}
}

func (m multiSelectorModel) View() string {
	if m.cancelled || m.confirmed {
		return ""
	}

	renderItem := m.renderSingleItem
	if m.multi {
		renderItem = m.renderMultiItem
	}

	var s strings.Builder

	s.WriteString(selectorTitleStyle.Render(m.title))
	s.WriteString(" ")
	if m.filter == "" {
		s.WriteString(selectorFilterStyle.Render("Type to filter..."))
	} else {
		s.WriteString(selectorInputStyle.Render(m.filter))
	}
	s.WriteString("\n\n")

	filtered := m.filteredItems()

	if len(filtered) == 0 {
		s.WriteString(selectorItemStyle.Render(selectorDescStyle.Render("(no matches)")))
		s.WriteString("\n")
	} else if m.filter != "" {
		// Filtering: flat scroll through all matches
		displayCount := min(len(filtered), maxSelectorItems)
		for i := range displayCount {
			idx := m.scrollOffset + i
			if idx >= len(filtered) {
				break
			}
			renderItem(&s, filtered[idx], idx)
		}

		if remaining := len(filtered) - m.scrollOffset - displayCount; remaining > 0 {
			s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)))
			s.WriteString("\n")
		}
	} else {
		// Split into pinned recommended and scrollable others (matches single-select layout)
		var recItems, otherItems []int
		for i, item := range filtered {
			if item.Recommended {
				recItems = append(recItems, i)
			} else {
				otherItems = append(otherItems, i)
			}
		}

		// Always render all recommended items (pinned)
		if len(recItems) > 0 {
			s.WriteString(sectionHeaderStyle.Render("Recommended"))
			s.WriteString("\n")
			for _, idx := range recItems {
				renderItem(&s, filtered[idx], idx)
			}
		}

		if len(otherItems) > 0 {
			s.WriteString("\n")
			s.WriteString(sectionHeaderStyle.Render("More"))
			s.WriteString("\n")

			maxOthers := maxSelectorItems - len(recItems)
			if maxOthers < 3 {
				maxOthers = 3
			}
			displayCount := min(len(otherItems), maxOthers)

			for i := range displayCount {
				idx := m.scrollOffset + i
				if idx >= len(otherItems) {
					break
				}
				renderItem(&s, filtered[otherItems[idx]], otherItems[idx])
			}

			if remaining := len(otherItems) - m.scrollOffset - displayCount; remaining > 0 {
				s.WriteString(selectorMoreStyle.Render(fmt.Sprintf("... and %d more", remaining)))
				s.WriteString("\n")
			}
		}
	}

	s.WriteString("\n")

	count := m.selectedCount()
	if !m.multi {
		if count > 0 {
			s.WriteString(sectionHeaderStyle.Render(fmt.Sprintf("%d models selected - press tab to edit", count)))
			s.WriteString("\n\n")
		}
		s.WriteString(selectorHelpStyle.Render("↑/↓ navigate • enter select • tab add multiple • ← back"))
	} else {
		if count == 0 {
			s.WriteString(sectionHeaderStyle.Render("Select at least one model."))
		} else {
			s.WriteString(sectionHeaderStyle.Render(fmt.Sprintf("%d models selected - press enter to continue", count)))
		}
		s.WriteString("\n\n")
		s.WriteString(selectorHelpStyle.Render("↑/↓ navigate • space toggle • tab select single • enter confirm • ← back"))
	}

	result := s.String()
	if m.width > 0 {
		return lipgloss.NewStyle().MaxWidth(m.width).Render(result)
	}
	return result
}

// SelectMultiple 运行多选 TUI；最后一项勾选为默认模型。
func SelectMultiple(title string, items []SelectItem, preChecked []string) ([]string, error) {
	return SelectMultipleWithUpdates(title, items, preChecked, nil)
}

// SelectMultipleWithUpdates 支持 live updates 的多选 TUI。
func SelectMultipleWithUpdates(title string, items []SelectItem, preChecked []string, updates <-chan []SelectItem) ([]string, error) {
	if len(items) == 0 {
		return nil, fmt.Errorf("no items to select from")
	}

	m := newMultiSelectorModel(title, items, preChecked)
	m.updates = updates

	p := tea.NewProgram(m)
	finalModel, err := p.Run()
	if err != nil {
		return nil, fmt.Errorf("error running selector: %w", err)
	}

	fm := finalModel.(multiSelectorModel)
	if fm.cancelled || !fm.confirmed {
		return nil, ErrCancelled
	}

	// Single-add mode: prepend the picked model, keep existing models deduped
	if fm.singleAdd != "" {
		result := []string{fm.singleAdd}
		for _, name := range preChecked {
			if name != fm.singleAdd {
				result = append(result, name)
			}
		}
		return result, nil
	}

	// Multi-edit mode: last checked is default (first in result)
	last := fm.checkOrder[len(fm.checkOrder)-1]
	result := []string{fm.items[last].Name}
	for _, idx := range fm.checkOrder {
		if idx != last {
			result = append(result, fm.items[idx].Name)
		}
	}
	return result, nil
}
