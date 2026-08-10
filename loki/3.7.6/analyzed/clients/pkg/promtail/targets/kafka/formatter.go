package kafka

// Kafka target 标签格式化：relabel 处理 discovered 标签并剔除 __ 前缀内部标签。

import (
	"strings"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/model/relabel"

	"github.com/grafana/loki/v3/pkg/util"
)

// 空标签集返回 nil；relabel 丢弃时返回 nil；否则转为 model.LabelSet 并过滤 __ 前缀。
func format(lbs labels.Labels, cfg []*relabel.Config) model.LabelSet {
	if lbs.IsEmpty() {
		return nil
	}
	lb := labels.NewBuilder(lbs)
	if len(cfg) > 0 {
		if keep := relabel.ProcessBuilder(lb, cfg...); !keep {
			return nil
		}
	}
	processed := lb.Labels()
	labelOut := model.LabelSet(util.LabelsToMetric(processed))
// 删除 __meta_kafka_* 等内部标签，避免写入 Loki stream。
	for k := range labelOut {
		if strings.HasPrefix(string(k), "__") {
			delete(labelOut, k)
		}
	}
	return labelOut
}
