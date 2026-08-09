// Copyright 2020 Gin Core Team. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

//go:build nomsgpack

package binding

import "net/http"

// 常见数据格式的 Content-Type MIME 类型。
const (
	MIMEJSON              = "application/json"
	MIMEHTML              = "text/html"
	MIMEXML               = "application/xml"
	MIMEXML2              = "text/xml"
	MIMEPlain             = "text/plain"
	MIMEPOSTForm          = "application/x-www-form-urlencoded"
	MIMEMultipartPOSTForm = "multipart/form-data"
	MIMEPROTOBUF          = "application/x-protobuf"
	MIMEYAML              = "application/x-yaml"
	MIMEYAML2             = "application/yaml"
	MIMETOML              = "application/toml"
	MIMEBSON              = "application/bson"
)

// Binding 描述将请求数据（如 JSON 请求体、查询参数或表单 POST）绑定到结构体所需实现的接口。
type Binding interface {
	Name() string
	Bind(*http.Request, any) error
}

// BindingBody 在 Binding 基础上增加 BindBody 方法。
// BindBody 与 Bind 类似，但从提供的字节切片读取请求体，而非 req.Body。
type BindingBody interface {
	Binding
	BindBody([]byte, any) error
}

// BindingUri 在 Binding 基础上增加 BindUri 方法。
// BindUri 与 Bind 类似，但从路由 Params 读取数据。
type BindingUri interface {
	Name() string
	BindUri(map[string][]string, any) error
}

// StructValidator 是用作请求校验引擎所需实现的最小接口。
// Gin 默认使用 https://github.com/go-playground/validator/tree/v10.6.1 实现。
type StructValidator interface {
	// ValidateStruct 可接收任意类型，配置错误时也不应 panic。
	// 若接收类型不是 struct，应跳过校验并返回 nil。
	// 若接收类型为 struct 或 struct 指针，应执行校验。
	// 若 struct 无效或校验本身失败，应返回描述性错误；否则返回 nil。
	ValidateStruct(any) error

	// Engine 返回驱动 StructValidator 实现的底层校验引擎。
	Engine() any
}

// Validator 是默认校验器，实现 StructValidator 接口。
// 底层使用 https://github.com/go-playground/validator/tree/v10.6.1。
var Validator StructValidator = &defaultValidator{}

// 以下变量实现 Binding 接口，用于将请求数据绑定到结构体实例。
var (
	JSON          = jsonBinding{}
	XML           = xmlBinding{}
	Form          = formBinding{}
	Query         = queryBinding{}
	FormPost      = formPostBinding{}
	FormMultipart = formMultipartBinding{}
	ProtoBuf      = protobufBinding{}
	YAML          = yamlBinding{}
	Uri           = uriBinding{}
	Header        = headerBinding{}
	TOML          = tomlBinding{}
	Plain         = plainBinding{}
	BSON          BindingBody = bsonBinding{}
)

// Default 根据 HTTP 方法和 Content-Type 返回合适的 Binding 实例。
func Default(method, contentType string) Binding {
	if method == "GET" {
		return Form
	}

	switch contentType {
	case MIMEJSON:
		return JSON
	case MIMEXML, MIMEXML2:
		return XML
	case MIMEPROTOBUF:
		return ProtoBuf
	case MIMEYAML, MIMEYAML2:
		return YAML
	case MIMEMultipartPOSTForm:
		return FormMultipart
	case MIMETOML:
		return TOML
	case MIMEBSON:
		return BSON
	default: // case MIMEPOSTForm:
		return Form
	}
}

func validate(obj any) error {
	if Validator == nil {
		return nil
	}
	return Validator.ValidateStruct(obj)
}
