// pdftype 包提供 PDF 专用类型，并通过类型别名重新导出 doctype 共享类型。现有 PDF 解析代码无需修改 import 即可继续工作。
package pdftype

import doctype "ragflow/internal/deepdoc/parser/type"

// ── 通过别名重新导出共享类型 ──

type PipelineMetrics = doctype.PipelineMetrics
type ParseResult = doctype.ParseResult
type DLAPageRegions = doctype.DLAPageRegions
type TSRRawCell = doctype.TSRRawCell
type TextChar = doctype.TextChar
type TextBox = doctype.TextBox
type Position = doctype.Position
type Section = doctype.Section
type TableItem = doctype.TableItem
type TSRCell = doctype.TSRCell
type DLARegion = doctype.DLARegion
type OCRBox = doctype.OCRBox
type OCRText = doctype.OCRText
type ParserConfig = doctype.ParserConfig
type DocAnalyzer = doctype.DocAnalyzer
type Outline = doctype.Outline
type PDFEngine = doctype.PDFEngine
type Tokenizer = doctype.Tokenizer
type SampleFunc = doctype.SampleFunc
type TableBuilder = doctype.TableBuilder
type Rectangular = doctype.Rectangular

// ── 重新导出常量 ──

const DlaDPI = doctype.DlaDPI
const DlaScale = doctype.DlaScale

const (
	LayoutTypeText        = doctype.LayoutTypeText
	LayoutTypeTable       = doctype.LayoutTypeTable
	LayoutTypeFigure      = doctype.LayoutTypeFigure
	LayoutTypeEquation    = doctype.LayoutTypeEquation
	LayoutTypeTitle       = doctype.LayoutTypeTitle
	LayoutTypeReference   = doctype.LayoutTypeReference
	LayoutTypeFooter      = doctype.LayoutTypeFooter
	LayoutTypeHeader      = doctype.LayoutTypeHeader
	DLALabelFigureCaption = doctype.DLALabelFigureCaption
	DLALabelTableCaption  = doctype.DLALabelTableCaption
)

// ── 重新导出函数与变量 ──

var (
	CollectFigures      = doctype.CollectFigures
	DefaultParserConfig = doctype.DefaultParserConfig
	IsCJK               = doctype.IsCJK
)
