// parser.go — DeepDoc PDF 核心解析流水线：对应 Python pdf_parser.RAGFlowPdfParser，负责预扫描、分页批处理、OCR/版面/表格识别与 Section 输出，构造后无状态可复用。

package pdf

import (
	"context"
	"errors"
	"fmt"
	"image"
	"log/slog"
	"sync"

	lyt "ragflow/internal/deepdoc/parser/pdf/layout"
	tbl "ragflow/internal/deepdoc/parser/pdf/table"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	util "ragflow/internal/deepdoc/parser/pdf/util"
)

// Parser 是 PDF 文本与版面提取的核心流水线，对齐 pdf_parser.RAGFlowPdfParser；构造后无状态，可安全跨文档复用。
type Parser struct {
	Config pdf.ParserConfig
}

// pageResult 保存 extractPages 单页处理结果（字符、OCR 框、错误等）。
type pageResult struct {
	// pg 页码（0 基）
	pg       int
	// ocrBoxes 本页 OCR/字符转框结果
	ocrBoxes []pdf.TextBox
	// chars 本页字符（含 OCR 合成字符）
	chars    []pdf.TextChar
	// ocrUsed 本页是否走了 OCR 路径
	ocrUsed  bool
	// pageImg 异步路径缓存的页渲染图
	pageImg  image.Image
	// err 本页处理错误
	err      error
}

// NewParser 以给定 ParserConfig 创建解析器实例。
func NewParser(cfg pdf.ParserConfig) *Parser {
	return &Parser{Config: cfg}
}

// ── TableBuilder 工厂：EE 包可在 init 中注入自定义表格构建器 ──

var tableBuilderFactory func(pdf.DocAnalyzer) pdf.TableBuilder

// RegisterTableBuilder 注册 TableBuilder 工厂；EE 包在 init() 中注入企业版实现。
func RegisterTableBuilder(factory func(pdf.DocAnalyzer) pdf.TableBuilder) {
	tableBuilderFactory = factory
}

// NewTableBuilderFor 优先使用已注册工厂，否则默认 DeepDocTableBuilder。
func NewTableBuilderFor(doc pdf.DocAnalyzer) pdf.TableBuilder {
	if tableBuilderFactory != nil {
		return tableBuilderFactory(doc)
	}
	return tbl.NewDeepDocTableBuilder(doc)
}

// ── 公开 API ──

// ParseRaw 在已打开的 PDFEngine 上运行核心流水线；导出供注入 Mock 引擎的测试使用。
func (p *Parser) ParseRaw(ctx context.Context, engine pdf.PDFEngine, docAnalyzer pdf.DocAnalyzer) (*pdf.ParseResult, error) {
	tb := NewTableBuilderFor(docAnalyzer)

	_, fromPage, toPage, err := p.normalizePageRange(engine)
	if err != nil {
		return nil, fmt.Errorf("page count: %w", err)
	}
	if toPage < fromPage {
		return &pdf.ParseResult{PageImages: make(map[int]image.Image)}, nil
	}

	totalPages := toPage - fromPage + 1
	batchSize := p.getBatchSize()

	prescanChars, prescanMedianH, prescanMedianW, isEnglish, scanNoise := p.prescanPages(ctx, engine, fromPage, toPage, totalPages)
	outlines := p.extractOutlines(engine)

	if totalPages <= batchSize {
		result, err := p.processPages(ctx, engine, fromPage, toPage,
			prescanChars, prescanMedianH, prescanMedianW, isEnglish, scanNoise,
			docAnalyzer, tb)
		if err != nil {
			return nil, err
		}
		result.Outlines = outlines
		return result, nil
	}

	result, err := p.processLargeDocument(ctx, engine, fromPage, toPage, batchSize,
		prescanChars, prescanMedianH, prescanMedianW, isEnglish, scanNoise,
		docAnalyzer, tb)
	if err != nil {
		return nil, err
	}
	result.Outlines = outlines
	return result, nil
}

// ── ParseRaw 辅助函数 ──

// normalizePageRange 根据配置 FromPage/ToPage 与实际页数规范化页区间。
func (p *Parser) normalizePageRange(engine pdf.PDFEngine) (pageCount, fromPage, toPage int, err error) {
	pageCount, err = engine.PageCount()
	if err != nil {
		return 0, 0, 0, err
	}
	fromPage = p.Config.FromPage
	if fromPage < 0 {
		fromPage = 0
	} else if fromPage >= pageCount {
		fromPage = pageCount - 1
	}
	toPage = p.Config.ToPage
	if toPage < 0 || toPage >= pageCount {
		toPage = pageCount - 1
	}
	if toPage < fromPage {
		toPage = fromPage
	}
	return pageCount, fromPage, toPage, nil
}

// getBatchSize 返回批处理大小，≤0 时默认 50 页一批。
func (p *Parser) getBatchSize() int {
	batchSize := p.Config.BatchSize
	if batchSize <= 0 {
		batchSize = 50
	}
	return batchSize
}

// prescanPages 预扫描全页字符并计算中位字高/字宽，同时检测英文文档与扫描噪声特征。
func (p *Parser) prescanPages(ctx context.Context, engine pdf.PDFEngine, fromPage, toPage, totalPages int) (
	map[int][]pdf.TextChar, map[int]float64, map[int]float64, bool, bool,
) {
	prescanChars := make(map[int][]pdf.TextChar)
	prescanMedianH := make(map[int]float64)
	prescanMedianW := make(map[int]float64)

	for pg := fromPage; pg <= toPage; pg++ {
		chars, extractErr := engine.ExtractChars(pg)
		if extractErr != nil {
			slog.Warn("prescan: ExtractChars failed", "page", pg, "err", extractErr)
			chars = nil
		}
		prescanChars[pg] = chars
		prescanMedianH[pg] = util.MedianCharHeight(chars)
		prescanMedianW[pg] = util.MedianCharWidth(chars)
	}

	isEnglish := util.DetectEnglish(prescanChars, totalPages, nil)
	scanNoise := util.IsScanNoise(util.FullTextFromChars(prescanChars))
	return prescanChars, prescanMedianH, prescanMedianW, isEnglish, scanNoise
}

// extractOutlines 提取 PDF 书签/大纲，失败时打日志并返回 nil 继续解析。
func (p *Parser) extractOutlines(engine pdf.PDFEngine) []pdf.Outline {
	outlines, outlineErr := engine.Outlines()
	if outlineErr != nil {
		slog.Warn("Failed to extract PDF outlines; continuing without them", "err", outlineErr)
		outlines = nil
	}
	return outlines
}

// processLargeDocument 大文档按 batchSize 分批调用 processPages 并合并结果。
func (p *Parser) processLargeDocument(ctx context.Context, engine pdf.PDFEngine, fromPage, toPage, batchSize int,
	prescanChars map[int][]pdf.TextChar, prescanMedianH, prescanMedianW map[int]float64,
	isEnglish, scanNoise bool, docAnalyzer pdf.DocAnalyzer, tb pdf.TableBuilder,
) (*pdf.ParseResult, error) {
	slog.Info("batched processing", "pages", toPage-fromPage+1, "batchSize", batchSize)
	result := &pdf.ParseResult{PageImages: make(map[int]image.Image)}

	for start := fromPage; start <= toPage; start += batchSize {
		if err := ctx.Err(); err != nil {
			return nil, fmt.Errorf("cancelled at batch starting page %d: %w", start, err)
		}
		end := min(start+batchSize-1, toPage)

		batchChars := make(map[int][]pdf.TextChar, end-start+1)
		batchMH := make(map[int]float64, end-start+1)
		batchMW := make(map[int]float64, end-start+1)
		for pg := start; pg <= end; pg++ {
			batchChars[pg] = prescanChars[pg]
			batchMH[pg] = prescanMedianH[pg]
			batchMW[pg] = prescanMedianW[pg]
		}

		batch, err := p.processPages(ctx, engine, start, end,
			batchChars, batchMH, batchMW, isEnglish, scanNoise,
			docAnalyzer, tb)
		if err != nil {
			return nil, err
		}

		p.mergeBatchResults(result, batch)
	}
	return result, nil
}

// mergeBatchResults 将单批 Sections/Tables/Metrics/PageImages 追加到总结果。
func (p *Parser) mergeBatchResults(result, batch *pdf.ParseResult) {
	result.Sections = append(result.Sections, batch.Sections...)
	result.Tables = append(result.Tables, batch.Tables...)
	if result.PageImages == nil {
		result.PageImages = make(map[int]image.Image)
	}
	for pg, img := range batch.PageImages {
		result.PageImages[pg] = img
	}
	result.Metrics.BoxesInitial += batch.Metrics.BoxesInitial
	result.Metrics.BoxesTextMerge += batch.Metrics.BoxesTextMerge
	result.Metrics.BoxesVertMerge += batch.Metrics.BoxesVertMerge
	result.Metrics.BoxesFinal += batch.Metrics.BoxesFinal
	result.Metrics.TablesCount += batch.Metrics.TablesCount
}

// ── extractPages 页级并发提取 ──

// extractPages 并发处理页区间：正常页同步渲染+合并，乱码/扫描页异步 OCR。
func (p *Parser) extractPages(ctx context.Context, engine pdf.PDFEngine,
	fromPage, toPage int,
	prescanChars map[int][]pdf.TextChar,
	medianHeights, medianWidths map[int]float64,
	pageImages map[int]image.Image,
	docAnalyzer pdf.DocAnalyzer,
) ([]pdf.TextBox, map[int][]pdf.TextChar, bool, error) {
	pageCount := toPage - fromPage + 1
	results := make([]pageResult, pageCount)

	sem, wg := p.setupPageConcurrency()

	for i := 0; i < pageCount; i++ {
		pg := fromPage + i
		chars := prescanChars[pg]

		if len(chars) > 0 && !util.IsGarbledPage(chars) {
			if err := ctx.Err(); err != nil {
				results[i] = pageResult{pg: pg, err: fmt.Errorf("cancelled before sync page %d: %w", pg, err)}
				continue
			}
			results[i] = p.processPageSync(ctx, engine, pg, chars, pageImages, docAnalyzer)
			continue
		}

		wg.Add(1)
		go func(i, pg int, chars []pdf.TextChar) {
			defer wg.Done()
			results[i] = p.processPageAsync(ctx, engine, pg, chars, sem, docAnalyzer)
		}(i, pg, chars)
	}
	wg.Wait()
	return p.collectPageResults(results, pageImages, medianHeights, medianWidths)
}

// setupPageConcurrency 按 MaxOCRConcurrency 创建信号量与 WaitGroup。
func (p *Parser) setupPageConcurrency() (chan struct{}, *sync.WaitGroup) {
	maxConc := p.Config.MaxOCRConcurrency
	if maxConc <= 0 {
		maxConc = 1
	}
	return make(chan struct{}, maxConc), &sync.WaitGroup{}
}

// processPageSync 同步处理正常页：渲染页图后 ocrMergeChars 或 CharsToBoxes。
func (p *Parser) processPageSync(ctx context.Context, engine pdf.PDFEngine, pg int, chars []pdf.TextChar,
	pageImages map[int]image.Image, docAnalyzer pdf.DocAnalyzer,
) pageResult {
	pageImg, renderErr := RenderPageToImage(engine, pg)
	if renderErr == nil && pageImg != nil {
		pageImages[pg] = pageImg
	} else if renderErr != nil {
		slog.Warn("processPageSync: RenderPageToImage failed", "page", pg, "err", renderErr)
	}

	ocrBoxes, updatedChars, ocrUsed := p.processPageBoxes(ctx, pageImg, chars, pg, renderErr, docAnalyzer, false)
	return pageResult{pg: pg, ocrBoxes: ocrBoxes, chars: updatedChars, ocrUsed: ocrUsed}
}

// processPageAsync 异步处理乱码/扫描页：受信号量限制并发 OCR。
func (p *Parser) processPageAsync(ctx context.Context, engine pdf.PDFEngine, pg int, chars []pdf.TextChar,
	sem chan struct{}, docAnalyzer pdf.DocAnalyzer,
) pageResult {
	select {
	case <-ctx.Done():
		return pageResult{pg: pg, err: ctx.Err()}
	case sem <- struct{}{}:
	}
	defer func() { <-sem }()

	pageImg, err := RenderPageToImage(engine, pg)
	if err != nil {
		return pageResult{pg: pg, err: err}
	}
	if err := ctx.Err(); err != nil {
		return pageResult{pg: pg, err: err}
	}

	ocrBoxes, updatedChars, ocrUsed := p.processPageBoxes(ctx, pageImg, chars, pg, nil, docAnalyzer, true)
	return pageResult{pg: pg, ocrBoxes: ocrBoxes, chars: updatedChars, ocrUsed: ocrUsed, pageImg: pageImg}
}

// processPageBoxes 同步/异步共用的 OCR 框提取逻辑；返回 (ocrBoxes, updatedChars, ocrUsed)。OCR 成功时会追加合成字符，调用方须用返回的 chars 计算中位值。
func (p *Parser) processPageBoxes(ctx context.Context, pageImg image.Image, chars []pdf.TextChar, pg int,
	renderErr error, docAnalyzer pdf.DocAnalyzer, isAsync bool,
) ([]pdf.TextBox, []pdf.TextChar, bool) {
	var ocrBoxes []pdf.TextBox
	ocrUsed := false

	if !p.Config.SkipOCR {
		if isAsync {
			label := "scan page"
			if len(chars) > 0 {
				label = "garbled page"
			}
			ocrBoxes = ocrDetectAndRecognize(ctx, pageImg, docAnalyzer, pg, label)
			if ocrBoxes != nil {
				for j := range ocrBoxes {
					for _, r := range ocrBoxes[j].Text {
						chars = append(chars, pdf.TextChar{Text: string(r), PageNumber: pg})
						break
					}
				}
				ocrUsed = true
			}
			if !ocrUsed && len(chars) > 0 {
				ocrBoxes = ocrMergeChars(ctx, pageImg, chars, docAnalyzer, pg)
				if ocrBoxes != nil {
					ocrUsed = true
				}
			}
		} else {
			if renderErr == nil && pageImg != nil {
				ocrBoxes = ocrMergeChars(ctx, pageImg, chars, docAnalyzer, pg)
				if ocrBoxes != nil {
					ocrUsed = true
				}
			}
		}
	}

	if !ocrUsed && len(chars) > 0 {
		if ocrBoxes == nil {
			ocrBoxes = lyt.CharsToBoxes(chars, pg, p.Config.SortByTop)
		}
	}

	return ocrBoxes, chars, ocrUsed
}

// collectPageResults 汇总各 pageResult，合并 boxes/chars 并收集 OCR 错误。
func (p *Parser) collectPageResults(results []pageResult, pageImages map[int]image.Image,
	medianHeights, medianWidths map[int]float64,
) ([]pdf.TextBox, map[int][]pdf.TextChar, bool, error) {
	var boxes []pdf.TextBox
	pageChars := make(map[int][]pdf.TextChar)
	ocrUsedAny := false
	var errs []error

	for _, r := range results {
		if r.err != nil {
			slog.Warn("page OCR failed", "page", r.pg, "err", r.err)
			errs = append(errs, fmt.Errorf("page %d: %w", r.pg, r.err))
			continue
		}
		if r.ocrUsed {
			boxes = append(boxes, r.ocrBoxes...)
			ocrUsedAny = true
		} else if len(r.ocrBoxes) > 0 {
			boxes = append(boxes, r.ocrBoxes...)
		}
		if r.pageImg != nil {
			pageImages[r.pg] = r.pageImg
		}
		pageChars[r.pg] = r.chars
		if r.ocrUsed {
			medianHeights[r.pg] = util.MedianCharHeight(r.chars)
			medianWidths[r.pg] = util.MedianCharWidth(r.chars)
		}
	}
	return boxes, pageChars, ocrUsedAny, errors.Join(errs...)
}

// ── 内部流水线步骤 ──

// retryScanNoise 扫描噪声文档全页重跑 detect+recognize OCR。
func (p *Parser) retryScanNoise(ctx context.Context, engine pdf.PDFEngine,
	fromPage, toPage int,
	pageImages map[int]image.Image,
	pageChars map[int][]pdf.TextChar,
	medianHeights, medianWidths map[int]float64,
	ocrUsedAny bool,
	docAnalyzer pdf.DocAnalyzer,
) ([]pdf.TextBox, map[int][]pdf.TextChar, bool) {
	slog.Warn("scan noise: OCR retry", "from", fromPage, "to", toPage)
	var boxes []pdf.TextBox
	for pg := fromPage; pg <= toPage; pg++ {
		img := pageImages[pg]
		if img == nil {
			var err error
			img, err = RenderPageToImage(engine, pg)
			if err != nil {
				slog.Warn("scan noise: page render failed", "page", pg, "err", err)
				continue
			}
			pageImages[pg] = img
		}
		ocrBoxes := ocrDetectAndRecognize(ctx, img, docAnalyzer, pg, "scan page")
		if ocrBoxes == nil {
			slog.Warn("scan noise: page OCR empty", "page", pg)
			continue
		}
		boxes = append(boxes, ocrBoxes...)
		var chars []pdf.TextChar
		for _, b := range ocrBoxes {
			for _, r := range b.Text {
				chars = append(chars, pdf.TextChar{Text: string(r), Top: b.Top, Bottom: b.Bottom, PageNumber: pg})
				break
			}
		}
		pageChars[pg] = chars
		medianHeights[pg] = util.MedianCharHeight(chars)
		medianWidths[pg] = util.MedianCharWidth(chars)
	}
	slog.Debug("scan noise OCR retry complete", "pages", toPage-fromPage+1, "boxes", len(boxes))
	return boxes, pageChars, true
}

// retryZoom 无框时提高 Zoom×DlaScale 重渲染并 OCR，坐标按缩放比还原。
func (p *Parser) retryZoom(ctx context.Context, engine pdf.PDFEngine,
	fromPage, toPage int,
	pageImages map[int]image.Image,
	boxes []pdf.TextBox, ocrUsedAny bool,
	docAnalyzer pdf.DocAnalyzer,
) ([]pdf.TextBox, bool) {
	retryZoomVal := p.Config.Zoom * pdf.DlaScale
	retryDPI := retryZoomVal * 72
	slog.Info("zoom retry: re-rendering", "oldZoom", p.Config.Zoom, "newZoom", retryZoomVal)
	for pg := fromPage; pg <= toPage; pg++ {
		img, err := engine.RenderPageImage(pg, retryDPI)
		if err != nil {
			slog.Warn("zoom retry: render failed", "page", pg, "err", err)
			continue
		}
		pageImages[pg] = img
		if retryDPI != pdf.DlaDPI {
			if dlaImg, dlaErr := engine.RenderPageImage(pg, pdf.DlaDPI); dlaErr == nil {
				pageImages[pg] = dlaImg
			}
		}
		ocrBoxes := ocrDetectAndRecognize(ctx, img, docAnalyzer, pg, "zoom retry")
		if ocrBoxes == nil {
			continue
		}
		scaleFactor := retryZoomVal / p.Config.Zoom
		for i := range ocrBoxes {
			ocrBoxes[i].X0 /= scaleFactor
			ocrBoxes[i].X1 /= scaleFactor
			ocrBoxes[i].Top /= scaleFactor
			ocrBoxes[i].Bottom /= scaleFactor
		}
		boxes = append(boxes, ocrBoxes...)
		ocrUsedAny = true
	}
	return boxes, ocrUsedAny
}

// buildLayout 执行 DeepDoc 表格增强、列分配、文本/纵向合并、表格替换与 Section 生成及 caption 合并。
func (p *Parser) buildLayout(ctx context.Context,
	result *pdf.ParseResult, engine pdf.PDFEngine,
	boxes []pdf.TextBox, pageChars map[int][]pdf.TextChar,
	medianHeights, medianWidths map[int]float64,
	fromPage, toPage int, ocrUsedAny bool, isEnglish bool,
	docAnalyzer pdf.DocAnalyzer, tb pdf.TableBuilder,
) error {
	result.Metrics.BoxesInitial = len(boxes)

	result.Tables = p.enrichWithDeepDoc(ctx, result, engine, boxes, result.PageImages, docAnalyzer, tb)
	result.Metrics.TablesCount = len(result.Tables)
	if err := ctx.Err(); err != nil {
		return err
	}

	boxes = lyt.AssignColumn(boxes, p.Config.Zoom)
	boxes = lyt.TextMerge(boxes, medianHeights, p.Config.Zoom)
	result.Metrics.BoxesTextMerge = len(boxes)

	lyt.SortByPageThenY(boxes, p.Config.SortByTop)

	if ocrUsedAny {
		isEnglish = util.DetectEnglish(pageChars, toPage-fromPage+1, nil)
	}
	boxes = lyt.NaiveVerticalMerge(boxes, medianHeights, medianWidths, isEnglish)
	result.Metrics.BoxesVertMerge = len(boxes)
	if err := ctx.Err(); err != nil {
		return err
	}

	boxes = tbl.ExtractTableAndReplace(boxes, result.Tables)
	boxes = tbl.ConsolidateFigures(boxes)

	pageHeights := make(map[int]float64, len(result.PageImages))
	for pg, img := range result.PageImages {
		pageHeights[pg] = float64(img.Bounds().Dy()) / p.Config.Zoom
	}
	result.Sections = lyt.BoxesToSections(boxes, pageHeights)
	result.Metrics.BoxesFinal = len(result.Sections)
	result.Sections = tbl.MergeCaptions(result.Sections, result.Figures())
	return nil
}

// processPages 单批页完整流程：extractPages → 扫描噪声/Zoom 重试 → buildLayout → 填充 Section 图片。
func (p *Parser) processPages(ctx context.Context, engine pdf.PDFEngine,
	fromPage, toPage int,
	prescanChars map[int][]pdf.TextChar,
	medianHeights, medianWidths map[int]float64,
	isEnglish, isScanNoiseDoc bool,
	docAnalyzer pdf.DocAnalyzer, tb pdf.TableBuilder,
) (*pdf.ParseResult, error) {
	result := &pdf.ParseResult{PageImages: make(map[int]image.Image)}

	boxes, pageChars, ocrUsedAny, ocrErr := p.extractPages(ctx, engine,
		fromPage, toPage, prescanChars,
		medianHeights, medianWidths, result.PageImages, docAnalyzer)
	if ocrErr != nil {
		slog.Warn("extractPages: some pages failed OCR", "err", ocrErr)
	}

	if isScanNoiseDoc {
		boxes, pageChars, ocrUsedAny = p.retryScanNoise(ctx, engine,
			fromPage, toPage, result.PageImages,
			pageChars, medianHeights, medianWidths, ocrUsedAny, docAnalyzer)
	}

	if len(boxes) == 0 && p.Config.Zoom < 9 && !p.Config.SkipOCR {
		boxes, ocrUsedAny = p.retryZoom(ctx, engine, fromPage, toPage,
			result.PageImages, boxes, ocrUsedAny, docAnalyzer)
	}

	if len(boxes) == 0 {
		return result, nil
	}

	if err := p.buildLayout(ctx, result, engine, boxes, pageChars,
		medianHeights, medianWidths, fromPage, toPage, ocrUsedAny, isEnglish,
		docAnalyzer, tb); err != nil {
		return nil, fmt.Errorf("buildLayout: %w", err)
	}
	p.fillSectionImages(result)
	return result, nil
}

// fillSectionImages 为表格/图片 Section 匹配预渲染 base64 或按 position tag 裁剪页图。
func (p *Parser) fillSectionImages(result *pdf.ParseResult) {
	if len(result.PageImages) == 0 {
		return
	}
	tableImgByRegion := make(map[string]string, len(result.Tables))
	for _, tbl := range result.Tables {
		if tbl.ImageB64 == "" {
			continue
		}
		pg := 0
		if len(tbl.Positions) > 0 && len(tbl.Positions[0].PageNumbers) > 0 {
			pg = tbl.Positions[0].PageNumbers[0]
		}
		key := fmt.Sprintf("%d_%.1f_%.1f_%.1f_%.1f",
			pg, tbl.RegionLeft, tbl.RegionRight, tbl.RegionTop, tbl.RegionBottom)
		tableImgByRegion[key] = tbl.ImageB64
	}
	for i := range result.Sections {
		if result.Sections[i].LayoutType == pdf.LayoutTypeTable {
			if img, ok := matchTableImage(&result.Sections[i], tableImgByRegion); ok {
				result.Sections[i].Image = img
				continue
			}
		}
		if result.Sections[i].LayoutType == pdf.LayoutTypeFigure && len(result.Sections[i].Positions) > 0 {
			if dlaImg := util.CropSectionByDLA(result.Sections[i], result.DLADebug, result.PageImages); dlaImg != "" {
				result.Sections[i].Image = dlaImg
				continue
			}
		}
		img := util.CropSectionImage(result.Sections[i].PositionTag, result.PageImages, p.Config.Zoom)
		result.Sections[i].Image = img
		if img == "" && result.Sections[i].Text != "" {
			tag := result.Sections[i].PositionTag
			slog.Warn("cropSectionImage empty for non-empty section",
				"section", i, "posTag", tag[:min(80, len(tag))])
		}
	}
}

// matchTableImage 按页码+区域键查找预渲染表格图片；优先 Positions，回退 TableItem Region 边界。
func matchTableImage(sec *pdf.Section, tableImgByRegion map[string]string) (string, bool) {
	pg := 0
	if len(sec.Positions) > 0 {
		pos := sec.Positions[0]
		if len(pos.PageNumbers) > 0 {
			pg = pos.PageNumbers[0]
		}
		key := fmt.Sprintf("%d_%.1f_%.1f_%.1f_%.1f", pg, pos.Left, pos.Right, pos.Top, pos.Bottom)
		if img, ok := tableImgByRegion[key]; ok {
			return img, true
		}
		return "", false
	}
	if sec.TableItem != nil {
		if len(sec.TableItem.Positions) > 0 && len(sec.TableItem.Positions[0].PageNumbers) > 0 {
			pg = sec.TableItem.Positions[0].PageNumbers[0]
		}
		key := fmt.Sprintf("%d_%.1f_%.1f_%.1f_%.1f", pg,
			sec.TableItem.RegionLeft, sec.TableItem.RegionRight,
			sec.TableItem.RegionTop, sec.TableItem.RegionBottom)
		if img, ok := tableImgByRegion[key]; ok {
			return img, true
		}
	}
	return "", false
}

