//go:build cgo

// renderer_pdfium.go — CGO 构建专用：用 pdfium 高质量光栅化替代 fallbackRender，提升 OCR/DLA 准确率。

package pdf

import (
	"image"

	"ragflow/internal/deepdoc/parser/pdf/pdfium"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// pdfiumRender 使用 pdfium C 库高质量光栅化（抗锯齿、hinting），对扫描件与低质量 PDF 的 OCR/DLA 至关重要。
func pdfiumRender(engine pdf.PDFEngine, pageNum int) (image.Image, error) {
	raw := engine.RawData()
	if raw == nil {
		// PythonCharEngine and mocks don't carry PDF bytes —
		// fall back to the engine's own RenderPageImage.
		return fallbackRender(engine, pageNum)
	}
	// Guard against typed nil: (*image.RGBA)(nil) wrapped as non-nil interface
	// would panic on downstream .Bounds() / .At() calls.
	img, err := pdfium.RenderPage(raw, pageNum, 216)
	if err != nil {
		return nil, err
	}
	if img == nil {
		return nil, ErrNoPDFData
	}
	return img, nil
}

// init 注册 pdfiumRender 为全局 renderFn。
func init() {
	renderFn = pdfiumRender
}
