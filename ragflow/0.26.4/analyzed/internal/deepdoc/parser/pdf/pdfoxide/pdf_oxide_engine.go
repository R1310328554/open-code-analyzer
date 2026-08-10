//go:build cgo

// pdf_oxide_engine.go — pdf_oxide 引擎封装：字符提取含旋转/CropBox 校正，页渲染委托 pdfium 以匹配坐标系。

package pdfoxide

import (
	"image"
	"math"

	"ragflow/internal/deepdoc/parser/pdf/pdfium"
)

// Char 单字符，坐标已校正为与 pdfium 渲染一致的旋转后空间。
type Char struct {
	X0, X1      float64
	Top, Bottom float64
	Text        string
	FontName    string
	FontSize    float64
	PageNumber  int
}

// Engine 包装 pdf_oxide Document，负责字符提取与页渲染调度。
type Engine struct {
	doc     *Document
	rawData []byte
}

// NewEngine 从字节打开 PDF 并返回 Engine。
func NewEngine(pdfBytes []byte) (*Engine, error) {
	doc, err := OpenBytes(pdfBytes)
	if err != nil {
		return nil, err
	}
	return &Engine{doc: doc, rawData: pdfBytes}, nil
}

// RawData 返回原始 PDF 字节供 pdfium 渲染与大纲提取。
func (e *Engine) RawData() []byte { return e.rawData }

// ExtractChars 提取去重字符并将 pdf_oxide 未旋转坐标变换为 pdfium 有效坐标系。
func (e *Engine) ExtractChars(pageNum int) ([]Char, error) {
	chars, err := e.doc.GetDedupePageChars(pageNum, 0.5)
	if err != nil {
		return nil, err
	}

	// pdf_oxide returns characters in the original (unrotated) PDF
	// coordinate space.  Rotate to match pdfium's effective (post-
	// /Rotate) coordinate space used for rendering and DLA/OCR.
	//
	// Rotation detection uses two sources:
	// 1. Byte-scan for explicit /Rotate (finds directly-defined values).
	// 2. Dimension comparison: pdf_oxide raw vs pdfium effective.
	//    If dimensions are swapped, the page has implicit rotation
	//    (inherited /Rotate or ContentBox rotation).
	rawW, rawH, _ := e.doc.PageSize(pageNum)
	effW, effH, pdfErr := pdfium.PageSize(e.rawData, pageNum)
	if pdfErr != nil {
		effW, effH = rawW, rawH
	}

	dimSwapped := rawW > 0 && rawH > 0 && effW > 0 && effH > 0 &&
		math.Abs(rawW-effH) < 1 && math.Abs(rawH-effW) < 1

	rawRot := parsePageRotationFromRaw(e.rawData, pageNum)

	needsRotate := false
	rotation90 := false
	rotation180 := false

	if dimSwapped {
		needsRotate = true
		if rawRot == 270 {
			rotation90 = false
		} else {
			rotation90 = true
		}
	} else if rawRot == 90 || rawRot == 270 {
		// Explicit /Rotate found but dimension-swap check failed
		// (e.g. CropBox alters effective dimensions).  Trust the
		// explicit /Rotate value.
		needsRotate = true
		rotation90 = (rawRot != 270)
	} else if rawRot == 180 {
		needsRotate = true
		rotation180 = true
	}

	// CropBox 校正：CropBox 与 MediaBox 不一致时平移原点。
	var cropDX, cropDY float64
	realCrop, hasCrop := parseCropBoxFromRaw(e.rawData, pageNum)
	if hasCrop {
		cropH := realCrop[3] - realCrop[1]
		oxideCropH := rawH
		if cropH > 0 && (realCrop[0] != 0 || realCrop[1] != 0 ||
			math.Abs(realCrop[3]-oxideCropH) > 0.5) {
			cropDX = -realCrop[0]
			cropDY = -(oxideCropH - realCrop[3])
		}
	}

	// When rotation is applied, the crop shift must be applied AFTER
	// rotation, using the correct axes for the rotated coordinate space.
	rotateCropDX, rotateCropDY := cropDX, cropDY
	if needsRotate && (cropDX != 0 || cropDY != 0) {
		switch {
		case rotation90:
			// rotate(x+cropDX,y+cropDY) = (rawH-(y+cropDY),x+cropDX)
			// = rotate(x,y) + (-cropDY, +cropDX)
			// cropDX=-30,cropDY=-10 => post-rotate shift = (+10,-30)
			rotateCropDX = -cropDY
			rotateCropDY = cropDX
		case rotation180:
			rotateCropDX = -cropDX
			rotateCropDY = -cropDY
		default: // 270 CW
			rotateCropDX = cropDY
			rotateCropDY = -cropDX
		}
		cropDX, cropDY = 0, 0
	}

	result := make([]Char, len(chars))
	for i, c := range chars {
		x0, x1 := c.X0, c.X1
		top, bottom := c.Top, c.Bottom

		x0 += cropDX
		x1 += cropDX
		top += cropDY
		bottom += cropDY

		if needsRotate {
			origX0, origX1 := x0, x1
			origTop, origBottom := top, bottom

			switch {
			case rotation90:
				x0 = rawH - origBottom
				x1 = rawH - origTop
				top = origX0
				bottom = origX1
			case rotation180:
				x0 = rawW - origX1
				x1 = rawW - origX0
				top = rawH - origBottom
				bottom = rawH - origTop
			default: // 270 CW
				x0 = origTop
				x1 = origBottom
				top = rawW - origX1
				bottom = rawW - origX0
			}

			if x0 > x1 {
				x0, x1 = x1, x0
			}
			if top > bottom {
				top, bottom = bottom, top
			}
		}

		// 在最终坐标系施加旋转后的 CropBox 平移。
		x0 += rotateCropDX
		x1 += rotateCropDX
		top += rotateCropDY
		bottom += rotateCropDY

		result[i] = Char{
			X0: x0, X1: x1, Top: top, Bottom: bottom,
			Text: c.Text, FontName: c.Fontname, FontSize: c.Size,
			PageNumber: pageNum,
		}
	}
	return result, nil
}

// parsePageRotationFromRaw 顺序扫描 /Rotate 值；仅检测页对象直接定义的旋转；继承旋转由 ExtractChars 尺寸对比兜底。
func parsePageRotationFromRaw(data []byte, pageIdx int) int {
	var rotations []int
	rest := data
	for {
		idx := -1
		for i := 0; i < len(rest)-7; i++ {
			if rest[i] == '/' && rest[i+1] == 'R' && rest[i+2] == 'o' &&
				rest[i+3] == 't' && rest[i+4] == 'a' && rest[i+5] == 't' &&
				rest[i+6] == 'e' {
				idx = i
				break
			}
		}
		if idx < 0 {
			break
		}
		rest = rest[idx+7:]
		for len(rest) > 0 && (rest[0] == ' ' || rest[0] == '\t' || rest[0] == '\n' || rest[0] == '\r') {
			rest = rest[1:]
		}
		if len(rest) == 0 {
			break
		}
		val := 0
		i := 0
		for i < len(rest) && rest[i] >= '0' && rest[i] <= '9' {
			val = val*10 + int(rest[i]-'0')
			i++
		}
		if i > 0 {
			rotations = append(rotations, val)
		}
		rest = rest[i:]
	}
	if pageIdx < len(rotations) {
		return rotations[pageIdx]
	}
	return 0
}

// RenderPageImage 委托 pdfium 渲染（正确应用 /Rotate），与字符坐标及 DLA 一致；pdf_oxide 不应用 /Rotate 故不作回退。
func (e *Engine) RenderPageImage(pageNum int, dpi float64) (image.Image, error) {
	return pdfium.RenderPage(e.rawData, pageNum, dpi)
}

func (e *Engine) RenderPage(pageNum int, dpi float64) ([]byte, error) {
	result, err := e.doc.RenderPage(pageNum, dpi)
	if err != nil {
		return nil, err
	}
	return result.Data, nil
}

// PageSize 优先 pdfium 有效尺寸（含 /Rotate）；失败回退 pdf_oxide 原始尺寸。
func (e *Engine) PageSize(pageNum int) (float64, float64, error) {
	w, h, err := pdfium.PageSize(e.rawData, pageNum)
	if err != nil {
		return e.doc.PageSize(pageNum)
	}
	return w, h, nil
}
// PageCount 返回文档页数。
func (e *Engine) PageCount() (int, error) { return e.doc.PageCount() }
// Close 关闭底层 Document。
func (e *Engine) Close() error            { e.doc.Close(); return nil }
