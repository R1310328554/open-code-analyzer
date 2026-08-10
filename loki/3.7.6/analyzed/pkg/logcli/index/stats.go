package index

// index/stats 实现 logcli stats 子命令：查询索引统计（流数、chunk 数、字节数等）。

import (
	"fmt"
	"log"
	"time"

	"github.com/fatih/color"

	"github.com/grafana/loki/v3/pkg/logcli/client"
	"github.com/grafana/loki/v3/pkg/logproto"
)

type StatsQuery struct {
	QueryString string
	Start       time.Time
	End         time.Time
	Quiet       bool
}

// DoStats 调用 Stats 并以彩色键值对格式打印 IndexStatsResponse。
// DoStats executes the stats query and prints the results
func (q *StatsQuery) DoStats(c client.Client) {
	stats := q.Stats(c)
	kvs := stats.LoggingKeyValues()

	fmt.Print("{\n")
	for i := 0; i < len(kvs)-1; i = i + 2 {
		k := kvs[i].(string)
		v := kvs[i+1]
// bytes 字段以原样字符串输出（可能含单位），其余字段格式化为整数。
		if k == "bytes" {
			fmt.Printf("  %s: %s\n", color.BlueString(k), v)
			continue
		}

		fmt.Printf("  %s: %d\n", color.BlueString(k), v)
	}
	fmt.Print("}\n")
}

// Stats 请求 /loki/api/v1/index/stats 端点并返回结构化响应。
// Stats returns an index stats response
func (q *StatsQuery) Stats(c client.Client) *logproto.IndexStatsResponse {
	var statsResponse *logproto.IndexStatsResponse
	var err error

	statsResponse, err = c.GetStats(q.QueryString, q.Start, q.End, q.Quiet)

	if err != nil {
		log.Fatalf("Error doing request: %+v", err)
	}
	return statsResponse
}
// LoggingKeyValues 将 IndexStatsResponse 展平为交替 key/value 切片供打印循环使用。
