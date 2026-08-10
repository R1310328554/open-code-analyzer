// doctype 包提供 deepdoc 解析管道共享的类型、接口与常量。pdf/docx/xlsx 等格式解析器共用；无同级包依赖，避免循环 import。
package doctype

import (
	"context"
	"image"
	"unicode"
)

// ── 管道类型 ──

// PipelineMetrics 记录各管道阶段的诊断计数。
type PipelineMetrics struct {
	BoxesInitial   int
	BoxesTextMerge int
	BoxesVertMerge int
	BoxesFinal     int
	TablesCount    int
}

// ParseResult 封装单次 Parse() 的全部输出。
type ParseResult struct {
	Sections   []Section
	Tables     []TableItem
	PageImages map[int]image.Image
	Metrics    PipelineMetrics
	Outlines   []Outline // PDF 大纲/书签

	DLADebug []DLAPageRegions
	TSRDebug []TSRRawCell
}

// Figures 返回 layout 为 figure 的全部段落。
// 按需从 Sections 计算，无独立存储字段。
func (r *ParseResult) Figures() []Section {
	return CollectFigures(r.Sections)
}

// DLAPageRegions 单页 DLA 布局区域列表。
type DLAPageRegions struct {
	Page    int
	Regions []DLARegion
}

// TSRRawCell TSR 原始单元格（行列分组前）。
type TSRRawCell struct {
	TableIndex int     `json:"table_index"`
	Page       int     `json:"page"`
	Label      string  `json:"label"`
	X0         float64 `json:"x0"`
	Y0         float64 `json:"y0"`
	X1         float64 `json:"x1"`
	Y1         float64 `json:"y1"`
	Text       string  `json:"text"`
}

// ── 字符与文本框类型 ──

// TextChar 表示 PDF 页上提取的单个字符。
type TextChar struct {
	X0, X1      float64
	Top, Bottom float64
	Text        string
	FontName    string
	FontSize    float64
	PageNumber  int
	LayoutType  string
	LayoutNo    string
	ColID       int
	R           int
}

func (c TextChar) Bounds() (float64, float64, float64, float64) {
	return c.X0, c.Top, c.X1, c.Bottom
}

// TextBox 表示页上矩形文本区域（合并后）。
type TextBox struct {
	X0, X1      float64
	Top, Bottom float64
	Text        string
	PageNumber  int
	LayoutType  string
	LayoutNo    string
	ColID       int
	R           int
	// TSR 后表格网格注释字段（Python R/H/C/SP 标签）
	RTop, RBott   float64
	HTop, HBott   float64
	HLeft, HRight float64
	H             int
	C             int
	CLeft, CRight float64
	SP            int
}

func (b TextBox) Bounds() (float64, float64, float64, float64) {
	return b.X0, b.Top, b.X1, b.Bottom
}

// ── 位置与段落类型 ──

// Position 表示解析后的 @@...## 位置。
type Position struct {
	PageNumbers []int
	Left        float64
	Right       float64
	Top         float64
	Bottom      float64
}

// Section 带空间位置的文本段，可含表格/图片。
type Section struct {
	Text        string
	PositionTag string
	LayoutType  string
	DocTypeKwd  string // 文档类型关键字 text/table/image，后处理赋值
	Positions   []Position
	TableItem   *TableItem
	Image       string // base64 裁剪页图
}

// CollectFigures 收集 layout 为 figure 的段落。
func CollectFigures(sections []Section) []Section {
	if sections == nil {
		return nil
	}
	figures := make([]Section, 0)
	for _, s := range sections {
		if s.LayoutType == LayoutTypeFigure {
			figures = append(figures, s)
		}
	}
	return figures
}

// ── 表格类型 ──

// TableItem 检测到的表格或图区域及其内容。
type TableItem struct {
	ImageB64  string
	Rows      [][]string
	Cells     []TSRCell
	Positions []Position
	Scale     float64
	CropOffX  float64
	CropOffY  float64
	Caption   string

	RegionLeft, RegionRight, RegionTop, RegionBottom float64
	NoMerge                                          bool
	Grid                                             [][]TSRCell
}

// TSRCell TSR 识别的单个表格单元格。
type TSRCell struct {
	X0, Y0, X1, Y1 float64
	Text           string
	Label          string
}

func (c TSRCell) Bounds() (float64, float64, float64, float64) {
	return c.X0, c.Y0, c.X1, c.Y1
}

// ── DeepDoc 视觉类型 ──

// DLARegion 单块 DLA 布局检测区域。
type DLARegion struct {
	X0, Y0, X1, Y1 float64
	Label          string
	Confidence     float64
}

func (r DLARegion) Bounds() (float64, float64, float64, float64) {
	return r.X0, r.Y0, r.X1, r.Y1
}

// OCRBox DeepDoc OCR 检测到的文本框四边形。
type OCRBox struct {
	X0, Y0, X1, Y1, X2, Y2, X3, Y3 float64
}

// OCRText OCR 识别文本与置信度。
type OCRText struct {
	Text       string
	Confidence float64
}

// ── 解析器配置 ──

// ParserConfig 解析器运行参数。
type ParserConfig struct {
	Zoom               float64
	FromPage           int
	ToPage             int
	TableContextSize   int
	ImageContextSize   int
	AutoRotateTables   *bool
	SeparateTablesFigs bool
	SortByTop          bool
	BatchSize          int
	SkipOCR            bool
	MaxOCRConcurrency  int
}

// DefaultParserConfig 返回默认配置（zoom=3 等）。
func DefaultParserConfig() ParserConfig {
	return ParserConfig{
		Zoom:               3,
		FromPage:           0,
		ToPage:             -1,
		BatchSize:          50,
		TableContextSize:   0,
		ImageContextSize:   0,
		SeparateTablesFigs: false,
	}
}

// DlaDPI DeepDoc DLA/OCR 渲染 DPI，216。
const DlaDPI = 216

// DlaScale PDF 点（72 DPI）到 DLA 像素空间的缩放比。
const DlaScale = DlaDPI / 72.0

// ── 布局类型常量 ──

const (
	LayoutTypeText      = "text"
	LayoutTypeTable     = "table"
	LayoutTypeFigure    = "figure"
	LayoutTypeEquation  = "equation"
	LayoutTypeTitle     = "title"
	LayoutTypeReference = "reference"
	LayoutTypeFooter    = "footer"
	LayoutTypeHeader    = "header"

	DLALabelFigureCaption = "figure caption"
	DLALabelTableCaption  = "table caption"
)

// ── 接口 ──

// DocAnalyzer 抽象 DeepDoc 视觉能力：DLA/TSR/OCR。
type DocAnalyzer interface {
	DLA(ctx context.Context, pageImage image.Image) ([]DLARegion, error)
	TSR(ctx context.Context, cropped image.Image) ([]TSRCell, error)
	OCRDetect(ctx context.Context, cropped image.Image) ([]OCRBox, error)
	OCRRecognize(ctx context.Context, cropped image.Image) ([]OCRText, error)
	OCRRecognizeBatch(ctx context.Context, cropped []image.Image) ([][]OCRText, []error)
	Health() bool
}

// ── PDF 大纲 ──

// Outline PDF 文档大纲（目录）的一项；对齐 extract_pdf_outlines()。
type Outline struct {
	Title      string
	Level      int
	PageNumber int // 1-indexed, matching Python
}

// PDFEngine 抽象 PDF 页字符提取与渲染。
type PDFEngine interface {
	ExtractChars(pageNum int) ([]TextChar, error)
	RenderPage(pageNum int, dpi float64) ([]byte, error)
	RenderPageImage(pageNum int, dpi float64) (image.Image, error)
	RawData() []byte
	PageCount() (int, error)
	Outlines() ([]Outline, error)
	Close() error
}

// Tokenizer 与 rag_tokenizer 一致的分词接口。
type Tokenizer interface {
	Tag(token string) string
}

// SampleFunc 从页字符中采样最多 n 个。
type SampleFunc func(chars []TextChar, n int) string

// TableBuilder 封装 TSR 模型单元格检测与分组。
type TableBuilder interface {
	Name() string
	DetectCells(ctx context.Context, cropped image.Image) ([]TSRCell, error)
	GroupCells(cells []TSRCell) [][]TSRCell
}

// Rectangular 可报告边界的轴对齐矩形接口。
type Rectangular interface {
	Bounds() (x0, y0, x1, y1 float64)
}

// IsCJK 判断是否为 CJK（汉字/假名/谚文）字符。
func IsCJK(r rune) bool {
	return unicode.Is(unicode.Han, r) ||
		unicode.Is(unicode.Hiragana, r) ||
		unicode.Is(unicode.Katakana, r) ||
		unicode.Is(unicode.Hangul, r)
}
