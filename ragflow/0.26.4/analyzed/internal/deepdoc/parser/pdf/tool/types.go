// types.go — PDF 批处理/对比工具的数据结构：批处理统计、Python 参考输出镜像、表格序列化与日志接口。

package tool

// BatchResult 存储单个 PDF 各管道阶段的统计输出。
type BatchResult struct {
	File          string  `json:"file"`          // PDF 文件名
	Pages         int     `json:"pages"`         // 页数
	Chars         int     `json:"chars"`         // 字符总数
	BoxesInitial  int     `json:"boxes_initial"`  // 初始文本框数
	BoxesTextMerg int     `json:"boxes_text_merge"` // 横向合并后框数
	BoxesVertMerg int     `json:"boxes_vertical_merge"` // 纵向合并后框数
	Sections      int     `json:"sections"`      // 段落数
	TSTables      int     `json:"tsr_tables,omitempty"` // TSR 表格数（可选）
	TextLen       int     `json:"text_len"`       // 输出文本长度
	TimeS         float64 `json:"time_s"`         // 耗时（秒）
	Error         string  `json:"error,omitempty"` // 错误信息（可选）
}

// PyResult 镜像 Python dump_py_results.py 的输出结构。
type PyResult struct {
	File           string  `json:"file"`
	Pages          int     `json:"pages"`
	Chars          int     `json:"chars"`
	BoxesInitial   int     `json:"boxes_initial"`
	BoxesTextMerge int     `json:"boxes_text_merge"`
	BoxesVertMerge int     `json:"boxes_vertical_merge"`
	Sections       int     `json:"sections"`
	Tables         int     `json:"tables"`         // Python 侧表格数
	TextLen        int     `json:"text_len"`
	IsEnglish      *bool   `json:"is_english"`      // 是否英文文档（可空）
	TimeS          float64 `json:"time_s"`
	Error          string  `json:"error,omitempty"`
}

// TableItem 存储单张表格的序列化结果。
type TableItem struct {
	ImageB64  string     `json:"image_b64"`  // 表格区域 base64 PNG
	Rows      [][]string `json:"rows"`      // 二维单元格文本
	Cells     []TSRCell  `json:"cells,omitempty"` // TSR 原始单元格（可选）
	Positions []Position `json:"positions"` // 表格在 PDF 中的位置标签
}

// TSRCell 与 parser.TSRCell 一致，便于 JSON 序列化。
type TSRCell struct {
	X0    float64 `json:"x0"`
	Y0    float64 `json:"y0"`
	X1    float64 `json:"x1"`
	Y1    float64 `json:"y1"`
	Text  string  `json:"text"`
	Label string  `json:"label"`
}

// Position 存储矩形边界框（PDF 点坐标）。
type Position struct {
	Left, Right, Top, Bottom float64 // 左/右/上/下边坐标
}

// RealPDFResult 存储真实 PDF 对比用的精简统计。
type RealPDFResult struct {
	File     string `json:"file"`
	Pages    int    `json:"pages"`
	Chars    int    `json:"chars"`
	Sections int    `json:"sections"`
	TextLen  int    `json:"text_len"`
	Error    string `json:"error,omitempty"`
}

// TLogger 对比函数使用的最小日志接口（兼容 testing.T）。
type TLogger interface {
	Logf(format string, args ...any)
	Errorf(format string, args ...any)
	Fatalf(format string, args ...any)
	Skipf(format string, args ...any)
}
