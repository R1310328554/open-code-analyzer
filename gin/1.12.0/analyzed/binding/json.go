// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package binding

import (
	"bytes"
	"errors"
	"io"
	"net/http"

	"github.com/gin-gonic/gin/codec/json"
)

// EnableDecoderUseNumber 用于在 JSON Decoder 上调用 UseNumber。
// UseNumber 会将数字反序列化到 any 时为 Number 类型，而非 float64。
var EnableDecoderUseNumber = false

// EnableDecoderDisallowUnknownFields 用于在 JSON Decoder 上调用 DisallowUnknownFields。
// 当目标为 struct 且输入包含与任何非忽略、可导出字段不匹配的键时，Decoder 将返回错误。
var EnableDecoderDisallowUnknownFields = false

type jsonBinding struct{}

func (jsonBinding) Name() string {
	return "json"
}

func (jsonBinding) Bind(req *http.Request, obj any) error {
	if req == nil || req.Body == nil {
		return errors.New("invalid request")
	}
	return decodeJSON(req.Body, obj)
}

func (jsonBinding) BindBody(body []byte, obj any) error {
	return decodeJSON(bytes.NewReader(body), obj)
}

func decodeJSON(r io.Reader, obj any) error {
	decoder := json.API.NewDecoder(r)
	if EnableDecoderUseNumber {
		decoder.UseNumber()
	}
	if EnableDecoderDisallowUnknownFields {
		decoder.DisallowUnknownFields()
	}
	if err := decoder.Decode(obj); err != nil {
		return err
	}
	return validate(obj)
}
