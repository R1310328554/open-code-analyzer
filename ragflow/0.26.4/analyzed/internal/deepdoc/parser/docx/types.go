// types.go — DOCX 解析中间类型：RawBlock 与 office_oxide IR JSON 结构体定义。

package docx

// RawBlock 文档顺序的单块；Type 为 paragraph/table/image；标题为 paragraph+Heading N 样式。
type RawBlock struct {
	Type  string     `json:"type"`            // 块类型：paragraph 或 table
	Text  string     `json:"text"`            // 段落文本；表格为空
	Style string     `json:"style"`           // Word 样式名，如 Normal、Heading 1
	Image string     `json:"image,omitempty"` // Base64 编码的图片数据
	Rows  [][]string `json:"rows,omitempty"`  // 表格行；段落为 nil
}

// ── office_oxide IR JSON 中间表示类型 ──

// irElement office_oxide IR 文档元素。
type irElement struct {
	Type    string  `json:"type"`    // 元素类型：paragraph/heading/table/image
	Level   int     `json:"level"`   // 标题级别 1-6
	Style   string  `json:"style"`   // Word 样式名
	Content []irRun `json:"content"` // 富文本 run 列表
	Data    []byte  `json:"data"`    // 图片原始字节（image 类型）
	Rows    []irRow `json:"rows"`    // 表格行
}

// irRun 富文本 run。
type irRun struct {
	Type    string      `json:"type"`    // run 类型：text 或 image
	Text    string      `json:"text"`    // 纯文本内容
	Content []irElement `json:"content"` // 嵌套元素（表格单元格内）
}

// irRow 表格行。
type irRow struct {
	Cells []irCell `json:"cells"`
}

// irCell 表格单元格。
type irCell struct {
	Content []irElement `json:"content"` // 单元格内嵌套段落
}

// irSection IR 文档节。
type irSection struct {
	Title    string      `json:"title"`
	Elements []irElement `json:"elements"`
}

// irDocument IR 根文档。
type irDocument struct {
	Sections []irSection `json:"sections"`
}
