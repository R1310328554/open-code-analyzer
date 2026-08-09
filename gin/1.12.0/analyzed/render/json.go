// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package render

import (
	"bytes"
	"fmt"
	"html/template"
	"net/http"
	"unicode"

	"github.com/gin-gonic/gin/codec/json"
	"github.com/gin-gonic/gin/internal/bytesconv"
)

// JSON 包含给定的接口对象。
type JSON struct {
	Data any
}

// IndentedJSON 包含给定的接口对象。
type IndentedJSON struct {
	Data any
}

// SecureJSON 包含给定的接口对象及其前缀。
type SecureJSON struct {
	Prefix string
	Data   any
}

// JsonpJSON 包含给定的接口对象及其回调。
type JsonpJSON struct {
	Callback string
	Data     any
}

// AsciiJSON 包含给定的接口对象。
type AsciiJSON struct {
	Data any
}

// PureJSON 包含给定的接口对象。
type PureJSON struct {
	Data any
}

var (
	jsonContentType      = []string{"application/json; charset=utf-8"}
	jsonpContentType     = []string{"application/javascript; charset=utf-8"}
	jsonASCIIContentType = []string{"application/json"}
)

// 渲染 (JSON) 使用自定义 ContentType 写入数据。
func (r JSON) Render(w http.ResponseWriter) error {
	return WriteJSON(w, r.Data)
}

// WriteContentType (JSON) 写入 JSON ContentType。
func (r JSON) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, jsonContentType)
}

// WriteJSON 编组给定的接口对象并使用自定义 ContentType 写入它。
func WriteJSON(w http.ResponseWriter, obj any) error {
	writeContentType(w, jsonContentType)
	jsonBytes, err := json.API.Marshal(obj)
	if err != nil {
		return err
	}
	_, err = w.Write(jsonBytes)
	return err
}

// Render (IndentedJSON) 封送给定的接口对象并使用自定义 ContentType 写入它。
func (r IndentedJSON) Render(w http.ResponseWriter) error {
	r.WriteContentType(w)
	jsonBytes, err := json.API.MarshalIndent(r.Data, "", "    ")
	if err != nil {
		return err
	}
	_, err = w.Write(jsonBytes)
	return err
}

// WriteContentType (IndentedJSON) 写入 JSON ContentType。
func (r IndentedJSON) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, jsonContentType)
}

// 渲染 (SecureJSON) 封送给定的接口对象并使用自定义 ContentType 写入它。
func (r SecureJSON) Render(w http.ResponseWriter) error {
	r.WriteContentType(w)
	jsonBytes, err := json.API.Marshal(r.Data)
	if err != nil {
		return err
	}
	// 如果 jsonBytes 是数组值
	if bytes.HasPrefix(jsonBytes, bytesconv.StringToBytes("[")) && bytes.HasSuffix(jsonBytes,
		bytesconv.StringToBytes("]")) {
		if _, err = w.Write(bytesconv.StringToBytes(r.Prefix)); err != nil {
			return err
		}
	}
	_, err = w.Write(jsonBytes)
	return err
}

// WriteContentType (SecureJSON) 写入 JSON ContentType。
func (r SecureJSON) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, jsonContentType)
}

// 渲染 (JsonpJSON) 封送给定的接口对象，并使用自定义 ContentType 写入它及其回调。
func (r JsonpJSON) Render(w http.ResponseWriter) (err error) {
	r.WriteContentType(w)
	ret, err := json.API.Marshal(r.Data)
	if err != nil {
		return err
	}

	if r.Callback == "" {
		_, err = w.Write(ret)
		return err
	}

	callback := template.JSEscapeString(r.Callback)
	if _, err = w.Write(bytesconv.StringToBytes(callback)); err != nil {
		return err
	}

	if _, err = w.Write(bytesconv.StringToBytes("(")); err != nil {
		return err
	}

	if _, err = w.Write(ret); err != nil {
		return err
	}

	if _, err = w.Write(bytesconv.StringToBytes(");")); err != nil {
		return err
	}

	return nil
}

// WriteContentType (JsonpJSON) 写入 Javascript ContentType。
func (r JsonpJSON) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, jsonpContentType)
}

// 渲染 (AsciiJSON) 封送给定的接口对象并使用自定义 ContentType 写入它。
func (r AsciiJSON) Render(w http.ResponseWriter) error {
	r.WriteContentType(w)
	ret, err := json.API.Marshal(r.Data)
	if err != nil {
		return err
	}

	var buffer bytes.Buffer
	escapeBuf := make([]byte, 0, 6) // Preallocate 6 bytes for Unicode escape sequences

	for _, r := range bytesconv.BytesToString(ret) {
		if r > unicode.MaxASCII {
			escapeBuf = fmt.Appendf(escapeBuf[:0], "\\u%04x", r) // Reuse escapeBuf
			buffer.Write(escapeBuf)
		} else {
			buffer.WriteByte(byte(r))
		}
	}

	_, err = w.Write(buffer.Bytes())
	return err
}

// WriteContentType (AsciiJSON) 写入 JSON ContentType。
func (r AsciiJSON) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, jsonASCIIContentType)
}

// Render (PureJSON) 编写自定义 ContentType 并对给定的接口对象进行编码。
func (r PureJSON) Render(w http.ResponseWriter) error {
	r.WriteContentType(w)
	encoder := json.API.NewEncoder(w)
	encoder.SetEscapeHTML(false)
	return encoder.Encode(r.Data)
}

// WriteContentType (PureJSON) 写入自定义 ContentType。
func (r PureJSON) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, jsonContentType)
}
