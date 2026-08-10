package frontend

// frontend HTTP 适配：将 JSON exceeds-limits 请求转换为 gRPC 调用并注入 tenant org ID。

import (
	"encoding/json"
	"net/http"

	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/user"

	"github.com/grafana/loki/v3/pkg/limits/proto"
	"github.com/grafana/loki/v3/pkg/util"
)

// httpExceedsLimitsRequest 是 ExceedsLimits 的 JSON 请求体 schema。
type httpExceedsLimitsRequest struct {
	Tenant  string                  `json:"tenant"`
	Streams []*proto.StreamMetadata `json:"streams"`
}

type httpExceedsLimitsResponse struct {
	Results []*proto.ExceedsLimitsResult `json:"results,omitempty"`
}

// ServeHTTP 解析 JSON、校验 tenant、注入 org ID 后委托 ExceedsLimits。
// ServeHTTP implements http.Handler.
func (f *Frontend) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var req httpExceedsLimitsRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "JSON is invalid or does not match expected schema", http.StatusBadRequest)
		return
	}

	if req.Tenant == "" {
		http.Error(w, "tenant is required", http.StatusBadRequest)
		return
	}

// 将 HTTP tenant 转为 gRPC metadata 中的 org ID，与内部 RPC 路径一致。
	ctx, err := user.InjectIntoGRPCRequest(user.InjectOrgID(r.Context(), req.Tenant))
	if err != nil {
		http.Error(w, "failed to inject org ID", http.StatusInternalServerError)
		return
	}

	resp, err := f.ExceedsLimits(ctx, &proto.ExceedsLimitsRequest{
		Tenant:  req.Tenant,
		Streams: req.Streams,
	})
	if err != nil {
		level.Error(f.logger).Log("msg", "failed to check if request exceeds limits", "err", err)
		http.Error(w, "an unexpected error occurred while checking if request exceeds limits", http.StatusInternalServerError)
		return
	}

	util.WriteJSONResponse(w, httpExceedsLimitsResponse{
		Results: resp.Results,
	})
}
// 该 handler 便于 curl/集成测试直接探测 frontend 限流逻辑，无需 gRPC 客户端。
