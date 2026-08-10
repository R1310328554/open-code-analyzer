// pdf_vision_dispatch_nocgo.go — 非 CGO 构建：PDF 视觉渲染不可用 stub。

//go:build !cgo

package component

import "fmt"

// defaultRenderPDFVisionPages 非 CGO 构建返回明确错误，提示需 cgo 渲染支持。
func defaultRenderPDFVisionPages(_ []byte) ([]pdfVisionPage, error) {
	return nil, fmt.Errorf("tenant-aware PDF IMAGE2TEXT backend requires cgo rendering support")
}

// 租户感知 PDF IMAGE2TEXT 后端依赖 CGO + deepdoc 渲染；!cgo 构建无法启用。
