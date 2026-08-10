// office_parsers_cgo_shared.go — CGO 构建共享：OfficeOxide 常量与 htmlEscape 辅助。

//go:build cgo

package parser

import "html"

// OfficeOxide 为 office_oxide 后端的 lib_type 标识符。
const OfficeOxide = "office_oxide"

func htmlEscape(s string) string {
	return html.EscapeString(s)
}

// htmlEscape 供 Office 解析路径 HTML 转义；与 CGO 版 doc/docx/ppt/xls 解析器共用。
