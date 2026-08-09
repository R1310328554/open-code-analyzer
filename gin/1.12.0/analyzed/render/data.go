// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package render

import (
	"net/http"
	"strconv"
)

// 数据包含ContentType和字节数据。
type Data struct {
	ContentType string
	Data        []byte
}

// Render (Data) 使用自定义 ContentType 写入数据。
func (r Data) Render(w http.ResponseWriter) (err error) {
	r.WriteContentType(w)
	if len(r.Data) > 0 {
		w.Header().Set("Content-Length", strconv.Itoa(len(r.Data)))
	}
	_, err = w.Write(r.Data)
	return
}

// WriteContentType (Data) 写入自定义ContentType。
func (r Data) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, []string{r.ContentType})
}
