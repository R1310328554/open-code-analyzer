//go:build cgo

// CGO 构建：可调用 pdfoxide/DeepDOC 本地 PDF 引擎。
package parser

import (
	"context"
	"errors"
	"fmt"

	deepdocpdf "ragflow/internal/deepdoc/parser/pdf"
	deepdoctype "ragflow/internal/deepdoc/parser/type"
)

// ParseWithResult 按 parse_method 分发 PDF 解析；默认走 DeepDOC 本地引擎。
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
	cfg := deepdoctype.DefaultParserConfig()
	cfg.SkipOCR = false
	parser := deepdocpdf.NewParser(cfg)
	res := parsePDFWithDeepDocOptions(context.Background(), filename, data, pdfPostProcessOptions{
		outputFormat:       p.OutputFormat,
		zoom:               cfg.Zoom,
		enableMultiColumn:  p.EnableMultiColumn,
		flattenMediaToText: p.FlattenMediaToText,
		removeTOC:          p.RemoveTOC,
		removeHeaderFooter: p.RemoveHeaderFooter,
	}, parser.Parse)
	if res.Err != nil && errors.Is(res.Err, deepdocpdf.ErrNoPDFData) {
		return ParseResult{Err: fmt.Errorf("%w: %s", ErrPDFEngineUnavailable, filename)}
	}
	if res.Err != nil && res.Err.Error() == "deepdoc/pdf: cgo required" {
		return ParseResult{Err: fmt.Errorf("%w: %s", ErrPDFEngineUnavailable, filename)}
	}
	return res
}
// pdf_parser_cgo.go — CGO 构建下 PDF 解析入口，按 parse_method 分发至 DeepDOC 或远程引擎。
