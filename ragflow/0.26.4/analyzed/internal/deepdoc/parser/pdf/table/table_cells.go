// table_cells.go — TSR 单元格分组、文本填充与内容类型分类：按 Y 邻近度分行、从文本框填充单元格文字、识别 caption 与 blockType 表头检测。

package table

import (
	"log/slog"
	"math"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	"ragflow/internal/deepdoc/parser/pdf/util"
	"regexp"
	"sort"
	"strings"
)

// ── TSR 单元格分组 ──

// GroupTSRCellsToRows 按 Y 坐标邻近度将 TSR 单元格分组为行。无模型专用分组（如 EE 标签感知）时的基础回退策略。
func GroupTSRCellsToRows(cells []pdf.TSRCell) [][]pdf.TSRCell {
	if len(cells) == 0 {
		return nil
	}
	if len(cells) == 1 {
		return [][]pdf.TSRCell{{cells[0]}}
	}
	heights := make([]float64, len(cells))
	for i, c := range cells {
		heights[i] = c.Y1 - c.Y0
	}
	sort.Float64s(heights)
	medianH := heights[len(heights)/2]
	if medianH <= 0 {
		medianH = 10
	}
	rowThreshold := medianH * 0.5

	sort.Slice(cells, func(i, j int) bool {
		if math.Abs(cells[i].Y0-cells[j].Y0) < rowThreshold {
			return cells[i].X0 < cells[j].X0
		}
		return cells[i].Y0 < cells[j].Y0
	})

	var rows [][]pdf.TSRCell
	var curRow []pdf.TSRCell
	curY := 0.0
	for _, c := range cells {
		if len(curRow) == 0 {
			curRow = append(curRow, c)
			curY = c.Y0
			continue
		}
		if c.Y0-curY > rowThreshold {
			rows = append(rows, curRow)
			curRow = []pdf.TSRCell{c}
			curY = c.Y0
		} else {
			curRow = append(curRow, c)
		}
	}
	if len(curRow) > 0 {
		rows = append(rows, curRow)
	}
	for _, row := range rows {
		sort.Slice(row, func(i, j int) bool { return row[i].X0 < row[j].X0 })
	}
	return rows
}

// ── 单元格文本填充 ──

// FillCellTextFromBoxes 用重叠文本框文字填充 TSR 单元格，跳过 caption 框。
func FillCellTextFromBoxes(cells []pdf.TSRCell, boxes []pdf.TextBox) {
	slog.Debug("fillCellTextFromBoxes", "cells", len(cells), "boxes", len(boxes))
	if len(cells) > 0 && len(boxes) > 0 {
		c0 := cells[0]
		slog.Debug("fillCellTextFromBoxes cell[0]", "x0", c0.X0, "y0", c0.Y0, "x1", c0.X1, "y1", c0.Y1)
		b0 := boxes[0]
		slog.Debug("fillCellTextFromBoxes box[0]", "x0", b0.X0, "y0", b0.Top, "x1", b0.X1, "y1", b0.Bottom, "text_len", len(b0.Text))
	}
	matched, filled := 0, 0
	for ci := range cells {
		var matches []string
		for _, b := range boxes {
			if IsCaptionBox(b.Text, b.LayoutType) {
				continue
			}
			if BoxMatchesCell(cells[ci], b, cells[ci].Text == "") {
				matched++
				t := strings.TrimSpace(b.Text)
				if t != "" {
					matches = append(matches, t)
				}
			}
		}
		if len(matches) > 0 {
			cells[ci].Text = strings.Join(matches, " ")
			filled++
		}
	}
	slog.Debug("fillCellTextFromBoxes done", "cell_box_matches", matched, "cells_filled", filled)
}

// BoxMatchesCell 判断文本框文字是否应归入 TSR 单元格。单元格已有文字时要求框≥85% 在单元格内；空单元格时重叠≥30% 即可，对齐 Python thr=0.3。
func BoxMatchesCell(cell pdf.TSRCell, box pdf.TextBox, cellIsEmpty bool) bool {
	inter := util.OverlapInter(&cell, &box)
	boxArea := util.Area(&box)
	if boxArea <= 0 {
		return false
	}
	if cellIsEmpty {
		return inter/boxArea >= 0.3 // Python find_overlapped_with_threshold 默认阈值
	}
	return inter/boxArea >= 0.85
}

// IsCaptionBox 判断文本框是否为表格/图片 caption，caption 不应进入表格单元格，对齐 Python is_caption。
var reCaption = regexp.MustCompile(`^[图表]+[ 0-9:：]{2,}|(?i)Fig\.?\s*\d+|(?i)Figure\s+\d+|(?i)Table\s+\d+`)

func IsCaptionBox(text string, layoutType string) bool {
	if strings.Contains(layoutType, "caption") {
		return true
	}
	return reCaption.MatchString(strings.TrimSpace(text))
}

// reTableCaptionText 匹配表格 caption 文本模式（非 figure caption）。
var reTableCaptionText = regexp.MustCompile(`^表|(?i)Table\s+\d+`)

// reFigureCaptionText 匹配图片 caption 文本模式。
var reFigureCaptionText = regexp.MustCompile(`^图|(?i)Fig\.?\s*\d+|(?i)Figure\s+\d+`)

// CaptionKind 返回 section 的 caption 类型：table、figure 或空（非 caption）。结合 layout_type 与文本模式，对齐 Python is_caption。
func CaptionKind(s pdf.Section) string {
	lt := s.LayoutType
	if lt == pdf.DLALabelTableCaption || (strings.Contains(lt, "caption") && reTableCaptionText.MatchString(strings.TrimSpace(s.Text))) {
		return pdf.LayoutTypeTable
	}
	if lt == pdf.DLALabelFigureCaption || strings.Contains(lt, "caption") {
		return pdf.LayoutTypeFigure
	}
	// DLA 可能将 caption 标为 text 等类型 — 再用文本模式判定。
	t := strings.TrimSpace(s.Text)
	if reTableCaptionText.MatchString(t) {
		return pdf.LayoutTypeTable
	}
	if reFigureCaptionText.MatchString(t) {
		return pdf.LayoutTypeFigure
	}
	// 「图表」模式可能为表或图 — 再用 IsCaptionBox 判定。
	if IsCaptionBox(t, "") {
		return pdf.LayoutTypeTable
	}
	return ""
}

// ── blockType：单元格内容分类（Python TableStructureRecognizer.blockType）──

// 包初始化时编译正则，仅执行一次。
var blockTypePatterns = []struct {
	re   *regexp.Regexp
	kind string
}{
	// Dt（日期）模式 — Python blockType 161-168 行。
	{regexp.MustCompile(`^(20|19)[0-9]{2}[年/-][0-9]{1,2}[月/-][0-9]{1,2}日*$`), "Dt"},
	{regexp.MustCompile(`^(20|19)[0-9]{2}年$`), "Dt"},
	{regexp.MustCompile(`^(20|19)[0-9]{2}[年-][0-9]{1,2}月*$`), "Dt"},
	{regexp.MustCompile(`^[0-9]{1,2}[月-][0-9]{1,2}日*$`), "Dt"},
	{regexp.MustCompile(`^第*[一二三四1-4]季度$`), "Dt"},
	{regexp.MustCompile(`^(20|19)[0-9]{2}年*[一二三四1-4]季度$`), "Dt"},
	{regexp.MustCompile(`^(20|19)[0-9]{2}[ABCDE]$`), "Dt"},
	// Nu（数值）— Python blockType 169 行。
	{regexp.MustCompile(`^[0-9.,+%/ -]+$`), "Nu"},
	// Ca（类别码）— Python blockType 170 行。
	{regexp.MustCompile(`^[0-9A-Z/\._~-]+$`), "Ca"},
	// En（英文）— Python blockType 171 行。
	{regexp.MustCompile(`^[A-Z]*[a-z' -]+$`), "En"},
	// NE（命名实体/混合 alphanumeric）— Python 172 行。
	{regexp.MustCompile(`^[0-9.,+-]+[0-9A-Za-z/$￥%<>（）()' -]+$`), "NE"},
	// Sg（单字符）— Python blockType 173 行。
	{regexp.MustCompile(`^.{1}$`), "Sg"},
}

// BlockType 将单元格文本分为 9+1 类：Dt/Nu/Ca/En/NE/Sg/Tx/Lx/Nr/Ot，对齐 Python blockType。
func BlockType(text string) string {
	t := strings.TrimSpace(text)
	for _, p := range blockTypePatterns {
		if p.re.MatchString(t) {
			return p.kind
		}
	}
	// 基于 token 数：>3 且 <12 为 Tx，≥12 为 Lx。
	// 使用简单 token 计数（空白分词 + 每个汉字单独计 token）。
	tkn := simpleTokenCount(t)
	if tkn > 3 {
		if tkn < 12 {
			return "Tx"
		}
		return "Lx"
	}
	// 单 token 且词性 nr → Nr（需分词器，当前不可用）。
	// 默认 Ot（其他）。
	return "Ot"
}

// simpleTokenCount 估算 token 数：空白分词，每个 CJK 字符单独计为一个 token。
// simpleTokenCount 估算中英文混合文本的 token 数量。
func simpleTokenCount(text string) int {
	count := 0
	for _, r := range text {
		if pdf.IsCJK(r) {
			count++
		} else if r == ' ' || r == '\t' {
			// 空白仅作分词边界，非 CJK 词已通过 Fields 计数
		}
	}
	// 另计空白分隔的非 CJK 词。
	words := strings.Fields(text)
	for _, w := range words {
		if !containsCJK(w) {
			count++
		}
	}
	return count
}

// containsCJK 判断字符串是否含 CJK 字符。
func containsCJK(s string) bool {
	for _, r := range s {
		if pdf.IsCJK(r) {
			return true
		}
	}
	return false
}

// HeaderSetWithBlockType 结合 TSR 标签与 blockType 判定表头行，对齐 Python construct_table 表头检测。
func HeaderSetWithBlockType(rows [][]pdf.TSRCell) map[int]bool {
	// 统计全表 dominant blockType。
	typeCounts := make(map[string]int)
	for _, row := range rows {
		for _, cell := range row {
			t := strings.TrimSpace(cell.Text)
			if t != "" {
				typeCounts[BlockType(t)]++
			}
		}
	}
	maxType := ""
	maxCount := 0
	for t, c := range typeCounts {
		if c > maxCount {
			maxType = t
			maxCount = c
		}
	}

	hdrs := make(map[int]bool)
	for ri, row := range rows {
		cnt, h := 0, 0
		for _, cell := range row {
			t := strings.TrimSpace(cell.Text)
			if t == "" {
				continue
			}
			cnt++
			bt := BlockType(t)
			// Python：dominant 为 Nu 且单元格也是 Nu → 跳过
			if maxType == "Nu" && bt == "Nu" {
				continue
			}
			// Python：dominant 为 Nu 且单元格非 Nu → 计为表头
			if maxType == "Nu" && bt != "Nu" {
				h++
			}
		}
		if cnt > 0 && float64(h)/float64(cnt) > 0.5 {
			hdrs[ri] = true
		}
	}
	// 回退：blockType 未找到表头时，检查 cell.Label 是否含 header 子串（跨 TSR 模型通用）。
	if len(hdrs) == 0 {
		for ri, row := range rows {
			for _, cell := range row {
				if strings.Contains(cell.Label, "header") || strings.Contains(cell.Label, "Header") {
					hdrs[ri] = true
					break
				}
			}
		}
	}
	return hdrs
}
