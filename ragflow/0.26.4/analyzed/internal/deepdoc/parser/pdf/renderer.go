// renderer.go — 页渲染调度：默认走引擎 RenderPageImage；CGO 构建时由 renderer_pdfium 替换为 pdfium 路径。

package pdf

import (
	"image"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	"reflect"
)

// renderFn 当前页渲染函数；默认 fallbackRender；CGO 构建时 renderer_pdfium.init 替换为 pdfiumRender。
var renderFn = fallbackRender

// RenderPageToImage 以 DlaDPI(216) 渲染页图，供下游 DLA/TSR/OCR 使用。
func RenderPageToImage(engine pdf.PDFEngine, pageNum int) (image.Image, error) {
	return renderFn(engine, pageNum)
}

// fallbackRender 调用引擎自带 RenderPageImage，无 C 依赖；并防御 typed-nil 接口误报非 nil。
func fallbackRender(engine pdf.PDFEngine, pageNum int) (image.Image, error) {
	img, err := engine.RenderPageImage(pageNum, pdf.DlaDPI)
	if err != nil {
		return nil, err
	}
	// Guard against typed-nil (e.g. (*image.RGBA)(nil) returned as non-nil
	// interface).  The plain img==nil check misses that case.
	if img == nil {
		return nil, ErrNoPDFData
	}
	if rv := reflect.ValueOf(img); rv.Kind() == reflect.Ptr && rv.IsNil() {
		return nil, ErrNoPDFData
	}
	return img, nil
}

// ErrNoPDFData 引擎无原始 PDF 字节可渲染时返回。
var ErrNoPDFData = &pdfError{"engine has no raw PDF data"}

// pdfError 简单错误包装类型。
type pdfError struct{ msg string }

func (e *pdfError) Error() string { return e.msg }
