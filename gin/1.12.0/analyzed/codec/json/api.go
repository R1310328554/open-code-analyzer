// Copyright 2025 Gin Core Team. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package json

import "io"

// API 当前使用的 JSON 编解码器。
var API Core

// Core 定义 JSON 编解码器的 API。
type Core interface {
	Marshal(v any) ([]byte, error)
	Unmarshal(data []byte, v any) error
	MarshalIndent(v any, prefix, indent string) ([]byte, error)
	NewEncoder(writer io.Writer) Encoder
	NewDecoder(reader io.Reader) Decoder
}

// Encoder 将 JSON 值写入输出流的接口。
type Encoder interface {
	// SetEscapeHTML 指定是否转义 JSON 引号字符串中的 HTML 特殊字符。
	// 默认会转义 &、<、> 为 \u0026、\u003c、\u003e，
	// 以避免将 JSON 嵌入 HTML 时可能出现的安全问题。
	//
	// 在非 HTML 场景下若转义影响可读性，可调用 SetEscapeHTML(false) 关闭。
	SetEscapeHTML(on bool)

	// Encode 将 v 的 JSON 编码写入流，并在末尾追加换行符。
	//
	// Go 值到 JSON 的转换细节参见 Marshal 文档。
	Encode(v any) error
}

// Decoder 从输入流读取并解码 JSON 值的接口。
type Decoder interface {
	// UseNumber 使 Decoder 将数字反序列化到 any 时为 Number 类型，而非 float64。
	UseNumber()

	// DisallowUnknownFields 使 Decoder 在目标为 struct 且输入包含
	// 与任何非忽略、可导出字段不匹配的键时返回错误。
	DisallowUnknownFields()

	// Decode 从输入读取下一个 JSON 编码值并存入 v 指向的变量。
	//
	// JSON 到 Go 值的转换细节参见 Unmarshal 文档。
	Decode(v any) error
}
