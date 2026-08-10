//go:build cgo

// parse_cgo.go — CGO 构建下的 PDF 解析入口：从原始字节创建 pdf_oxide 引擎并驱动完整 ParseRaw 流水线。

package pdf

import (
	"context"
	"fmt"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// Parse 从原始 PDF 字节运行完整提取流水线；内部创建并管理 PDF 引擎生命周期（NewEngine → ParseRaw → Close）。
func (p *Parser) Parse(ctx context.Context, data []byte, docAnalyzer pdf.DocAnalyzer) (*pdf.ParseResult, error) {
	engine, err := NewEngine(data)
	if err != nil {
		return nil, fmt.Errorf("pdfoxide.NewEngine: %w", err)
	}
	defer engine.Close()

	return p.ParseRaw(ctx, engine, docAnalyzer)
}
