package detected

// detected 包处理 logproto.DetectedLabel：反序列化 HyperLogLog sketch、按标签名合并并估算基数，供日志标签检测 API 使用。

import (
	"github.com/axiomhq/hyperloglog"

	"github.com/grafana/loki/v3/pkg/logproto"
)

type UnmarshaledDetectedLabel struct {
	Label  string
	Sketch *hyperloglog.Sketch
}

// unmarshalDetectedLabel 将 protobuf 中的二进制 sketch 反序列化为 hyperloglog.Sketch。
func unmarshalDetectedLabel(l *logproto.DetectedLabel) (*UnmarshaledDetectedLabel, error) {
	sketch := hyperloglog.New()
	err := sketch.UnmarshalBinary(l.Sketch)
	if err != nil {
		return nil, err
	}
	return &UnmarshaledDetectedLabel{
		Label:  l.Label,
		Sketch: sketch,
	}, nil
}

// Merge 将同标签的新 sketch 合并进已有 HyperLogLog 结构以累加基数估计。
func (m *UnmarshaledDetectedLabel) Merge(dl *logproto.DetectedLabel) error {
	sketch := hyperloglog.New()
	err := sketch.UnmarshalBinary(dl.Sketch)
	if err != nil {
		return err
	}
	return m.Sketch.Merge(sketch)
}

// MergeLabels 按标签名聚合 DetectedLabel，输出带 Cardinality 与可再次合并的二进制 sketch。
func MergeLabels(labels []*logproto.DetectedLabel) (result []*logproto.DetectedLabel, err error) {
	mergedLabels := make(map[string]*UnmarshaledDetectedLabel)
	for _, label := range labels {
		l, ok := mergedLabels[label.Label]
		if !ok {
			unmarshaledLabel, err := unmarshalDetectedLabel(label)
			if err != nil {
				return nil, err
			}
			mergedLabels[label.Label] = unmarshaledLabel
		} else {
			err := l.Merge(label)
			if err != nil {
				return nil, err
			}
		}
	}

	for _, label := range mergedLabels {
		// Keep the marshalled sketch so the result can be merged again, e.g. when
		// the query frontend merges responses that MultiTenantQuerier already
		// merged across tenants.
		sketch, err := label.Sketch.MarshalBinary()
		if err != nil {
			return nil, err
		}

		detectedLabel := &logproto.DetectedLabel{
			Label:       label.Label,
			Cardinality: label.Sketch.Estimate(),
			Sketch:      sketch,
		}

		result = append(result, detectedLabel)
	}

	return
}
// 保留序列化 sketch 以便 query frontend 与 MultiTenantQuerier 跨租户响应再次合并。
