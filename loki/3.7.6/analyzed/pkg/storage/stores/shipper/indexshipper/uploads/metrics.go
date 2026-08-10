package uploads

// uploads metrics 记录表级上传批次成败计数，供监控 index shipper 周期性 sync 是否稳定。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

const (
	statusFailure = "failure"
	statusSuccess = "success"
)

type metrics struct {
	tablesUploadOperationTotal *prometheus.CounterVec
}

// newMetrics 注册 upload 操作 counter，由 TableManager 每轮 sync 递增。
func newMetrics(r prometheus.Registerer) *metrics {
	return &metrics{
		tablesUploadOperationTotal: promauto.With(r).NewCounterVec(prometheus.CounterOpts{
			Name: "tables_upload_operation_total",
			Help: "Total number of upload operations done by status",
		}, []string{"status"}),
	}
}
// statusSuccess 与 statusFailure 常量作为 counter 标签值上报上传结果。
