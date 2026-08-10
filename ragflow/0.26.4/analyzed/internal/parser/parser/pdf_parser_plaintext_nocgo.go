//go:build !cgo

// 非 CGO 构建：plain_text 本地提取不可用。
package parser

import "fmt"

// parsePDFWithPlainText 非 CGO 下返回 ErrPDFEngineUnavailable。
func parsePDFWithPlainText(filename string, data []byte, parser *PDFParser) ParseResult {
	if len(data) == 0 {
		return emptyPDFResult(filename)
	}
	return ParseResult{Err: fmt.Errorf("%w: %s", ErrPDFEngineUnavailable, filename)}
}
// pdf_parser_plaintext_nocgo.go — 非 CGO 构建拒绝 plain_text 本地提取。
