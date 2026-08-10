package loki

// format_query_handler 暴露 /loki/api/v1/format_query：解析 LogQL 并以 Prettify 格式化，供 UI 或 CLI 校验与美化查询表达式。

import (
	"encoding/json"
	"net/http"

	"github.com/grafana/loki/v3/pkg/logql/syntax"
	"github.com/grafana/loki/v3/pkg/util/server"
)

func formatQueryHandler() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			statusCode = http.StatusOK
			status     = "success"
			formatted  string
			errStr     string
		)

		expr, err := syntax.ParseExpr(r.FormValue("query"))
		if err != nil {
			statusCode = http.StatusBadRequest
			status = "invalid-query"
			errStr = err.Error()
		}

		if err == nil {
			formatted = syntax.Prettify(expr)
		}

		resp := FormatQueryResponse{
			Status: status,
			Data:   formatted,
			Err:    errStr,
		}

		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		w.WriteHeader(statusCode)

		if err := json.NewEncoder(w).Encode(resp); err != nil {
			server.WriteError(err, w)
		}

	}
}

// FormatQueryResponse 统一 status/data/error 三字段，与 Loki API 错误风格一致。
type FormatQueryResponse struct {
	Status string `json:"status"`
	Data   string `json:"data,omitempty"`
	Err    string `json:"error,omitempty"`
}
// 解析失败时 HTTP 400 且 status 为 invalid-query；成功时 data 为格式化后的 LogQL 字符串。
