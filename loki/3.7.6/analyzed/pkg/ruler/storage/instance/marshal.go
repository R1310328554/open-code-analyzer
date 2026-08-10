// This directory was copied and adapted from https://github.com/grafana/agent/tree/main/pkg/metrics.
// We cannot vendor the agent in since the agent vendors loki in, which would cause a cyclic dependency.
// NOTE: many changes have been made to the original code for our use-case.
package instance

// marshal 提供 instance.Config 的 YAML 严格解码与编码，Clone 依赖 Marshal/Unmarshal 深拷贝。

import (
	"bytes"
	"io"

	"gopkg.in/yaml.v2"
)

// UnmarshalConfig 使用 yaml.Decoder SetStrict(true) 拒绝未知字段。
// UnmarshalConfig unmarshals an instance config from a reader based on a
// provided content type.
func UnmarshalConfig(r io.Reader) (*Config, error) {
	var cfg Config
	dec := yaml.NewDecoder(r)
	dec.SetStrict(true)
	err := dec.Decode(&cfg)
	return &cfg, err
}

// MarshalConfig 委托 MarshalConfigToWriter；scrubSecrets 参数当前未使用。
// MarshalConfig marshals an instance config based on a provided content type.
func MarshalConfig(c *Config, scrubSecrets bool) ([]byte, error) {
	var buf bytes.Buffer
	err := MarshalConfigToWriter(c, &buf, scrubSecrets)
	return buf.Bytes(), err
}

// MarshalConfigToWriter 通过 plain 类型别名避免触发 Config 自定义 MarshalYAML。
// MarshalConfigToWriter marshals a config to an io.Writer.
func MarshalConfigToWriter(c *Config, w io.Writer, _ bool) error {
	enc := yaml.NewEncoder(w)

	type plain Config
	return enc.Encode((*plain)(c))
}
// Config.MarshalYAML 内部也调用 MarshalConfig 以保持键顺序与 MapSlice 一致。
