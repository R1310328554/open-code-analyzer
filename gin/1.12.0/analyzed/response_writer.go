// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"bufio"
	"errors"
	"io"
	"net"
	"net/http"
)

const (
	noWritten     = -1
	defaultStatus = http.StatusOK
)

var errHijackAlreadyWritten = errors.New("gin: response body already written")

// ResponseWriter 扩展 http.ResponseWriter，提供 gin 所需的额外能力。
type ResponseWriter interface {
	http.ResponseWriter
	http.Hijacker
	http.Flusher
	http.CloseNotifier

	// Status 返回当前请求的 HTTP 响应状态码。
	Status() int

	// Size 返回已写入响应体的字节数，参见 Written()。
	Size() int

	// WriteString 将字符串写入响应体。
	WriteString(string) (int, error)

	// Written 表示响应体是否已写入。
	Written() bool

	// WriteHeaderNow 强制立即写入 HTTP 响应头（状态码与头部字段）。
	WriteHeaderNow()

	// Pusher 返回用于服务端推送的 http.Pusher。
	Pusher() http.Pusher
}

type responseWriter struct {
	http.ResponseWriter
	size   int
	status int
}

var _ ResponseWriter = (*responseWriter)(nil)

func (w *responseWriter) Unwrap() http.ResponseWriter {
	return w.ResponseWriter
}

func (w *responseWriter) reset(writer http.ResponseWriter) {
	w.ResponseWriter = writer
	w.size = noWritten
	w.status = defaultStatus
}

func (w *responseWriter) WriteHeader(code int) {
	if code > 0 && w.status != code {
		if w.Written() {
			debugPrint("[WARNING] Headers were already written. Wanted to override status code %d with %d", w.status, code)
			return
		}
		w.status = code
	}
}

func (w *responseWriter) WriteHeaderNow() {
	if !w.Written() {
		w.size = 0
		w.ResponseWriter.WriteHeader(w.status)
	}
}

func (w *responseWriter) Write(data []byte) (n int, err error) {
	w.WriteHeaderNow()
	n, err = w.ResponseWriter.Write(data)
	w.size += n
	return
}

func (w *responseWriter) WriteString(s string) (n int, err error) {
	w.WriteHeaderNow()
	n, err = io.WriteString(w.ResponseWriter, s)
	w.size += n
	return
}

func (w *responseWriter) Status() int {
	return w.status
}

func (w *responseWriter) Size() int {
	return w.size
}

func (w *responseWriter) Written() bool {
	return w.size != noWritten
}

// Hijack 实现 http.Hijacker 接口。
func (w *responseWriter) Hijack() (net.Conn, *bufio.ReadWriter, error) {
	// 允许在尚未写入任何数据（size == -1）或仅写入响应头（size == 0）时 hijack，
	// 但在已写入响应体数据（size > 0）后禁止 hijack。
	// 为兼容 WebSocket 库（如 github.com/coder/websocket）。
	if w.size > 0 {
		return nil, nil, errHijackAlreadyWritten
	}
	if w.size < 0 {
		w.size = 0
	}
	return w.ResponseWriter.(http.Hijacker).Hijack()
}

// CloseNotify 实现 http.CloseNotifier 接口。
func (w *responseWriter) CloseNotify() <-chan bool {
	return w.ResponseWriter.(http.CloseNotifier).CloseNotify()
}

// Flush 实现 http.Flusher 接口。
func (w *responseWriter) Flush() {
	w.WriteHeaderNow()
	if f, ok := w.ResponseWriter.(http.Flusher); ok {
		f.Flush()
	}
}

func (w *responseWriter) Pusher() (pusher http.Pusher) {
	if pusher, ok := w.ResponseWriter.(http.Pusher); ok {
		return pusher
	}
	return nil
}
