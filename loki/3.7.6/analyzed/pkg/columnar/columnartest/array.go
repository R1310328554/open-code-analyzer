package columnartest

// columnartest 为 columnar 包提供测试辅助：
// 从 Go 原生值快速构造各 Kind 的 Array，供单元测试断言使用。

import (
	"testing"

	"github.com/stretchr/testify/require"

	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/memory"
)

// Array 按指定 Kind 从可变参数 values 构造列式数组，nil 表示空元素。
// Array returns an array value representing the given kind.
//
// Each value must either be nil (for a null value) or a Go type that can be
// directly converted to kind. See [Scalar] for conversion rules.
//
// If alloc is nil, a new allocator will be created for the returned array.
//
// If a value cannot be represented as the kind provided, Array fails t.
func Array(t testing.TB, kind columnar.Kind, alloc *memory.Allocator, values ...any) columnar.Array {
	t.Helper()

	if alloc == nil {
		alloc = memory.NewAllocator(nil)
	}

	switch kind {
	case columnar.KindNull:
		return arrayNull(t, alloc, values...)
	case columnar.KindBool:
		return arrayBool(t, alloc, values...)
	case columnar.KindInt64:
		return arrayNumber[int64](t, alloc, values...)
	case columnar.KindUint64:
		return arrayNumber[uint64](t, alloc, values...)
	case columnar.KindUTF8:
		return arrayUTF8(t, alloc, values...)
	default:
		require.FailNow(t, "unsupported kind", "kind %s is currently not supported", kind)
		panic("unreachable")
	}
}

// arrayNull 构建全 null 数组，要求每个 value 均为 nil。
func arrayNull(t testing.TB, alloc *memory.Allocator, values ...any) *columnar.Null {
	t.Helper()

	builder := columnar.NewNullBuilder(alloc)
	builder.Grow(len(values))

	for _, value := range values {
		require.Nil(t, value, "all values must be nil for null array")
		builder.AppendNull()
	}

	return builder.Build()
}

// arrayBool 构建布尔数组，支持 nil 与 bool 值。
func arrayBool(t testing.TB, alloc *memory.Allocator, values ...any) *columnar.Bool {
	t.Helper()

	builder := columnar.NewBoolBuilder(alloc)
	builder.Grow(len(values))

	for _, value := range values {
		if value == nil {
			builder.AppendNull()
			continue
		}

		require.IsType(t, false, value, "all values must be nil or bool for bool array")
		builder.AppendValue(value.(bool))
	}

	return builder.Build()
}

// arrayNumber 构建 int64/uint64 数值数组，接受 int 或 T 类型值。
func arrayNumber[T columnar.Numeric](t testing.TB, alloc *memory.Allocator, values ...any) *columnar.Number[T] {
	t.Helper()

	builder := columnar.NewNumberBuilder[T](alloc)
	builder.Grow(len(values))

	for _, value := range values {
		if value == nil {
			builder.AppendNull()
			continue
		}

		switch value := value.(type) {
		case int:
			builder.AppendValue(T(value))
		case T:
			builder.AppendValue(value)
		default:
			var zero T
			require.FailNow(t, "unexpected value type", "all values must be nil, %T, or int for %T array", zero, zero)
		}
	}

	return builder.Build()
}

// arrayUTF8 构建 UTF-8 字符串数组，接受 string 或 []byte。
func arrayUTF8(t testing.TB, alloc *memory.Allocator, values ...any) *columnar.UTF8 {
	t.Helper()

	builder := columnar.NewUTF8Builder(alloc)
	builder.Grow(len(values))

	for _, value := range values {
		if value == nil {
			builder.AppendNull()
			continue
		}

		switch value := value.(type) {
		case string:
			builder.AppendValue([]byte(value))
		case []byte:
			builder.AppendValue(value)
		default:
			require.FailNow(t, "unexpected value type", "all values must be nil, []byte, or string for UTF8 array, found %T", value)
		}
	}

	return builder.Build()
}
