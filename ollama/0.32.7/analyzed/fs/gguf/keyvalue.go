// GGUF 键值对：元数据 Value 的类型安全读取与切片转换。
package gguf

import (
	"reflect"
	"slices"
)

// KeyValue 表示 GGUF 元数据中的一对键与强类型值。
type KeyValue struct {
	Key string
	Value
}

// Valid 判断键非空且底层值已设置。
func (kv KeyValue) Valid() bool {
	return kv.Key != "" && kv.Value.value != nil
}

// Value 包装任意 GGUF 元数据值，提供类型化访问器。
type Value struct {
	value any
}

// value 在 reflect.Kind 匹配时将 Value 转为单个标量 T。
func value[T any](v Value, kinds ...reflect.Kind) (t T) {
	vv := reflect.ValueOf(v.value)
	if slices.Contains(kinds, vv.Kind()) {
		t = vv.Convert(reflect.TypeOf(t)).Interface().(T)
	}
	return
}

// values 将 Value 转为元素类型匹配的切片。
func values[T any](v Value, kinds ...reflect.Kind) (ts []T) {
	switch vv := reflect.ValueOf(v.value); vv.Kind() {
	case reflect.Slice:
		if slices.Contains(kinds, vv.Type().Elem().Kind()) {
			ts = make([]T, vv.Len())
			for i := range vv.Len() {
				ts[i] = vv.Index(i).Convert(reflect.TypeOf(ts[i])).Interface().(T)
			}
		}
	}
	return
}

// Int 将 Value 转为有符号整数；类型不符时返回 0。
// Int returns Value as a signed integer. If it is not a signed integer, it returns 0.
func (v Value) Int() int64 {
	return value[int64](v, reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64)
}

// Ints 将 Value 转为有符号整数切片；类型不符时返回 nil。
// Ints returns Value as a signed integer slice. If it is not a signed integer slice, it returns nil.
func (v Value) Ints() (i64s []int64) {
	return values[int64](v, reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64)
}

// Uint 将 Value 转为无符号整数；类型不符时返回 0。
// Uint converts an unsigned integer value to uint64. If the value is not a unsigned integer, it returns 0.
func (v Value) Uint() uint64 {
	return value[uint64](v, reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64)
}

// Uints 将 Value 转为无符号整数切片；类型不符时返回 nil。
// Uints returns Value as a unsigned integer slice. If it is not a unsigned integer slice, it returns nil.
func (v Value) Uints() (u64s []uint64) {
	return values[uint64](v, reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64)
}

// Float 将 Value 转为浮点数；类型不符时返回 0。
// Float returns Value as a float. If it is not a float, it returns 0.
func (v Value) Float() float64 {
	return value[float64](v, reflect.Float32, reflect.Float64)
}

// Floats 将 Value 转为浮点切片；类型不符时返回 nil。
// Floats returns Value as a float slice. If it is not a float slice, it returns nil.
func (v Value) Floats() (f64s []float64) {
	return values[float64](v, reflect.Float32, reflect.Float64)
}

// Bool 将 Value 转为布尔值；类型不符时返回 false。
// Bool returns Value as a boolean. If it is not a boolean, it returns false.
func (v Value) Bool() bool {
	return value[bool](v, reflect.Bool)
}

// Bools 将 Value 转为布尔切片；类型不符时返回 nil。
// Bools returns Value as a boolean slice. If it is not a boolean slice, it returns nil.
func (v Value) Bools() (bools []bool) {
	return values[bool](v, reflect.Bool)
}

// String 将 Value 转为字符串；类型不符时返回空串。
// String returns Value as a string. If it is not a string, it returns an empty string.
func (v Value) String() string {
	return value[string](v, reflect.String)
}

// Strings 将 Value 转为字符串切片；类型不符时返回 nil。
// Strings returns Value as a string slice. If it is not a string slice, it returns nil.
func (v Value) Strings() (strings []string) {
	return values[string](v, reflect.String)
}
