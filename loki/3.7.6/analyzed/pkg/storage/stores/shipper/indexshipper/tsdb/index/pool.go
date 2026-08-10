package index

// pool 为索引读写路径提供 ChunkMeta 与 chunkPageMarker 切片对象池，减少 Stats/Series 解码时的堆分配。

import (
	"sync"

	"github.com/prometheus/prometheus/util/pool"
)

var (
	ChunkMetasPool       PoolChunkMetas
	chunkPageMarkersPool = poolChunkPageMarkers{
		// pools of lengths 64->1024
		pool: pool.New(64, 1024, 2, func(sz int) interface{} {
			return make(chunkPageMarkers, 0, sz)
		}),
	}
)

// PoolChunkMetas 用 sync.Pool 复用 []ChunkMeta，Get 默认 cap 1024。
type PoolChunkMetas struct {
	pool sync.Pool
}

func (p *PoolChunkMetas) Get() []ChunkMeta {
	if xs := p.pool.Get(); xs != nil {
		return xs.([]ChunkMeta)
	}
	return make([]ChunkMeta, 0, 1<<10)
}

func (p *PoolChunkMetas) Put(xs []ChunkMeta) {
	xs = xs[:0]
	//nolint:staticcheck
	p.pool.Put(xs)
}

// poolChunkPageMarkers 按容量分档（64-1024）复用 marker 切片供 V3 页扫描。
type poolChunkPageMarkers struct {
	pool *pool.Pool
}

func (p *poolChunkPageMarkers) Get(sz int) chunkPageMarkers {
	return p.pool.Get(sz).(chunkPageMarkers)
}

func (p *poolChunkPageMarkers) Put(xs chunkPageMarkers) {
	xs = xs[:0]
	//nolint:staticcheck
	p.pool.Put(xs)
}
// Put 时将切片截断为 len=0 再归还池，避免持有大底层数组引用。
