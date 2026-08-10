package distributor

// ShardTracker 按流哈希记录各租户上次推送的分片编号，使 distributor 在单次推送条目少于计算分片数时仍能均匀轮转分片。

import (
	"sync"
)

const (
	// defaultStripeSize is the default number of entries to allocate in the
	// stripeSeries list.
	defaultStripeSize = 1 << 10
)

// stripeLock 借鉴 ruler WAL 分条锁设计，带填充避免多锁共享同一缓存行。
// stripeLock is taken from ruler/storage/wal/series.go
type stripeLock struct {
	sync.RWMutex
	// Padding to avoid multiple locks being on the same cache line.
	_ [40]byte
}

// ShardTracker 维护 (tenant, streamHash) → 最近分片号的映射，支持跨推送轮询分片。
// ShardTracker is a data structure to keep track of the last pushed shard
// number for a given stream hash. This allows the distributor to evenly shard
// streams across pushes even when any given push has fewer entries than the
// calculated number of shards
type ShardTracker struct {
	size         int
	currentShard []map[string]int
	locks        []stripeLock
}

// NewShardTracker 初始化默认 1024 条 stripe 及每 stripe 独立 map 与锁。
func NewShardTracker() *ShardTracker {
	tracker := &ShardTracker{
		size:         defaultStripeSize,
		currentShard: make([]map[string]int, defaultStripeSize),
		locks:        make([]stripeLock, defaultStripeSize),
	}

	for i := 0; i < defaultStripeSize; i++ {
		tracker.currentShard[i] = make(map[string]int)
	}

	return tracker
}

// LastShardNum 读取指定租户与流哈希上次使用的分片编号，未设置时返回零值。
func (t *ShardTracker) LastShardNum(tenant string, streamHash uint64) int {
	i := streamHash & uint64(t.size-1)

	t.locks[i].Lock()
	defer t.locks[i].Unlock()

	return t.currentShard[i][tenant]
}

func (t *ShardTracker) SetLastShardNum(tenant string, streamHash uint64, shardNum int) {
	i := streamHash & uint64(t.size-1)

	t.locks[i].Lock()
	defer t.locks[i].Unlock()

	t.currentShard[i][tenant] = shardNum
}
// streamHash 经位与运算映射到 stripe 索引，降低全局锁争用。
