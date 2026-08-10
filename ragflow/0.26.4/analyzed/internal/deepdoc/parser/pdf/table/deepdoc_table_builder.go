// deepdoc_table_builder.go — DeepDoc TSR 表格构建器：实现 pdf.TableBuilder，将 TSR 结构单元格组装为行列网格。

package table

import (
	"context"
	"image"
	"sort"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// DeepDocTableBuilder 实现 pdf.TableBuilder，对接 DeepDoc 表格结构识别服务；标签注入由 NewTableBuilderFor 工厂处理。
type DeepDocTableBuilder struct {
	doc pdf.DocAnalyzer
}

// NewDeepDocTableBuilder 创建构建器；底层客户端标签由调用方设置。
func NewDeepDocTableBuilder(doc pdf.DocAnalyzer) *DeepDocTableBuilder {
	return &DeepDocTableBuilder{doc: doc}
}
// Name 返回构建器标识 "deepdoc"。
func (b *DeepDocTableBuilder) Name() string { return "deepdoc" }
// DetectCells 对裁剪表格图调用 DocAnalyzer.TSR 检测结构单元格。
func (b *DeepDocTableBuilder) DetectCells(ctx context.Context, cropped image.Image) ([]pdf.TSRCell, error) {
	return b.doc.TSR(ctx, cropped)
}

// GroupCells 从 TSR 结构标签单元格构建行列网格：① 行/列边界排序；② 行列叉积；③ 表头行传播；④ 合并单元格注入（中心点在 span 内的格归零）。
func (b *DeepDocTableBuilder) GroupCells(cells []pdf.TSRCell) [][]pdf.TSRCell {
	if len(cells) == 0 {
		return nil
	}

	// 1. 收集 table row/column/header/spanning 结构元素。
	var rows, cols, spans []pdf.TSRCell
	var header *pdf.TSRCell

	for _, c := range cells {
		switch {
		case strings.HasSuffix(c.Label, "table row"):
			rows = append(rows, c)
		case strings.HasSuffix(c.Label, "table column"):
			cols = append(cols, c)
		case strings.Contains(strings.ToLower(c.Label), "spanning"):
			spans = append(spans, c)
		case strings.HasSuffix(c.Label, "table column header"):
			h := c
			header = &h
		}
	}

	if len(rows) == 0 {
		return nil
	}

	SortYFirstly(rows, 10)
	SortXFirstly(cols, 10)

	// 2. 无列单元格时按行范围合成单列。
	if len(cols) == 0 {
		x0 := rows[0].X0
		x1 := rows[0].X1
		cols = []pdf.TSRCell{{X0: x0, Y0: rows[0].Y0, X1: x1, Y1: rows[len(rows)-1].Y1, Label: "table column"}}
	}

	// 3. 行列叉积生成 grid[r][c] 边界。
	grid := make([][]pdf.TSRCell, len(rows))
	for r := range rows {
		grid[r] = make([]pdf.TSRCell, len(cols))
		for c := range cols {
			grid[r][c] = pdf.TSRCell{
				X0: cols[c].X0,
				Y0: rows[r].Y0,
				X1: cols[c].X1,
				Y1: rows[r].Y1,
			}
		}
	}

	// 4. 与表头 Y 重叠的行标记为 table column header。
	if header != nil {
		for ri := range rows {
			if rows[ri].Y0 >= header.Y0 && rows[ri].Y1 <= header.Y1 ||
				overlapsY(rows[ri], *header) {
				for cj := range grid[ri] {
					grid[ri][cj].Label = "table column header"
				}
			}
		}
	}

	// 5. 合并单元格：左上角扩展 bbox，其余覆盖格清零。
	for _, sp := range spans {
		type cellIdx struct{ r, c int }
		var covered []cellIdx
		for ri := range grid {
			for cj := range grid[ri] {
				cell := grid[ri][cj]
				cx := (cell.X0 + cell.X1) / 2
				cy := (cell.Y0 + cell.Y1) / 2
				if cx >= sp.X0 && cx <= sp.X1 && cy >= sp.Y0 && cy <= sp.Y1 {
					covered = append(covered, cellIdx{ri, cj})
				}
			}
		}
		if len(covered) < 2 {
			continue
		}
		sort.Slice(covered, func(a, b int) bool {
			if covered[a].r != covered[b].r {
				return covered[a].r < covered[b].r
			}
			return covered[a].c < covered[b].c
		})
		first := covered[0]
		grid[first.r][first.c].X0 = sp.X0
		grid[first.r][first.c].Y0 = sp.Y0
		grid[first.r][first.c].X1 = sp.X1
		grid[first.r][first.c].Y1 = sp.Y1
		grid[first.r][first.c].Label = sp.Label
		for _, idx := range covered[1:] {
			grid[idx.r][idx.c] = pdf.TSRCell{}
		}
	}

	return grid
}

// overlapsY 判断两单元格在 Y 方向是否重叠。
func overlapsY(a, b pdf.TSRCell) bool {
	return a.Y0 < b.Y1 && a.Y1 > b.Y0
}
