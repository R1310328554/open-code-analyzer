package stores

// series_store_write 实现 ChunkWriter：PutOne 在副本 dedupe 场景下可选只写索引、跳过重复 chunk 体，并回写 chunk cache。

import (
	"context"

	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/prometheus/common/model"
	"go.opentelemetry.io/otel"

	"github.com/grafana/loki/v3/pkg/storage/chunk"
	"github.com/grafana/loki/v3/pkg/storage/chunk/fetcher"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/index"
	"github.com/grafana/loki/v3/pkg/util/constants"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
	"github.com/grafana/loki/v3/pkg/util/spanlogger"
)

var tracer = otel.Tracer("pkg/storage/stores")

var (
	DedupedChunksTotal = promauto.NewCounter(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "chunk_store_deduped_chunks_total",
		Help:      "Count of chunks which were not stored because they have already been stored by another replica.",
	})

	DedupedBytesTotal = promauto.NewCounter(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "chunk_store_deduped_bytes_total",
		Help:      "Count of bytes from chunks which were not stored because they have already been stored by another replica.",
	})

	IndexEntriesPerChunk = promauto.NewHistogram(prometheus.HistogramOpts{
		Namespace: constants.Loki,
		Name:      "chunk_store_index_entries_per_chunk",
		Help:      "Number of entries written to storage per chunk.",
		Buckets:   prometheus.ExponentialBuckets(1, 2, 5),
	})
)

// Writer 组合 schemaCfg、Fetcher 与 indexWriter，支持 DisableIndexDeduplication 强制写索引。
type Writer struct {
	schemaCfg                 config.SchemaConfig
	DisableIndexDeduplication bool

	indexWriter index.Writer
	fetcher     *fetcher.Fetcher
}

func NewChunkWriter(fetcher *fetcher.Fetcher, schemaCfg config.SchemaConfig, indexWriter index.Writer, disableIndexDeduplication bool) ChunkWriter {
	return &Writer{
		schemaCfg:                 schemaCfg,
		DisableIndexDeduplication: disableIndexDeduplication,
		fetcher:                   fetcher,
		indexWriter:               indexWriter,
	}
}

// Put 逐 chunk 调用 PutOne，任一块失败则中断整批写入。
// Put implements Store
func (c *Writer) Put(ctx context.Context, chunks []chunk.Chunk) error {
	for _, chunk := range chunks {
		if err := c.PutOne(ctx, chunk.From, chunk.Through, chunk); err != nil {
			return err
		}
	}
	return nil
}

// PutOne 若 cache 命中且无跨周期 overlap 则跳过 chunk 体；按配置决定是否仍写索引。
// PutOne implements Store
func (c *Writer) PutOne(ctx context.Context, from, through model.Time, chk chunk.Chunk) error {
	ctx, sp := tracer.Start(ctx, "SeriesStore.PutOne")
	defer sp.End()

	log := spanlogger.FromContext(ctx, util_log.Logger)
	defer log.Finish()

	var (
		writeChunk = true
		overlap    bool
	)

	// always write the chunk if it spans multiple periods to ensure that it gets added to all the stores
	if chk.From < from || chk.Through > through {
		overlap = true
	}

	// If this chunk is in cache it must already be in the database so we don't need to write it again
	found, _, _, _ := c.fetcher.Cache().Fetch(ctx, []string{c.schemaCfg.ExternalKey(chk.ChunkRef)})

	if len(found) > 0 && !overlap {
		writeChunk = false
		DedupedChunksTotal.Inc()
		encoded, err := chk.Encoded()
		if err != nil {
			level.Error(log).Log("msg", "failed to encode chunk, cannot record compressed de-duped chunk size", "err", err)
		} else {
			DedupedBytesTotal.Add(float64(len(encoded)))
		}

	}

	// If we dont have to write the chunk and DisableIndexDeduplication is false, we do not have to do anything.
	// If we dont have to write the chunk and DisableIndexDeduplication is true, we have to write index and not chunk.
	// Otherwise write both index and chunk.
	if !writeChunk && !c.DisableIndexDeduplication {
		return nil
	}

	chunks := []chunk.Chunk{chk}

	// chunk not found, write it.
	if writeChunk {
		err := c.fetcher.Client().PutChunks(ctx, chunks)
		if err != nil {
			return err
		}
	}

	if err := c.indexWriter.IndexChunk(ctx, from, through, chk); err != nil {
		return err
	}

	// write chunk to the cache if it's not found.
	if len(found) == 0 {
		if cacheErr := c.fetcher.WriteBackCache(ctx, chunks); cacheErr != nil {
			level.Warn(log).Log("msg", "could not store chunks in chunk cache", "err", cacheErr)
		}
	}

	return nil
}
// DedupedChunksTotal/DedupedBytesTotal 记录多副本重复写入被省略的 chunk 数量与压缩字节。
