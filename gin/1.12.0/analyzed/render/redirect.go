// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package render

import (
	"fmt"
	"net/http"
)

// Redirect 封装 HTTP 请求引用、重定向状态码与目标地址。
type Redirect struct {
	Code     int
	Request  *http.Request
	Location string
}

// Render（Redirect）将请求重定向到新地址并写入重定向响应。
func (r Redirect) Render(w http.ResponseWriter) error {
	if (r.Code < http.StatusMultipleChoices || r.Code > http.StatusPermanentRedirect) && r.Code != http.StatusCreated {
		panic(fmt.Sprintf("Cannot redirect with status code %d", r.Code))
	}
	http.Redirect(w, r.Request, r.Location, r.Code)
	return nil
}

// WriteContentType（Redirect）不写入任何 Content-Type。
func (r Redirect) WriteContentType(http.ResponseWriter) {}
