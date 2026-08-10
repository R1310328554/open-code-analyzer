package loghttp

// versions 根据请求 URI 路径判断 Loki HTTP API 版本（legacy 或 v1）。

import (
	"strings"
)

// Version 枚举 loghttp API 版本号。
// Version holds a loghttp version
type Version int

// Valid Version values
const (
	VersionLegacy = Version(iota)
	VersionV1
)

// GetVersion 路径含 /loki/api/v1 时返回 VersionV1，否则为 VersionLegacy。
// GetVersion returns the loghttp version for a given path.
func GetVersion(uri string) Version {
	if strings.Contains(strings.ToLower(uri), "/loki/api/v1") {
		return VersionV1
	}

	return VersionLegacy
}
// push 解析等逻辑依版本选择 JSON 反序列化器（v1 与 legacy 格式略有差异）。
