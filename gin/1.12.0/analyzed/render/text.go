// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package render

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin/internal/bytesconv"
)

// String 封装格式化字符串及其参数列表。
type String struct {
	Format string
	Data   []any
}

var plainContentType = []string{"text/plain; charset=utf-8"}

// Render（String）以自定义 Content-Type 写入数据。
func (r String) Render(w http.ResponseWriter) error {
	return WriteString(w, r.Format, r.Data)
}

// WriteContentType（String）写入纯文本 Content-Type。
func (r String) WriteContentType(w http.ResponseWriter) {
	writeContentType(w, plainContentType)
}

// WriteString 按格式写入数据，并设置纯文本 Content-Type。
func WriteString(w http.ResponseWriter, format string, data []any) (err error) {
	writeContentType(w, plainContentType)
	if len(data) > 0 {
		_, err = fmt.Fprintf(w, format, data...)
		return
	}
	_, err = w.Write(bytesconv.StringToBytes(format))
	return
}
