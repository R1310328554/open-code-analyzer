// SPDX-License-Identifier: AGPL-3.0-only

package client

// reader_client 构造分区读取用的 kgo.Client：配置 fetch 上限、broker 读保护及自动建 topic 默认分区数。

import (
	"context"
	"fmt"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/twmb/franz-go/pkg/kadm"
	"github.com/twmb/franz-go/pkg/kgo"

	"github.com/grafana/loki/v3/pkg/kafka"
)

// fetchMaxBytes/fetchMaxPartitionBytes 限制单次 fetch 与单分区响应大小。
const (
	fetchMaxBytes          = 100_000_000 // 100MiB
	fetchMaxPartitionBytes = 50_000_000  // 50MiB
)

// NewReaderClient 创建 Reader 专用 kgo.Client，挂载 kprom 指标前缀。
// NewReaderClient returns the kgo.Client that should be used by the Reader.
//
// The returned Client utilizes the standard set of *kprom.Metrics, prefixed with
// `MetricsPrefix`
// 创建 Reader 侧 franz-go 客户端。
func NewReaderClient(component string, kafkaCfg kafka.Config, logger log.Logger, reg prometheus.Registerer, opts ...kgo.Opt) (*kgo.Client, error) {
	metrics := NewClientMetrics(component, reg, kafkaCfg.EnableKafkaHistograms)

	clientOpts := commonKafkaClientOptions(kafkaCfg, metrics, logger)
	clientOpts = append(clientOpts,
		kgo.ClientID(kafkaCfg.ReaderConfig.ClientID),
		kgo.SeedBrokers(kafkaCfg.ReaderConfig.Address),
		kgo.FetchMinBytes(1),
		kgo.FetchMaxBytes(fetchMaxBytes),
		kgo.FetchMaxWait(5*time.Second),
		kgo.FetchMaxPartitionBytes(fetchMaxPartitionBytes),
		// BrokerMaxReadBytes sets the maximum response size that can be read from
		// Kafka. This is a safety measure to avoid OOMing on invalid responses.
		// franz-go recommendation is to set it 2x FetchMaxBytes.
		kgo.BrokerMaxReadBytes(2*fetchMaxBytes),
	)
	clientOpts = append(clientOpts, opts...)

	client, err := kgo.NewClient(clientOpts...)
	if err != nil {
		return nil, fmt.Errorf("creating kafka client: %w", err)
	}
	if kafkaCfg.AutoCreateTopicEnabled {
		setDefaultNumberOfPartitionsForAutocreatedTopics(kafkaCfg, client, logger)
	}
	return client, nil
}

// setDefaultNumberOfPartitionsForAutocreatedTopics tries to set num.partitions config option on brokers.
// This is best-effort, if setting the option fails, error is logged, but not returned.
// setDefaultNumberOfPartitionsForAutocreatedTopics 更新运行时配置或内部字段。
func setDefaultNumberOfPartitionsForAutocreatedTopics(cfg kafka.Config, cl *kgo.Client, logger log.Logger) {
	if cfg.AutoCreateTopicDefaultPartitions <= 0 {
		return
	}

	// Note: this client doesn't get closed because it is owned by the caller
	adm := kadm.NewClient(cl)

	defaultNumberOfPartitions := fmt.Sprintf("%d", cfg.AutoCreateTopicDefaultPartitions)
	_, err := adm.AlterBrokerConfigsState(context.Background(), []kadm.AlterConfig{
		{
			Op:    kadm.SetConfig,
			Name:  "num.partitions",
			Value: &defaultNumberOfPartitions,
		},
	})
	if err != nil {
		level.Error(logger).Log("msg", "failed to alter default number of partitions", "err", err)
		return
	}

	level.Info(logger).Log("msg", "configured Kafka-wide default number of partitions for auto-created topics (num.partitions)", "value", cfg.AutoCreateTopicDefaultPartitions)
}
// BrokerMaxReadBytes 设为 2×FetchMaxBytes 防止异常响应导致 OOM。
