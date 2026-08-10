//go:build cgo

// pdfoxide_bridge.go — pdf.PDFEngine 适配层：将 pdfoxide.Engine 桥接到统一 PDFEngine 接口，供 Parser 使用。

package pdf

import (
	"image"

	"ragflow/internal/deepdoc/parser/pdf/pdfium"
	"ragflow/internal/deepdoc/parser/pdf/pdfoxide"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// PDFOxideEngine 将 pdfoxide.Engine 适配为 pdf.PDFEngine 接口。
type PDFOxideEngine struct {
	Inner *pdfoxide.Engine
}

// NewEngine 从 PDF 字节创建 pdf_oxide 支持的 PDFEngine 实例。
func NewEngine(pdfBytes []byte) (pdf.PDFEngine, error) {
	eng, err := pdfoxide.NewEngine(pdfBytes)
	if err != nil {
		return nil, err
	}
	return &PDFOxideEngine{Inner: eng}, nil
}

// RawData 透传原始 PDF 字节。
func (e *PDFOxideEngine) RawData() []byte         { return e.Inner.RawData() }
// PageCount 透传页数查询。
func (e *PDFOxideEngine) PageCount() (int, error) { return e.Inner.PageCount() }
// Close 关闭底层引擎。
func (e *PDFOxideEngine) Close() error            { return e.Inner.Close() }

// Outlines 经 pdfium 提取大纲并映射为 pdf.Outline 类型。
func (e *PDFOxideEngine) Outlines() ([]pdf.Outline, error) {
	ol := pdfium.ExtractOutlines(e.Inner.RawData())
	result := make([]pdf.Outline, len(ol))
	for i, o := range ol {
		result[i] = pdf.Outline{Title: o.Title, Level: o.Level, PageNumber: o.PageNumber}
	}
	return result, nil
}

// RenderPage 返回 pdf_oxide 渲染的原始 RGBA 字节。
func (e *PDFOxideEngine) RenderPage(pageNum int, dpi float64) ([]byte, error) {
	return e.Inner.RenderPage(pageNum, dpi)
}

// RenderPageImage 委托 pdfium 高质量渲染页图。
func (e *PDFOxideEngine) RenderPageImage(pageNum int, dpi float64) (image.Image, error) {
	return e.Inner.RenderPageImage(pageNum, dpi)
}

// ExtractChars 提取字符并转换为 pdf.TextChar 切片。
func (e *PDFOxideEngine) ExtractChars(pageNum int) ([]pdf.TextChar, error) {
	chars, err := e.Inner.ExtractChars(pageNum)
	if err != nil {
		return nil, err
	}
	result := make([]pdf.TextChar, len(chars))
	for i, c := range chars {
		result[i] = pdf.TextChar{
			X0: c.X0, X1: c.X1, Top: c.Top, Bottom: c.Bottom,
			Text: c.Text, FontName: c.FontName, FontSize: c.FontSize,
			PageNumber: c.PageNumber,
		}
	}
	return result, nil
}
