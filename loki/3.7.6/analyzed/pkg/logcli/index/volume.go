package index

// index/volume 实现 logcli volume 与 volume_range 子命令：
// 查询索引中日志字节量分布，支持按标签聚合与时间序列输出。

import (
	"log"

	"github.com/grafana/loki/v3/pkg/logcli/client"
	"github.com/grafana/loki/v3/pkg/logcli/output"
	"github.com/grafana/loki/v3/pkg/logcli/print"
	"github.com/grafana/loki/v3/pkg/logcli/volume"
	"github.com/grafana/loki/v3/pkg/loghttp"
)

// GetVolume 执行瞬时 volume 查询并委托 print 包格式化结果。
// GetVolume executes a volume query and prints the results
func GetVolume(q *volume.Query, c client.Client, out output.LogOutput, statistics bool) {
	do(q, false, c, out, statistics)
}

// GetVolumeRange 返回时间序列数据点集合，展示 volume 随时间变化。
// GetVolumeRange executes a volume query over a period of time and prints the results, which will
// be a collection of data points over time.
func GetVolumeRange(q *volume.Query, c client.Client, out output.LogOutput, statistics bool) {
	do(q, true, c, out, statistics)
}

// do 统一处理 volume 与 volume_range：可选打印查询 statistics。
func do(q *volume.Query, rangeQuery bool, c client.Client, out output.LogOutput, statistics bool) {
	resp := getVolume(q, rangeQuery, c)

	resultsPrinter := print.NewQueryResultPrinter(nil, nil, q.Quiet, 0, false, false)

	if statistics {
		resultsPrinter.PrintStats(resp.Data.Statistics)
	}

	_, _ = resultsPrinter.PrintResult(resp.Data.Result, out, nil)
}

// getVolume 按 rangeQuery 标志选择 GetVolume 或 GetVolumeRange API。
// getVolume returns a volume result
func getVolume(q *volume.Query, rangeQuery bool, c client.Client) *loghttp.QueryResponse {
	var resp *loghttp.QueryResponse
	var err error

	if rangeQuery {
		resp, err = c.GetVolumeRange(q)
	} else {
		resp, err = c.GetVolume(q)
	}
	if err != nil {
		log.Fatalf("Error doing request: %+v", err)
	}

	return resp
}
// resultsPrinter.PrintResult 将 volume 矩阵结果写入用户指定的 LogOutput 格式。
