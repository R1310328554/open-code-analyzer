//go:build !cgo

// 非 CGO 构建：DeepDOC 本地 PDF 引擎不可用，远程 parse_method 仍可用。
package parser

import (
	"fmt"
)

// ParseWithResult 非 CGO 下仅支持远程引擎；deepdoc 返回 ErrPDFEngineUnavailable。
func (p *PDFParser) ParseWithResult(filename string, data []byte) ParseResult {
	if err := p.validateParseMethod(); err != nil {
		return ParseResult{Err: err}
	}
	switch normalizePDFParseMethod(p.ParseMethod) {
	case "plain_text":
		return parsePDFWithPlainText(filename, data, p)
	case "mineru":
		return parsePDFWithMinerU(filename, data, p)
	case "paddleocr":
		return parsePDFWithPaddleOCR(filename, data, p)
	case "docling":
		return parsePDFWithDocling(filename, data, p)
	case "opendataloader":
		return parsePDFWithOpenDataLoader(filename, data, p)
	case "somark":
		return parsePDFWithSoMark(filename, data, p)
	case "tcadp":
		return parsePDFWithTCADP(filename, data, p)
	}
	if len(data) == 0 {
		return emptyPDFResult(filename)
	}
	return ParseResult{
		Err: fmt.Errorf("%w: %s", ErrPDFEngineUnavailable, filename),
	}
}
// pdf_parser_nocgo.go — 非 CGO 构建下 PDF 解析入口，DeepDOC 本地引擎不可用。
