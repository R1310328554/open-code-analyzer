// char_adapter.go — Python 字符引擎适配器：从 charspy JSON 加载 Python 导出的字符流，供 Go 管道 parity 测试使用。

package tool

import (
	"encoding/json"
	"fmt"
	"image"
	"os"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// PythonCharEngine 从 charspy/{pdf}.json 加载字符实现 pdf.PDFEngine，与 Python 同输入做 parity 测试，输出差异即 Go 管道逻辑问题。
type PythonCharEngine struct {
	chars map[int][]pdf.TextChar // pageNum → chars
	pages int
}

// LoadPythonChars 从 charspy JSON 文件加载字符数据。
func LoadPythonChars(jsonPath string) (*PythonCharEngine, error) {
	data, err := os.ReadFile(jsonPath)
	if err != nil {
		return nil, fmt.Errorf("read charspy json: %w", err)
	}
	var wrapper struct {
		Pages [][]struct {
			Text     string  `json:"text"`
			X0       float64 `json:"x0"`
			X1       float64 `json:"x1"`
			Top      float64 `json:"top"`
			Bottom   float64 `json:"bottom"`
			FontName string  `json:"fontname"`
			Size     float64 `json:"size"`
		} `json:"pages"`
	}
	if err := json.Unmarshal(data, &wrapper); err != nil {
		return nil, fmt.Errorf("parse charspy json: %w", err)
	}

	chars := make(map[int][]pdf.TextChar, len(wrapper.Pages))
	for pg, pageChars := range wrapper.Pages {
		result := make([]pdf.TextChar, len(pageChars))
		for i, c := range pageChars {
			result[i] = pdf.TextChar{
				Text:       c.Text,
				X0:         c.X0,
				X1:         c.X1,
				Top:        c.Top,
				Bottom:     c.Bottom,
				FontName:   c.FontName,
				FontSize:   c.Size,
				PageNumber: pg,
			}
		}
		chars[pg] = result
	}
	return &PythonCharEngine{chars: chars, pages: len(wrapper.Pages)}, nil
}

// ExtractChars 返回指定页（0 起）的全部字符。
func (e *PythonCharEngine) ExtractChars(pageNum int) ([]pdf.TextChar, error) {
	if pageNum < 0 || pageNum >= e.pages {
		return nil, fmt.Errorf("page %d out of range [0, %d)", pageNum, e.pages)
	}
	return e.chars[pageNum], nil
}

// RenderPage 不支持渲染，parity 测试不使用。
func (e *PythonCharEngine) RenderPage(pageNum int, dpi float64) ([]byte, error) {
	return nil, fmt.Errorf("PythonCharEngine: RenderPage not supported")
}

// RenderPageImage 不支持，parity 测试不使用。
func (e *PythonCharEngine) RenderPageImage(pageNum int, dpi float64) (image.Image, error) {
	return nil, fmt.Errorf("PythonCharEngine: RenderPageImage not supported")
}

// PageCount 返回 PDF 页数。
func (e *PythonCharEngine) PageCount() (int, error) {
	return e.pages, nil
}

// RawData 返回 nil — 本引擎仅提供预加载字符，不持有 PDF 字节。
func (e *PythonCharEngine) RawData() []byte { return nil }

func (e *PythonCharEngine) Outlines() ([]pdf.Outline, error) { return nil, nil }

// Close 空操作。
func (e *PythonCharEngine) Close() error {
	return nil
}
