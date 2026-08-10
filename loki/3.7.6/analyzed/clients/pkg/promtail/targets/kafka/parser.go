package kafka

// 默认 Kafka 消息解析器：将 message.Value 原样作为 log line，
// 可选使用 Kafka 消息时间戳或当前时间。

import (
	"github.com/IBM/sarama"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/relabel"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"

	"github.com/grafana/loki/v3/pkg/logproto"
)

// 零拷贝解析：不修改原始字节，直接 string(message.Value) 作为 Line。
// messageParser implements MessageParser. It doesn't modify the content of the original `message.Value`.
type messageParser struct{}

func (n messageParser) Parse(message *sarama.ConsumerMessage, labels model.LabelSet, _ []*relabel.Config, useIncomingTimestamp bool) ([]api.Entry, error) {
	return []api.Entry{
		{
			Labels: labels,
			Entry: logproto.Entry{
				Timestamp: timestamp(useIncomingTimestamp, message.Timestamp),
				Line:      string(message.Value),
			},
		},
	}, nil
}
