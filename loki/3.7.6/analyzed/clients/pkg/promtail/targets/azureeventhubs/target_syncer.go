package azureeventhubs

// Azure Event Hubs target 同步器工厂：Sarama 客户端 + ConsumerGroup，复用 kafka.TargetSyncer。
// 连接串经 SASL PLAIN 认证，pipeline stages 在推送 Loki 前处理每条 Entry。

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/IBM/sarama"
	"github.com/go-kit/log"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/clients/pkg/logentry/stages"
	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/kafka"
)

// 校验 azure_event_hubs 配置，创建 Kafka client/group 与 messageParser 并启动 syncer。
func NewSyncer(
	reg prometheus.Registerer,
	logger log.Logger,
	cfg scrapeconfig.Config,
	pushClient api.EntryHandler,
) (*kafka.TargetSyncer, error) {
	if err := validateConfig(&cfg); err != nil {
		return nil, err
	}
	config := getConfig(cfg.AzureEventHubsConfig.ConnectionString)

	client, err := sarama.NewClient([]string{cfg.AzureEventHubsConfig.FullyQualifiedNamespace}, config)
	if err != nil {
		return nil, fmt.Errorf("error creating kafka client: %w", err)
	}
	group, err := sarama.NewConsumerGroup([]string{cfg.AzureEventHubsConfig.FullyQualifiedNamespace}, cfg.AzureEventHubsConfig.GroupID, config)
	if err != nil {
		return nil, fmt.Errorf("error creating consumer group client: %w", err)
	}
	pipeline, err := stages.NewPipeline(log.With(logger, "component", "azure_event_hubs_pipeline"), cfg.PipelineStages, &cfg.JobName, reg)
	if err != nil {
		return nil, fmt.Errorf("error creating pipeline: %w", err)
	}

	targetSyncConfig := &kafka.TargetSyncerConfig{
		RelabelConfigs:       cfg.RelabelConfigs,
		UseIncomingTimestamp: cfg.AzureEventHubsConfig.UseIncomingTimestamp,
		Labels:               cfg.AzureEventHubsConfig.Labels,
		GroupID:              cfg.AzureEventHubsConfig.GroupID,
	}

	t, err := kafka.NewSyncer(context.Background(), reg, logger, pushClient, pipeline, group, client, &messageParser{
		disallowCustomMessages: cfg.AzureEventHubsConfig.DisallowCustomMessages,
	}, cfg.AzureEventHubsConfig.EventHubs, targetSyncConfig)
	if err != nil {
		return nil, fmt.Errorf("error starting azure_event_hubs target: %w", err)
	}

	return t, nil
}

// 必填 fully_qualified_namespace 与 event_hubs；默认 GroupID 为 promtail。
func validateConfig(cfg *scrapeconfig.Config) error {
	if cfg.AzureEventHubsConfig == nil {
		return errors.New("azure_event_hubs configuration is empty")
	}

	if len(cfg.AzureEventHubsConfig.FullyQualifiedNamespace) == 0 {
		return errors.New("no fully_qualified_namespace defined")
	}

	if len(cfg.AzureEventHubsConfig.EventHubs) == 0 {
		return errors.New("no event_hubs defined")
	}

	if cfg.AzureEventHubsConfig.GroupID == "" {
		cfg.AzureEventHubsConfig.GroupID = "promtail"
	}
	return nil
}

// Event Hubs Kafka 协议：SASL 用户 $ConnectionString、TLS 与 V1_0_0_0 版本。
func getConfig(connection string) *sarama.Config {
	config := sarama.NewConfig()
	config.Net.DialTimeout = 30 * time.Second
	config.Net.SASL.Enable = true
	config.Net.SASL.User = "$ConnectionString"
	config.Net.SASL.Password = connection
	config.Net.SASL.Mechanism = sarama.SASLTypePlaintext

	config.Net.TLS.Enable = true
	config.Version = sarama.V1_0_0_0

	return config
}
