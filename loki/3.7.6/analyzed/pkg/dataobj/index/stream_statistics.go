package index

// index 包内 stream 统计计算：遍历 logs 批次，将每条日志行写入 stream 统计 builder，供索引侧聚合使用。

import (
	"context"
	"fmt"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/logs"
)

type streamStatisticsCalculation struct{}

func (c *streamStatisticsCalculation) Prepare(_ context.Context, _ *dataobj.Section, _ logs.Stats) error {
	return nil
}

// ProcessBatch 逐条调用 builder.ObserveLogLine 记录租户、流 ID 与行长度。
func (c *streamStatisticsCalculation) ProcessBatch(_ context.Context, context *logsCalculationContext, batch []logs.Record) error {
	for _, log := range batch {
		err := context.builder.ObserveLogLine(context.tenantID, context.objectPath, context.sectionIdx, log.StreamID, context.streamIDLookup[log.StreamID], log.Timestamp, int64(len(log.Line)))
		if err != nil {
			return fmt.Errorf("failed to observe log line: %w", err)
		}
	}
	return nil
}

func (c *streamStatisticsCalculation) Flush(_ context.Context, _ *logsCalculationContext) error {
	return nil
}
// Flush 与 Prepare 均为空操作，统计在 ProcessBatch 阶段即时写入。
