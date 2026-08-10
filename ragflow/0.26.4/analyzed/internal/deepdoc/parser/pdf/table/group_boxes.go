// group_boxes.go — 文本框网格化：按 R/C 标注或 Y/X 坐标将 TextBox 分组为表格单元格，对齐 Python construct_table。

package table

import (
	"sort"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// rb 网格构建过程中的行-列单元格中间态（文本、坐标、标签）。
type rb struct {
	row, col       int
	txt            string
	x0, y0, x1, y1 float64
	label          string
}

// GroupBoxesByRC 按 R/C 标注将 TextBox 分组为单元格网格；对齐 Python construct_table 的 R/C 排序与列压缩。
func GroupBoxesByRC(boxes []pdf.TextBox) [][]pdf.TSRCell {
	if len(boxes) == 0 {
		return nil
	}
	// If no real R/C annotations (maxR <= 0), fall back to YX coordinate
	// grouping — matching Python's construct_table when all R=-1.
	maxR := 0
	for _, b := range boxes {
		if b.R > maxR {
			maxR = b.R
		}
	}
	if maxR <= 0 {
		return GroupBoxesByYX(boxes)
	}
	// 先按 R（sort_R_firstly），再 Y、X 排序。
	sort.Slice(boxes, func(i, j int) bool {
		if boxes[i].R != boxes[j].R {
			return boxes[i].R < boxes[j].R
		}
		if boxes[i].Top != boxes[j].Top {
			return boxes[i].Top < boxes[j].Top
		}
		return boxes[i].X0 < boxes[j].X0
	})

	// 压缩 R 索引为连续行号。
	rowMap, compressed := compressRowIndices(boxes)

	// 按行收集并合并同格 TextBox。
	cmap, _ := collectBoxesPerRow(boxes, rowMap)

	// 每行内按 X 顺序压缩列索引。
	cCompressed, cMaxCol := compressColIndices(boxes, rowMap, compressed)

	// 构建最终二维单元格网格。
	return buildGrid(cmap, cCompressed, cMaxCol, compressed)
}

// GroupBoxesByYX 无 R/C 时按页/Y/X 坐标分组：Y 重叠同行、X 重叠同列，对齐 Python sort_R/C_firstly 回退逻辑。
func GroupBoxesByYX(boxes []pdf.TextBox) [][]pdf.TSRCell {
	if len(boxes) == 0 {
		return nil
	}
	// 按 (页码, top, x0) 排序，等同 Python R=-1 的 sort_R_firstly。
	sort.Slice(boxes, func(i, j int) bool {
		if boxes[i].PageNumber != boxes[j].PageNumber {
			return boxes[i].PageNumber < boxes[j].PageNumber
		}
		if boxes[i].Top != boxes[j].Top {
			return boxes[i].Top < boxes[j].Top
		}
		return boxes[i].X0 < boxes[j].X0
	})

	// 按 Y 重叠与页码分组为行。
	type rowGroup struct {
		boxes    []pdf.TextBox
		top, btm float64
	}
	var rowGroups []rowGroup
	rowGroups = append(rowGroups, rowGroup{
		boxes: []pdf.TextBox{boxes[0]},
		top:   boxes[0].Top,
		btm:   boxes[0].Bottom,
	})
	for i := 1; i < len(boxes); i++ {
		prev := &rowGroups[len(rowGroups)-1]
		// 同行条件：同页且 top < 前行 bottom（Y 重叠）。
		if boxes[i].PageNumber == prev.boxes[0].PageNumber && boxes[i].Top < prev.btm {
			prev.boxes = append(prev.boxes, boxes[i])
			if boxes[i].Top < prev.top {
				prev.top = boxes[i].Top
			}
			if boxes[i].Bottom > prev.btm {
				prev.btm = boxes[i].Bottom
			}
		} else {
			rowGroups = append(rowGroups, rowGroup{
				boxes: []pdf.TextBox{boxes[i]},
				top:   boxes[i].Top,
				btm:   boxes[i].Bottom,
			})
		}
	}

	// 行内按 X 重叠分组为列并拼接文本。
	rows := make([][]pdf.TSRCell, len(rowGroups))
	for ri, rg := range rowGroups {
		// 行内按 X0 排序。
		sort.Slice(rg.boxes, func(i, j int) bool {
			return rg.boxes[i].X0 < rg.boxes[j].X0
		})
		// 按 X 重叠合并相邻框为列。
		var cols []struct {
			boxes []pdf.TextBox
			x1    float64
		}
		cols = append(cols, struct {
			boxes []pdf.TextBox
			x1    float64
		}{
			boxes: []pdf.TextBox{rg.boxes[0]},
			x1:    rg.boxes[0].X1,
		})
		for i := 1; i < len(rg.boxes); i++ {
			prev := &cols[len(cols)-1]
			if rg.boxes[i].X0 < prev.x1 {
				prev.boxes = append(prev.boxes, rg.boxes[i])
				if rg.boxes[i].X1 > prev.x1 {
					prev.x1 = rg.boxes[i].X1
				}
			} else {
				cols = append(cols, struct {
					boxes []pdf.TextBox
					x1    float64
				}{
					boxes: []pdf.TextBox{rg.boxes[i]},
					x1:    rg.boxes[i].X1,
				})
			}
		}
		rows[ri] = make([]pdf.TSRCell, len(cols))
		for ci, col := range cols {
			var sb strings.Builder
			for _, b := range col.boxes {
				t := strings.TrimSpace(b.Text)
				if t == "" {
					continue
				}
				if sb.Len() > 0 {
					sb.WriteByte(' ')
				}
				sb.WriteString(t)
			}
			rows[ri][ci].Text = sb.String()
		}
	}
	return rows
}

// cellPosFromBox 从 TextBox 取单元格坐标与标签；表头/合并格用 H*/SP 扩展边界。
func cellPosFromBox(b pdf.TextBox) (x0, y0, x1, y1 float64, label string) {
	x0, y0, x1, y1 = b.X0, b.Top, b.X1, b.Bottom
	if b.H > 0 {
		label = "table header"
		if b.HLeft != 0 || b.HRight != 0 {
			if b.HLeft != 0 {
				x0 = b.HLeft
			}
			if b.HRight != 0 {
				x1 = b.HRight
			}
		}
		if b.HTop != 0 {
			y0 = b.HTop
		}
		if b.HBott != 0 {
			y1 = b.HBott
		}
	} else if b.SP > 0 {
		label = "table spanning cell"
	}
	return
}

// cellLabelFromBox 根据 H/SP 标注返回 TSR 标签，合并多框时保留合并格标签。
func cellLabelFromBox(b pdf.TextBox) string {
	if b.H > 0 {
		return "table header"
	}
	if b.SP > 0 {
		return "table spanning cell"
	}
	return ""
}

// compressRowIndices 将原始 R 映射为连续行索引 rowMap。
func compressRowIndices(boxes []pdf.TextBox) (map[int]int, int) {
	rowMap := make(map[int]int) // original R → compressed row index
	compressed := 0
	rowMap[boxes[0].R] = 0
	lastR := boxes[0].R
	for i := 1; i < len(boxes); i++ {
		if boxes[i].R != lastR {
			compressed++
			rowMap[boxes[i].R] = compressed
			lastR = boxes[i].R
		} else {
			rowMap[boxes[i].R] = compressed
		}
	}
	return rowMap, compressed
}

// collectBoxesPerRow 按行-列合并 TextBox 文本与 spanning 坐标。
func collectBoxesPerRow(boxes []pdf.TextBox, rowMap map[int]int) (map[int]map[int]*rb, map[int]int) {
	cmap := make(map[int]map[int]*rb) // row → col → entry
	maxCols := make(map[int]int)
	for _, b := range boxes {
		t := strings.TrimSpace(b.Text)
		// Keep boxes with SP/H annotations even if text is empty —
		// their coordinates are needed for colspan/rowspan calculation.
		if t == "" && b.H <= 0 && b.SP <= 0 {
			continue
		}
		r := rowMap[b.R]
		c := b.C
		if cmap[r] == nil {
			cmap[r] = make(map[int]*rb)
		}
		x0, y0, x1, y1, label := cellPosFromBox(b)
		if v, ok := cmap[r][c]; ok {
			if t != "" {
				v.txt += " " + t
			}
			// 合并 spanning 坐标取最宽范围。
			if b.H > 0 || b.SP > 0 {
				v.label = cellLabelFromBox(b)
				if v.x0 > x0 {
					v.x0 = x0
				}
				if v.y0 > y0 {
					v.y0 = y0
				}
				if v.x1 < x1 {
					v.x1 = x1
				}
				if v.y1 < y1 {
					v.y1 = y1
				}
			}
		} else {
			cmap[r][c] = &rb{r, c, t, x0, y0, x1, y1, label}
		}
		if c > maxCols[r] {
			maxCols[r] = c
		}
	}
	return cmap, maxCols
}

// rowBox compressColIndices 用的行内框辅助结构。
type rowBox struct {
	c, idx int
	x0, x1 float64
	txt    string
}

// compressColIndices 每行按 X0 排序并压缩列号，X 重叠则合并到上一列。
func compressColIndices(boxes []pdf.TextBox, rowMap map[int]int, compressed int) (map[int]map[int]int, map[int]int) {
	cCompressed := make(map[int]map[int]int) // row → (original C → compressed col)
	cMaxCol := make(map[int]int)
	for ri := 0; ri <= compressed; ri++ {
		// Collect all boxes in this row, sorted by X0.
		var rowBoxes []rowBox
		for i, b := range boxes {
			if rowMap[b.R] == ri && (strings.TrimSpace(b.Text) != "" || b.H > 0 || b.SP > 0) {
				rowBoxes = append(rowBoxes, rowBox{c: b.C, idx: i, x0: b.X0, x1: b.X1, txt: b.Text})
			}
		}
		sort.Slice(rowBoxes, func(i, j int) bool { return rowBoxes[i].x0 < rowBoxes[j].x0 })
		// 按 X 顺序分配压缩列号，X 不重叠则新开列。
		cMap := make(map[int]int) // original C → compressed col
		right := 0.0
		nCols := 0
		for _, rb := range rowBoxes {
			if len(cMap) == 0 || rb.x0 >= right {
				cMap[rb.c] = nCols
				nCols++
				right = rb.x1
			} else {
				// X 重叠则并入上一列。
				cMap[rb.c] = nCols - 1
				if rb.x1 > right {
					right = rb.x1
				}
			}
		}
		cCompressed[ri] = cMap
		cMaxCol[ri] = nCols - 1
	}
	return cCompressed, cMaxCol
}

// buildGrid 从 cmap 与列压缩映射构建最终 [][]TSRCell 网格。
func buildGrid(cmap map[int]map[int]*rb, cCompressed map[int]map[int]int, cMaxCol map[int]int, compressed int) [][]pdf.TSRCell {
	rows := make([][]pdf.TSRCell, compressed+1)
	for ri := 0; ri <= compressed; ri++ {
		maxC := cMaxCol[ri]
		rows[ri] = make([]pdf.TSRCell, maxC+1)
		for ci, v := range cmap[ri] {
			cci := cCompressed[ri][ci]
			if cci <= maxC {
				if rows[ri][cci].Text == "" {
					rows[ri][cci].Text = v.txt
					rows[ri][cci].X0 = v.x0
					rows[ri][cci].Y0 = v.y0
					rows[ri][cci].X1 = v.x1
					rows[ri][cci].Y1 = v.y1
					rows[ri][cci].Label = v.label
				} else {
					// 多原始列映射同一压缩格时确定性合并文本与边界。
					if v.txt != "" {
						rows[ri][cci].Text += " " + v.txt
					}
					if v.x0 < rows[ri][cci].X0 {
						rows[ri][cci].X0 = v.x0
					}
					if v.y0 < rows[ri][cci].Y0 {
						rows[ri][cci].Y0 = v.y0
					}
					if v.x1 > rows[ri][cci].X1 {
						rows[ri][cci].X1 = v.x1
					}
					if v.y1 > rows[ri][cci].Y1 {
						rows[ri][cci].Y1 = v.y1
					}
					if rows[ri][cci].Label == "" && v.label != "" {
						rows[ri][cci].Label = v.label
					}
				}
			}
		}
	}
	return rows
}

// BoxesHaveAnnotations 判断是否至少有 2 行或 2 列 R/C 标注（maxR>0 或 maxC>0）。
func BoxesHaveAnnotations(boxes []pdf.TextBox) bool {
	maxR, maxC := 0, 0
	for _, b := range boxes {
		if b.R > maxR {
			maxR = b.R
		}
		if b.C > maxC {
			maxC = b.C
		}
	}
	// R/C 为 0 基，maxR>0 表示至少两行或两列。
	return maxR > 0 || maxC > 0
}
