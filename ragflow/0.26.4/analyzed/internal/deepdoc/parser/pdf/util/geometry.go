// geometry.go — PDF 文本几何度量：字符/框宽高、间距、重叠比、中位高度、FastCrop 与矩形相交；对齐 Python pdf_parser 中 __char_width/_x_dis/_y_dis 等。

package util

import (
	"image"
	"math"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	"sort"
)

// CharWidth 返回平均字符宽度 (x1-x0)/len(text)；空文本为 0。
//
// Example:
//
//	c := pdf.TextChar{X0: 50, X1: 58, Text: "A"}
//	w := CharWidth(c)  // (58-50)/1 = 8
func CharWidth(c pdf.TextChar) float64 {
	if len(c.Text) == 0 {
		return 0
	}
	return (c.X1 - c.X0) / float64(len(c.Text))
}

// CharHeight 返回字符高度（PDF 点）bottom-top。
//
// Python: pdf_parser.py:110 __height()
//
// Example:
//
//	c := pdf.TextChar{Top: 200, Bottom: 212}
//	h := CharHeight(c)  // 212-200 = 12
func CharHeight(c pdf.TextChar) float64 {
	return c.Bottom - c.Top
}

// XDis 计算两字符最小水平间距，用于同行判定。
// Used to determine if they belong to the same text line.
//
// Python: pdf_parser.py:113 _x_dis()
//
// Example:
//
//	a := pdf.TextChar{X0: 50, X1: 58}
//	b := pdf.TextChar{X0: 60, X1: 68}
//	d := XDis(a, b)  // min(|58-60|=2, |50-68|=18, |108-128|/2=10) = 2
func XDis(a, b pdf.TextChar) float64 {
	return min(
		math.Abs(a.X1-b.X0),
		min(math.Abs(a.X0-b.X1), math.Abs(a.X0+a.X1-b.X0-b.X1)/2),
	)
}

// YDis 计算两字符中心线垂直距离；正表示 b 在 a 下方。
// Positive means b is below a.
//
// Python: pdf_parser.py:116 _y_dis()
//
// Example:
//
//	a := pdf.TextChar{Top: 100, Bottom: 112}
//	b := pdf.TextChar{Top: 114, Bottom: 126}
//	d := YDis(a, b)  // (114+126-100-112)/2 = 14
func YDis(a, b pdf.TextChar) float64 {
	return (b.Top + b.Bottom - a.Top - a.Bottom) / 2
}

// BoxWidth 返回文本框宽度。
func BoxWidth(b pdf.TextBox) float64 {
	return b.X1 - b.X0
}

// BoxHeight 返回文本框高度。
func BoxHeight(b pdf.TextBox) float64 {
	return b.Bottom - b.Top
}

// BoxYDis 两框中心线垂直距离。
// Positive means b2 is below b1.
func BoxYDis(b1, b2 pdf.TextBox) float64 {
	return (b2.Top + b2.Bottom - b1.Top - b1.Bottom) / 2
}

// BoxXDis 两框水平距离。
func BoxXDis(b1, b2 pdf.TextBox) float64 {
	return min(
		math.Abs(b1.X1-b2.X0),
		min(math.Abs(b1.X0-b2.X1), math.Abs(b1.X0+b1.X1-b2.X0-b2.X1)/2),
	)
}

// OverlapRatio 交集面积除以 denom 面积。
// Returns 0 when denom has zero area or there is no intersection.
func OverlapRatio(a, b, denom pdf.Rectangular) float64 {
	inter := OverlapInter(a, b)
	if inter <= 0 {
		return 0
	}
	d := Area(denom)
	if d <= 0 {
		return 0
	}
	return inter / d
}

// OverlapRatioMax 交集除以两矩形较大面积。
func OverlapRatioMax(a, b pdf.Rectangular) float64 {
	inter := OverlapInter(a, b)
	if inter <= 0 {
		return 0
	}
	d := max(Area(a), Area(b))
	if d <= 0 {
		return 0
	}
	return inter / d
}

// OverlapX 仅 X 轴方向重叠宽度比；用于 _naive_vertical_merge。
//
// Python: pdf_parser.py:964-965 overlap calculation in _naive_vertical_merge
func OverlapX(a, b pdf.Rectangular) float64 {
	ax0, _, ax1, _ := a.Bounds()
	bx0, _, bx1, _ := b.Bounds()
	overlap := math.Max(0, math.Min(ax1, bx1)-math.Max(ax0, bx0))
	wA := ax1 - ax0
	wB := bx1 - bx0
	minWidth := math.Max(1, math.Min(wA, wB))
	return overlap / minWidth
}

// MedianCharHeight 页内字符高度中位数，作纵向间距参考单位。
func MedianCharHeight(chars []pdf.TextChar) float64 {
	heights := make([]float64, len(chars))
	for i, c := range chars {
		heights[i] = CharHeight(c)
	}
	return medianFloat64(heights, 10)
}

// MedianCharWidth 页内字符宽度中位数。
// matching Python's np.median(char width) in __images__ (pdf_parser.py:1553).
func MedianCharWidth(chars []pdf.TextChar) float64 {
	widths := make([]float64, len(chars))
	for i, c := range chars {
		widths[i] = CharWidth(c)
	}
	return medianFloat64(widths, 5)
}

// MedianHeight 文本框高度中位数；空列表回退 10。
// Falls back to 10 if list is empty.
//
// Python: np.median([b["bottom"]-b["top"] for b in bxs]) or 10
// in _naive_vertical_merge:941
func MedianHeight(boxes []pdf.TextBox) float64 {
	heights := make([]float64, len(boxes))
	for i, b := range boxes {
		heights[i] = b.Bottom - b.Top
	}
	return medianFloat64(heights, 10)
}

// medianFloat64 求中位数，空切片返回 fallback。
func medianFloat64(vals []float64, fallback float64) float64 {
	if len(vals) == 0 {
		return fallback
	}
	sort.Float64s(vals)
	n := len(vals)
	if n%2 == 0 {
		return (vals[n/2-1] + vals[n/2]) / 2
	}
	return vals[n/2]
}

// Rect 轻量矩形，坐标空间由调用方决定（像素或 PDF 点）。
type Rect struct{ X0, Y0, X1, Y1 float64 }

func (r Rect) Bounds() (float64, float64, float64, float64) { return r.X0, r.Y0, r.X1, r.Y1 }

// RectOverlap 两 Rect 重叠比（OverlapRatioMax）。
func RectOverlap(a, b Rect) float64 {
	return OverlapRatioMax(a, b)
}

// FastCrop 从 src 裁剪矩形到 RGBA；RGBA 源走 Pix 行拷贝快路径。
func FastCrop(src image.Image, x0, y0, x1, y1 int) *image.RGBA {
	//  clamp 到源图边界
	b := src.Bounds()
	if x0 < b.Min.X {
		x0 = b.Min.X
	}
	if y0 < b.Min.Y {
		y0 = b.Min.Y
	}
	if x1 > b.Max.X {
		x1 = b.Max.X
	}
	if y1 > b.Max.Y {
		y1 = b.Max.Y
	}
	if x0 >= x1 || y0 >= y1 {
		return image.NewRGBA(image.Rect(0, 0, 1, 1))
	}
	w, h := x1-x0, y1-y0
	dst := image.NewRGBA(image.Rect(0, 0, w, h))
	if rgba, ok := src.(*image.RGBA); ok {
		for y := y0; y < y1; y++ {
			srcRow := rgba.Pix[rgba.PixOffset(x0, y):rgba.PixOffset(x1, y)]
			dstRow := dst.Pix[dst.PixOffset(0, y-y0):]
			copy(dstRow, srcRow)
		}

	} else {
		for y := y0; y < y1; y++ {
			for x := x0; x < x1; x++ {
				dst.Set(x-x0, y-y0, src.At(x, y))
			}
		}
	}
	return dst
}

// ── 几何辅助纯函数（自 type/types.go 迁出）──

// Area 返回 Rectangular 面积；退化矩形为 0。
func Area(r pdf.Rectangular) float64 {
	x0, y0, x1, y1 := r.Bounds()
	if x1 <= x0 || y1 <= y0 {
		return 0
	}
	return (x1 - x0) * (y1 - y0)
}

// RectOverlapInter 两轴对齐矩形交集面积。
func RectOverlapInter(x0a, y0a, x1a, y1a, x0b, y0b, x1b, y1b float64) float64 {
	x0 := max(x0a, x0b)
	y0 := max(y0a, y0b)
	x1 := min(x1a, x1b)
	y1 := min(y1a, y1b)
	if x0 >= x1 || y0 >= y1 {
		return 0
	}
	return (x1 - x0) * (y1 - y0)
}

// OverlapInter 两 Rectangular 交集面积。
func OverlapInter(a, b pdf.Rectangular) float64 {
	ax0, ay0, ax1, ay1 := a.Bounds()
	bx0, by0, bx1, by1 := b.Bounds()
	return RectOverlapInter(ax0, ay0, ax1, ay1, bx0, by0, bx1, by1)
}

// OverlapRatioA 交集除以 a 的面积。
func OverlapRatioA(a, b pdf.Rectangular) float64 {
	inter := OverlapInter(a, b)
	if inter <= 0 {
		return 0
	}
	d := Area(a)
	if d <= 0 {
		return 0
	}
	return inter / d
}
