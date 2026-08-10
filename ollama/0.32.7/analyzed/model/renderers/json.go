// JSON 格式化：在冒号/逗号后插入空格以匹配部分模型期望。
package renderers

import "encoding/json"

// marshalWithSpaces 序列化 JSON 并在字符串外的 : 与 , 后加空格。
// marshalWithSpaces marshals v to JSON and adds a space after each ':' and ','
// that appears outside of string values. This matches the formatting expected
// by certain model architectures.
// marshalWithSpaces 调用 json.Marshal 再经 addJSONSpaces 格式化。
func marshalWithSpaces(v any) ([]byte, error) {
	b, err := json.Marshal(v)
	if err != nil {
		return nil, err
	}
	return addJSONSpaces(b), nil
}

// addJSONSpaces 扫描字节流，在非字符串内的 : 与 , 后插入空格。
func addJSONSpaces(b []byte) []byte {
	out := make([]byte, 0, len(b)+len(b)/8)
	inStr, esc := false, false
	for _, c := range b {
		if inStr {
			out = append(out, c)
			if esc {
				esc = false
				continue
			}
			if c == '\\' {
				esc = true
				continue
			}
			if c == '"' {
				inStr = false
			}
			continue
		}
		switch c {
		case '"':
			inStr = true
			out = append(out, c)
		case ':':
			out = append(out, ':', ' ')
		case ',':
			out = append(out, ',', ' ')
		default:
			out = append(out, c)
		}
	}
	return out
}
