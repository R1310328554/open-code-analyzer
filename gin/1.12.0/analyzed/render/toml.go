// Copyright 2022 Gin Core Team. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package render

import (
	"net/http"

	"github.com/pelletier/go-toml/v2"
)

// TOML 封装待序列化的任意对象。
type TOML struct {
	Data any
}

var tomlContentType = []string{"application/toml; charset=utf-8"}

// Render（TOML）将给定对象序列化为 TOML 并以自定义 Content-Type 写入响应。
func (r TOML) Render(w http.ResponseWriter) error {
	r.WriteContentType(w)

	bytes, err := toml.Marshal(r.Data)
	if err != nil {
		return err
	}

	_, err = w.Write(bytes)
	return err
}

// WriteContentType（TOML）写入 TOML 的 Content-Type。
func (r TOML) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, tomlContentType)
}
