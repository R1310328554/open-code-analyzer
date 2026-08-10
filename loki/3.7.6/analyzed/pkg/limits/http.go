package limits

// limits HTTP 调试端点：按 tenant 返回当前活跃流数量、按 policy 分组与估算 ingest 速率。

import (
	"net/http"

	"github.com/gorilla/mux"

	"github.com/grafana/loki/v3/pkg/util"
)

type httpTenantLimitsResponse struct {
	Tenant          string            `json:"tenant"`
	StreamsTotal    uint64            `json:"streams_total"`
	StreamsByPolicy map[string]uint64 `json:"streams_by_policy"`
	Rate            float64           `json:"rate"`
}

// ServeHTTP 从 mux 路径变量读取 tenant，汇总 usageStore 中活跃流与 rate 桶。
// ServeHTTP implements the http.Handler interface.
// It returns the current stream counts and status per tenant as a JSON response.
func (s *Service) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	tenant := mux.Vars(r)["tenant"]
	if tenant == "" {
		http.Error(w, "invalid tenant", http.StatusBadRequest)
		return
	}
	var streams, sumBuckets uint64
	streamsByPolicy := make(map[string]uint64)
// 遍历租户活跃流，累加各 rate bucket 的 size 以估算字节 ingest 速率。
	for _, stream := range s.usage.TenantActiveStreams(tenant) {
		streams++
		for _, bucket := range stream.rateBuckets {
			sumBuckets += bucket.size
		}
		streamsByPolicy[stream.policy]++
	}
	rate := float64(sumBuckets) / s.cfg.ActiveWindow.Seconds()
	util.WriteJSONResponse(w, httpTenantLimitsResponse{
		Tenant:          tenant,
		StreamsTotal:    streams,
		StreamsByPolicy: streamsByPolicy,
		Rate:            rate,
	})
}
// streams_by_policy 字段展示各 retention/enforcement policy 下的活跃流分布。
