package seriesquery

// seriesquery 包封装 logcli series 子命令：查询匹配器对应的标签组合并可分析标签基数。

import (
	"fmt"
	"log"
	"os"
	"sort"
	"text/tabwriter"
	"time"

	"github.com/grafana/loki/v3/pkg/logcli/client"
	"github.com/grafana/loki/v3/pkg/loghttp"
)

// SeriesQuery 持有 matcher、时间范围及 AnalyzeLabels 等输出选项。
// SeriesQuery contains all necessary fields to execute label queries and print out the results
type SeriesQuery struct {
	Matcher       string
	Start         time.Time
	End           time.Time
	AnalyzeLabels bool
	Quiet         bool
}

// labelDetails 统计某标签名在多少流中出现及其唯一值个数。
type labelDetails struct {
	name       string
	inStreams  int
	uniqueVals map[string]struct{}
}

// DoSeries 调用 GetSeries 后按 AnalyzeLabels 决定表格分析或逐行打印。
// DoSeries prints out series results
func (q *SeriesQuery) DoSeries(c client.Client) {
	streams := q.GetSeries(c)

	if q.AnalyzeLabels {
		labelMap := map[string]*labelDetails{}

		for _, stream := range streams {
			for labelName, labelValue := range stream {
				if _, ok := labelMap[labelName]; ok {
					labelMap[labelName].inStreams++
					labelMap[labelName].uniqueVals[labelValue] = struct{}{}
				} else {
					labelMap[labelName] = &labelDetails{
						name:       labelName,
						inStreams:  1,
						uniqueVals: map[string]struct{}{labelValue: {}},
					}
				}
			}
		}

		lds := make([]*labelDetails, 0, len(labelMap))
		for _, ld := range labelMap {
			lds = append(lds, ld)
		}
// 分析模式下按唯一值数量降序排列标签，便于发现高基数标签。
		sort.Slice(lds, func(ld1, ld2 int) bool {
			return len(lds[ld1].uniqueVals) > len(lds[ld2].uniqueVals)
		})

		fmt.Println("Total Streams: ", len(streams))
		fmt.Println("Unique Labels: ", len(labelMap))
		fmt.Println()

		w := tabwriter.NewWriter(os.Stdout, 0, 0, 2, ' ', 0)
		fmt.Fprintf(w, "Label Name\tUnique Values\tFound In Streams\n")
		for _, details := range lds {
			fmt.Fprintf(w, "%v\t%v\t%v\n", details.name, len(details.uniqueVals), details.inStreams)
		}
		w.Flush()

	} else {
		for _, value := range streams {
			fmt.Println(value)
		}
	}

}

// GetSeries 调用 client.Series API 并返回 Data 中的 LabelSet 切片。
// GetSeries returns an array of label sets
func (q *SeriesQuery) GetSeries(c client.Client) []loghttp.LabelSet {
	seriesResponse, err := c.Series([]string{q.Matcher}, q.Start, q.End, q.Quiet)
	if err != nil {
		log.Fatalf("Error doing request: %+v", err)
	}
	return seriesResponse.Data
}
// Quiet 为真时抑制 client 层的请求日志输出。
