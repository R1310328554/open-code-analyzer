// layout.go — PDF 版面后处理：列分配（KMeans+轮廓系数）、横/纵合并、阅读顺序与项目符号合并。

package layout

import (
	"log/slog"
	"math"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	util "ragflow/internal/deepdoc/parser/pdf/util"
	"regexp"
	"slices"
	"sort"
	"strings"
	"unicode"
	"unicode/utf8"
)

// ---- 列分配 ----

// AssignColumn 每页对 x0 做 KMeans+轮廓系数选 k 并分配 ColID；对齐 _assign_column()。
func AssignColumn(boxes []pdf.TextBox, zoom float64) []pdf.TextBox {
	if len(boxes) == 0 {
		return boxes
	}

	pageGroups, sortedPages := groupBoxesByPage(boxes)

	result := make([]pdf.TextBox, len(boxes))
	copy(result, boxes)

	// 步骤 A：每页用轮廓系数选最佳 k。
	pageCols := make(map[int]int)
	for _, pg := range sortedPages {
		indices := pageGroups[pg]
		determineBestKForPage(boxes, result, indices, pg, pageCols)
	}

	// Step B: assign col_id per page using per-page best k.
	// Labels are remapped by centroid x-order: leftmost column → 0.
	for _, pg := range sortedPages {
		indices := pageGroups[pg]
		assignColIDsForPage(boxes, result, indices, pg, pageCols)
	}

	return result
}

// determineBestKForPage 用轮廓系数在 k∈[1,min(4,n)] 中选最佳 k。
func determineBestKForPage(boxes, result []pdf.TextBox, indices []int, pg int, pageCols map[int]int) {
	n := len(indices)
	if n < 2 {
		pageCols[pg] = 1
		for _, idx := range indices {
			result[idx].ColID = 0
		}
		return
	}

	x0s, minX0, maxX1 := extractX0Values(boxes, indices)
	pageWidth := maxX1 - minX0
	indentTol := pageWidth * 0.12
	applyIndentTolerance(x0s, minX0, indentTol)

	bestK, _ := findBestK(x0s, n)
	pageCols[pg] = bestK
}

// extractX0Values 收集页内 box 的 x0 及 minX0、maxX1。
func extractX0Values(boxes []pdf.TextBox, indices []int) (x0s []float64, minX0 float64, maxX1 float64) {
	n := len(indices)
	x0s = make([]float64, n)
	minX0 = math.MaxFloat64
	maxX1 = 0.0
	for i, idx := range indices {
		x0s[i] = boxes[idx].X0
		if x0s[i] < minX0 {
			minX0 = x0s[i]
		}
		if boxes[idx].X1 > maxX1 {
			maxX1 = boxes[idx].X1
		}
	}
	return x0s, minX0, maxX1
}

// applyIndentTolerance 将接近 minX0 的 x0 钳到 minX0，改善聚类。
func applyIndentTolerance(x0s []float64, minX0, indentTol float64) {
	for i := range x0s {
		if math.Abs(x0s[i]-minX0) < indentTol {
			x0s[i] = minX0
		}
	}
}

// findBestK 遍历 k 取轮廓系数最高者。
func findBestK(x0s []float64, n int) (bestK int, bestScore float64) {
	maxTry := min(4, n)
	if maxTry < 2 {
		maxTry = 1
	}
	bestK, bestScore = 1, -1.0

	for k := 1; k <= maxTry; k++ {
		labels, _ := util.KMeans1D(x0s, k)
		var score float64
		if k > 1 {
			score = util.Silhouette1D(x0s, labels)
		}
		// score = 0 for k=1; score = -1 if silhouette undefined.
		if score > bestScore {
			bestScore = score
			bestK = k
		}
	}
	return bestK, bestScore
}

// assignColIDsForPage 按页最佳 k 为 box 写入 ColID。
func assignColIDsForPage(boxes, result []pdf.TextBox, indices []int, pg int, pageCols map[int]int) {
	if len(indices) == 0 {
		return
	}
	k := pageCols[pg]
	if len(indices) < k {
		k = 1
	}

	x0s := make([]float64, len(indices))
	for i, idx := range indices {
		x0s[i] = boxes[idx].X0
	}

	labels, centroids := util.KMeans1D(x0s, k)
	remap := remapLabelsByCentroidOrder(centroids)

	for i, idx := range indices {
		result[idx].ColID = remap[labels[i]]
	}
}

// remapLabelsByCentroidOrder 按质心 x 升序将簇标签映射为 0 起列号。
func remapLabelsByCentroidOrder(centroids []float64) map[int]int {
	type clPair struct {
		center float64
		label  int
	}
	var pairs []clPair
	for lbl, c := range centroids {
		pairs = append(pairs, clPair{c, lbl})
	}
	sort.Slice(pairs, func(i, j int) bool { return pairs[i].center < pairs[j].center })
	remap := make(map[int]int, len(centroids))
	for newL, p := range pairs {
		remap[p.label] = newL
	}
	return remap
}

// ---- 横向文本合并 ----

// TextMerge 同页同列、layoutno 一致且纵向接近的相邻 box 横向合并；对齐 _text_merge()。
func TextMerge(boxes []pdf.TextBox, medianHeights map[int]float64, zoom float64) []pdf.TextBox {
	if len(boxes) < 2 {
		return boxes
	}
	// 线性扫描合并，O(n)，避免 O(n²) 切片删除。
	out := make([]pdf.TextBox, 0, len(boxes))
	i := 0
	for i < len(boxes) {
		cur := boxes[i]
		i++
		for i < len(boxes) {
			nxt := boxes[i]
			if cur.PageNumber != nxt.PageNumber || cur.ColID != nxt.ColID {
				break
			}
			// Python: b.get("layoutno", "0") != b_.get("layoutno", "1") —
			// asymmetric defaults mean empty/missing layoutno never merge horizontally.
			if cur.LayoutNo != nxt.LayoutNo || cur.LayoutNo == "" || nxt.LayoutNo == "" ||
				cur.LayoutType == pdf.LayoutTypeTable || cur.LayoutType == pdf.LayoutTypeFigure || cur.LayoutType == pdf.LayoutTypeEquation {
				break
			}
			mh := medianHeights[cur.PageNumber]
			if mh <= 0 {
				mh = 10
			}
			if math.Abs(util.BoxYDis(cur, nxt)) < mh/3 {
				cur.X1 = nxt.X1
				cur.Top = (cur.Top + nxt.Top) / 2
				cur.Bottom = (cur.Bottom + nxt.Bottom) / 2
				cur.Text += nxt.Text
				i++
			} else {
				break
			}
		}
		out = append(out, cur)
	}
	return out
}

// ---- 朴素纵向合并 ----

// NaiveVerticalMerge 同页纵向合并段落块；对齐 _naive_vertical_merge()。
func NaiveVerticalMerge(boxes []pdf.TextBox, medianHeights map[int]float64, medianWidths map[int]float64, isEnglish bool) []pdf.TextBox {
	if len(boxes) < 2 {
		return boxes
	}

	// 按页分组处理
	pageGroups, sortedPages := groupBoxesByPage(boxes)

	var result []pdf.TextBox
	for _, pg := range sortedPages {
		// 收集当前页全部 box
		indices := pageGroups[pg]
		bxs := make([]pdf.TextBox, len(indices))
		for i, idx := range indices {
			bxs[i] = boxes[idx]
		}

		mh := medianHeights[pg]
		if mh <= 0 {
			mh = util.MedianHeight(bxs)
		}
		mw := medianWidths[pg]
		if mw <= 0 {
			mw = 8 // 中位宽缺失时回退 8（对齐 Python pdf_parser.py:1465）
		}

		// 对单页执行 processPageBoxes
		processed := processPageBoxes(bxs, mh, mw, isEnglish)
		result = append(result, processed...)
	}
	slog.Debug("vm result", "in", len(boxes), "out", len(result))
	return result
}

// ---- 阅读顺序 ----

// FinalReadingOrderMerge 按页→列→top→x0 排序；对齐 _final_reading_order_merge()。
func FinalReadingOrderMerge(boxes []pdf.TextBox) []pdf.TextBox {
	if len(boxes) == 0 {
		return boxes
	}
	sort.Slice(boxes, func(i, j int) bool {
		bi, bj := boxes[i], boxes[j]
		if bi.PageNumber != bj.PageNumber {
			return bi.PageNumber < bj.PageNumber
		}
		if bi.ColID != bj.ColID {
			return bi.ColID < bj.ColID
		}
		if bi.Top != bj.Top {
			return bi.Top < bj.Top
		}
		return bi.X0 < bj.X0
	})
	return boxes
}

var pageNumSuffixPattern = regexp.MustCompile(`[0-9  •一—-]+$`)

// groupBoxesByPage 按页号分组，返回页→索引列表与排序后的页号列表。
func groupBoxesByPage(boxes []pdf.TextBox) (map[int][]int, []int) {
	if len(boxes) == 0 {
		return map[int][]int{}, []int{}
	}

	pageGroups := make(map[int][]int)
	for i, b := range boxes {
		pageGroups[b.PageNumber] = append(pageGroups[b.PageNumber], i)
	}

	// 对页号键排序
	pageKeys := make([]int, 0, len(pageGroups))
	for pg := range pageGroups {
		pageKeys = append(pageKeys, pg)
	}
	sort.Ints(pageKeys)

	return pageGroups, pageKeys
}

// shouldMergeBoxes 根据 layoutno、纵向间隙、水平重叠及中英文标点规则判断是否纵向合并。
func shouldMergeBoxes(prev, curr *pdf.TextBox, mh, mw float64, isEnglish bool) bool {
	// 检查 layoutno 是否一致
	if prev.LayoutNo != curr.LayoutNo {
		slog.Debug("vm reject", "reason", "layoutNo", "prevLayout", prev.LayoutNo, "currLayout", curr.LayoutNo)
		return false
	}

	// 检查纵向间隙是否 ≤ mh*1.5
	gap := curr.Top - prev.Bottom
	if gap > mh*1.5 {
		slog.Debug("vm reject", "reason", "gap", "gap", gap, "threshold", mh*1.5, "mh", mh)
		return false
	}

	// 检查水平重叠率是否 ≥ 0.3
	ov := util.OverlapX(prev, curr)
	if ov < 0.3 {
		slog.Debug("vm reject", "reason", "ovX", "ov", ov, "threshold", 0.3)
		return false
	}

	// 检查连接符/反连接/ detach 条件
	prevText := strings.TrimSpace(prev.Text)
	currText := strings.TrimSpace(curr.Text)

	concatting := []bool{
		endsWithOneOf(prevText, ",;:\"，、‘“；：-"),
		endsSecondLastOneOf(prevText, ",;:\"，、‘“；："),
		startsWithOneOf(currText, "。；？！?\"）),，、："),
	}
	anti := []bool{
		endsWithOneOf(prevText, "。？！?"),
		isEnglish && endsWithOneOf(prevText, ".!?"),
		prev.PageNumber < curr.PageNumber && math.Abs(prev.X0-curr.X0) > mw*4,
	}
	detach := []bool{prev.X1 < curr.X0, prev.X0 > curr.X1}

	if (slices.Contains(anti, true) && !slices.Contains(concatting, true)) || slices.Contains(detach, true) {
		return false
	}

	return true
}

// mergeTwoBoxes 合并两 box 的文本与 bbox。
func mergeTwoBoxes(prev, curr pdf.TextBox) pdf.TextBox {
	prevText := strings.TrimSpace(prev.Text)
	currText := strings.TrimSpace(curr.Text)

	prev.Text = strings.TrimSpace(strings.TrimRight(prevText, " \t") + " " + strings.TrimLeft(currText, " \t"))
	prev.Bottom = math.Max(prev.Bottom, curr.Bottom)
	prev.X0 = math.Min(prev.X0, curr.X0)
	prev.X1 = math.Max(prev.X1, curr.X1)

	prevTrunc, currTrunc := prevText, currText
	if r := []rune(prevTrunc); len(r) > 40 {
		prevTrunc = string(r[:40])
	}
	if r := []rune(currTrunc); len(r) > 40 {
		currTrunc = string(r[:40])
	}
	slog.Debug("vm merge", "prev", prevTrunc, "curr", currTrunc)

	return prev
}

// processPageBoxes 单页按 Top/X0 排序后逐对尝试纵向合并。
func processPageBoxes(boxes []pdf.TextBox, mh, mw float64, isEnglish bool) []pdf.TextBox {
	if len(boxes) == 0 {
		return boxes
	}

	// 按 Top、X0 排序
	sortedBoxes := make([]pdf.TextBox, len(boxes))
	copy(sortedBoxes, boxes)
	sort.Slice(sortedBoxes, func(i, j int) bool {
		if sortedBoxes[i].Top != sortedBoxes[j].Top {
			return sortedBoxes[i].Top < sortedBoxes[j].Top
		}
		return sortedBoxes[i].X0 < sortedBoxes[j].X0
	})

	out := make([]pdf.TextBox, 0, len(sortedBoxes))
	for i := 0; i < len(sortedBoxes); i++ {
		curr := sortedBoxes[i]

		// 跳过跨页页码后缀（如上一页页脚页码）
		if i > 0 && sortedBoxes[i-1].PageNumber < curr.PageNumber && pageNumSuffixPattern.MatchString(sortedBoxes[i-1].Text) {
			continue
		}

		// 空文本 box：若与上一块接近则延伸 bottom
		if strings.TrimSpace(curr.Text) == "" {
			if len(out) > 0 {
				prev := &out[len(out)-1]
				if curr.Top-prev.Bottom <= mh*1.5 && util.OverlapX(prev, &curr) >= 0.3 {
					// TODO: prev.Bottom = math.Max(prev.Bottom, curr.Bottom) — direct assignment might shrink tall merged boxes
					// Matches Python behavior (also direct assignment). Defer fix until pipeline alignment release.
					prev.Bottom = curr.Bottom
				}
			}
			continue
		}

		if len(out) == 0 {
			out = append(out, curr)
			continue
		}

		prev := &out[len(out)-1]
		if shouldMergeBoxes(prev, &curr, mh, mw, isEnglish) {
			out[len(out)-1] = mergeTwoBoxes(*prev, curr)
		} else {
			out = append(out, curr)
		}
	}

	return out
}

// ---- 基于 rune 的文本辅助（CJK 安全） ----

// lastRune 取字符串最后一个 rune。
func lastRune(s string) rune {
	r, _ := utf8.DecodeLastRuneInString(s)
	return r
}

// firstRune 取字符串第一个 rune。
func firstRune(s string) rune {
	r, _ := utf8.DecodeRuneInString(s)
	return r
}

// secondLastRune 取倒数第二个 rune。
func secondLastRune(s string) rune {
	r, size := utf8.DecodeLastRuneInString(s)
	if r == utf8.RuneError && size == 0 {
		return 0
	}
	r2, _ := utf8.DecodeLastRuneInString(s[:len(s)-size])
	return r2
}

// endsWithOneOf 末字符是否在 set 中。
func endsWithOneOf(s, set string) bool {
	r := lastRune(s)
	if r == 0 {
		return false
	}
	return strings.ContainsRune(set, r)
}

// endsSecondLastOneOf 倒数第二字符是否在 set 中。
func endsSecondLastOneOf(s, set string) bool {
	r := secondLastRune(s)
	if r == 0 {
		return false
	}
	return strings.ContainsRune(set, r)
}

// startsWithOneOf 首字符是否在 set 中。
func startsWithOneOf(s, set string) bool {
	r := firstRune(s)
	if r == 0 {
		return false
	}
	return strings.ContainsRune(set, r)
}

// MergeSameBullet 合并首字符相同且非拉丁/非中文的项目符号相邻 box，文本用换行连接。
func MergeSameBullet(boxes []pdf.TextBox, tok pdf.Tokenizer) []pdf.TextBox {
	if len(boxes) < 2 {
		return boxes
	}
	out := make([]pdf.TextBox, 0, len(boxes))
	i := 0
	for i < len(boxes) {
		if strings.TrimSpace(boxes[i].Text) == "" {
			i++
			continue
		}
		cur := boxes[i]
		i++
		for i < len(boxes) {
			if strings.TrimSpace(boxes[i].Text) == "" {
				i++
				continue
			}
			nxt := boxes[i]
			firstCur := firstRuneString(cur.Text)
			firstNxt := firstRuneString(nxt.Text)
			if firstCur != firstNxt ||
				unicode.Is(unicode.Latin, firstCur) ||
				isChinese(firstCur, tok) ||
				cur.Top > nxt.Bottom {
				break
			}
			cur.Text = cur.Text + "\n" + nxt.Text
			cur.X0 = min(cur.X0, nxt.X0)
			cur.X1 = max(cur.X1, nxt.X1)
			cur.Bottom = nxt.Bottom
			i++
		}
		out = append(out, cur)
	}
	return out
}

// firstRuneString 取 TrimSpace 后首 rune。
func firstRuneString(s string) rune {
	s = strings.TrimSpace(s)
	if s == "" {
		return 0
	}
	return []rune(s)[0]
}

// isChinese checks if a rune is a Chinese character (CJK Unified Ideograph).
func isChinese(r rune, tok pdf.Tokenizer) bool {
	if tok != nil {
		return strings.Contains(tok.Tag(string(r)), "n")
	}
	return (r >= 0x4E00 && r <= 0x9FFF) ||
		(r >= 0x3400 && r <= 0x4DBF) ||
		(r >= 0x20000 && r <= 0x2A6DF)
}
