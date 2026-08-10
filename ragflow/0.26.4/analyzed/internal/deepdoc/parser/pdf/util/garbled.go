// garbled.go — PDF 乱码/扫描噪声检测：CID 占位符、PUA 私用区、子集字体编码异常、pdf_oxide # 占位与扫描页噪声；触发 OCR 回退。对齐 Python _is_garbled_* / _is_scan_noise。

package util

import (
	"regexp"
	"strings"
	"unicode"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// CIDPattern 匹配 pdfminer CID 占位符如 "(cid:123)"。
//
// 对齐 Python pdf_parser.py:198 _CID_PATTERN
var CIDPattern = regexp.MustCompile(`\(cid\s*:\s*\d+\s*\)`)

// subsetFontPattern 匹配 PDF 子集字体前缀如 "ABCDEF+"。
// 子集字体名为 2–6 位大写字数字 + '+'。
//
// 对齐 Python _has_subset_font_prefix()
var subsetFontPattern = regexp.MustCompile(`^[A-Z0-9]{2,6}\+`)

// HasSubsetFontPrefix 判断字体名是否含 PDF 子集前缀。
//
// 示例：
//
//	HasSubsetFontPrefix("DY1+ZLQDm1-1") → true
//	HasSubsetFontPrefix("SimSun")        → false
//	HasSubsetFontPrefix("")              → false
//
// 对齐 Python pdf_parser.py:253
func HasSubsetFontPrefix(fontname string) bool {
	if fontname == "" {
		return false
	}
	return subsetFontPattern.MatchString(fontname)
}

// IsGarbledChar 判断单字符是否乱码：PUA、U+FFFD、控制符（除\t\n\r）、C1 控制区、Unicode Cn/Cs 等。
//
// 对齐 Python _is_garbled_char()
//
// Example:
//
//	IsGarbledChar("") → true  (PUA)
//	IsGarbledChar("A")       → false
//	IsGarbledChar("�")  → true  (replacement char)
//	IsGarbledChar("")        → false
func IsGarbledChar(ch string) bool {
	if ch == "" {
		return false
	}
	// 始终按 rune 值判断（正确处理多字节 UTF-8）
	runes := []rune(ch)
	cp := int(runes[0])

	// 私用区 PUA
	if (cp >= 0xE000 && cp <= 0xF8FF) ||
		(cp >= 0xF0000 && cp <= 0xFFFFF) ||
		(cp >= 0x100000 && cp <= 0x10FFFF) {
		return true
	}
	// 替换字符 U+FFFD
	if cp == 0xFFFD {
		return true
	}
	// 控制字符（tab/换行/回车除外）
	if cp < 0x20 && ch != "\t" && ch != "\n" && ch != "\r" {
		return true
	}
	// C1 控制区 U+0080–U+009F
	if cp >= 0x80 && cp <= 0x9F {
		return true
	}

	// 逐 rune 检查 Unicode 类别
	for _, r := range ch {
		cat := catOf(rune(r))
		if cat == "Cn" || cat == "Cs" {
			return true
		}
	}
	return false
}

// IsGarbledText 判断文本乱码字符占比是否超过阈值；亦检测 CID 模式。
// 亦将 "(cid:123)" 类 CID 占位视为乱码。
//
// 对齐 Python _is_garbled_text()
//
// Example:
//
//	IsGarbledText("正常文本", 0.5)     → false
//	IsGarbledText("", 0.5) → true
//	IsGarbledText("(cid:123)", 0.5)   → true
//	IsGarbledText("", 0.5)             → false
func IsGarbledText(text string, threshold float64) bool {
	trimmed := strings.TrimSpace(text)
	if trimmed == "" {
		return false
	}
	if CIDPattern.MatchString(trimmed) {
		return true
	}

	garbledCount := 0
	total := 0
	for _, r := range trimmed {
		if unicode.IsSpace(r) {
			continue
		}
		total++
		if IsGarbledChar(string(r)) {
			garbledCount++
		}
	}
	if total == 0 {
		return false
	}
	return float64(garbledCount)/float64(total) >= threshold
}

// IsGarbledByFontEncoding 检测子集字体编码映射异常：≥30% 子集字体且 CJK<5% 且 ASCII 标点>40% 时判乱码，需 OCR。
//
// 对齐 Python _is_garbled_by_font_encoding()
//
// Example:
//
//	chars := []pdf.TextChar{
//	  {Text: "!", FontName: "DY1+SimSun"},
//	  {Text: "#", FontName: "DY1+SimSun"},
//	  // ... mostly ASCII punctuation with subset font prefix
//	}
//	IsGarbledByFontEncoding(chars, 20) → true  // OCR needed!
func IsGarbledByFontEncoding(chars []pdf.TextChar, minChars int) bool {
	if len(chars) < minChars {
		return false
	}

	subsetFontCount := 0
	totalNonSpace := 0
	asciiPunctSym := 0
	cjkLike := 0

	for _, c := range chars {
		text := strings.TrimSpace(c.Text)
		if text == "" {
			continue
		}
		totalNonSpace++

		if HasSubsetFontPrefix(c.FontName) {
			subsetFontCount++
		}

		// Always use the rune value
		runes := []rune(text)
		cp := int(runes[0])

		// CJK Unified Ideographs, CJK Compatibility, CJK Extension B
		// Hangul syllables, Hiragana, Katakana
		// Fullwidth forms (U+FF00-U+FF5E): legitimate CJK typographic characters
		if (cp >= 0x2E80 && cp <= 0x9FFF) ||
			(cp >= 0xF900 && cp <= 0xFAFF) ||
			(cp >= 0x20000 && cp <= 0x2FA1F) ||
			(cp >= 0xAC00 && cp <= 0xD7AF) ||
			(cp >= 0x3040 && cp <= 0x30FF) ||
			(cp >= 0xFF00 && cp <= 0xFF5E) {
			cjkLike++
		} else if (cp >= 0x21 && cp <= 0x2F) || // !"#$%&'()*+,-./
			(cp >= 0x3A && cp <= 0x40) || // :;<=>?@
			(cp >= 0x5B && cp <= 0x60) || // [\]^_`
			(cp >= 0x7B && cp <= 0x7E) { // {|}~
			asciiPunctSym++
		}
	}

	if totalNonSpace < minChars {
		return false
	}

	subsetRatio := float64(subsetFontCount) / float64(totalNonSpace)
	if subsetRatio < 0.3 {
		return false
	}

	cjkRatio := float64(cjkLike) / float64(totalNonSpace)
	punctRatio := float64(asciiPunctSym) / float64(totalNonSpace)

	return cjkRatio < 0.05 && punctRatio > 0.4
}

// catOf 返回 Unicode 类别简写：Cs  surrogate、Cn 未分配、其余为空；对齐 unicodedata.category。
func catOf(r rune) string {
	if r >= 0xD800 && r <= 0xDFFF {
		return "Cs" // surrogate
	}
	// C1 controls (0x80-0x9F): Python returns "Cc", not "Cn".
	if r >= 0x80 && r <= 0x9F {
		return ""
	}
	// A rune is unassigned (Cn) if it's NOT in any recognized category.
	// Python unicodedata.category() returns "Cc" for control chars,
	// "Cn" only for truly unassigned. We match that behavior.
	if !unicode.IsPrint(r) &&
		!unicode.IsSpace(r) &&
		!unicode.IsControl(r) &&
		!unicode.Is(unicode.Cf, r) &&
		!unicode.Is(unicode.Co, r) &&
		r > 0x20 {
		return "Cn"
	}
	return ""
}

// IsGarbledPage 综合 PUA 比例、字体编码、pdf_oxide # 占位与扫描噪声判定整页乱码。
func IsGarbledPage(chars []pdf.TextChar) bool {
	if len(chars) < 20 {
		return false
	}
	// 单次遍历拼接全页文本供检测。
	var fullText strings.Builder
	for _, c := range chars {
		fullText.WriteString(c.Text)
	}
	text := fullText.String()
	if IsGarbledText(text, 0.3) {
		return true
	}
	if PdfOxideUnmappedGarbled(text) && IsScanNoise(text) {
		return true
	}
	if IsGarbledByFontEncoding(chars, 20) {
		return true
	}
	if IsScanNoise(text) {
		return true
	}
	return false
}

// IsScanNoise 检测扫描页 pdf_oxide 噪声：无≥4 小写拉丁、≥2 CJK 或≥4 非 ASCII 字母连续段；纯大写碎片如 RASB 不计为真实词。
func IsScanNoise(text string) bool {
	nonSpace := 0
	digitCount := 0
	lowerRun := 0
	maxLowerRun := 0
	cjkRun := 0
	maxCJKRun := 0
	nonASCIILetterRun := 0
	maxNonASCIILetterRun := 0

	for _, r := range text {
		if r == ' ' || r == '\t' || r == '\n' || r == '\r' {
			lowerRun = 0
			cjkRun = 0
			nonASCIILetterRun = 0
			continue
		}
		nonSpace++

		// 数字密度：真实内容（表格/日期）含数字；
		// pdf_oxide 噪声不会产生数字。
		if r >= '0' && r <= '9' {
			digitCount++
		}

		// 小写拉丁 Ll
		if unicode.Is(unicode.Ll, r) {
			lowerRun++
			if lowerRun > maxLowerRun {
				maxLowerRun = lowerRun
			}
		} else {
			lowerRun = 0
		}

		// CJK：汉字/假名/谚文
		if pdf.IsCJK(r) {
			cjkRun++
			if cjkRun > maxCJKRun {
				maxCJKRun = cjkRun
			}
		} else {
			cjkRun = 0
		}

		// 非 ASCII 字母（阿拉伯/泰/西里尔等）；排除 ASCII 大写碎片。
		if unicode.IsLetter(r) && r > unicode.MaxASCII {
			nonASCIILetterRun++
			if nonASCIILetterRun > maxNonASCIILetterRun {
				maxNonASCIILetterRun = nonASCIILetterRun
			}
		} else {
			nonASCIILetterRun = 0
		}
	}

	// 非空格字符≥30 才做判定。
	if nonSpace < 30 {
		return false
	}

	// 数字占比≥10% 视为真实内容而非噪声。
	if float64(digitCount)/float64(nonSpace) >= 0.10 {
		return false
	}

	// 任一脚本的真实文本指标满足即非噪声。
	isNoise := maxLowerRun < 4 && maxCJKRun < 2 && maxNonASCIILetterRun < 4

	return isNoise
}

// isCJK 注释已移至 doctype.IsCJK；此处为历史占位。
// Katakana, Hangul syllable, or Hangul Jamo.

// PdfOxideUnmappedGarbled 检测 pdf_oxide 用 '#' 占位未映射字形：≥2 处 "###" 或非空格中 # 密度≥3%（≥40 字符时）。
func PdfOxideUnmappedGarbled(text string) bool {
	hashCount := 0
	total := 0
	consecutive := 0
	tripleClusters := 0

	for _, r := range text {
		if r == ' ' || r == '\t' || r == '\n' || r == '\r' {
			continue
		}
		total++
		if r == '#' {
			hashCount++
			consecutive++
			if consecutive == 3 {
				tripleClusters++
			}
		} else {
			consecutive = 0
		}
	}

	if total == 0 {
		return false
	}

	density := float64(hashCount) / float64(total)

	if tripleClusters >= 1 {
		return true
	}
	// 密度判定需足够字符数；生产采样约 200 字符。
	if total >= 40 && density >= 0.03 {
		return true
	}
	return false
}

// ocrDetectAndRecognize（声明）对乱码/扫描页跑 OCR 检测+识别，logLabel 区分调用场景。
