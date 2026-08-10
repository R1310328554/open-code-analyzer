package tracing

// otel_kv 将 go-kit 风格交替 key/value 参数转换为 OpenTelemetry attribute.KeyValue 切片，便于 Loki 内部 span 标注与 dskit tracing 互操作。

import (
	"fmt"

	"github.com/grafana/dskit/tracing"
	"go.opentelemetry.io/otel/attribute"
)

func KeyValuesToOTelAttributes(kvps ...any) []attribute.KeyValue {
	attrs := make([]attribute.KeyValue, 0, len(kvps)/2)
	for i := 0; i < len(kvps); i += 2 {
		if i+1 < len(kvps) {
			key, ok := kvps[i].(string)
			if !ok {
				key = fmt.Sprintf("not_string_key:%v", kvps[i])
			}
			attrs = append(attrs, tracing.KeyValueToOTelAttribute(key, kvps[i+1]))
		}
	}
	return attrs
}
// 非 string 键会格式化为 not_string_key:%v，value 仍经 tracing.KeyValueToOTelAttribute 类型推断。
