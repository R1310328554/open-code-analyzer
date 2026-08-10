package sketch

// series 定义 approx_topk 矩阵结果类型 TopKMatrix，实现 promql.Value 接口并支持与 logproto 双向序列化。

import (
	"github.com/prometheus/prometheus/promql/parser"

	"github.com/grafana/loki/v3/pkg/logproto"
)

const ValueTypeTopKMatrix = "topk_matrix"

type TopKVector struct {
	topk *Topk
	ts   uint64
}

// TopKMatrix 是按时间排序的 TopKVector 切片，供矩阵查询返回。
// TopkMatrix is `promql.Value` and `parser.Value`
type TopKMatrix []TopKVector

// Type implements `promql.Value` and `parser.Value`
func (TopKMatrix) Type() parser.ValueType { return ValueTypeTopKMatrix }

// String implements `promql.Value` and `parser.Value`
func (TopKMatrix) String() string {
	return ""
}

// ToProto 将每个时间步的 Topk 草图序列化为 gRPC 传输结构。
func (s TopKMatrix) ToProto() (*logproto.TopKMatrix, error) {
	points := make([]*logproto.TopKMatrix_Vector, 0, len(s))
	for _, point := range s {
		topk, err := point.topk.ToProto()
		if err != nil {
			return nil, err
		}

		points = append(points, &logproto.TopKMatrix_Vector{Topk: topk, TimestampMs: int64(point.ts)})
	}

	return &logproto.TopKMatrix{Values: points}, nil
}

// TopKMatrixFromProto 从 proto 重建 TopKMatrix，供查询结果反序列化。
func TopKMatrixFromProto(proto *logproto.TopKMatrix) (TopKMatrix, error) {
	values := make(TopKMatrix, 0, len(proto.Values))
	for _, vector := range proto.Values {
		topk, err := TopkFromProto(vector.Topk)
		if err != nil {
			return nil, err
		}

		values = append(values, TopKVector{topk, uint64(vector.TimestampMs)})

	}

	return values, nil
}
// Type/String 满足 PromQL 值接口；String 当前返回空串，格式化由上层处理。
