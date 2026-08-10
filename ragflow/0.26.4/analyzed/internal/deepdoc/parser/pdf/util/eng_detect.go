// eng_detect.go — PDF 英文文档检测：按页采样字符、检测连续 ASCII 序列，多数页投票判定是否英文；对齐 Python pdf_parser.__images__ 中 is_english 逻辑。

package util

import (
	"math/rand/v2"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// IsASCIIPrintable 判断 rune 是否属于 Python is_english 正则允许的 ASCII 可打印字符集。
func IsASCIIPrintable(r rune) bool {
	if r == ' ' {
		return true
	}
	if r >= 'a' && r <= 'z' {
		return true
	}
	if r >= 'A' && r <= 'Z' {
		return true
	}
	if r >= '0' && r <= '9' {
		return true
	}
	// Python 正则中的额外 ASCII 符号
	switch r {
	case ',', '/', '¸', ';', ':', '\'', '[', ']', '(', ')',
		'!', '@', '#', '$', '%', '^', '&', '*', '"', '?',
		'<', '>', '.', '_', '-':
		return true
	}
	return false
}

// DefaultSampleChars 随机采样最多 n 个字符文本并拼接；对齐 Python random.choices。
func DefaultSampleChars(chars []pdf.TextChar, n int) string {
	if n <= 0 || len(chars) == 0 {
		return ""
	}
	m := min(n, len(chars))
	// Fisher-Yates 洗牌后取前 m 个索引。
	indices := make([]int, len(chars))
	for i := range indices {
		indices[i] = i
	}
	rand.Shuffle(len(indices), func(i, j int) {
		indices[i], indices[j] = indices[j], indices[i]
	})
	var buf strings.Builder
	for i := 0; i < m; i++ {
		buf.WriteString(chars[indices[i]].Text)
	}
	return buf.String()
}

// FullTextFromChars 拼接全页字符文本，供扫描噪声检测使用。
func FullTextFromChars(pageChars map[int][]pdf.TextChar) string {
	var sb strings.Builder
	for _, chars := range pageChars {
		for _, c := range chars {
			sb.WriteString(c.Text)
		}
	}
	return sb.String()
}

// DetectEnglish 逐页采样并检测≥30 连续 ASCII，多数页满足则判为英文。totalPages 含无字符的纯图页，与 Python page_images 长度一致。
func DetectEnglish(pageChars map[int][]pdf.TextChar, totalPages int, sample pdf.SampleFunc) bool {
	if totalPages == 0 || len(pageChars) == 0 {
		return false
	}
	if sample == nil {
		sample = DefaultSampleChars
	}
	pagesWithSeq := 0

	for _, chars := range pageChars {
		if len(chars) == 0 {
			continue
		}
		sampleText := sample(chars, 100)
		run := 0
		for _, r := range sampleText {
			if IsASCIIPrintable(r) {
				run++
				if run >= 30 {
					pagesWithSeq++
					break
				}
			} else {
				run = 0
			}
		}
	}

	return pagesWithSeq > totalPages/2
}
