// table_annotate.go — PDF 表格解析中的 DLA 区域匹配与布局标注：将 DLA 检测区域与文本框配对、按优先级写入 layout 类型，并为表格单元格写入 R/C/H/SP 网格注释，对齐 Python LayoutRecognizer 与 _table_transformer_job。

package table

import (
	"fmt"
	"math"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	"ragflow/internal/deepdoc/parser/pdf/util"
)

// ── 区域匹配 ──

// TableMatch 将 DLA 表格区域与与之重叠的文本框索引列表配对。
type TableMatch struct {
	Region pdf.DLARegion
	BoxIdx []int
}



// regionOverlapsBox 判断 DLA 区域与文本框是否重叠≥40%（按框面积计）。
func regionOverlapsBox(region pdf.DLARegion, box pdf.TextBox, scale float64) bool {
	rx0 := region.X0 / scale
	ry0 := region.Y0 / scale
	rx1 := region.X1 / scale
	ry1 := region.Y1 / scale
	scaledR := pdf.DLARegion{X0: rx0, Y0: ry0, X1: rx1, Y1: ry1}
	inter := util.OverlapInter(&scaledR, &box)
	boxArea := util.Area(&box)
	if boxArea <= 0 {
		return false
	}
	return inter/boxArea >= 0.4 // 与 Python thr=0.4 一致
}

// MatchTableRegions 将 DLA 表格区域与重叠文本框配对。至少一个框重叠面积≥40%，或无文本框（纯图片 PDF）时仍保留该区域，对齐 Python _table_transformer_job。
func MatchTableRegions(boxes []pdf.TextBox, regions []pdf.DLARegion, scale float64) []TableMatch {
	var matches []TableMatch
	for _, r := range regions {
		if r.Label != pdf.LayoutTypeTable {
			continue
		}
		var matched []int
		for i, b := range boxes {
			if regionOverlapsBox(r, b, scale) {
				matched = append(matched, i)
			}
		}
		if len(matched) > 0 || len(boxes) == 0 {
			matches = append(matches, TableMatch{Region: r, BoxIdx: matched})
		}
	}
	return matches
}

// ── 布局标注 ──

// AnnotateBoxLayouts 按 DLA 区域为文本框写入 LayoutType/LayoutNo，优先级顺序 footer→header→…→equation，重叠阈值 40%。跳过 CID 乱码框，压缩移除垃圾布局框，并为未匹配的 figure/equation 区域合成空文本框。对齐 Python LayoutRecognizer.__call__ 与 _layouts_rec。
func AnnotateBoxLayouts(boxes []pdf.TextBox, regions []pdf.DLARegion, scale float64, pageImgHeight float64) []pdf.TextBox {
	if len(regions) == 0 {
		return boxes
	}

	// 一次性将所有 DLA 区域缩放到 PDF 坐标空间。
	type scaledRegion struct {
		x0, y0, x1, y1 float64
		label          string
	}
	scaled := make([]scaledRegion, len(regions))
	for i, r := range regions {
		scaled[i] = scaledRegion{
			x0: r.X0 / scale, y0: r.Y0 / scale,
			x1: r.X1 / scale, y1: r.Y1 / scale,
			label: r.Label,
		}
	}

	// DLA 置信度过滤，对齐 Python score >= 0.4。
	regionOK := make([]bool, len(regions))
	for i, r := range regions {
		regionOK[i] = r.Confidence >= 0.4 || !isGarbageLayoutType(r.Label)
	}

	// 预计算各 layout 类型内的序号（Python matched 索引），text 与 figure 各自独立计数。
	typeIndex := make([]int, len(regions))
	typeCounters := make(map[string]int)
	for j, r := range scaled {
		if regionOK[j] {
			typeIndex[j] = typeCounters[r.label]
			typeCounters[r.label]++
		}
	}

	// 标记已访问区域（Python layout["visited"]）。
	visited := make([]bool, len(regions))

	// 标记待 pop 移除的框（Python bxs.pop）。
	dropped := make([]bool, len(boxes))

	// 与 Python findLayout 循环一致的优先级顺序。
	priorityOrder := []string{
		pdf.LayoutTypeFooter, pdf.LayoutTypeHeader, pdf.LayoutTypeReference,
		pdf.DLALabelFigureCaption, pdf.DLALabelTableCaption,
		pdf.LayoutTypeTitle, pdf.LayoutTypeTable, pdf.LayoutTypeText,
		pdf.LayoutTypeFigure, pdf.LayoutTypeEquation,
	}
	for _, ty := range priorityOrder {
		for i := range boxes {
			if boxes[i].LayoutType != "" || dropped[i] {
				continue
			}
			// CID 乱码：整框 pop 移除。
			if util.CIDPattern.MatchString(boxes[i].Text) {
				dropped[i] = true
				continue
			}
			boxArea := (boxes[i].X1 - boxes[i].X0) * (boxes[i].Bottom - boxes[i].Top)
			if boxArea <= 0 {
				continue
			}
			bestOverlap := 0.0
			bestJ := -1
			for j, r := range scaled {
				if r.label != ty || !regionOK[j] {
					continue
				}
				ix0 := math.Max(r.x0, boxes[i].X0)
				iy0 := math.Max(r.y0, boxes[i].Top)
				ix1 := math.Min(r.x1, boxes[i].X1)
				iy1 := math.Min(r.y1, boxes[i].Bottom)
				if ix0 < ix1 && iy0 < iy1 {
					ov := (ix1 - ix0) * (iy1 - iy0) / boxArea
					if ov > bestOverlap {
						bestOverlap = ov
						bestJ = j
					}
				}
			}
			if bestJ >= 0 && bestOverlap >= 0.4 {
				// 非页边垃圾布局 → pop 移除。
				if isGarbageLayoutType(ty) && pageImgHeight > 0 && !garbageKeepFeat(ty, boxes[i], pageImgHeight/scale) {
					dropped[i] = true
					continue
				}
				visited[bestJ] = true
				// Python：equation 映射为 figure 的 layout_type
				if ty == pdf.LayoutTypeEquation {
					boxes[i].LayoutType = pdf.LayoutTypeFigure
				} else {
					boxes[i].LayoutType = ty
				}
				// Python：LayoutNo 格式为 "{layout_type}-{matched}"，matched 为类型内序号
				boxes[i].LayoutNo = fmt.Sprintf("%s-%d", ty, typeIndex[bestJ])
			}
		}
	}

	// 压缩：将未 pop 的框写入新切片（Python bxs.pop）。故意分配新数组：调用方 enrichWithDeepDoc 依赖原索引写回，复用原 backing 会破坏映射。
	survivors := 0
	for i := range boxes {
		if !dropped[i] {
			survivors++
		}
	}
	compacted := make([]pdf.TextBox, 0, survivors)
	for i := range boxes {
		if !dropped[i] {
			compacted = append(compacted, boxes[i])
		}
	}
	boxes = compacted

	// 为未匹配的 figure/equation 区域合成空文本框（Python dla_cli.py:187-195）。
	synthIdx := 0
	for j, r := range scaled {
		if !regionOK[j] || visited[j] {
			continue
		}
		if r.label != pdf.LayoutTypeFigure && r.label != pdf.LayoutTypeEquation {
			continue
		}
		boxes = append(boxes, pdf.TextBox{
			X0:         r.x0,
			X1:         r.x1,
			Top:        r.y0,
			Bottom:     r.y1,
			Text:       "",
			LayoutType: pdf.LayoutTypeFigure,
			LayoutNo:   fmt.Sprintf("figure-%d", synthIdx),
		})
		synthIdx++
	}

	return boxes
}

// ── 垃圾布局辅助 ──
// garbageLayoutTypes 对应 Python self.garbage_layouts。
var garbageLayoutTypes = map[string]bool{
	pdf.LayoutTypeFooter: true, pdf.LayoutTypeHeader: true, pdf.LayoutTypeReference: true,
}

// isGarbageLayoutType 判断 layout 类型是否为页眉/页脚/参考文献等垃圾布局。
func isGarbageLayoutType(ty string) bool {
	return garbageLayoutTypes[ty]
}

// garbageKeepFeat 对齐 Python keep_feats：页脚靠近底边或页眉靠近顶边视为真实页眉页脚，其余为 DLA 噪声。
func garbageKeepFeat(ty string, box pdf.TextBox, pageImgHeight float64) bool {
	switch ty {
	case pdf.LayoutTypeFooter:
		return box.Bottom < pageImgHeight*0.9
	case pdf.LayoutTypeHeader:
		return box.Top > pageImgHeight*0.1
	}
	return false
}

// WriteTableAnnotations 为 boxIdx 指定框写入表格网格注释 R/C/H/SP：单元格加 crop 偏移、分组为 grid，再将注释坐标缩放回 PDF 空间。
func WriteTableAnnotations(boxes []pdf.TextBox, boxIdx []int, cells []pdf.TSRCell, scale, cropOffX, cropOffY float64, tb pdf.TableBuilder) {
	tableCells := make([]pdf.TSRCell, len(cells))
	for k := range cells {
		tableCells[k] = CellAddOffset(cells[k], cropOffX, cropOffY)
	}
	tblBoxes := make([]pdf.TextBox, len(boxIdx))
	for k, idx := range boxIdx {
		b := boxes[idx]
		tblBoxes[k] = pdf.TextBox{
			X0: b.X0 * scale, X1: b.X1 * scale,
			Top: b.Top * scale, Bottom: b.Bottom * scale,
			LayoutType: b.LayoutType,
			Text:       b.Text,
		}
	}
	annotGrid := tb.GroupCells(tableCells)
	AnnotateTableBoxes(tblBoxes, annotGrid)
	for k, idx := range boxIdx {
		bp := &tblBoxes[k]
		boxes[idx].R = bp.R
		boxes[idx].RTop = bp.RTop / scale
		boxes[idx].RBott = bp.RBott / scale
		boxes[idx].H = bp.H
		boxes[idx].HTop = bp.HTop / scale
		boxes[idx].HBott = bp.HBott / scale
		boxes[idx].HLeft = bp.HLeft / scale
		boxes[idx].HRight = bp.HRight / scale
		boxes[idx].C = bp.C
		boxes[idx].CLeft = bp.CLeft / scale
		boxes[idx].CRight = bp.CRight / scale
		boxes[idx].SP = bp.SP
	}
}
