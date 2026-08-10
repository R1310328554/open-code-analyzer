//go:build windows || darwin

// Package tools URL 白名单策略：仅允许打开用户消息中提供的直接链接。
package tools

import (
	"context"
	"regexp"
	"strings"
)

// directURLContextKey 为 context 中允许直接访问 URL 集合的键类型。
type directURLContextKey struct{}

// directURLPattern 匹配用户文本中的 http(s) URL。
var directURLPattern = regexp.MustCompile("https?://[^\\s<>\"'`]+")

// WithAllowedDirectURLs 从用户文本提取 URL 并写入 context 白名单。
func WithAllowedDirectURLs(ctx context.Context, text string) context.Context {
	allowed := make(map[string]struct{})
	for _, match := range directURLPattern.FindAllString(text, -1) {
		addAllowedDirectURLToMap(allowed, match)
	}
	return context.WithValue(ctx, directURLContextKey{}, allowed)
}

// addAllowedDirectURL 向 context 白名单追加一条 URL（如搜索结果链接）。
func addAllowedDirectURL(ctx context.Context, raw string) {
	allowed, _ := ctx.Value(directURLContextKey{}).(map[string]struct{})
	addAllowedDirectURLToMap(allowed, raw)
}

// addAllowedDirectURLToMap 清洗 URL 后写入允许集合。
func addAllowedDirectURLToMap(allowed map[string]struct{}, raw string) {
	if allowed == nil {
		return
	}

	raw = cleanDirectURL(raw)
	if raw == "" {
		return
	}

	allowed[raw] = struct{}{}
}

// allowedDirectURL 检查 URL 是否在白名单且未经清洗篡改。
func allowedDirectURL(ctx context.Context, raw string) bool {
	allowed, _ := ctx.Value(directURLContextKey{}).(map[string]struct{})
	cleaned := cleanDirectURL(raw)
	if cleaned == "" || cleaned != raw {
		return false
	}

	_, ok := allowed[cleaned]
	return ok
}

// cleanDirectURL 去除首尾空白与尾部标点，仅保留 http(s) URL。
func cleanDirectURL(raw string) string {
	raw = strings.TrimSpace(raw)
	raw = strings.TrimRight(raw, ".,;:!?)]}")

	if !strings.HasPrefix(raw, "http://") && !strings.HasPrefix(raw, "https://") {
		return ""
	}

	return raw
}
