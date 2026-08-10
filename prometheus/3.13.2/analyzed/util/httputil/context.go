// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 为 PromQL 查询日志注入 HTTP 上下文：路径参数与 clientIP/method 等请求元数据。

package httputil

import (
	"context"
	"net"
	"net/http"

	"github.com/prometheus/prometheus/promql"
)

type pathParam struct{}

// ContextWithPath 将请求路径存入 context，供后续查询日志使用。
// ContextWithPath returns a new context with the given path to be used later
// when logging the query.
func ContextWithPath(ctx context.Context, path string) context.Context {
	return context.WithValue(ctx, pathParam{}, path)
}

// ContextFromRequest 从 Request 提取 clientIP、method 与路径，写入 promql origin context。
// ContextFromRequest returns a new context with identifiers of
// the request to be used later when logging the query.
func ContextFromRequest(ctx context.Context, r *http.Request) context.Context {
	var ip string
	if r.RemoteAddr != "" {
		// r.RemoteAddr has no defined format, so don't return error if we cannot split it into IP:Port.
		ip, _, _ = net.SplitHostPort(r.RemoteAddr)
	}
	var path string
	if v := ctx.Value(pathParam{}); v != nil {
		path = v.(string)
	}
	return promql.NewOriginContext(ctx, map[string]any{
		"httpRequest": map[string]string{
			"clientIP": ip,
			"method":   r.Method,
			"path":     path,
		},
	})
}
