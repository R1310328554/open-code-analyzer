// This directory was copied and adapted from https://github.com/grafana/agent/tree/main/pkg/metrics.
// We cannot vendor the agent in since the agent vendors loki in, which would cause a cyclic dependency.
// NOTE: many changes have been made to the original code for our use-case.
package util //nolint:revive

// CompareYAML 将两个 Go 值分别 yaml.Marshal 后字节比较，用于配置等价性检测。

import (
	"bytes"

	"gopkg.in/yaml.v2"
)

// CompareYAML 任一侧 Marshal 失败则返回 false，不区分错误类型。
// CompareYAML marshals a and b to YAML and ensures that their contents are
// equal. If either Marshal fails, CompareYAML returns false.
func CompareYAML(a, b interface{}) bool {
	aBytes, err := yaml.Marshal(a)
	if err != nil {
		return false
	}
	bBytes, err := yaml.Marshal(b)
	if err != nil {
		return false
	}
	return bytes.Equal(aBytes, bBytes)
}
// 比较基于序列化文本而非语义，字段顺序不同可能导致 false。
