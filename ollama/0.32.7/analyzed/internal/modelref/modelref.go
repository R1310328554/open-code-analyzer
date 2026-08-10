// 模型引用解析：:local/:cloud 后缀与拉取名称规范化。
package modelref

import (
	"errors"
	"fmt"
	"strings"
)

// ModelSource 表示模型来源（未指定/本地/云）。
type ModelSource uint8

// ModelSource 枚举：未指定、本地或云。
const (
	ModelSourceUnspecified ModelSource = iota
	ModelSourceLocal
	ModelSourceCloud
)

// 解析错误：不可同时使用 :local 与 :cloud。
var (
	ErrConflictingSourceSuffix = errors.New("use either :local or :cloud, not both")
	ErrModelRequired           = errors.New("model is required")
)

// ParsedRef 保存原始引用、去后缀基名与显式来源。
type ParsedRef struct {
	Original string
	Base     string
	Source   ModelSource
}

// ParseRef 解析模型名并剥离 :local/:cloud 或 -cloud 后缀。
func ParseRef(raw string) (ParsedRef, error) {
	var zero ParsedRef

	raw = strings.TrimSpace(raw)
	if raw == "" {
		return zero, ErrModelRequired
	}

	base, source, explicit := parseSourceSuffix(raw)
	if explicit {
		if _, _, nested := parseSourceSuffix(base); nested {
			return zero, fmt.Errorf("%w: %q", ErrConflictingSourceSuffix, raw)
		}
	}

	return ParsedRef{
		Original: raw,
		Base:     base,
		Source:   source,
	}, nil
}

// HasExplicitCloudSource 判断引用是否显式指定云来源。
func HasExplicitCloudSource(raw string) bool {
	parsedRef, err := ParseRef(raw)
	return err == nil && parsedRef.Source == ModelSourceCloud
}

// HasExplicitLocalSource 判断引用是否显式指定本地来源。
func HasExplicitLocalSource(raw string) bool {
	parsedRef, err := ParseRef(raw)
	return err == nil && parsedRef.Source == ModelSourceLocal
}

// StripCloudSourceTag 去除 :cloud 后缀并返回是否发生过剥离。
func StripCloudSourceTag(raw string) (string, bool) {
	parsedRef, err := ParseRef(raw)
	if err != nil || parsedRef.Source != ModelSourceCloud {
		return strings.TrimSpace(raw), false
	}

	return parsedRef.Base, true
}

// NormalizePullName 将带云后缀的引用转为 legacy pull 名称格式。
func NormalizePullName(raw string) (string, bool, error) {
	parsedRef, err := ParseRef(raw)
	if err != nil {
		return "", false, err
	}

	if parsedRef.Source != ModelSourceCloud {
		return parsedRef.Base, false, nil
	}

	return toLegacyCloudPullName(parsedRef.Base), true, nil
}

// toLegacyCloudPullName 为云模型生成 -cloud 或 :cloud 后缀名。
func toLegacyCloudPullName(base string) string {
	if HasExplicitTag(base) {
		return base + "-cloud"
	}

	return base + ":cloud"
}

// HasExplicitTag 判断名称是否含显式 tag（如 model:8b），registry 主机端口不计。
// HasExplicitTag reports whether name contains an explicit tag (e.g.
// "model:8b"), as opposed to relying on the default tag. Colons in a
// registry host (e.g. "registry.example.com:5000/model") don't count.
func HasExplicitTag(name string) bool {
	lastSlash := strings.LastIndex(name, "/")
	lastColon := strings.LastIndex(name, ":")
	return lastColon > lastSlash
}

// parseSourceSuffix 解析末尾 :cloud/:local 或 -cloud 后缀。
func parseSourceSuffix(raw string) (string, ModelSource, bool) {
	idx := strings.LastIndex(raw, ":")
	if idx >= 0 {
		suffixRaw := strings.TrimSpace(raw[idx+1:])
		suffix := strings.ToLower(suffixRaw)

		switch suffix {
		case "cloud":
			return raw[:idx], ModelSourceCloud, true
		case "local":
			return raw[:idx], ModelSourceLocal, true
		}

		if !strings.Contains(suffixRaw, "/") && strings.HasSuffix(suffix, "-cloud") {
			return raw[:idx+1] + suffixRaw[:len(suffixRaw)-len("-cloud")], ModelSourceCloud, true
		}
	}

	return raw, ModelSourceUnspecified, false
}
