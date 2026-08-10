package labelquery

// labelquery 实现 logcli labels 子命令：列出 LogQL 匹配范围内的标签名或指定标签的值。

import (
	"fmt"
	"log"
	"time"

	"github.com/grafana/loki/v3/pkg/logcli/client"
	"github.com/grafana/loki/v3/pkg/loghttp"
)

// LabelQuery 含可选 LabelName；为空时列出全部标签名，否则列出该标签取值。
// LabelQuery contains all necessary fields to execute label queries and print out the results
type LabelQuery struct {
	LabelName string
	Quiet     bool
	Start     time.Time
	End       time.Time
}

// DoLabels 调用 ListLabels 并逐行打印到 stdout。
// DoLabels prints out label results
func (q *LabelQuery) DoLabels(c client.Client) {
	values := q.ListLabels(c)

	for _, value := range values {
		fmt.Println(value)
	}
}

// ListLabels 按 LabelName 是否为空选择 ListLabelValues 或 ListLabelNames。
// ListLabels returns an array of label strings
func (q *LabelQuery) ListLabels(c client.Client) []string {
	var labelResponse *loghttp.LabelResponse
	var err error
	if len(q.LabelName) > 0 {
		labelResponse, err = c.ListLabelValues(q.LabelName, q.Quiet, q.Start, q.End)
	} else {
		labelResponse, err = c.ListLabelNames(q.Quiet, q.Start, q.End)
	}
	if err != nil {
		log.Fatalf("Error doing request: %+v", err)
	}
	return labelResponse.Data
}
// Start/End 时间范围传递给 Loki API，限定标签发现的日志样本窗口。
