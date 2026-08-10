// UTF-8 流缓冲：提取并消费最长合法 UTF-8 前缀，保留不完整尾部。
package mlxrunner

import (
	"bytes"
	"unicode/utf8"
)

// flushValidUTF8Prefix 返回并消费 buffer 中最长合法 UTF-8 前缀。
// flushValidUTF8Prefix returns and consumes the longest valid UTF-8 prefix
// currently buffered, leaving any incomplete trailing bytes in place.
// flushValidUTF8Prefix 刷新合法 UTF-8 前缀并更新 buffer。
func flushValidUTF8Prefix(b *bytes.Buffer) string {
	data := b.Bytes()
	if len(data) == 0 {
		return ""
	}

	prefix := validUTF8PrefixLen(data)
	if prefix == 0 {
		return ""
	}

	text := string(data[:prefix])
	b.Next(prefix)
	return text
}

// validUTF8PrefixLen 计算 data 中最长合法 UTF-8 前缀长度。
func validUTF8PrefixLen(data []byte) int {
	i := 0
	prefix := 0
	for i < len(data) {
		r, size := utf8.DecodeRune(data[i:])
		if r == utf8.RuneError && size == 1 {
			if !utf8.FullRune(data[i:]) {
				break
			}

			// 非法 UTF-8 字节：消费一字节以保证前进。
			// Invalid UTF-8 byte; consume one byte to guarantee forward progress.
			i++
			prefix = i
			continue
		}

		i += size
		prefix = i
	}

	return prefix
}
