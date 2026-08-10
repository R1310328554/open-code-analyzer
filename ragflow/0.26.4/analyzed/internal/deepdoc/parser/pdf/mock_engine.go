// mock_engine.go — PDF 引擎 Mock 实现：为单元/集成测试提供最小 pdf.PDFEngine 桩，可注入预设字符与页数。

package pdf

import (
	"image"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// MockEngine 是 pdf.PDFEngine 的最小测试桩，可预设每页字符、页数与渲染尺寸。
type MockEngine struct {
	// Chars 按页号索引的预设字符列表
	Chars    map[int][]pdf.TextChar
	// NumPages 文档总页数（≤0 时 PageCount 返回 1）
	NumPages int
	// RenderW 模拟渲染宽度（≤0 默认 100）
	RenderW  int
	// RenderH 模拟渲染高度（≤0 默认 100）
	RenderH  int
}

// ExtractChars 返回预设页字符，无错误路径。
func (m *MockEngine) ExtractChars(pg int) ([]pdf.TextChar, error) {
	return m.Chars[pg], nil
}
// RenderPage 始终返回 ErrNoPDFData，模拟无原始 PDF 字节。
func (m *MockEngine) RenderPage(pg int, dpi float64) ([]byte, error) {
	return nil, ErrNoPDFData
}
// RenderPageImage 返回空白 RGBA 占位图，尺寸由 RenderW/RenderH 决定。
func (m *MockEngine) RenderPageImage(pg int, dpi float64) (image.Image, error) {
	w, h := m.RenderW, m.RenderH
	if w <= 0 {
		w = 100
	}
	if h <= 0 {
		h = 100
	}
	return image.NewRGBA(image.Rect(0, 0, w, h)), nil
}
// PageCount 返回 NumPages，未设置时默认为 1 页。
func (m *MockEngine) PageCount() (int, error) {
	if m.NumPages <= 0 {
		return 1, nil
	}
	return m.NumPages, nil
}
// RawData 无原始 PDF 数据。
func (m *MockEngine) RawData() []byte                  { return nil }
// Close 空实现，无需释放资源。
func (m *MockEngine) Close() error                     { return nil }
// Outlines 返回空大纲。
func (m *MockEngine) Outlines() ([]pdf.Outline, error) { return nil, nil }
