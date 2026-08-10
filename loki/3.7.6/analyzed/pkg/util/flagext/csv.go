package flagext

// CSV 泛型切片实现逗号分隔 flag 值解析，元素需实现 ListValue 接口以自定义 Parse 逻辑。

import (
	"strings"
)

type ListValue interface {
	String() string
	Parse(s string) (any, error)
}

// CSV[T] 从逗号分隔字符串解析为 []T，同时实现 flag.Value 与 yaml 编解码。
// StringSliceCSV is a slice of strings that is parsed from a comma-separated string
// It implements flag.Value and yaml Marshalers
type CSV[T ListValue] []T

// String implements flag.Value
func (v CSV[T]) String() string {
	s := make([]string, 0, len(v))
	for i := range v {
		s = append(s, v[i].String())
	}
	return strings.Join(s, ",")
}

// Set 按逗号拆分输入，空串清空切片；各段经 zero.Parse 转为 T 并追加。
// Set implements flag.Value
func (v *CSV[T]) Set(s string) error {
	if len(s) == 0 {
		*v = nil
		return nil
	}
	var zero T
	values := strings.Split(s, ",")
	*v = make(CSV[T], 0, len(values))
	for _, val := range values {
		el, err := zero.Parse(val)
		if err != nil {
			return err
		}
		*v = append(*v, el.(T))
	}
	return nil
}

// String implements flag.Getter
func (v CSV[T]) Get() []T {
	return v
}

// UnmarshalYAML 将 YAML 标量当作逗号分隔串交给 Set 解析。
// UnmarshalYAML implements yaml.Unmarshaler.
func (v *CSV[T]) UnmarshalYAML(unmarshal func(interface{}) error) error {
	var s string
	if err := unmarshal(&s); err != nil {
		return err
	}

	return v.Set(s)
}

// MarshalYAML implements yaml.Marshaler.
func (v CSV[T]) MarshalYAML() (interface{}, error) {
	return v.String(), nil
}
// MarshalYAML 序列化为逗号连接的元素 String 表示，便于配置文件人类可读。
