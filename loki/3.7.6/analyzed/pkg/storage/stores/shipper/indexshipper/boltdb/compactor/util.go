package compactor

// compactor util 提供 BoltDB 索引压缩测试辅助：构造带时间序列的 chunk 样本，并从表名解析该表覆盖的 chunk 索引时间区间。

import (
	"strconv"
	"testing"
	"time"
	"unsafe"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"
	"github.com/stretchr/testify/require"

	"github.com/grafana/loki/v3/pkg/chunkenc"
	"github.com/grafana/loki/v3/pkg/compression"
	ingesterclient "github.com/grafana/loki/v3/pkg/ingester/client"
	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/storage/chunk"
)

// unsafeGetString 零拷贝将 []byte 视为 string，调用方须保证底层字节不被修改。
// unsafeGetString is like yolostring but with a meaningful name
func unsafeGetString(buf []byte) string {
	return *((*string)(unsafe.Pointer(&buf))) // #nosec G103 -- we know the string is not mutated -- nosemgrep: use-of-unsafe-block
}

// createChunk 按分钟步进写入日志行并编码 MemChunk，供压缩器单元测试构造索引条目。
func createChunk(t testing.TB, chunkFormat byte, headBlockFmt chunkenc.HeadBlockFmt, userID string, lbs labels.Labels, from model.Time, through model.Time) chunk.Chunk {
	t.Helper()
	const (
		targetSize = 1500 * 1024
		blockSize  = 256 * 1024
	)
	labelsBuilder := labels.NewBuilder(lbs)
	labelsBuilder.Set(model.MetricNameLabel, "logs")
	metric := labelsBuilder.Labels()
	fp := ingesterclient.Fingerprint(lbs)
	chunkEnc := chunkenc.NewMemChunk(chunkFormat, compression.Snappy, headBlockFmt, blockSize, targetSize)

	for ts := from; !ts.After(through); ts = ts.Add(1 * time.Minute) {
		dup, err := chunkEnc.Append(&logproto.Entry{
			Timestamp: ts.Time(),
			Line:      ts.String(),
		})
		require.False(t, dup)
		require.NoError(t, err)
	}

	require.NoError(t, chunkEnc.Close())
	c := chunk.NewChunk(userID, fp, metric, chunkenc.NewFacade(chunkEnc, blockSize, targetSize), from, through)
	require.NoError(t, c.Encode())
	return c
}

// ExtractIntervalFromTableName 从表名末五位数字推导 UTC 日界区间，End 减一毫秒避免跨表重叠。
// ExtractIntervalFromTableName gives back the time interval for which the table is expected to hold the chunks index.
func ExtractIntervalFromTableName(tableName string) model.Interval {
	interval := model.Interval{
		Start: 0,
		End:   model.Now(),
	}
	tableNumber, err := strconv.ParseInt(tableName[len(tableName)-5:], 10, 64)
	if err != nil {
		return interval
	}

	interval.Start = model.TimeFromUnix(tableNumber * 86400)
	// subtract a millisecond here so that interval only covers a single table since adding 24 hours ends up covering the start time of next table as well.
	interval.End = interval.Start.Add(24*time.Hour) - 1
	return interval
}
// 表名解析失败时返回零起点至当前时间的兜底区间，便于测试与容错路径继续执行。
