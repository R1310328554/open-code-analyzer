package service

// document_upload_helpers.go 提供上传场景下的解析器选择与内容哈希辅助。

import (
	"encoding/hex"
	"path/filepath"
	"regexp"
	"strings"

	"ragflow/internal/utility"

	"github.com/zeebo/xxh3"
)

var (
	presentationUploadPattern = regexp.MustCompile(`(?i)\.(ppt|pptx|pages)$`)
	emailUploadPattern        = regexp.MustCompile(`(?i)\.(msg|eml)$`)
)

// selectUploadParser 按文件类型/扩展名选择解析器，对齐 Python get_parser。
func selectUploadParser(docType utility.FileType, filename, defaultParser string) string {
	switch docType {
	case utility.FileTypeVISUAL:
		return "picture"
	case utility.FileTypeAURAL:
		return "audio"
	}
	base := filepath.Base(strings.TrimSpace(filename))
	switch {
	case presentationUploadPattern.MatchString(base):
		return "presentation"
	case emailUploadPattern.MatchString(base):
		return "email"
	default:
		return defaultParser
	}
}

// contentHashHex 计算 blob 的 xxhash128 十六进制摘要。
func contentHashHex(blob []byte) string {
	sum := xxh3.Hash128(blob).Bytes()
	return hex.EncodeToString(sum[:])
}
// document_upload_helpers.go — 上传解析器选择与内容 xxhash 摘要辅助函数。
