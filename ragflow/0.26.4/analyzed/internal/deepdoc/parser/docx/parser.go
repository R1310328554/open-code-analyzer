// parser.go — DOCX 解析入口：将 office_oxide 提取的 RawBlock 映射为框架层 doctype.Section。

package docx

import (
	"strings"

	"ragflow/internal/deepdoc/parser/pdf/table"
	doctype "ragflow/internal/deepdoc/parser/type"
)

// blocksToSections 将 RawBlock 转为框架 Section；标题→title，表格→table+TableItem，其余→text。
func blocksToSections(blocks []RawBlock) []doctype.Section {
	sections := make([]doctype.Section, 0, len(blocks))
	for _, b := range blocks {
		sec := blockToSection(b)
		sections = append(sections, sec)
	}
	return sections
}

// blockToSection 单块映射：table/image/段落（含 Heading 样式→title）。
func blockToSection(b RawBlock) doctype.Section {
	switch b.Type {
	case "table":
		return doctype.Section{
			Text:       table.SimpleRowsToHTML(b.Rows),
			DocTypeKwd: "table",
			TableItem: &doctype.TableItem{
				Rows: b.Rows,
			},
		}
	case "image":
		return doctype.Section{
			DocTypeKwd: "image",
			Image:      b.Image,
		}
	default:
		layoutType := "text"
		if strings.HasPrefix(strings.ToLower(b.Style), "heading") {
			layoutType = "title"
		}
		return doctype.Section{
			Text:       b.Text,
			DocTypeKwd: "text",
			LayoutType: layoutType,
		}
	}
}

// Parse 将 DOCX 字节解析为 ParseResult：ExtractRawBlocks + blocksToSections。
func Parse(data []byte, cfg doctype.ParserConfig) (*doctype.ParseResult, error) {
	blocks, err := ExtractRawBlocks(data)
	if err != nil {
		return nil, err
	}
	return &doctype.ParseResult{
		Sections: blocksToSections(blocks),
	}, nil
}
