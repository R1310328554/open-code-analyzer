package flagext

// flagext 扩展标准 flag 包，提供人类可读的字节大小、CSV 列表与 Prometheus 标签集等配置类型。

import (
	"encoding/json"
	"strings"

	"github.com/c2h5oh/datasize"
)

// ByteSize 封装 uint64 字节数，支持 MB/KB 等可读字符串，实现 flag.Value 与 yaml/json 序列化。
// ByteSize is a flag parsing compatibility type for constructing human friendly sizes.
// It implements flag.Value & flag.Getter.
type ByteSize uint64

func (bs ByteSize) String() string {
	return datasize.ByteSize(bs).String()
}

// Set 解析大小字符串；datasize 仅识别大写单位，故先将输入转为大写再 UnmarshalText。
func (bs *ByteSize) Set(s string) error {
	var v datasize.ByteSize

	// Bytesize currently doesn't handle things like Mb, but only handles MB.
	// Therefore we capitalize just for convenience
	if err := v.UnmarshalText([]byte(strings.ToUpper(s))); err != nil {
		return err
	}
	*bs = ByteSize(v.Bytes())
	return nil
}

func (bs ByteSize) Get() interface{} {
	return bs.Val()
}

func (bs ByteSize) Val() int {
	return int(bs)
}

// UnmarshalYAML 从 YAML 字符串字段解析 ByteSize，复用 Set 逻辑。
// UnmarshalYAML the Unmarshaler interface of the yaml pkg.
func (bs *ByteSize) UnmarshalYAML(unmarshal func(interface{}) error) error {
	var str string
	err := unmarshal(&str)
	if err != nil {
		return err
	}

	return bs.Set(str)
}

// MarshalYAML 以可读字符串形式输出，与命令行 flag 展示保持一致。
// MarshalYAML implements yaml.Marshaller.
// Use a string representation for consistency
func (bs ByteSize) MarshalYAML() (interface{}, error) {
	return bs.String(), nil
}

// UnmarshalJSON 接受 JSON 字符串形式的字节大小配置。
// UnmarshalJSON implements json.Unmarsal interface to work with JSON.
func (bs *ByteSize) UnmarshalJSON(val []byte) error {
	var str string

	if err := json.Unmarshal(val, &str); err != nil {
		return err
	}

	return bs.Set(str)
}

// Use a string representation for consistency
func (bs ByteSize) MarshalJSON() ([]byte, error) {
	return json.Marshal(bs.String())
}
// MarshalJSON 同样输出可读字符串，便于 API 与配置双向转换。
