package cassandra

// instrumentation 注册 cassandra_request_duration_seconds 直方图，通过 gocql QueryObserver/BatchObserver 按操作类型与 HTTP 风格状态码记录延迟。

import (
	"context"
	"strings"

	gocql "github.com/apache/cassandra-gocql-driver/v2"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

var requestDuration = prometheus.NewHistogramVec(prometheus.HistogramOpts{
	Namespace: constants.Loki,
	Name:      "cassandra_request_duration_seconds",
	Help:      "Time spent doing Cassandra requests.",
	Buckets:   prometheus.ExponentialBuckets(0.001, 4, 9),
}, []string{"operation", "status_code"})

func init() {
	prometheus.MustRegister(requestDuration)
}

// observer 实现 gocql 观测接口，BATCH 固定标签，Query 取 SQL 首词作为 operation。
type observer struct{}

func err(err error) string {
	if err != nil {
		return "500"
	}
	return "200"
}

// ObserveBatch 记录批量语句耗时，err 非空时 status_code 记为 500。
func (observer) ObserveBatch(_ context.Context, b gocql.ObservedBatch) {
	requestDuration.WithLabelValues("BATCH", err(b.Err)).Observe(b.End.Sub(b.Start).Seconds())
}

// ObserveQuery 按语句首 token（SELECT/INSERT 等）分桶并观测单次查询延迟。
func (observer) ObserveQuery(_ context.Context, q gocql.ObservedQuery) {
	parts := strings.SplitN(q.Statement, " ", 2)
	requestDuration.WithLabelValues(parts[0], err(q.Err)).Observe(q.End.Sub(q.Start).Seconds())
}
// init 向默认 Prometheus registry 注册 requestDuration 供全局抓取。
