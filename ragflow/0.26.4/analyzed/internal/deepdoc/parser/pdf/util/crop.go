// crop.go — PDF 段落/区域图像裁剪：按 @@ 位置标签或 DLA 区域从渲染页图裁剪、拼接上下文带、旋转坐标映射，输出 base64 PNG；对齐 Python RAGFlowPdfParser.crop/cropout。

package util

import (
	"encoding/base64"
	"fmt"
	"image"
	"image/color"
	"log/slog"
	"math"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// CropSectionImage 按 @@ 位置标签从渲染页图裁剪并拼接，返回 base64 PNG；缺图/越界/无效标签时返回空串。对齐 Python RAGFlowPdfParser.crop()。
func CropSectionImage(posTag string, decodedImages map[int]image.Image, zoom float64) string {
	if len(decodedImages) == 0 {
		slog.Warn("cropSectionImage: no page images available, skipping image generation")
		return ""
	}

	positions := ExtractPositions(posTag)
	if len(positions) == 0 {
		slog.Warn("cropSectionImage: empty position list in tag", "posTag", posTag[:min(80, len(posTag))])
		return ""
	}

	// 过滤页图均存在的有效位置。
	var valid []pdf.Position
	for _, pos := range positions {
		allValid := true
		for _, pn := range pos.PageNumbers {
			if _, ok := decodedImages[pn]; !ok {
				allValid = false
				break
			}
		}
		if allValid {
			valid = append(valid, pos)
		}
	}
	if len(valid) == 0 {
		slog.Warn("cropSectionImage: no valid positions after filtering, skipping crop")
		return ""
	}

	// 上下文填充：首段上 120px、末段下 120px、段间 6px 灰隙。
	const contextPad = 120.0
	const gap = 6

	// 计算原始位置最大宽度，供边缘全宽条带使用。
	maxWidth := 6.0
	for _, pos := range valid {
		w := pos.Right - pos.Left
		if w > maxWidth {
			maxWidth = w
		}
	}

	// Python 风格：首尾插入合成上下文条带（全宽+半透明遮罩），中间为窄内容段。
	first := valid[0]
	last := valid[len(valid)-1]
	firstPageIdx := first.PageNumbers[0]
	lastPageIdx := last.PageNumbers[len(last.PageNumbers)-1]
	lastPageH := float64(decodedImages[lastPageIdx].Bounds().Dy()) / zoom

	// topBand：首段上方 120px 上下文。
	topBandPos := pdf.Position{
		PageNumbers: []int{firstPageIdx},
		Left:        first.Left,
		Right:       first.Right,
		Top:         math.Max(0, first.Top-contextPad),
		Bottom:      math.Max(first.Top-gap, 0),
	}
	// bottomBand：末段下方 120px 上下文。
	bottomBandPos := pdf.Position{
		PageNumbers: []int{lastPageIdx},
		Left:        last.Left,
		Right:       last.Right,
		Top:         math.Min(lastPageH, last.Bottom+gap),
		Bottom:      math.Min(lastPageH, last.Bottom+contextPad),
	}

	// 构建序列：[topBand, 原位置..., bottomBand]。
	type segment struct {
		img    image.Image
		isEdge bool
	}
	var segments []segment

	allPos := make([]struct {
		pos    pdf.Position
		isEdge bool
	}, 0, len(valid)+2)
	allPos = append(allPos, struct {
		pos    pdf.Position
		isEdge bool
	}{topBandPos, true})
	for _, pos := range valid {
		allPos = append(allPos, struct {
			pos    pdf.Position
			isEdge bool
		}{pos, false})
	}
	allPos = append(allPos, struct {
		pos    pdf.Position
		isEdge bool
	}{bottomBandPos, true})

	for _, entry := range allPos {
		pos := entry.pos
		isEdge := entry.isEdge

		top := pos.Top
		bottom := pos.Bottom
		left := pos.Left
		right := pos.Right

		// 边缘段全宽，中间段保持窄宽。
		if !isEdge {
			right = math.Max(left+10, right)
		} else {
			right = left + maxWidth
		}

		pn0 := pos.PageNumbers[0]

		// 跨页位置累加 bottom 像素高度。
		accumBottom := bottom * zoom
		for _, pn := range pos.PageNumbers[1:] {
			if pn == pn0 {
				continue
			}
			if img, ok := decodedImages[pn]; ok {
				accumBottom += float64(img.Bounds().Dy())
			}
		}

		pageImg, ok := decodedImages[pn0]
		if !ok {
			slog.Warn("cropSectionImage: page image not found", "page", pn0)
			return ""
		}
		pageH := float64(pageImg.Bounds().Dy())
		bottomClamped := math.Min(accumBottom, pageH)

		// 裁剪该位置的首页区域。
		cropped := FastCrop(pageImg,
			int(left*zoom), int(top*zoom),
			int(right*zoom), int(bottomClamped))
		if isEdge {
			cropped = applyEdgeOverlay(cropped)
		}
		segments = append(segments, segment{img: cropped, isEdge: isEdge})

		// 后续页（页码不同于首页）。
		bottomRemaining := accumBottom - pageH
		for _, pn := range pos.PageNumbers[1:] {
			if pn == pn0 {
				continue
			}
			pageImg2, ok := decodedImages[pn]
			if !ok {
				slog.Warn("cropSectionImage: page image not found for subsequent page", "page", pn)
				return ""
			}
			pageH2 := float64(pageImg2.Bounds().Dy())
			bottomClamped2 := math.Min(bottomRemaining, pageH2)
			cropped2 := FastCrop(pageImg2,
				int(left*zoom), 0,
				int(right*zoom), int(bottomClamped2))
			if isEdge {
				cropped2 = applyEdgeOverlay(cropped2)
			}
			segments = append(segments, segment{img: cropped2, isEdge: isEdge})
			bottomRemaining -= bottomClamped2
		}
	}

	if len(segments) == 0 {
		return ""
	}

	// 垂直拼接，灰底 245 与 6px 间隔。
	totalH := 0
	maxW := 0
	for _, seg := range segments {
		totalH += seg.img.Bounds().Dy() + gap
		maxW = max(maxW, seg.img.Bounds().Dx())
	}
	stitched := image.NewRGBA(image.Rect(0, 0, maxW, totalH))

	// 直接写 Pix 填充灰底（与 fastCrop 一致），BGRA 245,245,245,255。
	for y := 0; y < totalH; y++ {
		row := stitched.Pix[stitched.PixOffset(0, y):stitched.PixOffset(maxW, y)]
		for i := 0; i < len(row); i += 4 {
			row[i] = 245   // B
			row[i+1] = 245 // G
			row[i+2] = 245 // R
			row[i+3] = 255 // A
		}
	}

	curY := 0
	for _, seg := range segments {
		srcW := seg.img.Bounds().Dx()
		srcH := seg.img.Bounds().Dy()
		if rgba, ok := seg.img.(*image.RGBA); ok {
			// 快路径：RGBA 直接 Pix 行拷贝。
			srcMinX := seg.img.Bounds().Min.X
			srcMinY := seg.img.Bounds().Min.Y
			for ry := 0; ry < srcH; ry++ {
				srcStart := rgba.PixOffset(srcMinX, srcMinY+ry)
				srcRow := rgba.Pix[srcStart : srcStart+srcW*4]
				dstStart := stitched.PixOffset(0, curY+ry)
				copy(stitched.Pix[dstStart:], srcRow)
			}
		} else {
			// 非 RGBA（如边缘遮罩）逐像素回退。
			for y := 0; y < srcH; y++ {
				for x := 0; x < srcW; x++ {
					stitched.Set(x, curY+y, seg.img.At(x+seg.img.Bounds().Min.X, y+seg.img.Bounds().Min.Y))
				}
			}
		}
		curY += srcH + gap
	}

	data, err := EncodePNG(stitched)
	if err != nil {
		slog.Warn("cropSectionImage: PNG encode failed", "err", err)
		return ""
	}
	return base64.StdEncoding.EncodeToString(data)
}

// cropSectionByDLA crops a section using the best-overlapping DLA region.
// It finds a DLA "figure" or "equation" region whose overlap with the section's
// bounding box is maximal, then crops from the page image at 216 DPI using the
// DLA region boundary (plus 3% margin via cropImageRegion).
//
// Returns "" (empty string) if no matching DLA region or page image is found.
// The caller should fall through to cropSectionImage as a fallback.
//
// Python equivalent: cropout() in pdf_parser.py:1144-1148
//
//	louts = [layout for layout in self.page_layout[pn] if layout["type"] == ltype]
//	ii = Recognizer.find_overlapped(b, louts, naive=True)
//	if ii is not None: b = louts[ii]
func CropSectionByDLA(sec pdf.Section, dlaDebug []pdf.DLAPageRegions, pageImages map[int]image.Image) string {
	if len(sec.Positions) == 0 || len(sec.Positions[0].PageNumbers) == 0 {
		return ""
	}
	pg := sec.Positions[0].PageNumbers[0]
	pos := sec.Positions[0]

	// 查找该页的 DLA 区域列表。
	var regions []pdf.DLARegion
	for _, dp := range dlaDebug {
		if dp.Page == pg {
			regions = dp.Regions
			break
		}
	}
	if len(regions) == 0 {
		return ""
	}

	// 段落 bbox 从 72 DPI 点坐标缩放到 216 DPI 像素空间。
	scale := pdf.DlaDPI / 72.0 // 3.0
	bx := Rect{
		X0: pos.Left * scale,
		Y0: pos.Top * scale,
		X1: pos.Right * scale,
		Y1: pos.Bottom * scale,
	}

	// 在 figure/equation 中取重叠最大的 DLA 区域。
	bestIdx := -1
	bestOverlap := 0.0
	for i, r := range regions {
		if r.Label != pdf.LayoutTypeFigure && r.Label != pdf.LayoutTypeEquation {
			continue
		}
		overlap := RectOverlap(bx, Rect{r.X0, r.Y0, r.X1, r.Y1})
		if overlap > bestOverlap {
			bestOverlap = overlap
			bestIdx = i
		}
	}
	if bestIdx < 0 {
		slog.Warn("cropSectionByDLA: no matching layout region found", "page", pg)
		return ""
	}

	img, ok := pageImages[pg]
	if !ok {
		return ""
	}
	cropped, err := CropImageRegion(img, regions[bestIdx])
	if err != nil {
		slog.Warn("cropSectionByDLA: cropImageRegion failed", "page", pg, "err", err)
		return ""
	}
	data, err := EncodePNG(cropped)
	if err != nil {
		slog.Warn("cropSectionByDLA: PNG encode failed", "err", err)
		return ""
	}
	return base64.StdEncoding.EncodeToString(data)
}

// applyEdgeOverlay 对边缘条带施加半透明黑色遮罩，对齐 Python crop 边缘段处理：
//
//	img.convert("RGBA")
//	overlay = Image.new("RGBA", img.size, (0,0,0,0))
//	overlay.putalpha(128)
//	img = Image.alpha_composite(img, overlay).convert("RGB")
func applyEdgeOverlay(img image.Image) *image.RGBA {
	b := img.Bounds()
	result := image.NewRGBA(b)
	const overlayAlpha = 128 // ~50% opacity black overlay
	factor := 1.0 - float64(overlayAlpha)/255.0
	for y := 0; y < b.Dy(); y++ {
		for x := 0; x < b.Dx(); x++ {
			r, g, bb, a := img.At(x+b.Min.X, y+b.Min.Y).RGBA()
			r8, g8, b8, a8 := uint8(r>>8), uint8(g>>8), uint8(bb>>8), uint8(a>>8)
			result.Set(x, y, color.RGBA{
				R: uint8(float64(r8) * factor),
				G: uint8(float64(g8) * factor),
				B: uint8(float64(b8) * factor),
				A: a8,
			})
		}
	}
	return result
}

// rotateCoordCW 将 (x,y) 按顺时针角度映射到新坐标系；仅 0/90/180/270 有效。
func rotateCoordCW(x, y float64, origW, origH int, angle int) (float64, float64) {
	switch angle {
	case 0:
		return x, y
	case 90:
		return float64(origH-1) - y, x
	case 180:
		return float64(origW-1) - x, float64(origH-1) - y
	case 270:
		return y, float64(origW-1) - x
	default:
		return x, y
	}
}

// RotateImageCW 顺时针旋转图像；仅 0/90/180/270，对齐 PIL rotate(-angle, expand=True)。
func RotateImageCW(img image.Image, angle int) *image.RGBA {
	b := img.Bounds()
	w, h := b.Dx(), b.Dy()

	dstW, dstH := w, h
	switch angle {
	case 90, 270:
		dstW, dstH = h, w
	case 0, 180:
		// keep w, h
	default:
		return nil
	}

	dst := image.NewRGBA(image.Rect(0, 0, dstW, dstH))
	for y := 0; y < h; y++ {
		for x := 0; x < w; x++ {
			dx, dy := rotateCoordCW(float64(x), float64(y), w, h, angle)
			dst.Set(int(dx), int(dy), img.At(x+b.Min.X, y+b.Min.Y))
		}
	}
	return dst
}

// MapRotatedPointToOriginal 将旋转后坐标逆映射回原图；origW/origH 为旋转前尺寸。
func MapRotatedPointToOriginal(x, y float64, angle int, origW, origH int) (float64, float64) {
	switch angle {
	case 0:
		return x, y
	case 90:
		// rotateImageCW 90°: (ox,oy) → (origH-1-oy, ox) = (rx,ry).
		// Inverse: ox = ry, oy = origH-1 - rx.
		return y, float64(origH) - 1 - x
	case 180:
		// rotateImageCW 180°: (ox,oy) → (origW-1-ox, origH-1-oy).
		// Inverse: ox = origW-1 - rx, oy = origH-1 - ry.
		return float64(origW) - 1 - x, float64(origH) - 1 - y
	case 270:
		// rotateImageCW 270°: (ox,oy) → (oy, origW-1-ox) = (rx,ry).
		// Inverse: ox = origW-1 - ry, oy = rx.
		return float64(origW) - 1 - y, x
	default:
		return x, y
	}
}

// CropImageRegion 按 DLA 区域裁剪并加 3% 边距；无效区域返回 error 而非整页回退。
func CropImageRegion(img image.Image, r pdf.DLARegion) (image.Image, error) {
	w := r.X1 - r.X0
	h := r.Y1 - r.Y0
	marginX := w * 0.03
	marginY := h * 0.03
	maxX := float64(img.Bounds().Dx())
	maxY := float64(img.Bounds().Dy())
	x0 := int(math.Max(0, r.X0-marginX))
	y0 := int(math.Max(0, r.Y0-marginY))
	x1 := int(math.Min(maxX, r.X1+marginX))
	y1 := int(math.Min(maxY, r.Y1+marginY))
	// 与 PIL crop 一致：x0>=x1 或 y0>=y1 时返回 error，调用方跳过该表。
	if x0 >= x1 || y0 >= y1 {
		return nil, fmt.Errorf("crop: invalid region x0=%d y0=%d x1=%d y1=%d (DLA raw: %.1f,%.1f,%.1f,%.1f)",
			x0, y0, x1, y1, r.X0, r.Y0, r.X1, r.Y1)
	}
	cropped := FastCrop(img, x0, y0, x1, y1)
	return cropped, nil
}
