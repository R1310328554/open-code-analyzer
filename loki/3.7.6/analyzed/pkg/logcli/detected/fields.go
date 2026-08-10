package detected

// detected 包实现 logcli detected-fields 子命令：查询 Loki 自动检测的
// 日志字段标签、类型与基数，或列出指定字段的取值。

import (
	"encoding/json"
	"fmt"
	"log"
	"slices"
	"strings"
	"time"

	"github.com/fatih/color"

	"github.com/grafana/loki/v3/pkg/logcli/client"
	"github.com/grafana/loki/v3/pkg/loghttp"
)

type FieldsQuery struct {
	QueryString   string
	Start         time.Time
	End           time.Time
	Limit         int
	LineLimit     int
	Step          time.Duration
	Quiet         bool
	FieldName     string
	ColoredOutput bool
}

// Do 调用 GetDetectedFields 并按 outputMode 以 raw JSON 或彩色表格输出。
// DoQuery executes the query and prints out the results
func (q *FieldsQuery) Do(c client.Client, outputMode string) {
	var resp *loghttp.DetectedFieldsResponse
	var err error

	resp, err = c.GetDetectedFields(
		q.QueryString,
		q.FieldName,
		q.Limit,
		q.LineLimit,
		q.Start,
		q.End,
		q.Step,
		q.Quiet,
	)
	if err != nil {
		log.Fatalf("Error doing request: %+v", err)
	}

	switch outputMode {
// raw 模式输出完整 JSON 响应，便于与其他工具管道对接。
	case "raw":
		out, err := json.Marshal(resp)
		if err != nil {
			log.Fatalf("Error marshalling response: %+v", err)
		}
		fmt.Println(string(out))
	default:
		var output []string
		if len(resp.Fields) > 0 {
			output = make([]string, len(resp.Fields))
			for i, field := range resp.Fields {
				bold := color.New(color.Bold)
	// 默认模式格式化输出字段 label、type 与 cardinality 三列信息。
			output[i] = fmt.Sprintf("label: %s\t\t", bold.Sprintf("%s", field.Label)) +
					fmt.Sprintf("type: %s\t\t", bold.Sprintf("%s", field.Type)) +
					fmt.Sprintf("cardinality: %s", bold.Sprintf("%d", field.Cardinality))
			}
	// 指定 fieldName 时响应含 Values 列表而非 Fields 元数据。
	} else if len(resp.Values) > 0 {
			output = resp.Values
		}

		slices.Sort(output)
		fmt.Println(strings.Join(output, "\n"))
	}
}
// ColoredOutput 为 true 时使用 fatih/color 高亮字段名与统计数字。
