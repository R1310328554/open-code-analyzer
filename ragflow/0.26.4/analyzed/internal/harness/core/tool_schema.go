package core

// tool_schema.go — 反射生成 ToolInfo/JSON Schema 与 ReflectTool 便捷构造。


import (
	"context"
	"encoding/json"
	"fmt"
	"reflect"
	"strings"

	"ragflow/internal/harness/core/schema"
)

// ---- 基于反射的 ToolInfo 生成 ----

// ToolSchemaOptions 配置 schema 生成选项。
type ToolSchemaOptions struct {
	DescriptionTag string // struct tag to use for field descriptions (default: "description")
}

// DefaultToolSchemaOptions 返回默认 schema 选项（description 标签）。
func DefaultToolSchemaOptions() *ToolSchemaOptions {
	return &ToolSchemaOptions{DescriptionTag: "description"}
}

// GenerateToolInfo 从参数结构体 T 反射生成 ToolInfo 与 input_schema。
// using reflection. The function must have the signature:
//
//	func(ctx context.Context, args *T) (string, error)
//
// where T is a struct with json tags.
func GenerateToolInfo[T any](name string, desc string, fn any, opts ...*ToolSchemaOptions) (*schema.ToolInfo, error) {
	opt := DefaultToolSchemaOptions()
	if len(opts) > 0 && opts[0] != nil {
		opt = opts[0]
	}

	// Build param schema from T
	var param T
	t := reflect.TypeOf(param)
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}

	inputSchema, err := structToJSONSchema(t, opt.DescriptionTag)
	if err != nil {
		return nil, fmt.Errorf("generate schema for %s: %w", name, err)
	}

	return &schema.ToolInfo{
		Name:        name,
		Description: desc,
		InputSchema: inputSchema,
	}, nil
}

// structToJSONSchema 将 Go 结构体转为 JSON Schema 对象。
func structToJSONSchema(t reflect.Type, descTag string) (map[string]interface{}, error) {
	if t.Kind() != reflect.Struct {
		// For non-struct types, just use a simple schema
		return map[string]interface{}{
			"type": "object",
			"properties": map[string]interface{}{
				"value": map[string]interface{}{"type": jsonTypeName(t)},
			},
		}, nil
	}

	schema := map[string]interface{}{
		"type":       "object",
		"properties": make(map[string]interface{}),
	}

	props := schema["properties"].(map[string]interface{})
	var requiredFields []string

	for i := 0; i < t.NumField(); i++ {
		field := t.Field(i)
		if !field.IsExported() {
			continue
		}

		propName := fieldNameFromTag(field)
		if propName == "" || propName == "-" {
			continue
		}

		propSchema := fieldToJSONSchema(field, descTag)
		if propSchema != nil {
			// Collect required fields into top-level array per JSON Schema spec
			if field.Tag.Get("required") == "true" {
				requiredFields = append(requiredFields, propName)
			}
			delete(propSchema, "required") // remove from per-property position
			props[propName] = propSchema
		}
	}

	if len(requiredFields) > 0 {
		schema["required"] = requiredFields
	}

	return schema, nil
}

// fieldNameFromTag 从 struct tag 提取 JSON 字段名。
func fieldNameFromTag(field reflect.StructField) string {
	if tag := field.Tag.Get("json"); tag != "" {
		return strings.Split(tag, ",")[0]
	}
	return strings.ToLower(field.Name)
}

// fieldToJSONSchema 为单个字段生成 JSON Schema 属性。
func fieldToJSONSchema(field reflect.StructField, descTag string) map[string]interface{} {
	s := map[string]interface{}{
		"type": jsonTypeName(field.Type),
	}

	if desc := field.Tag.Get(descTag); desc != "" {
		s["description"] = desc
	}

	if enum := field.Tag.Get("enum"); enum != "" {
		s["enum"] = strings.Split(enum, ",")
	}

	if field.Tag.Get("required") == "true" {
		// Required is handled at the parent schema level (top-level array).
		// This field tag is read in structToJSONSchema.
	}

	// Handle nested structs
	if field.Type.Kind() == reflect.Struct {
		nested, err := structToJSONSchema(field.Type, descTag)
		if err == nil {
			return nested
		}
	}

	// Handle pointer or slice element type
	elemType := field.Type
	if elemType.Kind() == reflect.Ptr || elemType.Kind() == reflect.Slice {
		elemType = elemType.Elem()
		if elemType.Kind() == reflect.Struct {
			nested, err := structToJSONSchema(elemType, descTag)
			if err == nil {
				return nested
			}
		}
		s["type"] = jsonTypeName(elemType)
		if field.Type.Kind() == reflect.Slice {
			s["type"] = "array"
			s["items"] = map[string]interface{}{"type": jsonTypeName(elemType)}
		}
	}

	return s
}

// jsonTypeName 将 Go 类型映射为 JSON Schema 类型名。
func jsonTypeName(t reflect.Type) string {
	switch t.Kind() {
	case reflect.String:
		return "string"
	case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64,
		reflect.Uint, reflect.Uint8, reflect.Uint16, reflect.Uint32, reflect.Uint64:
		return "integer"
	case reflect.Float32, reflect.Float64:
		return "number"
	case reflect.Bool:
		return "boolean"
	case reflect.Slice, reflect.Array:
		return "array"
	case reflect.Map:
		return "object"
	default:
		return "string"
	}
}

// ---- ReflectTool：从函数反射创建 Tool ----

// ReflectTool 自动反射参数类型并生成 schema 创建 Tool。
// the ToolInfo schema via reflection. The function must have the signature:
//
//	func(ctx context.Context, args *T) (string, error)
//
// Example:
//
//	type WeatherArgs struct {
//	    City string `json:"city" description:"The city name"`
//	}
//	tool := ReflectTool("get_weather", "Get current weather", myFunc)
func ReflectTool[T any](name, desc string, fn func(context.Context, *T) (string, error)) (*ReflectToolImpl[T], error) {
	info, err := GenerateToolInfo[T](name, desc, fn)
	if err != nil {
		return nil, err
	}
	return &ReflectToolImpl[T]{
		name: name,
		desc: desc,
		fn:   fn,
		info: info,
	}, nil
}

// ReflectToolImpl 由反射生成 schema 的函数式 Tool 实现。
type ReflectToolImpl[T any] struct {
	name string
	desc string
	fn   func(context.Context, *T) (string, error)
	info *schema.ToolInfo
}

func (t *ReflectToolImpl[T]) Name() string               { return t.name }
func (t *ReflectToolImpl[T]) Description() string        { return t.desc }
func (t *ReflectToolImpl[T]) ToolInfo() *schema.ToolInfo { return t.info }

func (t *ReflectToolImpl[T]) Invoke(ctx context.Context, argsJSON string, opts ...ToolOption) (string, error) {
	var args T
	if err := json.Unmarshal([]byte(argsJSON), &args); err != nil {
		return "", fmt.Errorf("unmarshal args for %s: %w", t.name, err)
	}
	return t.fn(ctx, &args)
}

func (t *ReflectToolImpl[T]) Stream(ctx context.Context, argsJSON string, opts ...ToolOption) (*schema.StreamReader[string], error) {
	result, err := t.Invoke(ctx, argsJSON, opts...)
	if err != nil {
		return nil, err
	}
	return schema.StreamReaderFromArray([]string{result}), nil
}

// MustReflectTool 同 ReflectTool，失败时 panic。
func MustReflectTool[T any](name, desc string, fn func(context.Context, *T) (string, error)) *ReflectToolImpl[T] {
	t, err := ReflectTool(name, desc, fn)
	if err != nil {
		panic(fmt.Sprintf("MustReflectTool(%s): %v", name, err))
	}
	return t
}

// ---- 迁移兼容的便捷构造 ----

// InferTool 以 T 的类型名作为工具名反射创建 Tool。
// automatically generate the JSON input schema. The function must have
// signature: func(ctx context.Context, args *T) (string, error).
func InferTool[T any](ctx context.Context, fn func(context.Context, *T) (string, error)) (*ReflectToolImpl[T], error) {
	name := reflect.TypeOf((*T)(nil)).Elem().Name()
	return ReflectTool[T](name, "", fn)
}

// InferToolWithName 显式指定名称与描述，schema 仍由反射生成。
// using reflection for the input schema.
func InferToolWithName[T any](name, desc string, fn func(context.Context, *T) (string, error)) (*ReflectToolImpl[T], error) {
	return ReflectTool[T](name, desc, fn)
}

// NewTool 使用显式 ToolInfo 创建 Tool，T 为参数反序列化类型。
// The generic parameter T is the struct type for argument unmarshalling.
func NewTool[T any](info *schema.ToolInfo, fn func(context.Context, *T) (string, error)) Tool {
	return &toolWithInfo[T]{
		info: info,
		fn:   fn,
	}
}

// toolWithInfo 基于显式 ToolInfo 的简单 Tool 实现。
type toolWithInfo[T any] struct {
	info *schema.ToolInfo
	fn   func(context.Context, *T) (string, error)
}

func (t *toolWithInfo[T]) Name() string               { return t.info.Name }
func (t *toolWithInfo[T]) Description() string        { return t.info.Description }
func (t *toolWithInfo[T]) ToolInfo() *schema.ToolInfo { return t.info }

func (t *toolWithInfo[T]) Invoke(ctx context.Context, argsJSON string, opts ...ToolOption) (string, error) {
	var args T
	if err := json.Unmarshal([]byte(argsJSON), &args); err != nil {
		return "", fmt.Errorf("unmarshal args for %s: %w", t.info.Name, err)
	}
	return t.fn(ctx, &args)
}

func (t *toolWithInfo[T]) Stream(ctx context.Context, argsJSON string, opts ...ToolOption) (*schema.StreamReader[string], error) {
	result, err := t.Invoke(ctx, argsJSON, opts...)
	if err != nil {
		return nil, err
	}
	return schema.StreamReaderFromArray([]string{result}), nil
}

// GoStructToToolInfo 将结构体类型 T 转为 ToolInfo（供 NewTool 或手动绑定）。
// for use with NewTool or manual binding.
func GoStructToToolInfo[T any](name, desc string) (*schema.ToolInfo, error) {
	return GenerateToolInfo[T](name, desc, nil)
}
