package marshal

// marshal 包 labels 子模块将 PromQL 指标字符串解析为 loghttp.LabelSet，供 HTTP 响应与内部 stream 标签互转。

import (
	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/promql/parser"

	"github.com/grafana/loki/v3/pkg/loghttp"
)

// NewLabelSet 调用 parser.ParseMetric 解析 {foo="bar"} 形式，输出 map 标签集。
// NewLabelSet constructs a Labelset from a promql metric list as a string
func NewLabelSet(s string) (loghttp.LabelSet, error) {
	lbls, err := parser.NewParser(parser.Options{}).ParseMetric(s)
	if err != nil {
		return nil, err
	}

	ret := make(map[string]string, lbls.Len())
	lbls.Range(func(lbl labels.Label) {
		ret[lbl.Name] = lbl.Value
	})

	return ret, nil
}
// 解析失败时返回底层 parser 错误，调用方应包装上下文便于定位非法标签串。
