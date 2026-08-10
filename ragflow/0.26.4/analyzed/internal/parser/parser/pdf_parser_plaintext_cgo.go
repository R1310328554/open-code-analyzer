//go:build cgo

// CGO 构建：plain_text 模式经 pdfoxide 逐页提取文本层。
package parser

import (
	"fmt"

	"ragflow/internal/deepdoc/parser/pdf/pdfoxide"
)

// parsePDFWithPlainText 使用 pdfoxide 打开 PDF 并逐页 GetPageText，不做 OCR。
func parsePDFWithPlainText(filename string, data []byte, parser *PDFParser) ParseResult {
	if len(data) == 0 {
		return emptyPDFResult(filename)
	}
	doc, err := pdfoxide.OpenBytes(data)
	if err != nil {
		return ParseResult{Err: fmt.Errorf("parser: plain_text open: %w", err)}
	}
	defer doc.Close()

	pageCount, err := doc.PageCount()
	if err != nil {
		return ParseResult{Err: fmt.Errorf("parser: plain_text page count: %w", err)}
	}
	items := make([]map[string]any, 0, pageCount)
	for page := 0; page < pageCount; page++ {
		text, err := doc.GetPageText(page)
		if err != nil {
			return ParseResult{Err: fmt.Errorf("parser: plain_text page %d: %w", page+1, err)}
		}
		items = append(items, map[string]any{
			"text":         text,
			"doc_type_kwd": "text",
			"page_number":  page + 1,
		})
	}
	return pdfItemsToResult(filename, items, parser.OutputFormat, pageCount)
}
// pdf_parser_plaintext_cgo.go — CGO 下 pdfoxide 逐页提取纯文本。
