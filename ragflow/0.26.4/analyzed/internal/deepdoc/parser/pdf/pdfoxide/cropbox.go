// cropbox.go — PDF 原始字节解析：扫描 /CropBox 与 /Rotate 等字典项，无需完整 PDF 解析器。

package pdfoxide

import "strconv"

// parseCropBoxFromRaw 顺序扫描原始 PDF 中的 /CropBox [x0 y0 x1 y1]，按出现顺序对应页索引（0 基）；未找到返回 false。算法同 parsePageRotationFromRaw，兼容常见 PDF 生成器。
func parseCropBoxFromRaw(data []byte, pageIdx int) ([4]float64, bool) {
	type cb [4]float64
	var boxes []cb
	rest := data
	for {
		idx := indexAfter(rest, "/CropBox")
		if idx < 0 {
			break
		}
		rest = rest[idx:]
		// Skip whitespace, expect '['
		for len(rest) > 0 && isSpace(rest[0]) {
			rest = rest[1:]
		}
		if len(rest) == 0 || rest[0] != '[' {
			continue
		}
		rest = rest[1:]
		// Parse 4 float values inside [...]
		var vals [4]float64
		ok := true
		for i := 0; i < 4; i++ {
			for len(rest) > 0 && isSpace(rest[0]) {
				rest = rest[1:]
			}
			v, n := parseFloat(rest)
			if n == 0 {
				ok = false
				break
			}
			vals[i] = v
			rest = rest[n:]
		}
		if !ok {
			continue
		}
		boxes = append(boxes, cb(vals))
	}
	if pageIdx < len(boxes) {
		return boxes[pageIdx], true
	}
	return [4]float64{}, false
}

// indexAfter 查找 s 首次出现后的字节位置，未找到返回 -1。
func indexAfter(data []byte, s string) int {
	for i := 0; i < len(data)-len(s); i++ {
		match := true
		for j := 0; j < len(s); j++ {
			if data[i+j] != s[j] {
				match = false
				break
			}
		}
		if match {
			return i + len(s)
		}
	}
	return -1
}

// isSpace 判断空白字符（空格/制表/换行）。
func isSpace(b byte) bool {
	return b == ' ' || b == '\t' || b == '\n' || b == '\r'
}

// parseFloat 从字节串开头解析十进制浮点数，返回数值与消耗字节数（失败为 0）。
func parseFloat(s []byte) (float64, int) {
	i := 0
	for i < len(s) && isSpace(s[i]) {
		i++
	}
	j := i
	// Scan: optional sign, digits, optional decimal point + digits
	if j < len(s) && (s[j] == '+' || s[j] == '-') {
		j++
	}
	hasDigit := false
	for j < len(s) && s[j] >= '0' && s[j] <= '9' {
		j++
		hasDigit = true
	}
	if j < len(s) && s[j] == '.' {
		j++
		for j < len(s) && s[j] >= '0' && s[j] <= '9' {
			j++
			hasDigit = true
		}
	}
	if !hasDigit || j == i {
		return 0, 0
	}
	v, err := strconv.ParseFloat(string(s[i:j]), 64)
	if err != nil {
		return 0, 0
	}
	return v, j
}
