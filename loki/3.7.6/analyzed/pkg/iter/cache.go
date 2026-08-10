package iter

// cache 提供可重放的缓存迭代器：首次遍历底层 EntryIterator/SampleIterator 时写入内存缓存，Close 后可通过 Reset 再次迭代而无需重用底层迭代器。

import (
	"github.com/grafana/loki/v3/pkg/logproto"
)

type CacheEntryIterator interface {
	EntryIterator
	Wrapped() EntryIterator
	Reset()
}

// cachedIterator 边迭代边缓存 entryWithLabels，底层耗尽后仅读缓存。
// cachedIterator is an iterator that caches iteration to be replayed later on.
type cachedIterator struct {
	cache   []entryWithLabels
	wrapped EntryIterator // once set to nil it means we have to use the cache.

	curr int

	closeErr error
	iterErr  error
}

// NewCachedIterator 预分配 capacity 大小的缓存切片。
// NewCachedIterator creates an iterator that cache iteration result and can be iterated again
// after closing it without re-using the underlaying iterator `it`.
func NewCachedIterator(it EntryIterator, capacity int) CacheEntryIterator {
	c := &cachedIterator{
		wrapped: it,
		cache:   make([]entryWithLabels, 0, capacity),
		curr:    -1,
	}
	return c
}

func (it *cachedIterator) Reset() {
	it.curr = -1
}

func (it *cachedIterator) Wrapped() EntryIterator {
	return it.wrapped
}

// consumeWrapped 从底层迭代器拉取下一条并追加到 cache，耗尽时捕获 closeErr/iterErr。
func (it *cachedIterator) consumeWrapped() bool {
	if it.Wrapped() == nil {
		return false
	}
	ok := it.Wrapped().Next()
	// we're done with the base iterator.
	if !ok {
		it.closeErr = it.Wrapped().Close()
		it.iterErr = it.Wrapped().Err()
		it.wrapped = nil
		return false
	}
	// we're caching entries
	it.cache = append(it.cache, entryWithLabels{Entry: it.Wrapped().At(), labels: it.Wrapped().Labels(), streamHash: it.Wrapped().StreamHash()})
	it.curr++
	return true
}

func (it *cachedIterator) Next() bool {
	if len(it.cache) == 0 && it.Wrapped() == nil {
		return false
	}
	if it.curr+1 >= len(it.cache) {
		if it.Wrapped() != nil {
			return it.consumeWrapped()
		}
		return false
	}
	it.curr++
	return true
}

func (it *cachedIterator) At() logproto.Entry {
	if len(it.cache) == 0 || it.curr < 0 || it.curr >= len(it.cache) {
		return logproto.Entry{}
	}

	return it.cache[it.curr].Entry
}

func (it *cachedIterator) Labels() string {
	if len(it.cache) == 0 || it.curr < 0 || it.curr >= len(it.cache) {
		return ""
	}
	return it.cache[it.curr].labels
}

func (it *cachedIterator) StreamHash() uint64 {
	if len(it.cache) == 0 || it.curr < 0 || it.curr >= len(it.cache) {
		return 0
	}
	return it.cache[it.curr].streamHash
}

func (it *cachedIterator) Err() error { return it.iterErr }

func (it *cachedIterator) Close() error {
	it.Reset()
	return it.closeErr
}

// CacheSampleIterator 为 SampleIterator 提供同样的缓存与重放能力。
type CacheSampleIterator interface {
	SampleIterator
	Wrapped() SampleIterator
	Reset()
}

// cachedIterator is an iterator that caches iteration to be replayed later on.
type cachedSampleIterator struct {
	cache   []sampleWithLabels
	wrapped SampleIterator

	curr int

	closeErr error
	iterErr  error
}

// NewCachedSampleIterator creates an iterator that cache iteration result and can be iterated again
// after closing it without re-using the underlaying iterator `it`.
func NewCachedSampleIterator(it SampleIterator, capacity int) CacheSampleIterator {
	c := &cachedSampleIterator{
		wrapped: it,
		cache:   make([]sampleWithLabels, 0, capacity),
		curr:    -1,
	}
	return c
}

func (it *cachedSampleIterator) Wrapped() SampleIterator {
	return it.wrapped
}

func (it *cachedSampleIterator) Reset() {
	it.curr = -1
}

func (it *cachedSampleIterator) consumeWrapped() bool {
	if it.Wrapped() == nil {
		return false
	}
	ok := it.Wrapped().Next()
	// we're done with the base iterator.
	if !ok {
		it.closeErr = it.Wrapped().Close()
		it.iterErr = it.Wrapped().Err()
		it.wrapped = nil
		return false
	}
	// we're caching entries
	it.cache = append(it.cache, sampleWithLabels{Sample: it.Wrapped().At(), labels: it.Wrapped().Labels(), streamHash: it.Wrapped().StreamHash()})
	it.curr++
	return true
}

func (it *cachedSampleIterator) Next() bool {
	if len(it.cache) == 0 && it.Wrapped() == nil {
		return false
	}
	if it.curr+1 >= len(it.cache) {
		if it.Wrapped() != nil {
			return it.consumeWrapped()
		}
		return false
	}
	it.curr++
	return true
}

func (it *cachedSampleIterator) At() logproto.Sample {
	if len(it.cache) == 0 || it.curr < 0 || it.curr >= len(it.cache) {
		return logproto.Sample{}
	}
	return it.cache[it.curr].Sample
}

func (it *cachedSampleIterator) Labels() string {
	if len(it.cache) == 0 || it.curr < 0 || it.curr >= len(it.cache) {
		return ""
	}
	return it.cache[it.curr].labels
}

func (it *cachedSampleIterator) StreamHash() uint64 {
	if len(it.cache) == 0 || it.curr < 0 || it.curr >= len(it.cache) {
		return 0
	}
	return it.cache[it.curr].streamHash
}

func (it *cachedSampleIterator) Err() error { return it.iterErr }

func (it *cachedSampleIterator) Close() error {
	it.Reset()
	return it.closeErr
}
// 缓存迭代器常用于需要多次扫描同一查询结果的场景，如分页与统计二次遍历。
