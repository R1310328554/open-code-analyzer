// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package rpc

// serverError 封装 RPC 服务器返回的非 2xx HTTP 响应错误。
type serverError struct {
	Status  int
	Message string
}

// Error 返回服务器响应体中的错误消息文本。
func (s *serverError) Error() string {
	return s.Message
}
