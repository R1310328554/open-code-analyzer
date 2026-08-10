// table_construct.go — 表格 HTML 构造与后处理：从 TSR 单元格/注释框生成 HTML、清理 orphan 列、剥离 caption 与数据来源行。

package table

import (
	"math"
	"regexp"
	"sort"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// ConstructTable 从 TSR 单元格与文本框生成 HTML 表格（须同一 crop 像素坐标系）。填充 item.Rows 供下游使用。StripCaptionFromCells 清除 caption 类文字（含 fillCellTextFromBoxes 漏网项）。对齐 Python TableStructureRecognizer.construct_table。
func StripCaptionFromCells(cells []pdf.TSRCell) {
	for i := range cells {
		t := strings.TrimSpace(cells[i].Text)
		if t == "" {
			continue
		}
		// 清除匹配 caption 模式（如「表1」「Table 1」）的单元格文字。
		if IsCaptionBox(t, "") {
			cells[i].Text = ""
		}
	}
	// 第二遍：首行若全为序号类文字可能是 caption 编号行 — 保守清除，不误伤真实数据。
}

func ConstructTable(cells []pdf.TSRCell, boxes []pdf.TextBox, caption string, item *pdf.TableItem) string {
	// 深度防御：剥离单元格中 caption 类文字。
	StripCaptionFromCells(cells)

	// 优先使用 TableBuilder.GroupCells 预计算的 grid；测试无 grid 时回退 GroupTSRCellsToRows。
	var rows [][]pdf.TSRCell
	if item != nil {
		rows = item.Grid
	}
	if rows == nil && len(cells) > 0 && HasAnyText(cells) {
		rows = GroupTSRCellsToRows(cells)
	}
	if len(rows) > 0 && HasText(rows) {
		hdrs := HeaderSetWithBlockType(rows)
		if item != nil {
			item.Rows = RowsToStrings(rows)
		}
		rows = CleanupOrphanColumns(rows)
		spanInfo, covered := CalSpans(rows)
		return RowsToHTML(rows, caption, hdrs, spanInfo, covered)
	}
	// 回退：使用带 R/C 注释的文本框分组。
	if len(boxes) > 0 && BoxesHaveAnnotations(boxes) {
		rows := GroupBoxesByRC(boxes)
		if HasText(rows) {
			if item != nil {
				item.Rows = RowsToStrings(rows)
			}
			spanInfo, covered := CalSpans(rows)
			return RowsToHTML(rows, caption, BoxHeaderSet(rows, boxes), spanInfo, covered)
		}
	}
	// 仅测试：按 Y/X 坐标分组（table_parity_test.go 与 Python 框对齐验证）。
	if len(boxes) > 0 && !BoxesHaveAnnotations(boxes) {
		rows := GroupBoxesByYX(boxes)
		if HasText(rows) {
			if item != nil {
				item.Rows = RowsToStrings(rows)
			}
			spanInfo, covered := CalSpans(rows)
			return RowsToHTML(rows, caption, BoxHeaderSet(rows, boxes), spanInfo, covered)
		}
	}
	return ""
}

// BoxHeaderSet 返回含 H 注释框所在的行索引集合。
func BoxHeaderSet(rows [][]pdf.TSRCell, boxes []pdf.TextBox) map[int]bool {
	hdrs := make(map[int]bool)
	for _, b := range boxes {
		if b.H > 0 && b.R >= 0 && b.R < len(rows) {
			hdrs[b.R] = true
		}
	}
	return hdrs
}

// FillCellTextFromAnnotations 按 R/C 注释（非空间重叠）将文本框文字填入单元格，对齐 Python construct_table。
func FillCellTextFromAnnotations(rows [][]pdf.TSRCell, boxes []pdf.TextBox) {
	// 构建 R→(C→text) 映射：行索引 → 列索引 → 文本列表。
	rBoxes := make(map[int]map[int][]string)
	for _, b := range boxes {
		if b.Text == "" {
			continue
		}
		if rBoxes[b.R] == nil {
			rBoxes[b.R] = make(map[int][]string)
		}
		rBoxes[b.R][b.C] = append(rBoxes[b.R][b.C], b.Text)
	}
	// 按 R/C 位置填充各单元格。
	for ri, row := range rows {
		colMap := rBoxes[ri]
		if colMap == nil {
			continue
		}
		// 构建排序后的列列表以便按位置匹配。
		type colEntry struct {
			c     int
			texts []string
		}
		var cols []colEntry
		for c, texts := range colMap {
			cols = append(cols, colEntry{c, texts})
		}
		sort.Slice(cols, func(i, j int) bool {
			return cols[i].c < cols[j].c
		})
		for ci, col := range cols {
			if ci < len(row) {
				row[ci].Text = strings.TrimSpace(strings.Join(col.texts, " "))
			}
		}
	}
}

// dataSourceRe 匹配应丢弃的数据来源 attribution 行，对齐 Python pdf_parser.py:1040-1052。
var dataSourceRe = regexp.MustCompile(`^(数据|资料|图表)*来源[:： ]`)

// isDataSourceBox 判断文本框是否为数据来源行（Python _extract_table_figure 过滤）。
func isDataSourceBox(text string) bool {
	return dataSourceRe.MatchString(text)
}

// tableRegionBox 生成表格替换用 TextBox：优先 DLA Region* 边界，否则用锚点框坐标。
func tableRegionBox(tbl *pdf.TableItem, ref *pdf.TextBox, html string) pdf.TextBox {
	pg := 0
	if len(tbl.Positions) > 0 && len(tbl.Positions[0].PageNumbers) > 0 {
		pg = tbl.Positions[0].PageNumbers[0]
	}
	// 已设置 Region* 时使用 DLA 区域边界。
	if tbl.RegionLeft != 0 || tbl.RegionRight != 0 || tbl.RegionTop != 0 || tbl.RegionBottom != 0 {
		return pdf.TextBox{
			X0:         tbl.RegionLeft,
			X1:         tbl.RegionRight,
			Top:        tbl.RegionTop,
			Bottom:     tbl.RegionBottom,
			Text:       html,
			PageNumber: pg,
			LayoutType: pdf.LayoutTypeTable,
		}
	}
	// 回退：使用锚点文本框坐标。
	x0, x1, top, bot := ref.X0, ref.X1, ref.Top, ref.Bottom
	return pdf.TextBox{
		X0:         x0,
		X1:         x1,
		Top:        top,
		Bottom:     bot,
		Text:       html,
		PageNumber: pg,
		LayoutType: pdf.LayoutTypeTable,
	}
}

// minRectangleDistance 计算两矩形欧氏距离，重叠时为 0，对齐 Python insert_table_figures。
func minRectangleDistance(left1, right1, top1, bottom1, left2, right2, top2, bottom2 float64) float64 {
	if right1 >= left2 && right2 >= left1 && bottom1 >= top2 && bottom2 >= top1 {
		return 0
	}
	var dx, dy float64
	if right1 < left2 {
		dx = left2 - right1
	} else if right2 < left1 {
		dx = left1 - right2
	}
	if bottom1 < top2 {
		dy = top2 - bottom1
	} else if bottom2 < top1 {
		dy = top1 - bottom2
	}
	return math.Sqrt(dx*dx + dy*dy)
}

// 孤儿列清理（Python construct_table 256-368 行）

// CleanupOrphanColumns 当行数≥4 时移除仅有一个非空单元格的孤儿列，对齐 Python construct_table。
func CleanupOrphanColumns(rows [][]pdf.TSRCell) [][]pdf.TSRCell {
	if len(rows) < 4 || len(rows) == 0 {
		return rows
	}
	nCols := len(rows[0])

	j := 0
	for j < nCols {
		// 步骤 1：统计列内非空单元格数
		e, ii := countNonEmptyCells(rows, j)
		if e > 1 {
			j++
			continue
		}

		// 步骤 2：检查相邻列是否有文字
		hasLeftText, hasRightText := checkAdjacentColumns(rows, j, ii)
		if hasLeftText && hasRightText {
			j++
			continue
		}

		// 步骤 3：计算向左/右合并的距离
		leftDist, rightDist := calculateMergeDistance(rows, j, ii, nCols, hasLeftText, hasRightText)

		// 步骤 4：合并孤儿列到左或右邻列
		if leftDist < rightDist && j > 0 {
			mergeColumnIntoLeft(rows, j)
		} else if j+1 < nCols {
			mergeColumnIntoRight(rows, j)
		}

		// 步骤 5：删除该列
		rows = removeColumn(rows, j)
		nCols--
		// 不递增 j — 下一列已移入位置 j。
	}
	return rows
}

// countNonEmptyCells 统计列内非空单元格数及最后一个非空行索引。
func countNonEmptyCells(rows [][]pdf.TSRCell, col int) (count int, lastRow int) {
	count = 0
	lastRow = 0
	for i := range rows {
		if col < len(rows[i]) && strings.TrimSpace(rows[i][col].Text) != "" {
			count++
			lastRow = i
		}
	}
	return count, lastRow
}

// checkAdjacentColumns 检查给定行上左右邻列是否有文字。
func checkAdjacentColumns(rows [][]pdf.TSRCell, col int, row int) (hasLeft bool, hasRight bool) {
	hasLeft = (col > 0 && col-1 < len(rows[row]) && strings.TrimSpace(rows[row][col-1].Text) != "") || col == 0
	hasRight = (col+1 < len(rows[row]) && strings.TrimSpace(rows[row][col+1].Text) != "") || col+1 >= len(rows[row])
	return hasLeft, hasRight
}

// calculateMergeDistance 计算向左/右列合并的最小距离。
func calculateMergeDistance(rows [][]pdf.TSRCell, col int, row int, nCols int, hasLeft bool, hasRight bool) (leftDist float64, rightDist float64) {
	leftDist = 1e9
	rightDist = 1e9

	if col > 0 && !hasLeft {
		for i := range rows {
			if col-1 < len(rows[i]) && strings.TrimSpace(rows[i][col-1].Text) != "" {
				if d := rows[row][col].X0 - rows[i][col-1].X1; d < leftDist {
					leftDist = d
				}
			}
		}
	}

	if col+1 < nCols && !hasRight {
		for i := range rows {
			if col+1 < len(rows[i]) && strings.TrimSpace(rows[i][col+1].Text) != "" {
				if d := rows[i][col+1].X0 - rows[row][col].X1; d < rightDist {
					rightDist = d
				}
			}
		}
	}

	return leftDist, rightDist
}

// mergeColumn 将 src 列文字合并到 dst 列。
func mergeColumn(rows [][]pdf.TSRCell, src, dst int) {
	for i := range rows {
		if src < len(rows[i]) && dst < len(rows[i]) {
			if rows[i][dst].Text == "" {
				rows[i][dst].Text = rows[i][src].Text
			} else if rows[i][src].Text != "" {
				if src < dst {
					rows[i][dst].Text = rows[i][src].Text + " " + rows[i][dst].Text
				} else {
					rows[i][dst].Text += " " + rows[i][src].Text
				}
			}
		}
	}
}

// mergeColumnIntoLeft 将第 j 列合并到 j-1 列。
func mergeColumnIntoLeft(rows [][]pdf.TSRCell, j int) {
	mergeColumn(rows, j, j-1)
}

// mergeColumnIntoRight 将第 j 列合并到 j+1 列。
func mergeColumnIntoRight(rows [][]pdf.TSRCell, j int) {
	mergeColumn(rows, j, j+1)
}

// removeColumn 从所有行删除第 j 列。
func removeColumn(rows [][]pdf.TSRCell, j int) [][]pdf.TSRCell {
	for i := range rows {
		if j < len(rows[i]) {
			rows[i] = append(rows[i][:j], rows[i][j+1:]...)
		}
	}
	return rows
}
