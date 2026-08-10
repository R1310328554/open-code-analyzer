package loghttp

// tail 解析实时 tail WebSocket/HTTP 请求的 LogQL 与延迟参数。

import (
	"fmt"
	"net/http"
	"strconv"
	"time"

	json "github.com/json-iterator/go"

	"github.com/grafana/dskit/httpgrpc"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql/syntax"
	"github.com/grafana/loki/v3/pkg/querier/plan"
)

const (
	maxDelayForInTailing = 5
)

// TailResponse 封装 tail 推送的日志流及因限流丢弃的流信息。
// TailResponse represents the http json response to a tail query
type TailResponse struct {
	Streams        []Stream        `json:"streams,omitempty"`
	DroppedStreams []DroppedStream `json:"dropped_entries,omitempty"`
}

// DroppedStream 表示整条流在 tail 中被丢弃，含纳秒时间戳与标签集。
// DroppedStream represents a dropped stream in tail call
type DroppedStream struct {
	Timestamp time.Time
	Labels    LabelSet
}

// DroppedStream 自定义 JSON 序列化，时间戳以纳秒整数字符串输出。
// MarshalJSON implements json.Marshaller
func (s *DroppedStream) MarshalJSON() ([]byte, error) {
	return json.Marshal(struct {
		Timestamp string   `json:"timestamp"`
		Labels    LabelSet `json:"labels,omitempty"`
	}{
		Timestamp: fmt.Sprintf("%d", s.Timestamp.UnixNano()),
		Labels:    s.Labels,
	})
}

// UnmarshalJSON implements json.UnMarshaller
func (s *DroppedStream) UnmarshalJSON(data []byte) error {
	unmarshal := struct {
		Timestamp string   `json:"timestamp"`
		Labels    LabelSet `json:"labels,omitempty"`
	}{}

	err := json.Unmarshal(data, &unmarshal)

	if err != nil {
		return err
	}

	t, err := strconv.ParseInt(unmarshal.Timestamp, 10, 64)
	if err != nil {
		return err
	}

	s.Timestamp = time.Unix(0, t)
	s.Labels = unmarshal.Labels

	return nil
}

// ParseTailQuery parses a TailRequest request from an http request.
// ParseTailQuery 解析 LogQL、limit、start 与 delay_for（上限 maxDelayForInTailing）。
func ParseTailQuery(r *http.Request) (*logproto.TailRequest, error) {
	var err error
	qs := query(r)
	parsed, err := syntax.ParseExpr(qs)
	if err != nil {
		return nil, err
	}
	req := logproto.TailRequest{
		Query: qs,
		Plan: &plan.QueryPlan{
			AST: parsed,
		},
	}

	req.Query, err = parseRegexQuery(r)
	if err != nil {
		return nil, httpgrpc.Errorf(http.StatusBadRequest, "%s", err.Error())
	}

	req.Limit, err = limit(r)
	if err != nil {
		return nil, err
	}

	req.Start, _, err = bounds(r)
	if err != nil {
		return nil, err
	}
	req.DelayFor, err = tailDelay(r)
	if err != nil {
		return nil, err
	}
	if req.DelayFor > maxDelayForInTailing {
		return nil, fmt.Errorf("delay_for can't be greater than %d", maxDelayForInTailing)
	}
	return &req, nil
}
// TailRequest.Plan 携带已解析 AST，querier 可直接用于 tail 执行计划。
