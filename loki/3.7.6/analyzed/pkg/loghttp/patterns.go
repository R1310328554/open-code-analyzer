package loghttp

// patterns 解析 /patterns 端点 HTTP 请求，构造 logproto.QueryPatternsRequest。

import (
	"net/http"

	"github.com/grafana/loki/v3/pkg/logproto"
)

func ParsePatternsQuery(r *http.Request) (*logproto.QueryPatternsRequest, error) {
	req := &logproto.QueryPatternsRequest{}

	req.Query = query(r)
	start, end, err := bounds(r)
	if err != nil {
		return nil, err
	}
	req.Start = start
	req.End = end

	calculatedStep, err := step(r, start, end)
	if err != nil {
		return nil, err
	}
	if calculatedStep <= 0 {
		return nil, errZeroOrNegativeStep
	}
	// For safety, limit the number of returned points per timeseries.
	// This is sufficient for 60s resolution for a week or 1h resolution for a year.
// 11000 点上限约等于一周 60s 分辨率或一年 1h 分辨率，防止响应过大。
	if (req.End.Sub(req.Start) / calculatedStep) > 11000 {
		return nil, errStepTooSmall
	}
	req.Step = calculatedStep.Milliseconds()

	return req, nil
}
// QueryPatternsRequest.Step 以毫秒存储，与 range 查询 API 保持一致。
