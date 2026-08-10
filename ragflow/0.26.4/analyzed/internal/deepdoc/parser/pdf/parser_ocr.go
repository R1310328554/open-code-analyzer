// parser_ocr.go — PDF OCR 辅助：全页检测识别、字符与检测框合并、表格单元格 OCR 及批量识别。

package pdf

import (
	"context"
	"image"
	"log/slog"
	"math"
	lyt "ragflow/internal/deepdoc/parser/pdf/layout"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	util "ragflow/internal/deepdoc/parser/pdf/util"
	"sort"
	"strings"
)

// ocrDetectAndRecognize 对整页 detect 后逐框 crop 并 recognize，用于扫描页/Zoom 重试。
func ocrDetectAndRecognize(ctx context.Context, pageImg image.Image, doc pdf.DocAnalyzer, pageNum int, logLabel string) []pdf.TextBox {
	boxes, err := doc.OCRDetect(ctx, pageImg)
	if err != nil || len(boxes) == 0 {
		if err != nil {
			slog.Warn(logLabel+" OCR detect failed", "page", pageNum, "err", err)
		}
		return nil
	}

	var result []pdf.TextBox
	for _, b := range boxes {
		x0 := int(math.Min(b.X0, math.Min(b.X1, math.Min(b.X2, b.X3))))
		y0 := int(math.Min(b.Y0, math.Min(b.Y1, math.Min(b.Y2, b.Y3))))
		x1 := int(math.Max(b.X0, math.Max(b.X1, math.Max(b.X2, b.X3))))
		y1 := int(math.Max(b.Y0, math.Max(b.Y1, math.Max(b.Y2, b.Y3))))
		if x0 >= x1 || y0 >= y1 {
			continue
		}
		cropped := util.FastCrop(pageImg, x0, y0, x1, y1)
		texts, recErr := doc.OCRRecognize(ctx, cropped)
		if recErr != nil {
			slog.Warn(logLabel+" OCR recognize failed", "page", pageNum, "err", recErr)
			continue
		}
		for _, t := range texts {
			if strings.TrimSpace(t.Text) != "" {
				result = append(result, pdf.TextBox{
					X0: float64(x0), X1: float64(x1),
					Top: float64(y0), Bottom: float64(y1),
					Text: t.Text,
					PageNumber: pageNum,
				})
			}
		}
	}
	return result
}

// ocrMergeChars 对有嵌入字符的页做全页 detect，将字符匹配到检测框；有字符用嵌入文本，空/乱码框走 OCR；对齐 Python __ocr。
// ocrDetectBox 检测框及其 PDF 坐标边界（已按 DlaScale 归一化）。
type ocrDetectBox struct {
	// box 对应的 TextBox 结构
	box            pdf.TextBox
	// x0/y0/x1/y1 裁剪与匹配用边界
	x0, y0, x1, y1 float64
}

func ocrMergeChars(ctx context.Context, pageImg image.Image, chars []pdf.TextChar, doc pdf.DocAnalyzer, pageNum int) []pdf.TextBox {
	boxes, scale, err := detectBoxes(ctx, pageImg, doc, pageNum)
	if err != nil || len(boxes) == 0 {
		return nil
	}
	boxChars := matchCharsToBoxes(boxes, chars)
	return buildTextBoxes(ctx, pageImg, boxes, boxChars, doc, scale, pageNum)
}

// detectBoxes 调用 OCRDetect 并将四边形转为轴对齐框，按行内 Y 阈值排序。
func detectBoxes(ctx context.Context, pageImg image.Image, doc pdf.DocAnalyzer, pageNum int) ([]ocrDetectBox, float64, error) {
	ocrDetectBoxes, err := doc.OCRDetect(ctx, pageImg)
	if err != nil || len(ocrDetectBoxes) == 0 {
		return nil, 0, err
	}
	slog.Debug("ocrMergeChars detect", "page", pageNum, "boxes", len(ocrDetectBoxes))

	// scale 页图像素坐标到 PDF 点的缩放比（DlaScale=3.0）
	scale := pdf.DlaScale // 3.0
	imgBounds := pageImg.Bounds()
	imgW := float64(imgBounds.Dx()) / scale
	imgH := float64(imgBounds.Dy()) / scale

	boxes := make([]ocrDetectBox, 0, len(ocrDetectBoxes))
	for _, b := range ocrDetectBoxes {
		x0 := min(b.X0, b.X1, b.X2, b.X3) / scale
		y0 := min(b.Y0, b.Y1, b.Y2, b.Y3) / scale
		x1 := max(b.X0, b.X1, b.X2, b.X3) / scale
		y1 := max(b.Y0, b.Y1, b.Y2, b.Y3) / scale
		if x0 < 0 {
			x0 = 0
		}
		if y0 < 0 {
			y0 = 0
		}
		if x1 > imgW {
			x1 = imgW
		}
		if y1 > imgH {
			y1 = imgH
		}
		if x0 >= x1 || y0 >= y1 {
			continue
		}
		boxes = append(boxes, ocrDetectBox{box: pdf.TextBox{
			X0: x0, X1: x1, Top: y0, Bottom: y1, PageNumber: pageNum,
		}, x0: x0, y0: y0, x1: x1, y1: y1})
	}

	if len(boxes) > 1 {
		boxHeights := make([]float64, len(boxes))
		for i := range boxes {
			boxHeights[i] = boxes[i].y1 - boxes[i].y0
		}
		sort.Float64s(boxHeights)
		threshold := boxHeights[len(boxHeights)/2] / 3
		sort.Slice(boxes, func(i, j int) bool {
			if math.Abs(boxes[i].y0-boxes[j].y0) < threshold {
				return boxes[i].x0 < boxes[j].x0
			}
			return boxes[i].y0 < boxes[j].y0
		})
	}
	return boxes, scale, nil
}

// matchCharsToBoxes 按重叠比将每个字符分配到最佳检测框，过滤字高差异过大的误匹配。
func matchCharsToBoxes(boxes []ocrDetectBox, chars []pdf.TextChar) [][]pdf.TextChar {
	boxChars := make([][]pdf.TextChar, len(boxes))
	for _, c := range chars {
		bestIdx := -1
		bestOverlap := 1e-6
		for i := range boxes {
			overlap := charBoxOverlapRatio(c, boxes[i].x0, boxes[i].x1, boxes[i].y0, boxes[i].y1)
			if overlap >= bestOverlap {
				bestOverlap = overlap
				bestIdx = i
			}
		}
		if bestIdx < 0 {
			continue
		}
		ch := c.Bottom - c.Top
		if ch <= 0 {
			ch = 1
		}
		bh := boxes[bestIdx].y1 - boxes[bestIdx].y0
		if math.Abs(ch-bh)/math.Max(ch, bh) >= 0.7 && c.Text != " " {
			continue
		}
		boxChars[bestIdx] = append(boxChars[bestIdx], c)
	}
	return boxChars
}

// sortCharsYFirstly 先按 Y 模糊分行（差值<threshold 同行），同行内按 X 排序；对齐 Python Recognizer.sort_Y_firstly。
func sortCharsYFirstly(chars []pdf.TextChar, threshold float64) {
	sort.Slice(chars, func(i, j int) bool {
		diff := chars[i].Top - chars[j].Top
		if math.Abs(diff) < threshold {
			return chars[i].X0 < chars[j].X0
		}
		return diff < 0
	})
}

// charBoxOverlapRatio 从字符视角计算与框的重叠面积比（overlap/char_area）；对齐 Python overlapped_area(ratio=True)。
func charBoxOverlapRatio(c pdf.TextChar, x0, x1, y0, y1 float64) float64 {
	cw := c.X1 - c.X0
	ch := c.Bottom - c.Top
	if cw <= 0 {
		cw = 1
	}
	if ch <= 0 {
		ch = 1
	}
	charArea := cw * ch
	if charArea <= 0 {
		return 0
	}
	inter := util.RectOverlapInter(c.X0, c.Top, c.X1, c.Bottom, x0, y0, x1, y1)
	return inter / charArea
}

// ocrTableCells 对 TSR 网格中空文本单元格裁剪表格图并 OCR 填字。
func ocrTableCells(ctx context.Context, cells []pdf.TSRCell, tableImg image.Image, doc pdf.DocAnalyzer) {
	if doc == nil || tableImg == nil || len(cells) == 0 {
		return
	}
	for i := range cells {
		if cells[i].Text != "" {
			continue
		}
		x0 := int(math.Max(0, cells[i].X0))
		y0 := int(math.Max(0, cells[i].Y0))
		x1 := int(math.Min(float64(tableImg.Bounds().Dx()), cells[i].X1))
		y1 := int(math.Min(float64(tableImg.Bounds().Dy()), cells[i].Y1))
		if x0 >= x1 || y0 >= y1 {
			continue
		}
		cropped := util.FastCrop(tableImg, x0, y0, x1, y1)
		texts, err := doc.OCRRecognize(ctx, cropped)
		if err != nil {
			slog.Warn("table cell OCR failed", "err", err)
			continue
		}
		var parts []string
		for _, t := range texts {
			if t.Text != "" {
				parts = append(parts, t.Text)
			}
		}
		cells[i].Text = strings.TrimSpace(strings.Join(parts, " "))
	}
}

// buildTextBoxes 从嵌入字符组装框文本，乱码/空框批量 OCRRecognizeBatch 补全。
func buildTextBoxes(ctx context.Context, pageImg image.Image,
	boxes []ocrDetectBox, boxChars [][]pdf.TextChar, doc pdf.DocAnalyzer, scale float64, pageNum int,
) []pdf.TextBox {
	var result []pdf.TextBox
	var needOCR []int
	for i := range boxes {
		tb := boxes[i].box
		tb.Text = ""
		if len(boxChars[i]) > 0 {
			sortCharsYFirstly(boxChars[i], util.MedianCharHeight(boxChars[i]))
			lineBox := lyt.LineToTextBox(boxChars[i])
			tb.Text = lineBox.Text
			var garbledCnt, totalCnt int
			for _, c := range boxChars[i] {
				for _, r := range c.Text {
					totalCnt++
					if util.IsGarbledChar(string(r)) {
						garbledCnt++
					}
				}
			}
			if totalCnt > 0 && float64(garbledCnt)/float64(totalCnt) >= 0.5 {
				tb.Text = ""
			}
			if tb.Text != "" && util.IsGarbledByFontEncoding(boxChars[i], 5) {
				tb.Text = ""
			}
		}
		if strings.TrimSpace(tb.Text) == "" {
			tb.Text = ""
			needOCR = append(needOCR, i)
		}
		result = append(result, tb)
	}
	if len(needOCR) > 0 {
		cropped := make([]image.Image, len(needOCR))
		for j, idx := range needOCR {
			cropped[j] = util.FastCrop(pageImg,
				int(boxes[idx].x0*scale), int(boxes[idx].y0*scale),
				int(boxes[idx].x1*scale), int(boxes[idx].y1*scale))
		}
		allTexts, allErrs := doc.OCRRecognizeBatch(ctx, cropped)
		for j, idx := range needOCR {
			if allErrs[j] != nil {
				slog.Warn("ocr merge: recognize failed", "page", pageNum, "err", allErrs[j])
				continue
			}
			var ocrParts []string
			for _, t := range allTexts[j] {
				if strings.TrimSpace(t.Text) != "" {
					ocrParts = append(ocrParts, t.Text)
				}
			}
			result[idx].Text = strings.TrimSpace(strings.Join(ocrParts, " "))
		}
	}
	filtered := result[:0]
	for _, tb := range result {
		if strings.TrimSpace(tb.Text) != "" {
			filtered = append(filtered, tb)
		}
	}
	slog.Debug("ocrMergeChars result", "page", pageNum, "boxes", len(filtered))
	return filtered
}
