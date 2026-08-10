package compactor

// compactor worker 模块构造 JobQueue WorkerManager：
// 启用 retention 时注册删除 job 执行器，按表 schema 路由 chunk 对象存储客户端。

import (
	"github.com/grafana/dskit/services"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/compactor/client/grpc"
	"github.com/grafana/loki/v3/pkg/compactor/deletion"
	"github.com/grafana/loki/v3/pkg/compactor/jobqueue"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/config"
)

// NewWorkerManager 创建 WorkerManager 服务，retention 开启时挂载 deletion JobRunner。
func NewWorkerManager(
	cfg Config,
	grpcClient jobqueue.CompactorClient,
	schemaConfig config.SchemaConfig,
	chunkClients map[config.DayTime]client.Client,
	r prometheus.Registerer,
) (services.Service, error) {
	wm := jobqueue.NewWorkerManager(cfg.WorkerConfig, grpcClient, r)

	if cfg.RetentionEnabled {
		deletionJobRunner := initDeletionJobRunner(cfg.JobsConfig.Deletion.ChunkProcessingConcurrency, schemaConfig, chunkClients, r)
		err := wm.RegisterJobRunner(grpc.JOB_TYPE_DELETION, deletionJobRunner)
		if err != nil {
			return nil, err
		}
	}

	return services.NewBasicService(nil, wm.Start, nil), nil
}

// initDeletionJobRunner 构造删除 job 执行器，通过 SchemaPeriodForTable 解析表对应 chunk 客户端。
func initDeletionJobRunner(
	chunkProcessingConcurrency int,
	schemaConfig config.SchemaConfig,
	chunkClients map[config.DayTime]client.Client,
	r prometheus.Registerer,
) jobqueue.JobRunner {
	return deletion.NewJobRunner(chunkProcessingConcurrency, func(table string) (client.Client, error) {
		schemaCfg, ok := SchemaPeriodForTable(schemaConfig, table)
		if !ok {
			return nil, errSchemaForTableNotFound
		}

		return chunkClients[schemaCfg.From], nil
	}, r)
}
