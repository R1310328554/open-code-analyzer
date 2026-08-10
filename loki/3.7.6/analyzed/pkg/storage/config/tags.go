package config

// Tags 为 string→string 映射，实现 flag.Value 与 yaml.Unmarshaler，用于 DynamoDB 等托管表标签。

import (
	"fmt"
	"strings"
)

// Tags 类型表示云资源标签键值对，可通过命令行 key=value 或 YAML map 配置。
// Tags is a string-string map that implements flag.Value.
type Tags map[string]string

// String implements flag.Value
func (ts Tags) String() string {
	if ts == nil {
		return ""
	}

	return fmt.Sprintf("%v", map[string]string(ts))
}

// Set implements flag.Value
// Set 解析单个 key=value 标签并写入 map，格式错误返回 error。
func (ts *Tags) Set(s string) error {
	if *ts == nil {
		*ts = map[string]string{}
	}

	parts := strings.SplitN(s, "=", 2)
	if len(parts) != 2 {
		return fmt.Errorf("tag must of the format key=value")
	}
	(*ts)[parts[0]] = parts[1]
	return nil
}

// UnmarshalYAML implements yaml.Unmarshaler.
func (ts *Tags) UnmarshalYAML(unmarshal func(interface{}) error) error {
	var m map[string]string
	if err := unmarshal(&m); err != nil {
		return err
	}
	*ts = Tags(m)
	return nil
}

// Equals returns true is other matches ts.
// Equals 逐键比较两 Tags 是否完全相同（键集与值均一致）。
func (ts Tags) Equals(other Tags) bool {
	if len(ts) != len(other) {
		return false
	}

	for k, v1 := range ts {
		v2, ok := other[k]
		if !ok || v1 != v2 {
			return false
		}
	}

	return true
}
// String 将 nil Tags 转为空串，非 nil 时委托 fmt 打印底层 map 内容。
