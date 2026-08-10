package indexgateway

// metrics 定义 Index Gateway 服务端 chunk 过滤前后的数量直方图：按 route（chunk_refs/shards）区分 GetChunkRef 与 GetShards 路径。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

const (
	routeChunkRefs = "chunk_refs"
	routeShards    = "shards"
)

// Metrics 封装 preFilterChunks 与 postFilterChunks 两个 HistogramVec。
type Metrics struct {
	preFilterChunks  *prometheus.HistogramVec
	postFilterChunks *prometheus.HistogramVec
}

func NewMetrics(r prometheus.Registerer) *Metrics {
	return &Metrics{
		preFilterChunks: promauto.With(r).NewHistogramVec(prometheus.HistogramOpts{
			Namespace: constants.Loki,
			Subsystem: "index_gateway",
			Name:      "prefilter_chunks",
			Help:      "Number of chunks before filtering",
			Buckets:   prometheus.ExponentialBuckets(1, 4, 10),
		}, []string{"route"}),
		postFilterChunks: promauto.With(r).NewHistogramVec(prometheus.HistogramOpts{
			Namespace: constants.Loki,
			Subsystem: "index_gateway",
			Name:      "postfilter_chunks",
			Help:      "Number of chunks after filtering",
			Buckets:   prometheus.ExponentialBuckets(1, 4, 10),
		}, []string{"route"}),
	}
}
// bloom 过滤前后 chunk 数量差异反映索引剪枝效果。
