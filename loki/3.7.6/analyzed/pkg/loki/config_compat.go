package loki

// config_compat 校验 Loki 全局配置与索引分片、聚合分片等特性之间的兼容性，在启动前捕获会导致查询或写入异常的组合。

import (
	"errors"
	"fmt"

	"github.com/grafana/loki/v3/pkg/ingester/index"
	frontend "github.com/grafana/loki/v3/pkg/lokifrontend/frontend/v2"
	"github.com/grafana/loki/v3/pkg/storage/types"
)

func ValidateConfigCompatibility(c Config) []error {
	var errs []error
	for _, fn := range []func(Config) error{
		ensureInvertedIndexShardingCompatibility,
		ensureProtobufEncodingForAggregationSharding,
	} {
		if err := fn(c); err != nil {
			errs = append(errs, err)
		}
	}
	return errs
}

// ensureInvertedIndexShardingCompatibility 确保 Ingester 索引分片因子与各 schema 周期 row_shards 可整除。
func ensureInvertedIndexShardingCompatibility(c Config) error {

	for i, sc := range c.SchemaConfig.Configs {
		switch sc.IndexType {
		case types.TSDBType:
			if err := index.ValidateBitPrefixShardFactor(uint32(c.Ingester.IndexShards)); err != nil {
				return err
			}
		default:
			if sc.RowShards > 0 && c.Ingester.IndexShards%int(sc.RowShards) > 0 {
				return fmt.Errorf(
					"incompatible ingester index shards (%d) and period config row shard factor (%d) for period config at index (%d). The ingester factor must be evenly divisible by all period config factors",
					c.Ingester.IndexShards,
					sc.RowShards,
					i,
				)
			}
		}

	}
	return nil
}

// ensureProtobufEncodingForAggregationSharding 要求启用 shard_aggregation 时 Frontend V2 使用 protobuf 编码。
func ensureProtobufEncodingForAggregationSharding(c Config) error {
	if len(c.QueryRange.ShardAggregations) > 0 && c.Frontend.FrontendV2.Encoding != frontend.EncodingProtobuf {
		return errors.New("shard_aggregation requires frontend.encoding=protobuf")
	}
	return nil
}
// TSDB 索引使用 bit-prefix 分片校验；其他索引类型则检查 row_shards 与 IndexShards 的整除关系。
