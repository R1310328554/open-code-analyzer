package main

// index-analyzer analytics：遍历 TSDB 索引文件统计 series/chunk 数量、每 series 最大 chunk 数及超过 1000 chunk 的 series 计数。

import (
	"context"
	"fmt"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/index"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb"
	tsdb_index "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
)

// analyze 对每个租户 ForEach 索引，跳过 multi-tenant 文件并聚合全局指标。
func analyze(indexShipper indexshipper.IndexShipper, tableName string, tenants []string) error {

	var (
		series             int
		chunks             int
		seriesRes          []tsdb.Series
		chunkRes           []logproto.ChunkRefWithSizingInfo
		maxChunksPerSeries int
		seriesOver1kChunks int
	)
	for _, tenant := range tenants {
		fmt.Printf("analyzing tenant %s\n", tenant)
		err := indexShipper.ForEach(
			context.Background(),
			tableName,
			tenant,
			index.ForEachIndexCallback(func(isMultiTenantIndex bool, idx index.Index) error {
				if isMultiTenantIndex {
					return nil
				}

				casted := idx.(*tsdb.TSDBFile)
				seriesRes = seriesRes[:0]
				chunkRes = chunkRes[:0]

				res, err := casted.Series(
					context.Background(),
					tenant,
					model.Earliest,
					model.Latest,
					seriesRes, nil,
					labels.MustNewMatcher(labels.MatchEqual, "", ""),
				)

				if err != nil {
					return err
				}

				series += len(res)

				chunkRes, err := casted.GetChunkRefs(
					context.Background(),
					tenant,
					model.Earliest,
					model.Latest,
					chunkRes, nil,
					labels.MustNewMatcher(labels.MatchEqual, "", ""),
				)

				if err != nil {
					return err
				}

				chunks += len(chunkRes)

				// ForSeries 回调统计单 series chunk 分布，用于发现热点 stream 与 compaction 压力。
err = casted.Index.(*tsdb.TSDBIndex).ForSeries(
					context.Background(),
					"", nil,
					model.Earliest,
					model.Latest,
					func(_ labels.Labels, _ model.Fingerprint, chks []tsdb_index.ChunkMeta) (stop bool) {
						if len(chks) > maxChunksPerSeries {
							maxChunksPerSeries = len(chks)
							if len(chks) > 1000 {
								seriesOver1kChunks++
							}
						}
						return false
					},
					labels.MustNewMatcher(labels.MatchEqual, "", ""),
				)

				if err != nil {
					return err
				}

				return nil
			}),
		)

		if err != nil {
			return err
		}
	}

	fmt.Printf("analyzed %d series and %d chunks for an average of %f chunks per series. max chunks/series was %d. number of series with over 1k chunks: %d\n", series, chunks, float64(chunks)/float64(series), maxChunksPerSeries, seriesOver1kChunks)

	return nil
}
// 最终 stdout 打印 series/chunk 总数、均值、max chunks/series 与超 1k chunk 的 series 数。
