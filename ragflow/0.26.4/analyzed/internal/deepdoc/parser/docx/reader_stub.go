//go:build !cgo

// reader_stub.go（!cgo）— 无 CGO 时 ExtractRawBlocks 不可用，提示以 CGO_ENABLED=1 重新编译。

package docx

import "errors"

// ExtractRawBlocks 依赖 office_oxide 的 CGO；未启用 CGO 时返回明确错误。
func ExtractRawBlocks(_ []byte) ([]RawBlock, error) {
	return nil, errors.New("office_oxide requires cgo; rebuild with CGO_ENABLED=1")
}
