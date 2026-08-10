// legacy marshal 将内部对象序列化为 pkg/loghttp/legacy 协议 JSON，供未迁移的 v0 端点直接编码。
// Package marshal converts internal objects to loghttp model objects.  This package is designed to work with
// models in pkg/loghttp/legacy.
package marshal

import (
	"fmt"
	"io"

	"github.com/gorilla/websocket"
	json "github.com/json-iterator/go"

	loghttp "github.com/grafana/loki/v3/pkg/loghttp/legacy"
	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logqlmodel"
)

// 下列方法直接编码入参：若 legacy 模型变更，需像 v1 一样经 loghttp 中间层转换。
// Note that the below methods directly marshal the values passed in.  This is because these objects currently marshal
// cleanly to the legacy http protocol (because that was how it was initially implemented).  If this ever changes,
// it will be caught by testing and we will have to handle legacy like we do v1:  1) exchange a variety of structs for
// for loghttp model objects 2) marshal the loghttp model objects

// WriteQueryResponseJSON 仅支持 streams 结果类型，输出 streams 与 stats 字段。
// WriteQueryResponseJSON marshals promql.Value to legacy loghttp JSON and then writes it to the provided io.Writer
func WriteQueryResponseJSON(v logqlmodel.Result, w io.Writer) error {
	if v.Data.Type() != logqlmodel.ValueTypeStreams {
		return fmt.Errorf("legacy endpoints only support %s result type, current type is %s", logqlmodel.ValueTypeStreams, v.Data.Type())
	}

	j := map[string]interface{}{
		"streams": v.Data,
		"stats":   v.Statistics,
	}

	return json.NewEncoder(w).Encode(j)
}

// WriteLabelResponseJSON marshals the logproto.LabelResponse to legacy loghttp JSON and then writes it to the provided writer
func WriteLabelResponseJSON(l logproto.LabelResponse, w io.Writer) error {
	return json.NewEncoder(w).Encode(l)
}

// WriteTailResponseJSON 通过 websocket WriteJSON 推送 tail 增量，保持 legacy 字段布局。
// WriteTailResponseJSON marshals the TailResponse to legacy loghttp JSON and then writes it to the provided connection
func WriteTailResponseJSON(r loghttp.TailResponse, c *websocket.Conn) error {
	return c.WriteJSON(r)
}
// WriteLabelResponseJSON 直接 json.Encode LabelResponse，与早期 Loki API 完全兼容。
