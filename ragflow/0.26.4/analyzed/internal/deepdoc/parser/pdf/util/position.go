// position.go — RAGFlow @@ 位置标签解析与生成：格式 @@页码\tleft\tright\ttop\tbottom##，页码 1 起写入、0 起读出。

package util

import (
	"fmt"
	"log/slog"
	"regexp"
	"strconv"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// @@ 页位置标签正则。
//
// 对齐 Python remove_tag / extract_positions

// posTagPattern 匹配完整 @@...## 标签含坐标。
// 格式：@@{page_range}\t{left}\t{right}\t{top}\t{bottom}##
var posTagPattern = regexp.MustCompile(`@@[0-9-]+\t[0-9.\t]+##`)

// ExtractPositions 从文本解析所有 @@ 位置标签；page_range 可为单页或范围；返回页码为 0 起索引。
//
// Example:
//
//	text := "Some text @@0-1\t50.0\t300.0\t200.0\t400.0## more text"
//	poss := ExtractPositions(text)
//	// poss[0] = pdf.Position{PageNumbers: [-1, 0], Left: 50.0, Right: 300.0, Top: 200.0, Bottom: 400.0}
func ExtractPositions(text string) []pdf.Position {
	var poss []pdf.Position
	for _, tag := range posTagPattern.FindAllString(text, -1) {
		cleaned := strings.TrimPrefix(strings.TrimSuffix(tag, "##"), "@@")
		parts := strings.Split(cleaned, "\t")
		if len(parts) != 5 {
			continue
		}

		// 解析页码范围
		var pageNums []int
		for _, p := range strings.Split(parts[0], "-") {
			n, err := strconv.Atoi(p)
			if err != nil {
				slog.Warn("ExtractPositions: invalid page number in tag", "tag", tag, "part", p, "err", err)
				continue
			}
			pageNums = append(pageNums, n-1) // 转为 0 起索引
		}

		left, err := strconv.ParseFloat(parts[1], 64)
		if err != nil {
			slog.Warn("ExtractPositions: invalid left coordinate", "tag", tag, "err", err)
			continue
		}
		right, err := strconv.ParseFloat(parts[2], 64)
		if err != nil {
			slog.Warn("ExtractPositions: invalid right coordinate", "tag", tag, "err", err)
			continue
		}
		top, err := strconv.ParseFloat(parts[3], 64)
		if err != nil {
			slog.Warn("ExtractPositions: invalid top coordinate", "tag", tag, "err", err)
			continue
		}
		bottom, err := strconv.ParseFloat(parts[4], 64)
		if err != nil {
			slog.Warn("ExtractPositions: invalid bottom coordinate", "tag", tag, "err", err)
			continue
		}

		poss = append(poss, pdf.Position{
			PageNumbers: pageNums,
			Left:        left,
			Right:       right,
			Top:         top,
			Bottom:      bottom,
		})
	}
	return poss
}

// FormatPositionTag 由页码与 bbox 生成 @@ 标签；ExtractPositions 的逆操作。
//
// Example:
//
//	tag := FormatPositionTag(0, 50.0, 300.0, 200.0, 400.0)
//	// "@@0-0\t50.0\t300.0\t200.0\t400.0##"
func FormatPositionTag(pageNum int, left, right, top, bottom float64) string {
	return fmt.Sprintf("@@%d\t%.1f\t%.1f\t%.1f\t%.1f##",
		pageNum+1, left, right, top, bottom)
}

// FormatPositionTagRange 生成跨页 @@ 位置标签。
//
// Example:
//
//	tag := FormatPositionTagRange(0, 2, 50.0, 300.0, 200.0, 400.0)
//	// "@@0-2\t50.0\t300.0\t200.0\t400.0##"
func FormatPositionTagRange(fromPage, toPage int, left, right, top, bottom float64) string {
	return fmt.Sprintf("@@%d-%d\t%.1f\t%.1f\t%.1f\t%.1f##",
		fromPage+1, toPage+1, left, right, top, bottom)
}
