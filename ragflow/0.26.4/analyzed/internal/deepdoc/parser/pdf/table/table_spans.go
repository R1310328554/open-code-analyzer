// table_spans.go — 表格 colspan/rowspan 计算：根据单元格几何与列/行中心线推断跨列跨行，对齐 Python __cal_spans。

package table

import (
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// CalSpans 计算网格中跨列/跨行单元格，返回 spanInfo 与 covered（被 span 遮盖的格），对齐 Python __cal_spans。
func CalSpans(rows [][]pdf.TSRCell) (map[[2]int][2]int, map[[2]int]bool) {
	spanInfo := make(map[[2]int][2]int)
	covered := make(map[[2]int]bool)
	if len(rows) == 0 || len(rows[0]) == 0 { return spanInfo, covered }

	// 计算各列左右边界以得列中心。
	nCols := len(rows[0])
	colLeft := make([]float64, nCols)
	colRight := make([]float64, nCols)
	for j := 0; j < nCols; j++ {
		colLeft[j] = 1e9
		colRight[j] = -1e9
	}
	nRows := len(rows)
	rowTop := make([]float64, nRows)
	rowBott := make([]float64, nRows)
	for i := 0; i < nRows; i++ {
		rowTop[i] = 1e9
		rowBott[i] = -1e9
	}

	for i, row := range rows {
		for j, cell := range row {
			if j >= nCols { continue }
			// 跨行跨列单元格不参与列/行边界统计。
			// 用 label 含 spanning 检测（O(1)，不依赖列中点）。
			if strings.Contains(cell.Label, "spanning") { continue }
			if cell.X0 < colLeft[j] { colLeft[j] = cell.X0 }
			if cell.X1 > colRight[j] { colRight[j] = cell.X1 }
			if cell.Y0 < rowTop[i] { rowTop[i] = cell.Y0 }
			if cell.Y1 > rowBott[i] { rowBott[i] = cell.Y1 }
		}
	}

	// 对每个跨行跨列单元格计算覆盖的列数/行数。
	for i, row := range rows {
		for j, cell := range row {
			if j >= nCols || covered[[2]int{i,j}] { continue }
			// 无坐标数据的单元格无法 span，跳过。
			if cell.X0 == 0 && cell.X1 == 0 && cell.Y0 == 0 && cell.Y1 == 0 { continue }
			cs, rs := 1, 1
			// 统计列中心落在此 cell X 范围内的列数。
			for k := j+1; k < nCols; k++ {
				// 跳过无普通单元格的列（边界仍为初始极值）。
				if colLeft[k] == 1e9 && colRight[k] == -1e9 { continue }
				colCenter := (colLeft[k] + colRight[k]) / 2
				if colCenter >= cell.X0 && colCenter <= cell.X1 { cs++ }
			}
			// 统计行中心落在此 cell Y 范围内的行数。
			for k := i+1; k < nRows; k++ {
				// 跳过无普通单元格的行。
				if rowTop[k] == 1e9 && rowBott[k] == -1e9 { continue }
				rowCenter := (rowTop[k] + rowBott[k]) / 2
				if rowCenter >= cell.Y0 && rowCenter <= cell.Y1 { rs++ }
			}
			if cs > 1 || rs > 1 {
				spanInfo[[2]int{i,j}] = [2]int{cs, rs}
				// 标记被 span 遮盖的单元格。
				for ri := i; ri < i+rs && ri < nRows; ri++ {
					for cj := j; cj < j+cs && cj < nCols; cj++ {
						if ri != i || cj != j {
							covered[[2]int{ri, cj}] = true
						}
					}
				}
			}
		}
	}
	return spanInfo, covered
}

// FlattenGrid 将二维 grid 展平为一维切片供 FillCellTextFromBoxes 使用。
func FlattenGrid(grid [][]pdf.TSRCell) []pdf.TSRCell {
	n := 0
	for _, row := range grid { n += len(row) }
	flat := make([]pdf.TSRCell, 0, n)
	for _, row := range grid { flat = append(flat, row...) }
	return flat
}
