package main

// index-analyzer 命令行入口：加载 Loki 配置与 object client，构建只读 IndexShipper 后解析租户并调用 analyze 输出索引统计。

import (
	"flag"

	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/storage"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
	"github.com/grafana/loki/v3/tools/tsdb/helpers"
)

// 示例：BUCKET 为表号后缀，DIR 为本地 tsdb-active/cache 目录。
// go build ./tools/tsdb/index-analyzer && BUCKET=19453 DIR=/tmp/loki-index-analysis ./index-analyzer --config.file=/tmp/loki-config.yaml
func main() {
	conf, _, bucket, err := helpers.Setup()
	helpers.ExitErr("setting up", err)

	_, overrides, clientMetrics := helpers.DefaultConfigs()

	flag.Parse()

	periodCfg, tableRange, tableName, err := helpers.GetPeriodConfigForTableNumber(bucket, conf.SchemaConfig.Configs)
	helpers.ExitErr("find period config for bucket", err)

	objectClient, err := storage.NewObjectClient(periodCfg.ObjectType, "index-analyzer", conf.StorageConfig, clientMetrics)
	helpers.ExitErr("creating object client", err)

	shipper, err := indexshipper.NewIndexShipper(
		periodCfg.IndexTables.PathPrefix,
		conf.StorageConfig.TSDBShipperConfig,
		objectClient,
		overrides,
		nil,
		tsdb.OpenShippableTSDB,
		tableRange,
		prometheus.WrapRegistererWithPrefix("loki_tsdb_shipper_", prometheus.DefaultRegisterer),
		util_log.Logger,
	)
	helpers.ExitErr("creating index shipper", err)

	tenants, err := helpers.ResolveTenants(objectClient, periodCfg.IndexTables.PathPrefix, tableName)
	helpers.ExitErr("resolving tenants", err)

	err = analyze(shipper, tableName, tenants)
	helpers.ExitErr("analyzing", err)
}
// shipper 使用 tsdb.OpenShippableTSDB 与 tableRange 限制扫描范围。
