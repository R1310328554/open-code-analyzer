// table_layout.go — TSR 后布局清理与表格框 R/C/H/SP 标注：单元格排序、重叠去重、阈值匹配与网格注释写入，对齐 Python pdf_parser gather/layouts_cleanup。

package table

import (
	"math"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	"ragflow/internal/deepdoc/parser/pdf/util"
	"sort"
)

// ── TSR 后布局标注（Python pdf_parser gather/layouts_cleanup）──

// SortYFirstly 按 Y0 排序；Y 差小于阈值时改按 X 排序（同行内从左到右），对齐 Python sort_Y_firstly。
func SortYFirstly(cells []pdf.TSRCell, threshold float64) {
	sort.Slice(cells, func(i, j int) bool {
		diff := cells[i].Y0 - cells[j].Y0
		if math.Abs(diff) < threshold {
			return cells[i].X0 < cells[j].X0
		}
		return diff < 0
	})
}

// SortXFirstly 按 X0 排序；X 差小于阈值时改按 Y 排序。
func SortXFirstly(cells []pdf.TSRCell, threshold float64) {
	sort.Slice(cells, func(i, j int) bool {
		diff := cells[i].X0 - cells[j].X0
		if math.Abs(diff) < threshold {
			return cells[i].Y0 < cells[j].Y0
		}
		return diff < 0
	})
}

// layoutCleanup 移除同类型重叠单元格，对齐 Python layouts_cleanup(far=2, thr=0.7)。在后续 far 个单元格中找重叠且同 label 者，保留与文本框重叠更多的一方。
func layoutCleanup(cells []pdf.TSRCell, boxes []pdf.TextBox, far int, thr float64) []pdf.TSRCell {
	// 调用方传入前须已排序。
	out := make([]pdf.TSRCell, len(cells))
	copy(out, cells)

	i := 0
	for i+1 < len(out) {
		j := i + 1
		limit := i + far
		if limit > len(out) {
			limit = len(out)
		}
		for j < limit && (out[i].Label != "" && out[i].Label != out[j].Label || notOverlapped(out[i], out[j])) {
			j++
		}
		if j >= limit {
			i++
			continue
		}
		// 单元格 i 与 j 重叠且同类型，保留其一。
		areaI := util.OverlapRatioA(&out[i], &out[j])
		areaJ := util.OverlapRatioA(&out[j], &out[i])
		if areaI < thr && areaJ < thr {
			i++
			continue
		}

		// 优先保留与文本框重叠面积更大者。
		boxAreaI, boxAreaJ := 0.0, 0.0
		for _, b := range boxes {
			if !tsrBoxOverlap(b, out[i]) {
				boxAreaI += util.OverlapInter(&b, &out[i])
			}
			if !tsrBoxOverlap(b, out[j]) {
				boxAreaJ += util.OverlapInter(&b, &out[j])
			}
		}
		if boxAreaI >= boxAreaJ {
			out = append(out[:j], out[j+1:]...)
		} else {
			out = append(out[:i], out[i+1:]...)
		}
	}
	return out
}

// notOverlapped 判断两 TSR 单元格是否不重叠。
func notOverlapped(a, b pdf.TSRCell) bool {
	return a.X1 < b.X0 || a.X0 > b.X1 || a.Y1 < b.Y0 || a.Y0 > b.Y1
}

// tsrBoxOverlap 判断 TextBox 与 TSR 单元格是否不重叠。
func tsrBoxOverlap(b pdf.TextBox, c pdf.TSRCell) bool {
	return b.X1 < c.X0 || b.X0 > c.X1 || b.Bottom < c.Y0 || b.Top > c.Y1
}

// findOverlappedWithThreshold 返回双向重叠≥thr 的最佳单元格索引，对齐 Python find_overlapped_with_threshold。
func findOverlappedWithThreshold(box pdf.TextBox, cells []pdf.TSRCell, thr float64) int {
	boxArea := util.Area(&box)
	if boxArea <= 0 {
		return -1
	}
	bestIdx := -1
	bestOverlap := thr // Python：max_overlap 初始为 thr
	for i, c := range cells {
		cellArea := util.Area(&c)
		if cellArea <= 0 {
			continue
		}
		ol := util.OverlapInter(&box, &c)
		if ol <= 0 {
			continue
		}
		boxRatio := ol / boxArea
		cellRatio := ol / cellArea
		// Python：取 box 与 cell 双向重叠比的最大值
		overlap := math.Max(boxRatio, cellRatio)
		if overlap >= bestOverlap {
			bestOverlap = overlap
			bestIdx = i
		}
	}
	return bestIdx
}

// findHorizontallyTightestFit 返回与框水平边缘距离最小的列索引，对齐 Python find_horizontally_tightest_fit。
func findHorizontallyTightestFit(box pdf.TextBox, clmns []pdf.TSRCell) int {
	best := -1
	bestDist := float64(1<<63 - 1)
	for i, c := range clmns {
		// 框与列边界的最小边缘距离。
		dl := math.Abs(box.X0 - c.X0)
		dr := math.Abs(box.X1 - c.X1)
		d := math.Min(dl, dr)
		if d < bestDist {
			bestDist = d
			best = i
		}
	}
	return best
}

// AnnotateTableBoxes 用 TSR 网格为表格 TextBox 写入 R/H/C/SP 注释，对齐 Python pdf_parser.py:518-554。
func AnnotateTableBoxes(boxes []pdf.TextBox, grid [][]pdf.TSRCell) {
	// grid[0] 为表头行；span 由 CalSpans 后续计算。
	var headers, spans []pdf.TSRCell
	var clmns []pdf.TSRCell
	if len(grid) > 0 {
		headers = grid[0]
		clmns = append(clmns, grid[0]...)
	}
	SortYFirstly(headers, 10)
	SortXFirstly(clmns, 10)

	for i := range boxes {
		if boxes[i].LayoutType != pdf.LayoutTypeTable {
			continue
		}
		// 基于 grid：按重叠匹配行 R 与列 C。
		for ri, row := range grid {
			if idx := findOverlappedWithThreshold(boxes[i], row, 0.3); idx >= 0 {
				boxes[i].R = ri
				boxes[i].RTop = row[0].Y0
				boxes[i].RBott = row[0].Y1
				for ci, cell := range row {
					if !tsrBoxOverlap(boxes[i], cell) {
						boxes[i].C = ci
						boxes[i].CLeft = cell.X0
						boxes[i].CRight = cell.X1
						break
					}
				}
				break
			}
		}
		if idx := findOverlappedWithThreshold(boxes[i], headers, 0.3); idx >= 0 {
			boxes[i].HTop = headers[idx].Y0
			boxes[i].HBott = headers[idx].Y1
			boxes[i].HLeft = headers[idx].X0
			boxes[i].HRight = headers[idx].X1
			boxes[i].H = idx
		}
		if len(clmns) > 1 {
			if idx := findHorizontallyTightestFit(boxes[i], clmns); idx >= 0 {
				boxes[i].C = idx
				boxes[i].CLeft = clmns[idx].X0
				boxes[i].CRight = clmns[idx].X1
			}
		}
		if idx := findOverlappedWithThreshold(boxes[i], spans, 0.3); idx >= 0 {
			boxes[i].SP = idx
		}
	}

	// 两遍 C 回退：列 TSR 不足时，各行内按 X 顺序分配列号 C，对齐 Python 行为。
	if len(clmns) <= 1 {
		// 按 R 分组收集所有表格 TextBox。
		rBoxes := make(map[int][]int)
		for i := range boxes {
			if boxes[i].LayoutType == pdf.LayoutTypeTable {
				rBoxes[boxes[i].R] = append(rBoxes[boxes[i].R], i)
			}
		}
		for _, indices := range rBoxes {
			sort.Slice(indices, func(a, b int) bool { return boxes[indices[a]].X0 < boxes[indices[b]].X0 })
			for ci, bi := range indices {
				boxes[bi].C = ci
			}
		}
	}
}
