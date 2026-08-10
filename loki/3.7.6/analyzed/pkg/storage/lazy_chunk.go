package storage

// LazyChunk 封装尚未加载数据的 chunk 引用，在迭代时按需解码并缓存与相邻 chunk 重叠的 block。

import (
	"context"
	"errors"
	"time"

	"github.com/go-kit/log/level"

	"github.com/grafana/loki/v3/pkg/chunkenc"
	"github.com/grafana/loki/v3/pkg/iter"
	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql/log"
	"github.com/grafana/loki/v3/pkg/storage/chunk"
	"github.com/grafana/loki/v3/pkg/storage/chunk/fetcher"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

// LazyChunk 持有 chunk 元数据、有效性标志及负责拉取数据的 Fetcher。
// LazyChunk loads the chunk when it is accessed.
type LazyChunk struct {
	Chunk   chunk.Chunk
	IsValid bool
	Fetcher *fetcher.Fetcher

	// cache of overlapping block.
	// We use the offset of the block as key since it's unique per chunk.
	overlappingBlocks       map[int]iter.CacheEntryIterator
	overlappingSampleBlocks map[int]iter.CacheSampleIterator
}

// Iterator returns an entry iterator.
// The iterator returned will cache overlapping block's entries with the next chunk if passed.
// This way when we re-use them for ordering across batches we don't re-decompress the data again.
// Iterator 按时间范围与方向构建日志 entry 迭代器，重叠 block 可缓存供下一 chunk 复用。
func (c *LazyChunk) Iterator(
	ctx context.Context,
	from, through time.Time,
	direction logproto.Direction,
	pipeline log.StreamPipeline,
	nextChunk *LazyChunk,
) (iter.EntryIterator, error) {
	// If the chunk is not already loaded, then error out.
	if c.Chunk.Data == nil {
		return nil, errors.New("chunk is not loaded")
	}

	lokiChunk := c.Chunk.Data.(*chunkenc.Facade).LokiChunk()
	blocks := lokiChunk.Blocks(from, through)
	if len(blocks) == 0 {
		return iter.NoopEntryIterator, nil
	}
	its := make([]iter.EntryIterator, 0, len(blocks))

	for _, b := range blocks {
		// if we have already processed and cache block let's use it.
		if cache, ok := c.overlappingBlocks[b.Offset()]; ok {
			cache.Reset()
			its = append(its, cache)
			continue
		}
		// if the block is overlapping cache it with the next chunk boundaries.
		if nextChunk != nil && IsBlockOverlapping(b, nextChunk, direction) {
			// todo(cyriltovena) we can avoid to drop the metric name for each chunks since many chunks have the same metric/labelset.
			it := iter.NewCachedIterator(b.Iterator(ctx, pipeline), b.Entries())
			its = append(its, it)
			if c.overlappingBlocks == nil {
				c.overlappingBlocks = make(map[int]iter.CacheEntryIterator)
			}
			c.overlappingBlocks[b.Offset()] = it
			continue
		}
		if nextChunk != nil {
			if cache, ok := c.overlappingBlocks[b.Offset()]; ok {
				delete(c.overlappingBlocks, b.Offset())
				if err := cache.Wrapped().Close(); err != nil {
					level.Warn(util_log.Logger).Log(
						"msg", "failed to close cache block iterator",
						"err", err,
					)
				}
			}
		}
		// non-overlapping block with the next chunk are not cached.
		its = append(its, b.Iterator(ctx, pipeline))
	}

	if direction == logproto.FORWARD {
		return iter.NewTimeRangedIterator(
			iter.NewNonOverlappingIterator(its),
			from,
			through,
		), nil
	}
	for i, it := range its {
		r, err := iter.NewEntryReversedIter(
			iter.NewTimeRangedIterator(it,
				from,
				through,
			))
		if err != nil {
			return nil, err
		}
		its[i] = r
	}

	for i, j := 0, len(its)-1; i < j; i, j = i+1, j-1 {
		its[i], its[j] = its[j], its[i]
	}

	return iter.NewNonOverlappingIterator(its), nil
}

// SampleIterator returns an sample iterator.
// The iterator returned will cache overlapping block's entries with the next chunk if passed.
// This way when we re-use them for ordering across batches we don't re-decompress the data again.
// SampleIterator 与 Iterator 类似，为 metrics 查询构建 sample 迭代器并缓存重叠 block。
func (c *LazyChunk) SampleIterator(
	ctx context.Context,
	from, through time.Time,
	nextChunk *LazyChunk,
	extractors ...log.StreamSampleExtractor,
) (iter.SampleIterator, error) {
	// If the chunk is not already loaded, then error out.
	if c.Chunk.Data == nil {
		return nil, errors.New("chunk is not loaded")
	}

	lokiChunk := c.Chunk.Data.(*chunkenc.Facade).LokiChunk()
	blocks := lokiChunk.Blocks(from, through)
	if len(blocks) == 0 {
		return iter.NoopSampleIterator, nil
	}
	its := make([]iter.SampleIterator, 0, len(blocks))

	for _, b := range blocks {
		// if we have already processed and cache block let's use it.
		if cache, ok := c.overlappingSampleBlocks[b.Offset()]; ok {
			cache.Reset()
			its = append(its, cache)
			continue
		}
		// if the block is overlapping cache it with the next chunk boundaries.
		if nextChunk != nil && IsBlockOverlapping(b, nextChunk, logproto.FORWARD) {
			// todo(cyriltovena) we can avoid to drop the metric name for each chunks since many chunks have the same metric/labelset.
			it := iter.NewCachedSampleIterator(b.SampleIterator(ctx, extractors...), b.Entries())
			its = append(its, it)
			if c.overlappingSampleBlocks == nil {
				c.overlappingSampleBlocks = make(map[int]iter.CacheSampleIterator)
			}
			c.overlappingSampleBlocks[b.Offset()] = it
			continue
		}
		if nextChunk != nil {
			if cache, ok := c.overlappingSampleBlocks[b.Offset()]; ok {
				delete(c.overlappingSampleBlocks, b.Offset())
				if err := cache.Wrapped().Close(); err != nil {
					level.Warn(util_log.Logger).Log(
						"msg", "failed to close cache block sample iterator",
						"err", err,
					)
				}
			}
		}
		// non-overlapping block with the next chunk are not cached.
		its = append(its, b.SampleIterator(ctx, extractors...))
	}

	// build the final iterator bound to the requested time range.
	return iter.NewTimeRangedSampleIterator(
		iter.NewNonOverlappingSampleIterator(its),
		from.UnixNano(),
		through.UnixNano(),
	), nil
}

// IsBlockOverlapping 判断 block 时间边界是否与下一 LazyChunk 在查询方向上重叠。
func IsBlockOverlapping(b chunkenc.Block, with *LazyChunk, direction logproto.Direction) bool {
	if direction == logproto.BACKWARD {
		through := int64(with.Chunk.Through) * int64(time.Millisecond)
		if b.MinTime() <= through {
			return true
		}
	} else {
		from := int64(with.Chunk.From) * int64(time.Millisecond)
		if b.MaxTime() >= from {
			return true
		}
	}
	return false
}

func (c *LazyChunk) IsOverlapping(with *LazyChunk, direction logproto.Direction) bool {
	if direction == logproto.BACKWARD {
		if c.Chunk.From.Before(with.Chunk.Through) || c.Chunk.From == with.Chunk.Through {
			return true
		}
	} else {
		if !c.Chunk.Through.Before(with.Chunk.From) {
			return true
		}
	}
	return false
}

// lazyChunks 实现 sort.Interface，按查询方向对 chunk 起止时间与 fingerprint 排序。
// lazyChunks is a slice of lazy chunks that can ordered by chunk boundaries
// in ascending or descending depending on the direction
type lazyChunks struct {
	chunks    []*LazyChunk
	direction logproto.Direction
}

func (l lazyChunks) Len() int         { return len(l.chunks) }
func (l lazyChunks) Swap(i, j int)    { l.chunks[i], l.chunks[j] = l.chunks[j], l.chunks[i] }
func (l lazyChunks) Peek() *LazyChunk { return l.chunks[0] }
func (l lazyChunks) Less(i, j int) bool {
	if l.direction == logproto.FORWARD {
		t1, t2 := l.chunks[i].Chunk.From, l.chunks[j].Chunk.From
		if !t1.Equal(t2) {
			return t1.Before(t2)
		}
		return l.chunks[i].Chunk.Fingerprint < l.chunks[j].Chunk.Fingerprint
	}
	t1, t2 := l.chunks[i].Chunk.Through, l.chunks[j].Chunk.Through
	if !t1.Equal(t2) {
		return t1.After(t2)
	}
	return l.chunks[i].Chunk.Fingerprint > l.chunks[j].Chunk.Fingerprint
}

// pop returns the top `count` lazychunks, the original slice is splitted an copied
// to avoid retaining chunks in the slice backing array.
func (l *lazyChunks) pop(count int) []*LazyChunk {
	if len(l.chunks) <= count {
		old := l.chunks
		l.chunks = nil
		return old
	}
	// split slices into two new ones and copy parts to each so we don't keep old reference
	res := make([]*LazyChunk, count)
	copy(res, l.chunks[0:count])
	newChks := make([]*LazyChunk, len(l.chunks)-count)
	copy(newChks, l.chunks[count:len(l.chunks)])
	l.chunks = newChks
	return res
}
// pop 从堆顶取出 count 个 LazyChunk 并复制切片尾部，避免保留对已处理 chunk 的引用。
