package tsdb

// pool 在 tsdb 包层复用查询中间切片：SeriesPool 与 ChunkRefsPool 供 MultiIndex 合并，ChunkMetasPool 重新导出 index 包同名池。

import (
	"sync"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
)

// 包级 Pool 变量在 MultiIndex.GetChunkRefs/Series 合并路径上减少临时切片分配。
var (
	ChunkMetasPool = &index.ChunkMetasPool // re-exporting
	SeriesPool     PoolSeries
	ChunkRefsPool  PoolChunkRefs
)

// PoolSeries 缓存 []Series，Get 默认 cap 1024，Put 截断后归还 sync.Pool。
type PoolSeries struct {
	pool sync.Pool
}

func (p *PoolSeries) Get() []Series {
	if xs := p.pool.Get(); xs != nil {
		return xs.([]Series)
	}
	return make([]Series, 0, 1<<10)
}

func (p *PoolSeries) Put(xs []Series) {
	xs = xs[:0]
	//nolint:staticcheck
	p.pool.Put(xs)
}

// PoolChunkRefs 复用 []logproto.ChunkRefWithSizingInfo，合并完成后 Put 回池。
type PoolChunkRefs struct {
	pool sync.Pool
}

func (p *PoolChunkRefs) Get() []logproto.ChunkRefWithSizingInfo {
	if xs := p.pool.Get(); xs != nil {
		return xs.([]logproto.ChunkRefWithSizingInfo)
	}
	return make([]logproto.ChunkRefWithSizingInfo, 0, 1<<10)
}

func (p *PoolChunkRefs) Put(xs []logproto.ChunkRefWithSizingInfo) {
	xs = xs[:0]
	//nolint:staticcheck
	p.pool.Put(xs)
}
// MultiIndex 合并失败或空结果时不泄漏已 Get 的切片，成功路径在 merge 内 Put 各组。
