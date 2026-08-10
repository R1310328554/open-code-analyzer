//go:build windows || darwin

// ui 包内附件文本提取：按扩展名将 PDF、二进制或 UTF-8 文件转为可注入聊天的纯文本。
package ui

import (
	"bytes"
	"fmt"
	"path/filepath"
	"slices"
	"strings"
	"unicode/utf8"

	"github.com/ledongthuc/pdf"
)

// convertBytesToText 根据文件名扩展名将原始字节转为文本；PDF 走专用提取，二进制返回占位描述。
func convertBytesToText(data []byte, filename string) string {
	ext := strings.ToLower(filepath.Ext(filename))

	if ext == ".pdf" {
		text, err := extractPDFText(data)
		if err != nil {
			return fmt.Sprintf("[PDF file - %d bytes - failed to extract text: %v]", len(data), err)
		}
		if strings.TrimSpace(text) == "" {
			return fmt.Sprintf("[PDF file - %d bytes - no text content found]", len(data))
		}
		return text
	}

	// 已知二进制扩展名列表，直接返回类型与大小占位
	binaryExtensions := []string{
		".xlsx", ".pptx", ".zip", ".tar", ".gz", ".rar",
		".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".ico",
		".mp3", ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm",
		".exe", ".dll", ".so", ".dylib", ".app", ".dmg", ".pkg",
	}

	if slices.Contains(binaryExtensions, ext) {
		return fmt.Sprintf("[Binary file of type %s - %d bytes]", ext, len(data))
	}

	if utf8.Valid(data) {
		return string(data)
	}

	// 非合法 UTF-8 时返回占位说明
	return fmt.Sprintf("[Binary file - %d bytes - not valid UTF-8]", len(data))
}

// extractPDFText 从 PDF 字节流逐页提取纯文本，页间以分隔线连接。
func extractPDFText(data []byte) (string, error) {
	reader := bytes.NewReader(data)
	pdfReader, err := pdf.NewReader(reader, int64(len(data)))
	if err != nil {
		return "", fmt.Errorf("failed to create PDF reader: %w", err)
	}

	var textBuilder strings.Builder
	numPages := pdfReader.NumPage()

	for i := 1; i <= numPages; i++ {
		page := pdfReader.Page(i)
		if page.V.IsNull() {
			continue
		}

		text, err := page.GetPlainText(nil)
		if err != nil {
			// 单页失败时记录并继续处理其余页
			continue
		}

		if strings.TrimSpace(text) != "" {
			if textBuilder.Len() > 0 {
				textBuilder.WriteString("\n\n--- Page ")
				textBuilder.WriteString(fmt.Sprintf("%d", i))
				textBuilder.WriteString(" ---\n")
			}
			textBuilder.WriteString(text)
		}
	}

	return textBuilder.String(), nil
}
