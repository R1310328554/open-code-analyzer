package queryrange

// queryrange 包 parquet 将 LokiPromResponse 指标与 LokiResponse 日志流编码为 Apache Parquet 列式格式，供 Accept: application/vnd.apache.parquet 客户端消费。

import (
	"bytes"
	"context"
	"io"
	"net/http"

	"github.com/parquet-go/parquet-go"
	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/promql/parser"

	serverutil "github.com/grafana/loki/v3/pkg/util/server"

	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
)

// encodeResponseParquet 按响应类型选择指标或日志 Parquet 编码并设置 Content-Type。
func encodeResponseParquet(ctx context.Context, res queryrangebase.Response) (*http.Response, error) {
	_, sp := tracer.Start(ctx, "codec.EncodeResponse")
	defer sp.End()

	var buf bytes.Buffer

	err := encodeResponseParquetTo(ctx, res, &buf)
	if err != nil {
		return nil, err
	}

	resp := http.Response{
		Header: http.Header{
			"Content-Type": []string{ParquetType},
		},
		Body:       io.NopCloser(&buf),
		StatusCode: http.StatusOK,
	}
	return &resp, nil
}

func encodeResponseParquetTo(_ context.Context, res queryrangebase.Response, w io.Writer) error {
	switch response := res.(type) {
	case *LokiPromResponse:
		return encodeMetricsParquetTo(response, w)
	case *LokiResponse:
		return encodeLogsParquetTo(response, w)
	default:
		return serverutil.UserError("request does not support Parquet responses")
	}
}

// MetricRowType 定义指标 Parquet 行：毫秒时间戳、标签 map 与 float64 样本值。
type MetricRowType struct {
	Timestamp int64             `parquet:"timestamp,timestamp(millisecond),delta"`
	Labels    map[string]string `parquet:"labels"`
	Value     float64           `parquet:"value"`
}

// LogStreamRowType 定义日志 Parquet 行：纳秒时间戳、标签 map 与 lz4 压缩日志行。
type LogStreamRowType struct {
	Timestamp int64             `parquet:"timestamp,timestamp(nanosecond),delta"`
	Labels    map[string]string `parquet:"labels"`
	Line      string            `parquet:"line,lz4"`
}

// encodeMetricsParquetTo 遍历 Prom 样本流写入 GenericWriter[MetricRowType]。
func encodeMetricsParquetTo(response *LokiPromResponse, w io.Writer) error {
	schema := parquet.SchemaOf(new(MetricRowType))
	writer := parquet.NewGenericWriter[MetricRowType](w, schema)

	for _, stream := range response.Response.Data.Result {
		lbls := make(map[string]string)
		for _, keyValue := range stream.Labels {
			lbls[keyValue.Name] = keyValue.Value
		}
		for _, sample := range stream.Samples {
			row := MetricRowType{
				Timestamp: sample.TimestampMs,
				Labels:    lbls,
				Value:     sample.Value,
			}
			if _, err := writer.Write([]MetricRowType{row}); err != nil {
				return err
			}
		}
	}
	return writer.Close()
}

// encodeLogsParquetTo 解析 stream 标签后逐条 entry 写入 LogStreamRowType。
func encodeLogsParquetTo(response *LokiResponse, w io.Writer) error {
	schema := parquet.SchemaOf(new(LogStreamRowType))
	writer := parquet.NewGenericWriter[LogStreamRowType](w, schema)

	for _, stream := range response.Data.Result {
		lbls, err := parser.NewParser(parser.Options{}).ParseMetric(stream.Labels)
		if err != nil {
			return err
		}
		lblsMap := make(map[string]string)
		lbls.Range(func(lbl labels.Label) {
			lblsMap[lbl.Name] = lbl.Value
		})

		for _, entry := range stream.Entries {
			row := LogStreamRowType{
				Timestamp: entry.Timestamp.UnixNano(),
				Labels:    lblsMap,
				Line:      entry.Line,
			}
			if _, err := writer.Write([]LogStreamRowType{row}); err != nil {
				return err
			}
		}
	}

	return writer.Close()
}
// 不支持的响应类型返回 UserError，Parquet 编码在 codec 层由 Accept 头触发。
