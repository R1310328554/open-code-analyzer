// similarity.go — PDF 管道文本相似度工具：字符/LCS/段落对齐评分，用于 Go 与 Python 输出 parity 对比；比对前会剥离 #@meta 元数据行。

package tool

import (
	"sort"
	"strings"
	"unicode"
)

// StripMeta 剥离字符串末尾的 \n#@meta 元数据行。
func StripMeta(s string) string {
	if idx := strings.LastIndex(s, "\n#@meta"); idx >= 0 {
		return s[:idx]
	}
	return s
}

// CharSimilarity 基于字符频次（忽略空白）计算相似度，返回 0–100。
func CharSimilarity(a, b string) float64 {
	a = StripMeta(a)
	b = StripMeta(b)
	extract := func(s string) map[rune]int {
		m := make(map[rune]int)
		for _, r := range s {
			if !unicode.IsSpace(r) {
				m[r]++
			}
		}
		return m
	}
	ca, cb := extract(a), extract(b)
	if len(ca) == 0 && len(cb) == 0 {
		return 100
	}
	common, totalA, totalB := 0, 0, 0
	for r, n := range ca {
		totalA += n
		if n2, ok := cb[r]; ok {
			common += min(n, n2)
		}
	}
	for _, n := range cb {
		totalB += n
	}
	if totalA+totalB == 0 {
		return 100
	}
	return float64(common*2) / float64(totalA+totalB) * 100
}

// lcsRunes 用滚动数组计算最长公共子序列长度。
func lcsRunes(a, b []rune) int {
	if len(a) < len(b) {
		a, b = b, a
	}
	m, n := len(b), len(a)
	prev := make([]int, m+1)
	cur := make([]int, m+1)
	for i := 1; i <= n; i++ {
		for j := 1; j <= m; j++ {
			if a[i-1] == b[j-1] {
				cur[j] = prev[j-1] + 1
			} else {
				cur[j] = max(cur[j-1], prev[j])
			}
		}
		prev, cur = cur, prev
	}
	return prev[m]
}

// LcsSimilarity 基于 LCS 与较长串长度之比，忽略空白后返回 0–100。
func LcsSimilarity(a, b string) float64 {
	a = StripMeta(a)
	b = StripMeta(b)
	ra := make([]rune, 0)
	for _, r := range a {
		if !unicode.IsSpace(r) {
			ra = append(ra, r)
		}
	}
	rb := make([]rune, 0)
	for _, r := range b {
		if !unicode.IsSpace(r) {
			rb = append(rb, r)
		}
	}
	if len(ra) == 0 && len(rb) == 0 {
		return 100
	}
	if len(ra) == 0 || len(rb) == 0 {
		return 0
	}
	return float64(lcsRunes(ra, rb)) / float64(max(len(ra), len(rb))) * 100
}

// RawCharSimilarity 与 CharSimilarity 类似但保留空格；仍剥离 #@meta。
func RawCharSimilarity(a, b string) float64 {
	a = StripMeta(a)
	b = StripMeta(b)
	ca := make(map[rune]int)
	for _, r := range a {
		ca[r]++
	}
	cb := make(map[rune]int)
	for _, r := range b {
		cb[r]++
	}
	if len(ca) == 0 && len(cb) == 0 {
		return 100
	}
	common, totalA, totalB := 0, 0, 0
	for r, n := range ca {
		totalA += n
		if n2, ok := cb[r]; ok {
			common += min(n, n2)
		}
	}
	for _, n := range cb {
		totalB += n
	}
	if totalA+totalB == 0 {
		return 100
	}
	return float64(common*2) / float64(totalA+totalB) * 100
}

// RawLcsSimilarity 与 LcsSimilarity 类似但 LCS 含空白；仍剥离 #@meta。
func RawLcsSimilarity(a, b string) float64 {
	a = StripMeta(a)
	b = StripMeta(b)
	ra := []rune(a)
	rb := []rune(b)
	if len(ra) == 0 && len(rb) == 0 {
		return 100
	}
	if len(ra) == 0 || len(rb) == 0 {
		return 0
	}
	return float64(lcsRunes(ra, rb)) / float64(max(len(ra), len(rb))) * 100
}

// SectionAlignedScore 两阶段段落对齐 LCS 评分：阶段一按 CharSimilarity 贪心配对 Go/Python 段落并逐段 LCS；阶段二将未匹配段落拼接后做一次 LCS；最终按字符数加权平均。
func SectionAlignedScore(goText, pyText string) float64 {
	split := func(s string) []string {
		s = StripMeta(s)
		return strings.Split(strings.TrimSpace(s), "\n")
	}
	gs := split(goText)
	ps := split(pyText)
	if len(gs) == 0 && len(ps) == 0 {
		return 100
	}
	if len(gs) == 0 || len(ps) == 0 {
		return 0
	}

	// 阶段一：位置窗口内贪心匹配，避免大文档 O(n×m) 爆炸。
	const alignWindow = 5
	type candidate struct {
		gi, pi int
		sim    float64
	}
	// 预计算各段 rune 长度，用于长度比过滤。
	glens := make([]int, len(gs))
	plens := make([]int, len(ps))
	for i, s := range gs {
		glens[i] = len([]rune(s))
	}
	for i, s := range ps {
		plens[i] = len([]rune(s))
	}

	candidates := make([]candidate, 0, len(gs)*(alignWindow*2+1))
	for i, g := range gs {
		lo := max(0, i-alignWindow)
		hi := min(len(ps)-1, i+alignWindow)
		for j := lo; j <= hi; j++ {
			// 跳过长度差>2× 的配对，避免长短段 LCS 近零。
			if glens[i] > plens[j]*2 || plens[j] > glens[i]*2 {
				continue
			}
			if sim := CharSimilarity(g, ps[j]); sim > 30 {
				candidates = append(candidates, candidate{i, j, sim})
			}
		}
	}
	// 按相似度降序，优先最佳匹配。
	sort.Slice(candidates, func(a, b int) bool {
		return candidates[a].sim > candidates[b].sim
	})

	goUsed := make([]bool, len(gs))
	pyUsed := make([]bool, len(ps))
	matchedScore := 0.0
	matchedChars := 0

	for _, c := range candidates {
		if goUsed[c.gi] || pyUsed[c.pi] {
			continue
		}
		goUsed[c.gi] = true
		pyUsed[c.pi] = true

		// 对已配对段落计算 LCS 比率。
		ra := nonSpaceRunes(gs[c.gi])
		rb := nonSpaceRunes(ps[c.pi])
		lcsScore := 0.0
		if len(ra) > 0 && len(rb) > 0 {
			lcsScore = float64(lcsRunes(ra, rb)) / float64(max(len(ra), len(rb))) * 100
		} else if len(ra) == 0 && len(rb) == 0 {
			lcsScore = 100
		}
		chars := max(len(ra), len(rb))
		matchedScore += lcsScore * float64(chars)
		matchedChars += chars
	}

	// 阶段二：拼接未匹配段，统一算 LCS。
	var goRes, pyRes strings.Builder
	for i, g := range gs {
		if !goUsed[i] {
			goRes.WriteString(g)
			goRes.WriteByte(' ')
		}
	}
	for j, p := range ps {
		if !pyUsed[j] {
			pyRes.WriteString(p)
			pyRes.WriteByte(' ')
		}
	}

	residualScore := 0.0
	residualChars := 0
	goResRunes := nonSpaceRunes(goRes.String())
	pyResRunes := nonSpaceRunes(pyRes.String())
	residualChars = max(len(goResRunes), len(pyResRunes))
	if residualChars > 0 {
		if len(goResRunes) > 5000 || len(pyResRunes) > 5000 {
			// 残余串过大，O(n²) LCS 不可行，回退 CharSimilarity。
			residualScore = CharSimilarity(goRes.String(), pyRes.String())
		} else {
			residualScore = float64(lcsRunes(goResRunes, pyResRunes)) / float64(residualChars) * 100
		}
	} else if len(goResRunes) == 0 && len(pyResRunes) == 0 {
		residualScore = 100
	}

	// 按字符数加权平均得分。
	totalChars := matchedChars + residualChars
	if totalChars == 0 {
		return 100
	}
	return (matchedScore + residualScore*float64(residualChars)) / float64(totalChars)
}

// nonSpaceRunes 过滤空白 rune。
func nonSpaceRunes(s string) []rune {
	out := make([]rune, 0, len(s))
	for _, r := range s {
		if !unicode.IsSpace(r) {
			out = append(out, r)
		}
	}
	return out
}
