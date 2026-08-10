package frontend

// downstream_roundtripper 将 queryrange 请求转发至下游 HTTP 端点：Codec 编解码 HTTP，RoundTripper 实际发送请求并合并上下文中的原始请求头。

import (
	"context"
	"fmt"
	"net/http"
	"net/url"
	"path"

	"github.com/grafana/dskit/user"
	"go.opentelemetry.io/contrib/instrumentation/net/http/httptrace/otelhttptrace"

	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
	"github.com/grafana/loki/v3/pkg/util/httpreq"
)

// downstreamRoundTripper 持有下游 URL、传输层与 Codec，实现 queryrangebase.Handler。
// RoundTripper that forwards requests to downstream URL.
type downstreamRoundTripper struct {
	downstreamURL *url.URL
	transport     http.RoundTripper
	codec         queryrangebase.Codec
}

// NewDownstreamRoundTripper 解析下游地址并构造可直接 Do 的 Handler。
func NewDownstreamRoundTripper(downstreamURL string, transport http.RoundTripper, codec queryrangebase.Codec) (queryrangebase.Handler, error) {
	u, err := url.Parse(downstreamURL)
	if err != nil {
		return nil, err
	}

	return &downstreamRoundTripper{downstreamURL: u, transport: transport, codec: codec}, nil
}

// Do 将 queryrange 请求编码为 HTTP，注入租户 ID 与 trace，转发至下游并解码响应。
func (d downstreamRoundTripper) Do(ctx context.Context, req queryrangebase.Request) (queryrangebase.Response, error) {
	var r *http.Request

	r, err := d.codec.EncodeRequest(ctx, req)
	if err != nil {
		return nil, fmt.Errorf("connot convert request ot HTTP request: %w", err)
	}

	// Codec 重建请求时仅保留白名单头；此处从 context 恢复 PropagateAllHeaders 保存的完整头。
// Restore headers that were stored in context by PropagateAllHeadersMiddleware.
	// The codec encode cycle creates a new HTTP request with only a whitelist of headers,
	// so we need to restore the original headers from context.
	// Only add headers that weren't already set by the codec to avoid duplication.
	if ctxHeaders := httpreq.ExtractAllHeaders(ctx); ctxHeaders != nil {
		for k, values := range ctxHeaders {
			if r.Header.Get(k) == "" {
				for _, v := range values {
					r.Header.Add(k, v)
				}
			}
		}
	}

	if err := user.InjectOrgIDIntoHTTPRequest(ctx, r); err != nil {
		return nil, err
	}

	otelhttptrace.Inject(ctx, r)

	r.URL.Scheme = d.downstreamURL.Scheme
	r.URL.Host = d.downstreamURL.Host
	r.URL.Path = path.Join(d.downstreamURL.Path, r.URL.Path)
	r.Host = ""

	httpResp, err := d.transport.RoundTrip(r)
	if err != nil {
		return nil, err
	}

	resp, err := d.codec.DecodeResponse(ctx, httpResp, req)
	if err != nil {
		return nil, fmt.Errorf("cannot convert HTTP response to response: %w", err)
	}

	return resp, nil
}
// 下游 URL 的 scheme/host/path 会覆盖编码请求中的对应字段，Host 置空以使用 URL.Host。
