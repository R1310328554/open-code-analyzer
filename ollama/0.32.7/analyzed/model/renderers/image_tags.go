package renderers

import (
	"fmt"
	"strings"
	"unicode"
	"unicode/utf8"
)

// 图像占位符：[img] / [img-N] 顺序替换与前置补全。
// renderContentWithImageTags 保留服务端 [img] 占位语义：按序替换，余下图像前置标签。
// renderContentWithImageTags preserves the legacy server-side placeholder
// semantics for explicit [img] tokens: replace placeholders in order, and
// only prepend tags for any remaining images without placeholders.
// renderContentWithImageTags 将 content 中 [img] 或前置 [img-N] 与 imageCount 对齐。
func renderContentWithImageTags(content string, imageCount int, imageOffset int) (string, int) {
	if imageCount == 0 {
		return content, imageOffset
	}

	if strings.Contains(content, "[img-") {
		return content, imageOffset + imageCount
	}

	var prefix strings.Builder
	for i := range imageCount {
		imgTag := fmt.Sprintf("[img-%d]", imageOffset+i)
		if strings.Contains(content, "[img]") {
			content = strings.Replace(content, "[img]", imgTag, 1)
		} else {
			prefix.WriteString(imgTag)
		}
	}

	if prefix.Len() > 0 && content != "" {
		if r, _ := utf8.DecodeRuneInString(content); r != utf8.RuneError && !unicode.IsSpace(r) {
			prefix.WriteByte(' ')
		}
	}

	return prefix.String() + content, imageOffset + imageCount
}
