//go:build !cgo

// parse_nocgo.go — 非 CGO 构建桩：DeepDOC PDF 流水线不可用，上层将转为 parser.ErrPDFEngineUnavailable。

package pdf

import (
	"context"
	"fmt"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// Parse 为非 CGO 环境下的占位实现；外层 parser 包会将此错误转换为 ErrPDFEngineUnavailable。
func (p *Parser) Parse(ctx context.Context, data []byte, docAnalyzer pdf.DocAnalyzer) (*pdf.ParseResult, error) {
	_ = ctx
	_ = data
	_ = docAnalyzer
	return nil, fmt.Errorf("deepdoc/pdf: cgo required")
}
