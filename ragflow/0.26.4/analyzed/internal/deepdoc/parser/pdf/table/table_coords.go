// table_coords.go — 表格相关坐标空间转换：在 crop 像素空间、页面 72 DPI 空间之间转换 TSR 单元格与 TextBox，并复制网格注释字段。

package table

import (
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// ── 坐标空间转换辅助 ──

// CellToPageSpace 将单元格从 crop 像素空间转为页面全局 72 DPI 空间。
func CellToPageSpace(c pdf.TSRCell, cropOffX, cropOffY, scale float64) pdf.TSRCell {
	return pdf.TSRCell{
		X0: (c.X0 + cropOffX) / scale, Y0: (c.Y0 + cropOffY) / scale,
		X1: (c.X1 + cropOffX) / scale, Y1: (c.Y1 + cropOffY) / scale,
		Text: c.Text, Label: c.Label,
	}
}

// CellAddOffset 对单元格坐标加 crop 偏移（仍在像素空间）。
func CellAddOffset(c pdf.TSRCell, offX, offY float64) pdf.TSRCell {
	return pdf.TSRCell{
		X0: c.X0 + offX, Y0: c.Y0 + offY, X1: c.X1 + offX, Y1: c.Y1 + offY,
		Text: c.Text, Label: c.Label,
	}
}

// CellSliceToPageSpace 批量将单元格从 crop 像素转为页面 DPI 空间。
func CellSliceToPageSpace(cells []pdf.TSRCell, cropOffX, cropOffY, scale float64) []pdf.TSRCell {
	out := make([]pdf.TSRCell, len(cells))
	for i, c := range cells {
		out[i] = CellToPageSpace(c, cropOffX, cropOffY, scale)
	}
	return out
}

// BoxToCropSpace 将 TextBox 从 PDF 点空间转为 crop 像素空间。
func BoxToCropSpace(b pdf.TextBox, scale, cropOffX, cropOffY float64) pdf.TextBox {
	return pdf.TextBox{
		X0: b.X0*scale - cropOffX, X1: b.X1*scale - cropOffX,
		Top: b.Top*scale - cropOffY, Bottom: b.Bottom*scale - cropOffY,
		Text: b.Text,
	}
}

// CopyBoxAnnotations 将 src 的 DLA/TSR 注释字段（R/C/H/SP 等）复制到 dst。
func CopyBoxAnnotations(dst, src *pdf.TextBox) {
	dst.R = src.R
	dst.C = src.C
	dst.RTop = src.RTop
	dst.RBott = src.RBott
	dst.H = src.H
	dst.HTop = src.HTop
	dst.HBott = src.HBott
	dst.HLeft = src.HLeft
	dst.HRight = src.HRight
	dst.CLeft = src.CLeft
	dst.CRight = src.CRight
	dst.SP = src.SP
}
