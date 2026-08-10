// chars_boxes.go — 字符级聚合：按行分组、列间隙切分与 TextBox 构建，对齐 Python pdf_parser.__images__。

package layout

import (
	"math"
	"regexp"
	"sort"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	util "ragflow/internal/deepdoc/parser/pdf/util"
)

// CharsToBoxes 按垂直重叠将字符聚合成行再切列，生成初始 TextBox；对齐 pdf_parser.__images__。
func CharsToBoxes(chars []pdf.TextChar, pageNum int, sortByTop bool) []pdf.TextBox {
	if len(chars) == 0 {
		return nil
	}

	lines := GroupCharsToLines(chars, sortByTop)

	// Page-level column gap threshold from ALL inter-char gaps.
	// Falls back to per-line threshold when page has too few gaps.
	threshold := pageXGapThreshold(lines)

	boxes := make([]pdf.TextBox, 0, len(lines))
	for _, line := range lines {
		thr := threshold
		if thr > 100 {
			// 页级无显著列间隙 → 改用行内阈值。
			thr = perLineXGapThreshold(line)
		}
		subLines := splitLineByXGap(line, thr)
		for _, sub := range subLines {
			box := LineToTextBox(sub)
			box.PageNumber = pageNum
			boxes = append(boxes, box)
		}
	}
	return boxes
}

// perLineXGapThreshold 单行内列切分动态 X 间隙阈值（页级回退）。
func perLineXGapThreshold(chars []pdf.TextChar) float64 {
	if len(chars) <= 1 {
		return 1e9
	}
	var gaps []float64
	for i := 1; i < len(chars); i++ {
		g := chars[i].X0 - chars[i-1].X1
		gaps = append(gaps, g)
	}
	if len(gaps) == 0 {
		return 1e9
	}
	sort.Float64s(gaps)
	medianGap := gaps[len(gaps)/2]
	if medianGap < 6 {
		medianGap = 6
	}
	return medianGap * 2.5
}

// pageXGapThreshold 全页字符间隙 95 分位作为列边界阈值；间隙不足时返回 >100 触发 perLine 回退。
func pageXGapThreshold(lines [][]pdf.TextChar) float64 {
	var allGaps []float64
	for _, line := range lines {
		for i := 1; i < len(line); i++ {
			g := line[i].X0 - line[i-1].X1
			allGaps = append(allGaps, g)
		}
	}
	if len(allGaps) < 10 {
		return 1e9 // too few gaps for reliable p95 → fall back to per-line
	}
	sort.Float64s(allGaps)
	// 95 分位：仅最大 5% 间隙视为列边界。
	p95 := allGaps[len(allGaps)*95/100]
	if p95 < 30 {
		p95 = 30 // 下限 30pt：实际列间隙通常不小于此值
	}
	return p95
}

// splitLineByXGap 按 X 间隙≥阈值切分行内子行（列边界）；≥ 含等于 p95 边界值。
func splitLineByXGap(chars []pdf.TextChar, threshold float64) [][]pdf.TextChar {
	if len(chars) <= 1 {
		return [][]pdf.TextChar{chars}
	}
	var result [][]pdf.TextChar
	start := 0
	for i := 1; i < len(chars); i++ {
		gap := chars[i].X0 - chars[i-1].X1
		if gap >= threshold {
			result = append(result, chars[start:i])
			start = i
		}
	}
	result = append(result, chars[start:])
	return result
}

// ---- 内部辅助 ----

// GroupCharsToLines 按垂直重叠将字符分组为水平行。
func GroupCharsToLines(chars []pdf.TextChar, sortByTop bool) [][]pdf.TextChar {
	if len(chars) == 0 {
		return nil
	}

	key := func(c pdf.TextChar) float64 { return c.Bottom }
	if sortByTop {
		key = func(c pdf.TextChar) float64 { return c.Top }
	}

	// Sort by vertical key (Bottom or Top) then x0 using sort.SliceStable.
	// Guard against NaN: a NaN key sorts after everything else.
	sort.SliceStable(chars, func(i, j int) bool {
		ki, kj := key(chars[i]), key(chars[j])
		if ki != kj && !math.IsNaN(ki) && !math.IsNaN(kj) {
			return ki < kj
		}
		if math.IsNaN(ki) != math.IsNaN(kj) {
			return !math.IsNaN(ki) // non-NaN before NaN
		}
		return chars[i].X0 < chars[j].X0
	})

	var lines [][]pdf.TextChar
	var currentLine []pdf.TextChar

	for _, c := range chars {
		if len(currentLine) == 0 {
			currentLine = append(currentLine, c)
			continue
		}
		if verticalOverlap(currentLine[len(currentLine)-1], c) {
			currentLine = append(currentLine, c)
		} else {
			if len(currentLine) > 0 {
				lines = append(lines, currentLine)
			}
			currentLine = []pdf.TextChar{c}
		}
	}
	if len(currentLine) > 0 {
		lines = append(lines, currentLine)
	}
	return lines
}

// verticalOverlap 判断两字符是否同一水平行（Top 差 < 半行高）。
func verticalOverlap(a, b pdf.TextChar) bool {
	mh := math.Max(util.CharHeight(a), util.CharHeight(b))
	if mh <= 0 {
		mh = 1.0
	}
	return math.Abs(a.Top-b.Top) < mh*0.5
}

// LineToTextBox 将一行字符合并为 TextBox，扩展 bbox 并在 ASCII 词间按需插空格；对齐 pdf_parser.py:1524-1532。
var asciiWordPattern = regexp.MustCompile(`^[0-9a-zA-Z,.:;!%]+$`)

func LineToTextBox(chars []pdf.TextChar) pdf.TextBox {
	if len(chars) == 0 {
		return pdf.TextBox{}
	}
	box := pdf.TextBox{
		X0:     chars[0].X0,
		X1:     chars[0].X1,
		Top:    chars[0].Top,
		Bottom: chars[0].Bottom,
	}
	var textParts []string
	for i, c := range chars {
		// Insert space between adjacent ASCII words with a visible gap.
		// Python: pdf_parser.py:1524-1532 __img_ocr space insertion.
		if i > 0 {
			prev := chars[i-1]
			prevText := strings.TrimSpace(prev.Text)
			currText := strings.TrimSpace(c.Text)
			if prevText != "" && currText != "" {
				gap := c.X0 - prev.X1
				minWidth := math.Min(c.X1-c.X0, prev.X1-prev.X0)
				if gap >= minWidth/2 &&
					asciiWordPattern.MatchString(prevText+currText) {
					textParts = append(textParts, " ")
				}
			}
		}
		box.X0 = math.Min(box.X0, c.X0)
		box.X1 = math.Max(box.X1, c.X1)
		box.Top = math.Min(box.Top, c.Top)
		box.Bottom = math.Max(box.Bottom, c.Bottom)
		textParts = append(textParts, c.Text)
		if c.LayoutType != "" {
			box.LayoutType = c.LayoutType
		}
		if c.LayoutNo != "" {
			box.LayoutNo = c.LayoutNo
		}
	}
	box.Text = strings.Join(textParts, "")
	return box
}
