// boxes_sections.go — 版面框与 Section 互转：跨页位置标签、Markdown/JSON 导出及排序工具。

package layout

import (
	"sort"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	util "ragflow/internal/deepdoc/parser/pdf/util"
)

// ResolvePageSpan 计算跨页框的结束页与 bottom；pageHeights 无效或 box 不跨页则原样返回；零/负页高视为无效以防死循环。
func ResolvePageSpan(pageNum int, bottom float64, pageHeights map[int]float64) (toPage int, newBottom float64) {
	toPage = pageNum
	newBottom = bottom
	if pageHeights == nil {
		return
	}
	ph, ok := pageHeights[pageNum]
	if !ok || ph <= 0 || bottom <= ph {
		return
	}
	remaining := bottom
	for remaining > ph && ph > 0 {
		nextPh, ok := pageHeights[toPage+1]
		if !ok || nextPh <= 0 {
			// Unknown or invalid next page height — extend by the
			// last known height once and stop (Python: _line_tag
			// while-loop break path).
			remaining -= ph
			toPage++
			break
		}
		remaining -= ph
		ph = nextPh
		toPage++
	}
	newBottom = remaining
	return
}

// BoxesToSections 将 TextBox 转为带位置标签的 Section；跨页框生成多页 position tag（resolvePageSpan）；对齐 naive.py::chunk() 消费格式。
func BoxesToSections(boxes []pdf.TextBox, pageHeights map[int]float64) []pdf.Section {
	sections := make([]pdf.Section, 0, len(boxes))
	for _, b := range boxes {
		t := strings.TrimSpace(b.Text)
		if t == "" {
			continue
		}
		toPage, bottom := ResolvePageSpan(b.PageNumber, b.Bottom, pageHeights)

		var posTag string
		var pageNums []int
		if b.PageNumber == toPage {
			posTag = util.FormatPositionTag(b.PageNumber, b.X0, b.X1, b.Top, bottom)
			pageNums = []int{b.PageNumber}
		} else {
			posTag = util.FormatPositionTagRange(b.PageNumber, toPage, b.X0, b.X1, b.Top, bottom)
			pageNums = make([]int, 0, toPage-b.PageNumber+1)
			for p := b.PageNumber; p <= toPage; p++ {
				pageNums = append(pageNums, p)
			}
		}
		sections = append(sections, pdf.Section{
			Text:        t,
			PositionTag: posTag,
			LayoutType:  b.LayoutType,
			Positions:   []pdf.Position{{PageNumbers: pageNums, Left: b.X0, Right: b.X1, Top: b.Top, Bottom: bottom}},
		})
	}
	return sections
}

// NormalizeSectionPositions 在 Positions 为空时从 PositionTag 解析填充；对齐 Python normalize_pdf_items_metadata；应在 Parse 之后、分块之前调用；不嵌入解析管线因 Section 来源多样。
func NormalizeSectionPositions(sections []pdf.Section) {
	for i := range sections {
		if len(sections[i].Positions) == 0 && sections[i].PositionTag != "" {
			sections[i].Positions = util.ExtractPositions(sections[i].PositionTag)
		}
	}
}

// SortByPageThenY 按页码→纵向键→x0 排序。
func SortByPageThenY(boxes []pdf.TextBox, sortByTop bool) {
	key := func(b pdf.TextBox) float64 { return b.Bottom }
	if sortByTop {
		key = func(b pdf.TextBox) float64 { return b.Top }
	}
	sort.Slice(boxes, func(i, j int) bool {
		if boxes[i].PageNumber != boxes[j].PageNumber {
			return boxes[i].PageNumber < boxes[j].PageNumber
		}
		if key(boxes[i]) != key(boxes[j]) {
			return key(boxes[i]) < key(boxes[j])
		}
		return boxes[i].X0 < boxes[j].X0
	})
}

// SectionsToMarkdown 将 Section 转为 Markdown；标题加 ##，图片嵌 base64；对齐 parser.py:665-671。
func SectionsToMarkdown(sections []pdf.Section) string {
	var b strings.Builder
	for _, s := range sections {
		if s.LayoutType == pdf.LayoutTypeTitle {
			b.WriteString("\n## ")
		}
		if s.LayoutType == pdf.LayoutTypeFigure && s.Image != "" {
			b.WriteString("\n![Image](data:image/png;base64,")
			b.WriteString(s.Image)
			b.WriteString(")")
			continue
		}
		b.WriteString(s.Text)
		b.WriteString("\n")
	}
	return b.String()
}

// SectionsToJSON 转为 Python 兼容的 dict 列表；_pdf_positions 对齐 chunker extract_pdf_positions。
func SectionsToJSON(sections []pdf.Section) []map[string]any {
	result := make([]map[string]any, len(sections))
	for i, s := range sections {
		positions := make([][]any, len(s.Positions))
		for j, p := range s.Positions {
			pages := make([]any, len(p.PageNumbers))
			for k, pn := range p.PageNumbers {
				pages[k] = pn
			}
			positions[j] = []any{pages, p.Left, p.Right, p.Top, p.Bottom}
		}
		result[i] = map[string]any{
			"text":           s.Text,
			"layout_type":    s.LayoutType,
			"doc_type_kwd":   s.DocTypeKwd,
			"_pdf_positions": positions,
			"image":          s.Image,
		}
	}
	return result
}
