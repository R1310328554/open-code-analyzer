// office_parsers_no_cgo.go — 非 CGO 构建：Office 家族 stub 与 ErrOfficeCGORequired。

//go:build !cgo

package parser

import (
	"errors"
	"fmt"
)

// OfficeOxide is the lib_type identifier for office_oxide backend.
const OfficeOxide = "office_oxide"

// ErrOfficeCGORequired 在非 CGO 构建下由 Office 家族 ParseWithResult 统一返回。
// 覆盖 DOC/DOCX/PPT/PPTX/XLS/XLSX 六种格式。
// CGO 构建的实际实现在 docx_parser.go 等 //go:build cgo 文件中。
// implementation captures the office_oxide PlainText / ToMarkdown
// stub 保持类型与接口面一致，使 !cgo 包可编译且测试通过。
// and existing tests pass.
var ErrOfficeCGORequired = errors.New("parser: office family requires CGO (office_oxide)")

// docxParseWithResultNoCGO 为 DOCX 非 CGO stub（方法挂载在 DOCXParser 上）。
// family. The CGO build's implementation lives in docx_parser.go
// under //go:build cgo.
func (p *DOCXParser) ParseWithResult(filename string, _ []byte) ParseResult {
	return ParseResult{
		File: map[string]any{"name": filename},
		Err:  fmt.Errorf("%w: docx", ErrOfficeCGORequired),
	}
}

func (p *DOCParser) ParseWithResult(filename string, _ []byte) ParseResult {
	return ParseResult{
		File: map[string]any{"name": filename},
		Err:  fmt.Errorf("%w: doc", ErrOfficeCGORequired),
	}
}

func (p *XLSParser) ParseWithResult(filename string, _ []byte) ParseResult {
	return ParseResult{
		File: map[string]any{"name": filename},
		Err:  fmt.Errorf("%w: xls", ErrOfficeCGORequired),
	}
}

func (p *XLSXParser) ParseWithResult(filename string, _ []byte) ParseResult {
	return ParseResult{
		File: map[string]any{"name": filename},
		Err:  fmt.Errorf("%w: xlsx", ErrOfficeCGORequired),
	}
}

func (p *PPTParser) ParseWithResult(filename string, _ []byte) ParseResult {
	return ParseResult{
		File: map[string]any{"name": filename},
		Err:  fmt.Errorf("%w: ppt", ErrOfficeCGORequired),
	}
}

func (p *PPTXParser) ParseWithResult(filename string, _ []byte) ParseResult {
	return ParseResult{
		File: map[string]any{"name": filename},
		Err:  fmt.Errorf("%w: pptx", ErrOfficeCGORequired),
	}
}

type DOCParser struct {
	libType string
}

func NewDOCParser(libType string) (*DOCParser, error) {
	return nil, fmt.Errorf("DOC parser requires CGO (office_oxide)")
}

func (p *DOCParser) String() string {
	return "DOCParser(no-cgo)"
}

type DOCXParser struct {
	libType string
}

func NewDOCXParser(libType string) (*DOCXParser, error) {
	return nil, fmt.Errorf("DOCX parser requires CGO (office_oxide)")
}

func (p *DOCXParser) String() string {
	return "DOCXParser(no-cgo)"
}

type XLSParser struct {
	libType string
}

func NewXLSParser(libType string) (*XLSParser, error) {
	return nil, fmt.Errorf("XLS parser requires CGO (office_oxide)")
}

func (p *XLSParser) String() string {
	return "XLSParser(no-cgo)"
}

type XLSXParser struct {
	libType string
}

func NewXLSXParser(libType string) (*XLSXParser, error) {
	return nil, fmt.Errorf("XLSX parser requires CGO (office_oxide)")
}

func (p *XLSXParser) String() string {
	return "XLSXParser(no-cgo)"
}

type PPTParser struct {
	libType string
}

func NewPPTParser(libType string) (*PPTParser, error) {
	return nil, fmt.Errorf("PPT parser requires CGO (office_oxide)")
}

func (p *PPTParser) String() string {
	return "PPTParser(no-cgo)"
}

type PPTXParser struct {
	libType string
}

func NewPPTXParser(libType string) (*PPTXParser, error) {
	return nil, fmt.Errorf("PPTX parser requires CGO (office_oxide)")
}

func (p *PPTXParser) String() string {
	return "PPTXParser(no-cgo)"
}

// New*Parser 在非 CGO 构建直接返回错误；ParseWithResult 带格式后缀包装 ErrOfficeCGORequired。
