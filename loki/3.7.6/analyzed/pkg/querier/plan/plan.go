package plan

// plan 包 QueryPlan 封装 LogQL AST，提供 JSON/protobuf 序列化、Equal/Hash 供 query-frontend 缓存与分片。

import (
	"bytes"

	"github.com/grafana/loki/v3/pkg/logql/syntax"
	"github.com/grafana/loki/v3/pkg/util"
)

type QueryPlan struct {
	AST syntax.Expr
}

func (t QueryPlan) Marshal() ([]byte, error) {
	return t.MarshalJSON()
}

func (t *QueryPlan) MarshalTo(data []byte) (int, error) {
	appender := &appendWriter{
		slice: data[:0],
	}
	err := syntax.EncodeJSON(t.AST, appender)
	if err != nil {
		return 0, err
	}

	return len(appender.slice), nil
}

func (t *QueryPlan) Unmarshal(data []byte) error {
	return t.UnmarshalJSON(data)
}

func (t *QueryPlan) Size() int {
	counter := &countWriter{}
	err := syntax.EncodeJSON(t.AST, counter)
	if err != nil {
		return 0
	}

	return counter.bytes
}

func (t QueryPlan) MarshalJSON() ([]byte, error) {
	var buf bytes.Buffer
	err := syntax.EncodeJSON(t.AST, &buf)
	if err != nil {
		return nil, err
	}

	return buf.Bytes(), nil
}

// UnmarshalJSON 空 data 兼容旧版；否则 syntax.DecodeJSON 还原 AST。
func (t *QueryPlan) UnmarshalJSON(data []byte) error {
	// An empty query plan is ingored to be backwards compatible.
	if len(data) == 0 {
		return nil
	}

	expr, err := syntax.DecodeJSON(string(data))
	if err != nil {
		return err
	}

	t.AST = expr
	return nil
}

// Equal 比较双方 Marshal 字节是否相等。
func (t QueryPlan) Equal(other QueryPlan) bool {
	left, err := t.Marshal()
	if err != nil {
		return false
	}

	right, err := other.Marshal()
	if err != nil {
		return false
	}
	return bytes.Equal(left, right)
}

func (t QueryPlan) String() string {
	if t.AST == nil {
		return ""
	}
	return t.AST.String()
}

// Hash 对 AST 字符串做 HashedQuery，nil AST 返回 0。
func (t *QueryPlan) Hash() uint32 {
	if t.AST == nil {
		return 0
	}
	return util.HashedQuery(t.AST.String())
}

// countWriter/appendWriter 辅助 Size/MarshalTo 预分配与追加写入。
// countWriter is not writing any bytes. It just counts the bytes that would be
// written.
type countWriter struct {
	bytes int
}

// Write implements io.Writer.
func (w *countWriter) Write(p []byte) (int, error) {
	w.bytes += len(p)
	return len(p), nil
}

// appendWriter appends to a slice.
type appendWriter struct {
	slice []byte
}

func (w *appendWriter) Write(p []byte) (int, error) {
	w.slice = append(w.slice, p...)
	return len(p), nil
}
// QueryPlan.String 委托 AST.String，供日志与 span 属性展示人类可读 LogQL。
