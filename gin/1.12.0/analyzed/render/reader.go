// Copyright 2018 Gin Core Team. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package render

import (
	"io"
	"net/http"
	"strconv"
)

// Reader 封装 IO 读取器及其长度，以及自定义 Content-Type 与其他响应头。
type Reader struct {
	ContentType   string
	ContentLength int64
	Reader        io.Reader
	Headers       map[string]string
}

// Render（Reader）以自定义 Content-Type 和响应头写入数据。
func (r Reader) Render(w http.ResponseWriter) (err error) {
	r.WriteContentType(w)
	if r.ContentLength >= 0 {
		if r.Headers == nil {
			r.Headers = map[string]string{}
		}
		r.Headers["Content-Length"] = strconv.FormatInt(r.ContentLength, 10)
	}
	r.writeHeaders(w)
	_, err = io.Copy(w, r.Reader)
	return
}

// WriteContentType（Reader）写入自定义 Content-Type。
func (r Reader) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, []string{r.ContentType})
}

// writeHeaders 将 r.Headers 中的响应头写入 HTTP 响应。
func (r Reader) writeHeaders(w http.ResponseWriter) {
	header := w.Header()
	for k, v := range r.Headers {
		if header.Get(k) == "" {
			header.Set(k, v)
		}
	}
}
