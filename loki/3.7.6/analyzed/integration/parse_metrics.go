//go:build integration

package integration

// 集成测试 Prometheus 文本 exposition 解析：从 /metrics 响应体提取
// 指定 metric 的 counter/gauge 值与标签，供断言 Loki 组件指标。

import (
	"fmt"
	"strings"

	io_prometheus_client "github.com/prometheus/client_model/go"
	"github.com/prometheus/common/expfmt"
	"github.com/prometheus/common/model"
)

// ErrNoMetricFound 与 ErrInvalidMetricType 表示指标缺失或类型非 counter/gauge。
var (
	ErrNoMetricFound     = fmt.Errorf("metric not found")
	ErrInvalidMetricType = fmt.Errorf("invalid metric type")
)

func extractMetricFamily(name, metrics string) (*io_prometheus_client.MetricFamily, error) {
	parser := expfmt.NewTextParser(model.UTF8Validation)
	mfs, err := parser.TextToMetricFamilies(strings.NewReader(metrics))
	if err != nil {
		return nil, err
	}

	mf, ok := mfs[name]
	if !ok {
		return nil, ErrNoMetricFound
	}
	return mf, nil
}

// extractMetric 返回首个样本的数值与标签 map，仅支持 counter 与 gauge 类型。
func extractMetric(metricName, metrics string) (float64, map[string]string, error) {
	mf, err := extractMetricFamily(metricName, metrics)
	if err != nil {
		return 0, nil, err
	}

	var val float64
	switch mf.GetType() {
	case io_prometheus_client.MetricType_COUNTER:
		val = *mf.Metric[0].Counter.Value
	case io_prometheus_client.MetricType_GAUGE:
		val = *mf.Metric[0].Gauge.Value
	default:
		return 0, nil, ErrInvalidMetricType
	}

	labels := make(map[string]string)
	for _, l := range mf.Metric[0].Label {
		labels[*l.Name] = *l.Value
	}

	return val, labels, nil
}
