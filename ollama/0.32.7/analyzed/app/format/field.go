//go:build windows || darwin

// format 包提供桌面应用使用的字符串格式化工具。
package format

import (
	"strings"
	"unicode"
)

// KebabCase 将 camelCase/PascalCase 转为 kebab-case（如 camelCase -> camel-case）。
// KebabCase converts a string from camelCase or PascalCase to kebab-case.
// (e.g. "camelCase" -> "camel-case")
func KebabCase(str string) string {
	var result strings.Builder

	for i, char := range str {
		if i > 0 {
			prevChar := rune(str[i-1])

			// Add hyphen before uppercase letters
			if unicode.IsUpper(char) &&
				(unicode.IsLower(prevChar) || unicode.IsDigit(prevChar) ||
					(i < len(str)-1 && unicode.IsLower(rune(str[i+1])))) {
				result.WriteRune('-')
			}
		}
		result.WriteRune(unicode.ToLower(char))
	}

	return result.String()
}
