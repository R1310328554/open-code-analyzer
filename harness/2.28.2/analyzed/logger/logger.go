// Copyright 2019 Drone IO, Inc.
// Copyright 2016 The containerd Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// logger 包提供基于 context 的 logrus 日志器注入与提取，便于在 HTTP 请求链路中传递结构化日志。
package logger

import (
	"context"
	"net/http"

	"github.com/sirupsen/logrus"
)

// loggerKey 是 context 中存放 *logrus.Entry 的私有键类型。
type loggerKey struct{}

// L 是标准 logrus 日志器的便捷入口别名。
var L = logrus.NewEntry(logrus.StandardLogger())

// WithContext 将 logger 绑定到 context，可与 WithField(s) 配合在调用链中附加字段。
func WithContext(ctx context.Context, logger *logrus.Entry) context.Context {
	return context.WithValue(ctx, loggerKey{}, logger)
}

// FromContext 从 context 取出绑定的 logger；未绑定时返回默认 logger L。
func FromContext(ctx context.Context) *logrus.Entry {
	logger := ctx.Value(loggerKey{})
	if logger == nil {
		return L
	}
	return logger.(*logrus.Entry)
}

// FromRequest 从 HTTP 请求的 context 中提取 logger，等价于 FromContext(r.Context())。
func FromRequest(r *http.Request) *logrus.Entry {
	return FromContext(r.Context())
}
