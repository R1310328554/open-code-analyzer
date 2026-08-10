// table_post.go — 表格提取后处理与 figure 合并：将表格区域替换为 HTML 框、跨页合并、锚点插入、数据来源过滤与 figure  consolidation。

package table

import (
	"log/slog"
	"math"
	"sort"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// FilterBoxesByRemoveSet 按索引集合过滤待移除文本框。
// removeSet：键为待移除索引，值为 true 表示移除。
func FilterBoxesByRemoveSet(boxes []pdf.TextBox, removeSet map[int]bool) []pdf.TextBox {
	if len(removeSet) == 0 {
		return boxes
	}
	if len(boxes) == 0 {
		return boxes
	}
	// 预分配容量以减少扩容。
	// 用 max 防止 removeSet 大于 boxes 时容量为负。
	estimatedCap := len(boxes) - len(removeSet)
	if estimatedCap < 0 {
		estimatedCap = 0
	}
	out := make([]pdf.TextBox, 0, estimatedCap)
	for i, b := range boxes {
		if !removeSet[i] {
			out = append(out, b)
		}
	}
	return out
}

// createTableBoxFromItem 从 TableItem 创建含 HTML 的 TextBox。
func createTableBoxFromItem(tbl *pdf.TableItem, html string) pdf.TextBox {
	pg := 0
	if len(tbl.Positions) > 0 && len(tbl.Positions[0].PageNumbers) > 0 {
		pg = tbl.Positions[0].PageNumbers[0]
	}
	x0, x1, top, bottom := tbl.RegionLeft, tbl.RegionRight, tbl.RegionTop, tbl.RegionBottom
	if x0 == 0 && x1 == 0 && top == 0 && bottom == 0 && len(tbl.Positions) > 0 {
		p := tbl.Positions[0]
		x0, x1, top, bottom = p.Left, p.Right, p.Top, p.Bottom
	}
	return pdf.TextBox{
		X0:         x0,
		X1:         x1,
		Top:        top,
		Bottom:     bottom,
		Text:       html,
		PageNumber: pg,
		LayoutType: pdf.LayoutTypeTable,
	}
}

// handleImageOnlyPDFs 处理无文本框但有表格的纯图片 PDF。
func handleImageOnlyPDFs(tables []pdf.TableItem) []pdf.TextBox {
	var out []pdf.TextBox
	for ti := range tables {
		if len(tables[ti].Cells) == 0 {
			continue
		}
		s := tables[ti].Scale
		pageGlobalCells := CellSliceToPageSpace(tables[ti].Cells, tables[ti].CropOffX, tables[ti].CropOffY, s)
		var tableBoxes []pdf.TextBox
		html := ConstructTable(pageGlobalCells, tableBoxes, tables[ti].Caption, &tables[ti])
		if html != "" {
			out = append(out, createTableBoxFromItem(&tables[ti], html))
		}
	}
	return out
}

// findTableAnchors 为每张表找空间最近的非 table/figure 文本框作为 HTML 插入锚点，按位置排序。
func findTableAnchors(boxes []pdf.TextBox, tables []pdf.TableItem) []struct{ ti, pos int } {
	replacedByTable := make(map[int]int)

	for ti := range tables {
		if len(tables[ti].Cells) == 0 {
			continue
		}
		tbl := &tables[ti]
		tblLeft, tblRight := tbl.RegionLeft, tbl.RegionRight
		tblTop, tblBottom := tbl.RegionTop, tbl.RegionBottom
		tblPg := 0
		if len(tbl.Positions) > 0 {
			p := tbl.Positions[0]
			if len(p.PageNumbers) > 0 {
				tblPg = p.PageNumbers[0]
			}
			if tblLeft == 0 && tblRight == 0 && tblTop == 0 && tblBottom == 0 {
				tblLeft, tblRight = p.Left, p.Right
				tblTop, tblBottom = p.Top, p.Bottom
			}
		}
		bestDist := math.MaxFloat64
		bestIdx := -1
		for i, b := range boxes {
			if b.LayoutType == pdf.LayoutTypeTable || b.LayoutType == pdf.LayoutTypeFigure {
				continue
			}
			if b.PageNumber != tblPg {
				continue
			}
			dist := minRectangleDistance(
				b.X0, b.X1, b.Top, b.Bottom,
				tblLeft, tblRight, tblTop, tblBottom,
			)
			if dist < bestDist {
				bestDist = dist
				bestIdx = i
			}
		}
		if bestIdx >= 0 {
			if boxes[bestIdx].Bottom < tblTop {
				bestIdx++
			}
			replacedByTable[ti] = bestIdx
		}
	}

	// 构建锚点列表并按位置排序。
	anchorList := make([]struct{ ti, pos int }, 0, len(replacedByTable))
	for ti, pos := range replacedByTable {
		anchorList = append(anchorList, struct{ ti, pos int }{ti, pos})
	}
	sort.Slice(anchorList, func(i, j int) bool { return anchorList[i].pos < anchorList[j].pos })
	return anchorList
}

// buildTableHTMLs 先将 cells 转为页面空间再构造各表 HTML，返回表索引→HTML 映射。
func buildTableHTMLs(boxes []pdf.TextBox, tables []pdf.TableItem) map[int]string {
	htmls := make(map[int]string)
	for ti := range tables {
		if len(tables[ti].Cells) == 0 {
			continue
		}
		// 将 TSR 单元格从 crop 像素转为页面 72 DPI 空间。
		s := tables[ti].Scale
		pageGlobalCells := CellSliceToPageSpace(tables[ti].Cells, tables[ti].CropOffX, tables[ti].CropOffY, s)
		// 仅收集 layout 为 table 且与表位置重叠的框。
		var tableBoxes []pdf.TextBox
		for i := range boxes {
			if boxes[i].LayoutType != pdf.LayoutTypeTable {
				continue
			}
			for _, tp := range tables[ti].Positions {
				if boxOverlapsPosition(boxes[i], tp) {
					tableBoxes = append(tableBoxes, boxes[i])
					break
				}
			}
		}
		slog.Debug("extractTableAndReplace constructTable", "table", ti, "cells", len(pageGlobalCells), "boxes", len(tableBoxes))
		htmls[ti] = ConstructTable(pageGlobalCells, tableBoxes, tables[ti].Caption, &tables[ti])
	}
	return htmls
}

// insertTableBoxes 过滤 removeSet 中的框，在锚点位置插入表格 HTML 框。
func insertTableBoxes(boxes []pdf.TextBox, tables []pdf.TableItem, removeSet map[int]bool,
	anchors []struct{ ti, pos int }, htmls map[int]string) []pdf.TextBox {

	out := make([]pdf.TextBox, 0, len(boxes)-len(removeSet)+len(anchors))
	anchorIdx := 0
	for i, b := range boxes {
		// 在索引 i 之前或等于 i 的锚点处插入 HTML 框。
		for anchorIdx < len(anchors) && anchors[anchorIdx].pos <= i {
			ti := anchors[anchorIdx].ti
			if html, ok := htmls[ti]; ok && html != "" {
				tbl := &tables[ti]
				out = append(out, tableRegionBox(tbl, &b, html))
			}
			anchorIdx++
		}
		if !removeSet[i] {
			out = append(out, b)
		}
	}
	// 在最后一个框之后插入剩余锚点。
	for anchorIdx < len(anchors) {
		ti := anchors[anchorIdx].ti
		if html, ok := htmls[ti]; ok && html != "" {
			tbl := &tables[ti]
			last := &boxes[len(boxes)-1]
			out = append(out, tableRegionBox(tbl, last, html))
		}
		anchorIdx++
	}
	return out
}

// ExtractTableAndReplace 弹出表格区域内框并替换为 consolidated HTML 框（每表一个），对齐 Python _extract_table_figure；数据来源行整框丢弃不替换。

// MarkNoMergeTables 按页序遍历：表格后紧跟 caption/title/reference 时标记 NoMerge，对齐 Python nomerge_lout_no。
func MarkNoMergeTables(boxes []pdf.TextBox, tables []pdf.TableItem) {
	var lastTableTI int = -1
	for i := range boxes {
		lt := boxes[i].LayoutType
		if lt == pdf.LayoutTypeTable {
			matched := false
			for ti := range tables {
				for _, tp := range tables[ti].Positions {
					if boxOverlapsPosition(boxes[i], tp) {
						lastTableTI = ti
						matched = true
						break
					}
				}
			}
			if !matched {
				lastTableTI = -1
			}
			continue
		}
		if lastTableTI >= 0 && (lt == pdf.LayoutTypeTitle || lt == pdf.DLALabelTableCaption || lt == pdf.DLALabelFigureCaption || lt == pdf.LayoutTypeReference || IsCaptionBox(boxes[i].Text, lt)) {
			tables[lastTableTI].NoMerge = true
		}
	}
}

// 输入 boxes 须已完成 TextMerge 与 VerticalMerge。TableItem.Cells 在 crop 像素空间，boxes 在 PDF 点空间，经 Scale/CropOff 转换。replacement 记录表索引与被替换框索引。
type replacement struct {
	tableIdx int
	boxIdx   int
}

// buildRemoveSet 扫描数据来源 attribution 框加入 removeSet，可在 MergeTablesAcrossPages 之前调用。
func buildRemoveSet(boxes []pdf.TextBox) map[int]bool {
	removeSet := make(map[int]bool)
	for i := range boxes {
		if boxes[i].LayoutType == pdf.LayoutTypeTable && isDataSourceBox(boxes[i].Text) {
			removeSet[i] = true
		}
	}
	return removeSet
}

// buildReplacementsAfterMerge 在合并后将各表映射到重叠 table 框，须在 MergeTablesAcrossPages 之后调用。
func buildReplacementsAfterMerge(boxes []pdf.TextBox, tables []pdf.TableItem, removeSet map[int]bool) []replacement {
	var reps []replacement
	for ti := range tables {
		for i := range boxes {
			if boxes[i].LayoutType != pdf.LayoutTypeTable || removeSet[i] {
				continue
			}
			for _, tp := range tables[ti].Positions {
				if boxOverlapsPosition(boxes[i], tp) {
					reps = append(reps, replacement{tableIdx: ti, boxIdx: i})
					break
				}
			}
		}
	}
	return reps
}

// buildReplacements 已弃用：合并前版本，请改用 buildRemoveSet + MergeTablesAcrossPages + buildReplacementsAfterMerge。
func buildReplacements(boxes []pdf.TextBox, tables []pdf.TableItem) (map[int]bool, []replacement) {
	removeSet := buildRemoveSet(boxes)
	reps := buildReplacementsAfterMerge(boxes, tables, removeSet)
	return removeSet, reps
}

func ExtractTableAndReplace(boxes []pdf.TextBox, tables []pdf.TableItem) []pdf.TextBox {
	removeSet := buildRemoveSet(boxes)
	if len(tables) == 0 {
		return FilterBoxesByRemoveSet(boxes, removeSet)
	}

	MarkNoMergeTables(boxes, tables)
	tables = MergeTablesAcrossPages(tables, nil)

	// 合并后再建 replacement，确保 tableIdx 指向合并后切片。
	replacements := buildReplacementsAfterMerge(boxes, tables, removeSet)

	if len(replacements) == 0 && len(boxes) == 0 {
		return handleImageOnlyPDFs(tables)
	}
	if len(replacements) == 0 {
		return FilterBoxesByRemoveSet(boxes, removeSet)
	}

	return processTablesWithReplacements(boxes, tables, removeSet, replacements)
}

// buildAndSortAnchors 构建并排序锚点列表。
func buildAndSortAnchors(anchors map[int]int) []struct{ ti, pos int } {
	result := make([]struct{ ti, pos int }, 0, len(anchors))
	for ti, pos := range anchors {
		result = append(result, struct{ ti, pos int }{ti: ti, pos: pos})
	}
	sort.Slice(result, func(i, j int) bool { return result[i].pos < result[j].pos })
	return result
}

// processTablesWithReplacements 带 replacement 的正常表格替换流程。
func processTablesWithReplacements(
	boxes []pdf.TextBox,
	tables []pdf.TableItem,
	removeSet map[int]bool,
	replacements []replacement,
) []pdf.TextBox {
	for _, r := range replacements {
		removeSet[r.boxIdx] = true
	}
	anchors := findTableAnchorsWithReplacements(boxes, tables, replacements)
	htmls := buildTableHTMLs(boxes, tables)
	anchorList := buildAndSortAnchors(anchors)
	return insertTableBoxes(boxes, tables, removeSet, anchorList, htmls)
}

// findTableAnchorsWithReplacements 同 findTableAnchors，无文本锚点时回退到 replacement 位置。
func findTableAnchorsWithReplacements(boxes []pdf.TextBox, tables []pdf.TableItem,
	replacements []replacement) map[int]int {

	// 先从 findTableAnchors 获取锚点。
	anchorList := findTableAnchors(boxes, tables)
	result := make(map[int]int, len(anchorList))
	for _, a := range anchorList {
		result[a.ti] = a.pos
	}

	// 用 replacement 补全缺失表的锚点。
	for ti := range tables {
		if _, has := result[ti]; has {
			continue
		}
		// 取该表最早的 replacement 框索引。
		for _, r := range replacements {
			if r.tableIdx == ti {
				if _, ok := result[ti]; !ok || r.boxIdx < result[ti] {
					result[ti] = r.boxIdx
				}
			}
		}
	}
	return result
}

// figKey 按页码与 LayoutNo 分组 figure 框。
type figKey struct {
	page int
	ln   string
}

// markDataSourceBoxesForRemoval 标记数据来源 figure 框待移除。
func markDataSourceBoxesForRemoval(boxes []pdf.TextBox) map[int]bool {
	removeSet := make(map[int]bool)
	for i, b := range boxes {
		if b.LayoutType == pdf.LayoutTypeFigure && isDataSourceBox(b.Text) {
			removeSet[i] = true
		}
	}
	return removeSet
}

// groupFigureBoxes 按 (page, layoutno) 分组 figure 框。
func groupFigureBoxes(boxes []pdf.TextBox, removeSet map[int]bool) map[figKey][]int {
	groups := make(map[figKey][]int)
	for i, b := range boxes {
		if b.LayoutType != pdf.LayoutTypeFigure || removeSet[i] {
			continue
		}
		key := figKey{b.PageNumber, b.LayoutNo}
		groups[key] = append(groups[key], i)
	}
	return groups
}

// mergeFigureGroups 合并同组内多个 figure 框的文字与边界。
func mergeFigureGroups(boxes []pdf.TextBox, groups map[figKey][]int, removeSet map[int]bool) {
	for _, indices := range groups {
		if len(indices) <= 1 {
			continue
		}
		anchor := indices[0]
		for _, idx := range indices[1:] {
			b := boxes[idx]
			boxes[anchor].Text += "\n" + b.Text
			boxes[anchor].X0 = math.Min(boxes[anchor].X0, b.X0)
			boxes[anchor].X1 = math.Max(boxes[anchor].X1, b.X1)
			boxes[anchor].Top = math.Min(boxes[anchor].Top, b.Top)
			boxes[anchor].Bottom = math.Max(boxes[anchor].Bottom, b.Bottom)
			removeSet[idx] = true
		}
	}
}

// ConsolidateFigures 合并同 LayoutNo（同一 DLA figure 区域）的 figure 框为单个 TextBox，对齐 Python insert_table_figures；数据来源 figure 整框丢弃。
func ConsolidateFigures(boxes []pdf.TextBox) []pdf.TextBox {
	removeSet := markDataSourceBoxesForRemoval(boxes)
	groups := groupFigureBoxes(boxes, removeSet)

	if len(groups) > 0 {
		mergeFigureGroups(boxes, groups, removeSet)
	}

	return FilterBoxesByRemoveSet(boxes, removeSet)
}

// boxOverlapsPosition 判断 TextBox 是否与 Position 重叠（含 margin 容差）。
func boxOverlapsPosition(box pdf.TextBox, pos pdf.Position) bool {
	const margin = 2.0
	return box.X0 <= pos.Right+margin && box.X1 >= pos.Left-margin &&
		box.Top <= pos.Bottom+margin && box.Bottom >= pos.Top-margin
}

// 注：RowsToHTML 在 table_html.go 中实现；此处为历史注释占位。
