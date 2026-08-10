package queryrange

// serialize 在 HTTP 边界解码请求、调用 Handler 链并以 Codec 编码响应，支持可选 Parquet 输出格式。

import (
	"net/http"

	"github.com/grafana/loki/v3/pkg/loghttp"
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
	"github.com/grafana/loki/v3/pkg/util/httpreq"
	serverutil "github.com/grafana/loki/v3/pkg/util/server"
)

// serializeRoundTripper 实现 http.RoundTripper，供 grpc-gateway 或 proxy 调用。
type serializeRoundTripper struct {
	codec          queryrangebase.Codec
	next           queryrangebase.Handler
	parquetSupport bool
}

func NewSerializeRoundTripper(next queryrangebase.Handler, codec queryrangebase.Codec, parquetSupport bool) http.RoundTripper {
	return &serializeRoundTripper{
		next:           next,
		codec:          codec,
		parquetSupport: parquetSupport,
	}
}

func (rt *serializeRoundTripper) RoundTrip(r *http.Request) (*http.Response, error) {
	ctx := r.Context()
	ctx, sp := tracer.Start(ctx, "serializeRoundTripper.do")
	defer sp.End()

	request, err := rt.codec.DecodeRequest(ctx, r, nil)
	if err != nil {
		return nil, err
	}

	response, err := rt.next.Do(ctx, request)
	if err != nil {
		return nil, err
	}

	if r.Header.Get("Accept") == ParquetType && !rt.parquetSupport {
		return nil, serverutil.UserError("support for Parquet encoded responses is disabled. Enable with -frontend.support-parquet-encoding=true")
	}

	return rt.codec.EncodeResponse(ctx, r, response)
}

// serializeHTTPHandler 为原生 HTTP handler，Parquet Accept 头走专用编码路径。
type serializeHTTPHandler struct {
	codec queryrangebase.Codec
	next  queryrangebase.Handler
}

func NewSerializeHTTPHandler(next queryrangebase.Handler, codec queryrangebase.Codec) http.Handler {
	return &serializeHTTPHandler{
		next:  next,
		codec: codec,
	}
}

func (rt *serializeHTTPHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()
	ctx, sp := tracer.Start(ctx, "serializeHTTPHandler.ServerHTTP")
	defer sp.End()

	request, err := rt.codec.DecodeRequest(ctx, r, nil)
	if err != nil {
		serverutil.WriteError(err, w)
		return
	}

	response, err := rt.next.Do(ctx, request)
	if err != nil {
		serverutil.WriteError(err, w)
		return
	}

	// HTTP handler 路径尚未完全统一到 Codec.EncodeResponse，Parquet 仍单独处理。
// TODO(karsten): use rt.codec.EncodeResponse(ctx, r, response) which is the central encoding logic instead.
	if r.Header.Get("Accept") == ParquetType {
		w.Header().Add("Content-Type", ParquetType)
		if err := encodeResponseParquetTo(ctx, response, w); err != nil {
			serverutil.WriteError(err, w)
		}
		return
	}
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	version := loghttp.GetVersion(r.RequestURI)
	encodingFlags := httpreq.ExtractEncodingFlags(r)
	if err := encodeResponseJSONTo(version, response, w, encodingFlags); err != nil {
		serverutil.WriteError(err, w)
	}
}
// DecodeRequest 与 EncodeResponse 由 queryrangebase.Codec 实现，保持与 gRPC 路径一致。
