package discover

// assemble 模块：将 TSDB 结构发现、分类、关键词与范围元数据四路结果
// 合并为统一的 DatasetMetadata，纯函数不修改输入。

import (
	"time"

	bench "github.com/grafana/loki/v3/pkg/logql/bench"
	"github.com/grafana/loki/v3/pkg/logql/bench/discover/pkg/tsdb"
)

// AssembleMetadata 拷贝各阶段倒排索引与时间范围，生成完整数据集元数据。
// AssembleMetadata builds a DatasetMetadata from the four pipeline result types
// produced by the discover pipeline stages. The function is pure — it does not mutate
// any of its inputs.
//
// Each inverted-index map is copied directly from its source: no re-sorting,
// no re-keying. This preserves the ordering guarantees established by each
// pipeline stage.
//
// The MetadataBySelector field is copied as-is from RangeResult. Streams absent
// from ranges.MetadataBySelector are simply not present in the output map — the
// caller (RunValidation, metadata_resolver) handles missing entries gracefully.
func AssembleMetadata(
	discover *tsdb.StructuralResult,
	classify *ClassifyResult,
	keywords *KeywordResult,
	ranges *tsdb.RangeResult,
	cfg Config,
) *bench.DatasetMetadata {
	return &bench.DatasetMetadata{
		Version:              bench.MetadataVersion,
		AllSelectors:         discover.AllSelectors,
		ByServiceName:        discover.ByServiceName,
		ByFormat:             classify.ByFormat,
		ByUnwrappableField:   classify.ByUnwrappableField,
		ByDetectedField:      classify.ByDetectedField,
		ByStructuredMetadata: classify.ByStructuredMetadata,
		ByLabelKey:           classify.ByLabelKey,
		ByKeyword:            keywords.ByKeyword,
		MetadataBySelector:   ranges.MetadataBySelector,
		TimeRange: bench.TimeRange{
			Start: cfg.effectiveFrom(),
			End:   cfg.effectiveTo(),
		},
		Statistics: assembleStatistics(discover, classify),
	}
}

// assembleStatistics 统计总流数及按格式、服务名分组的流数量。
// assembleStatistics computes summary statistics from the discover and classify results.
func assembleStatistics(discover *tsdb.StructuralResult, classify *ClassifyResult) bench.DatasetStatistics {
	streamsByFormat := make(map[bench.LogFormat]int)
	for fmt, sels := range classify.ByFormat {
		streamsByFormat[fmt] = len(sels)
	}

	streamsByService := make(map[string]int)
	for svc, sels := range discover.ByServiceName {
		streamsByService[svc] = len(sels)
	}

	return bench.DatasetStatistics{
		Generated:        time.Now(),
		TotalStreams:     len(discover.AllSelectors),
		StreamsByFormat:  streamsByFormat,
		StreamsByService: streamsByService,
	}
}
// MetadataBySelector 直接来自 RangeResult，缺失条目的流由校验层容错处理。
