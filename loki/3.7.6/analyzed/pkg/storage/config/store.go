package config

// store 配置 chunk 存储层缓存：L1/L2 chunk cache、写去重 cache 及查询回写策略等参数。

import (
	"flag"
	"time"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/storage/chunk/cache"
)

type ChunkStoreConfig struct {
	ChunkCacheConfig            cache.Config  `yaml:"chunk_cache_config"`
	ChunkCacheConfigL2          cache.Config  `yaml:"chunk_cache_config_l2"`
	WriteDedupeCacheConfig      cache.Config  `yaml:"write_dedupe_cache_config" doc:"description=Write dedupe cache is deprecated along with legacy index types (aws, aws-dynamo, bigtable, bigtable-hashed, cassandra, gcp, gcp-columnkey, grpc-store).\nConsider using TSDB index which does not require a write dedupe cache."`
	SkipQueryWritebackOlderThan time.Duration `yaml:"skip_query_writeback_cache_older_than"`

	L2ChunkCacheHandoff   time.Duration  `yaml:"l2_chunk_cache_handoff"`
	CacheLookupsOlderThan model.Duration `yaml:"cache_lookups_older_than"`

	// Not visible in yaml because the setting shouldn't be common between ingesters and queriers.
	// This exists in case we don't want to cache all the chunks but still want to take advantage of
	// ingester chunk write deduplication. But for the queriers we need the full value. So when this option
	// is set, use different caches for ingesters and queriers.
	chunkCacheStubs bool // don't write the full chunk to cache, just a stub entry

	// When DisableIndexDeduplication is true and chunk is already there in cache, only index would be written to the store and not chunk.
	DisableIndexDeduplication bool `yaml:"-"`
}

// ChunkCacheStubs 返回是否仅向缓存写入 stub 条目而非完整 chunk 字节。
func (cfg *ChunkStoreConfig) ChunkCacheStubs() bool {
	return cfg.chunkCacheStubs
}

// RegisterFlags adds the flags required to configure this flag set.
// RegisterFlags 注册 store.chunks-cache.* 等命令行 flag，与 YAML 配置互补。
func (cfg *ChunkStoreConfig) RegisterFlags(f *flag.FlagSet) {
	cfg.ChunkCacheConfig.RegisterFlagsWithPrefix("store.chunks-cache.", "", f)
	cfg.ChunkCacheConfigL2.RegisterFlagsWithPrefix("store.chunks-cache-l2.", "", f)
	f.DurationVar(&cfg.L2ChunkCacheHandoff, "store.chunks-cache-l2.handoff", 0, "Chunks will be handed off to the L2 cache after this duration. 0 to disable L2 cache.")
	f.BoolVar(&cfg.chunkCacheStubs, "store.chunks-cache.cache-stubs", false, "If true, don't write the full chunk to cache, just a stub entry.")
	cfg.WriteDedupeCacheConfig.RegisterFlagsWithPrefix("store.index-cache-write.", "", f)
	f.DurationVar(&cfg.SkipQueryWritebackOlderThan, "store.skip-query-writeback-older-than", 0, "Chunks fetched from queriers before this duration will not be written to the cache. A value of 0 will write all chunks to the cache")

	f.Var(&cfg.CacheLookupsOlderThan, "store.cache-lookups-older-than", "Cache index entries older than this period. 0 to disable.")
}

func (cfg *ChunkStoreConfig) Validate() error {
	return nil
}
// SkipQueryWritebackOlderThan 跳过过旧 chunk 的查询侧写回；WriteDedupeCache 随旧索引类型弃用。
