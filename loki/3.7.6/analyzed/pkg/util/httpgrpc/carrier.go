package httpgrpc

// httpgrpc 包实现 OpenTelemetry TextMapCarrier，在 gRPC HTTP 请求头与 trace context 间传递。

import (
	"context"

	weaveworks_httpgrpc "github.com/grafana/dskit/httpgrpc"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/propagation"

	"github.com/grafana/loki/v3/pkg/querier/queryrange"
)

// Request 抽象 QueryRequest 与 HttpRequest，ExtractSpanFromRequest 按类型选择传播载体。
type Request interface {
	GetQueryRequest() *queryrange.QueryRequest
	GetHttpRequest() *weaveworks_httpgrpc.HTTPRequest
}

// HeadersCarrier 包装 httpgrpc.HTTPRequest，实现 propagation.TextMapCarrier 读写 trace 头。
// Used to transfer trace information from/to HTTP request.
type HeadersCarrier weaveworks_httpgrpc.HTTPRequest

func (c *HeadersCarrier) Get(key string) string {
	// Check if the key exists in the headers
	for _, h := range c.Headers {
		if h.Key == key {
			// Return the first value for the key
			if len(h.Values) > 0 {
				return h.Values[0]
			}
			break
		}
	}
	return ""
}

func (c *HeadersCarrier) Keys() []string {
	// Collect all unique keys from the headers
	keys := make([]string, 0, len(c.Headers))
	for _, h := range c.Headers {
		if h.Key != "" {
			keys = append(keys, h.Key)
		}
	}
	return keys
}

// Set 向 Headers 追加新 Header 条目，供 Inject 阶段写入 traceparent 等字段。
func (c *HeadersCarrier) Set(key, val string) {
	c.Headers = append(c.Headers, &weaveworks_httpgrpc.Header{
		Key:    key,
		Values: []string{val},
	})
}

func (c *HeadersCarrier) ForeachKey(handler func(key, val string) error) error {
	for _, h := range c.Headers {
		for _, v := range h.Values {
			if err := handler(h.Key, v); err != nil {
				return err
			}
		}
	}
	return nil
}

// ExtractSpanFromHTTPRequest 用全局 TextMapPropagator 从 HTTP 头提取父 span 上下文。
func ExtractSpanFromHTTPRequest(ctx context.Context, req *weaveworks_httpgrpc.HTTPRequest) context.Context {
	return otel.GetTextMapPropagator().Extract(ctx, (*HeadersCarrier)(req))
}

func ExtractSpanFromQueryRequest(ctx context.Context, req *queryrange.QueryRequest) context.Context {
	return otel.GetTextMapPropagator().Extract(ctx, propagation.MapCarrier(req.Metadata))
}

func ExtractSpanFromRequest(ctx context.Context, req Request) context.Context {
	if r := req.GetQueryRequest(); r != nil {
		return ExtractSpanFromQueryRequest(ctx, r)
	}

	if r := req.GetHttpRequest(); r != nil {
		return ExtractSpanFromHTTPRequest(ctx, r)
	}

	return ctx
}
// ExtractSpanFromRequest 优先 QueryRequest，否则 HttpRequest，均无则原样返回 ctx。
