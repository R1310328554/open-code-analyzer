package parser

import (
	"fmt"
	"os"
	"strings"

	models "ragflow/internal/entity/models"
)

// parsePDFWithPaddleOCR 调用 PaddleOCR-VL layout-parsing 端点并将 Markdown 结果分块。
func parsePDFWithPaddleOCR(filename string, data []byte, parser *PDFParser) ParseResult {
	if len(data) == 0 {
		return emptyPDFResult(filename)
	}
	baseURL := strings.TrimSpace(parser.PaddleOCRBaseURL)
	if baseURL == "" {
		baseURL = strings.TrimSpace(os.Getenv("PADDLEOCR_BASE_URL"))
	}
	if baseURL == "" {
		baseURL = strings.TrimSpace(os.Getenv("PADDLEOCR_API_URL"))
	}
	if baseURL == "" {
		return ParseResult{Err: fmt.Errorf("parser: PaddleOCR requires paddleocr_base_url or PADDLEOCR_BASE_URL")}
	}
	apiKey := parser.PaddleOCRAPIKey
	if strings.TrimSpace(apiKey) == "" {
		apiKey = strings.TrimSpace(os.Getenv("PADDLEOCR_ACCESS_TOKEN"))
	}
	algorithm := strings.TrimSpace(parser.PaddleOCRAlgorithm)
	if algorithm == "" {
		algorithm = strings.TrimSpace(os.Getenv("PADDLEOCR_ALGORITHM"))
	}
	if algorithm == "" {
		algorithm = "PaddleOCR-VL"
	}

	driver := models.NewPaddleOCRLocalModel(
		map[string]string{"default": baseURL},
		models.URLSuffix{OCR: "layout-parsing"},
	)
	apiConfig := &models.APIConfig{
		BaseURL: &baseURL,
	}
	if apiKey != "" {
		apiConfig.ApiKey = &apiKey
	}

	resp, err := driver.OCRFile(&algorithm, data, &filename, apiConfig, &models.OCRConfig{
		Algorithm: algorithm,
	})
	if err != nil {
		return ParseResult{Err: fmt.Errorf("parser: PaddleOCR OCRFile: %w", err)}
	}
	if resp == nil || resp.Text == nil {
		return ParseResult{Err: fmt.Errorf("parser: PaddleOCR returned empty text")}
	}
	pageCount := 1
	if resp.Text != nil && strings.TrimSpace(*resp.Text) == "" {
		pageCount = 0
	}
	return parseMinerUMarkdownResult(filename, *resp.Text, parser.OutputFormat, pageCount)
}
// pdf_parser_paddleocr.go — PaddleOCR-VL 布局解析远程调用封装。
