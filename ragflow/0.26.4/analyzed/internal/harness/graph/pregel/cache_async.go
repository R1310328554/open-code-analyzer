// Package pregel 为 Pregel 提供异步缓存操作支持。
//
// AsyncMemoryCache 通过 worker 池异步处理 Get/Set/Delete，避免阻塞主执行路径。
package pregel

import (
	"context"
	"time"
)

// AsyncCache 扩展 Cache 接口，提供异步读写操作。
type AsyncCache interface {
	Cache

	// AGet 异步读取，结果通过通道返回。
	// Returns a channel that will receive the result.
	AGet(ctx context.Context, key string) <-chan CacheResult

	// ASet 异步写入，完成时 done 通道收到 nil。
	// Returns a channel that will be closed when the operation completes.
	ASet(ctx context.Context, key string, value any, ttl time.Duration) <-chan error

	// ADelete 异步删除指定键。
	// Returns a channel that will be closed when the operation completes.
	ADelete(ctx context.Context, key string) <-chan error
}

// CacheResult 异步缓存读取的结果包装。
type CacheResult struct {
	Value any
	Found bool
	Error error
}

// AsyncMemoryCache 基于 MemoryCache 的异步实现，worker 池处理操作。
type AsyncMemoryCache struct {
	*MemoryCache
	workerCh chan asyncCacheOp
	stopCh   chan struct{}
}

// asyncCacheOp 异步缓存操作请求（get/set/delete）。
type asyncCacheOp struct {
	ctx    context.Context
	opType string // "get", "set", "delete"
	key    string
	value  any
	ttl    time.Duration
	result chan<- CacheResult
	done   chan<- error
}

// NewAsyncMemoryCache 创建异步内存缓存，默认 4 个 worker。
func NewAsyncMemoryCache(maxSize int, eviction EvictionPolicy, numWorkers int) *AsyncMemoryCache {
	if numWorkers <= 0 {
		numWorkers = 4
	}

	cache := &AsyncMemoryCache{
		MemoryCache: NewMemoryCache(maxSize, eviction),
		workerCh:    make(chan asyncCacheOp, 1000),
		stopCh:      make(chan struct{}),
	}

	// Start worker goroutines
	for i := 0; i < numWorkers; i++ {
		go cache.worker()
	}

	return cache
}

// worker 消费操作队列并调用 processOp。
func (c *AsyncMemoryCache) worker() {
	for {
		select {
		case op := <-c.workerCh:
			c.processOp(op)
		case <-c.stopCh:
			return
		}
	}
}

// processOp 分发单条缓存操作到 MemoryCache。
func (c *AsyncMemoryCache) processOp(op asyncCacheOp) {
	switch op.opType {
	case "get":
		value, found := c.MemoryCache.Get(op.ctx, op.key)
		if op.result != nil {
			op.result <- CacheResult{Value: value, Found: found}
		}
	case "set":
		c.MemoryCache.Set(op.ctx, op.key, op.value, op.ttl)
		if op.done != nil {
			op.done <- nil
		}
	case "delete":
		c.MemoryCache.Delete(op.ctx, op.key)
		if op.done != nil {
			op.done <- nil
		}
	}
}

// AGet 异步读取，结果通过通道返回。
func (c *AsyncMemoryCache) AGet(ctx context.Context, key string) <-chan CacheResult {
	resultCh := make(chan CacheResult, 1)

	select {
	case c.workerCh <- asyncCacheOp{
		ctx:    ctx,
		opType: "get",
		key:    key,
		result: resultCh,
	}:
	case <-ctx.Done():
		resultCh <- CacheResult{Error: ctx.Err()}
		close(resultCh)
	}

	return resultCh
}

// ASet 异步写入，完成时 done 通道收到 nil。
func (c *AsyncMemoryCache) ASet(ctx context.Context, key string, value any, ttl time.Duration) <-chan error {
	doneCh := make(chan error, 1)

	select {
	case c.workerCh <- asyncCacheOp{
		ctx:    ctx,
		opType: "set",
		key:    key,
		value:  value,
		ttl:    ttl,
		done:   doneCh,
	}:
	case <-ctx.Done():
		doneCh <- ctx.Err()
		close(doneCh)
	}

	return doneCh
}

// ADelete 异步删除指定键。
func (c *AsyncMemoryCache) ADelete(ctx context.Context, key string) <-chan error {
	doneCh := make(chan error, 1)

	select {
	case c.workerCh <- asyncCacheOp{
		ctx:    ctx,
		opType: "delete",
		key:    key,
		done:   doneCh,
	}:
	case <-ctx.Done():
		doneCh <- ctx.Err()
		close(doneCh)
	}

	return doneCh
}

// Stop 关闭 worker 池（关闭 stopCh）。
func (c *AsyncMemoryCache) Stop() {
	close(c.stopCh)
}

// AsyncCachePolicy 异步缓存行为配置。
type AsyncCachePolicy struct {
	// KeyFunc generates the cache key.
	KeyFunc func(context.Context, any) string

	// TTL is the time-to-live for cached values.
	TTL *time.Duration

	// Async determines if operations should be async.
	Async bool
}

// AsyncCachedExecutor 包装节点函数，支持异步缓存读写。
type AsyncCachedExecutor struct {
	cache       AsyncCache
	cachePolicy *AsyncCachePolicy
}

// NewAsyncCachedExecutor 创建异步缓存执行器。
func NewAsyncCachedExecutor(cache AsyncCache, policy *AsyncCachePolicy) *AsyncCachedExecutor {
	return &AsyncCachedExecutor{
		cache:       cache,
		cachePolicy: policy,
	}
}

// Execute 异步查缓存；写入可 fire-and-forget。
func (e *AsyncCachedExecutor) Execute(
	ctx context.Context,
	nodeName string,
	input any,
	fn func(context.Context, any) (any, error),
) (any, error) {
	// Generate cache key
	var key string
	if e.cachePolicy != nil && e.cachePolicy.KeyFunc != nil {
		key = e.cachePolicy.KeyFunc(ctx, input)
	} else {
		key = GenerateCacheKey(nodeName, input)
	}

	// Check cache asynchronously
	if e.cachePolicy != nil && e.cachePolicy.Async {
		resultCh := e.cache.AGet(ctx, key)

		select {
		case result := <-resultCh:
			if result.Error != nil {
				return nil, result.Error
			}
			if result.Found {
				return result.Value, nil
			}
		case <-ctx.Done():
			return nil, ctx.Err()
		}
	} else {
		// Synchronous fallback
		if cached, ok := e.cache.Get(ctx, key); ok {
			return cached, nil
		}
	}

	// Execute function
	result, err := fn(ctx, input)
	if err != nil {
		return nil, err
	}

	// Cache result asynchronously
	var ttl time.Duration
	if e.cachePolicy != nil && e.cachePolicy.TTL != nil {
		ttl = *e.cachePolicy.TTL
	}

	if e.cachePolicy != nil && e.cachePolicy.Async {
		// Fire and forget async set
		e.cache.ASet(context.Background(), key, result, ttl)
	} else {
		e.cache.Set(ctx, key, result, ttl)
	}

	return result, nil
}

// WaitForPending 等待挂起的异步操作完成（简化实现）。
func (e *AsyncCachedExecutor) WaitForPending(timeout time.Duration) bool {
	// In a real implementation, this would track pending operations
	// For now, just sleep briefly to allow operations to complete
	time.Sleep(timeout)
	return true
}
