package chunk

// factory 定义 chunk 编码类型 Encoding 及其注册表，支持 flag 解析与按编码构造 Data 实例。

import (
	"fmt"
	"strconv"
)

// Encoding 为 chunk 压缩/序列化格式的字节标识，当前仅内置 Dummy 占位编码。
// Encoding defines which encoding we are using, delta, doubledelta, or varbit
type Encoding byte

const (
	Dummy Encoding = iota
)

// String implements flag.Value.
// String 实现 flag.Value，返回已知编码名或数字字符串。
func (e Encoding) String() string {
	if known, found := encodings[e]; found {
		return known.Name
	}
	return fmt.Sprintf("%d", e)
}

// Set implements flag.Value.
func (e *Encoding) Set(s string) error {
	// First see if the name was given
	for k, v := range encodings {
		if s == v.Name {
			*e = k
			return nil
		}
	}
	// Otherwise, accept a number
	i, err := strconv.Atoi(s)
	if err != nil {
		return err
	}

	_, ok := encodings[Encoding(i)]
	if !ok {
		return fmt.Errorf("invalid chunk encoding: %s", s)
	}

	*e = Encoding(i)
	return nil
}

type encoding struct {
	Name string
	New  func() Data
}

var encodings = map[Encoding]encoding{
	Dummy: {
		Name: "dummy",
		New:  func() Data { return newDummyChunk() },
	},
}

// NewForEncoding allows configuring what chunk type you want
// NewForEncoding 根据编码查找注册工厂并构造新的 chunk.Data 实例。
func NewForEncoding(encoding Encoding) (Data, error) {
	enc, ok := encodings[encoding]
	if !ok {
		return nil, fmt.Errorf("unknown chunk encoding: %v", encoding)
	}

	return enc.New(), nil
}

// MustRegisterEncoding add a new chunk encoding.  There is no locking, so this
// must be called in init().
// MustRegisterEncoding 在 init 阶段注册新编码，重复注册会 panic。
func MustRegisterEncoding(enc Encoding, name string, f func() Data) {
	_, ok := encodings[enc]
	if ok {
		panic("double register encoding")
	}

	encodings[enc] = encoding{
		Name: name,
		New:  f,
	}
}
// Set 先按名称匹配 encodings 表，否则解析整数并校验是否为已知编码值。
