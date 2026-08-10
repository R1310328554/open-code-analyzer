package log

// util 提供标签键规范化与字符串去重等流水线内部小工具函数。

import (
	"bytes"
	"strings"
)

func uniqueString(s []string) []string {
	unique := make(map[string]bool, len(s))
	us := make([]string, 0, len(s))
	for _, elem := range s {
		if len(elem) != 0 {
			if !unique[elem] {
				us = append(us, elem)
				unique[elem] = true
			}
		}
	}
	return us
}

// sanitizeLabelKey 修剪空白并将非法字符替换为下划线；前缀模式下数字开头会加前导 _。
func sanitizeLabelKey(key string, isPrefix bool) string {
	if len(key) == 0 {
		return key
	}
	key = strings.TrimSpace(key)
	if len(key) == 0 {
		return key
	}
	if isPrefix && key[0] >= '0' && key[0] <= '9' {
		key = "_" + key
	}
	return strings.Map(func(r rune) rune {
		if (r >= 'a' && r <= 'z') || (r >= 'A' && r <= 'Z') || r == '_' || (r >= '0' && r <= '9') {
			return r
		}
		return '_'
	}, key)
}

// appendSanitized 将字节键规范化后追加到目标切片，供解析器就地构建标签名。
// appendSanitize appends the sanitized key to the slice.
func appendSanitized(to, key []byte) []byte {
	key = bytes.TrimSpace(key)

	if len(key) == 0 {
		return to
	}

	if len(to) == 0 && key[0] >= '0' && key[0] <= '9' {
		to = append(to, '_')
	}
	// range over rune

	for _, r := range string(key) {
		if (r < 'a' || r > 'z') && (r < 'A' || r > 'Z') && r != '_' && (r < '0' || r > '9') {
			to = append(to, '_')
			continue
		}
		to = append(to, byte(r))

	}
	return to
}
// 标签键规则与 Prometheus 标签命名一致，仅允许字母数字与下划线。
