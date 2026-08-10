// SPDX-License-Identifier: AGPL-3.0-only

package testkafka

// testkafka 为 Kafka 集成测试提供 franz-go 协议消息的构造辅助，
// 便于模拟 broker 对 Produce 请求的异常响应。

import (
	"github.com/twmb/franz-go/pkg/kerr"
	"github.com/twmb/franz-go/pkg/kmsg"
)

// CreateProduceResponseError 构造带指定 Kafka 错误码的 ProduceResponse，用于测试生产失败路径。
// CreateProduceResponseError returns a kmsg.ProduceResponse containing err for the input topic and partition.
func CreateProduceResponseError(version int16, topic string, partition int32, err *kerr.Error) *kmsg.ProduceResponse {
	return &kmsg.ProduceResponse{
		Version: version,
		Topics: []kmsg.ProduceResponseTopic{
			{
				Topic: topic,
				Partitions: []kmsg.ProduceResponseTopicPartition{
					{
						Partition: partition,
						ErrorCode: err.Code,
					},
				},
			},
		},
	}
}
// 测试场景可通过 version/topic/partition 精确控制 broker 返回的错误类型。
