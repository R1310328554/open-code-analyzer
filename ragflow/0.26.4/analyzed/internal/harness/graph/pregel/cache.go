// Package pregel 为 Pregel 节点执行提供结果缓存能力。
//
// 支持内存缓存（LRU/LFU/Random 淘汰）、缓存键生成与 CachedExecutor 包装。
package pregel

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"sync"
	"time"

	"ragflow/internal/harness/graph/types"
)

// Cache 节点输出缓存接口。
type Cache interface {
	// Get 读取缓存；过期条目视为未命中。
	Get(ctx context.Context, key string) (any, bool)
	// Set 写入缓存；满时按策略淘汰一条。
	Set(ctx context.Context, key string, value any, ttl time.Duration)
	// Delete 删除指定键。
	Delete(ctx context.Context, key string)
	// Clear 清空全部缓存条目。
	Clear()
}

// MemoryCache 线程安全的内存缓存实现。
type MemoryCache struct {
	mu       sync.RWMutex
	data     map[string]*cacheEntry
	maxSize  int
	eviction EvictionPolicy
}

type cacheEntry struct {
	value      any
	expiration time.Time
	lastAccess time.Time
	hits       int64
}

// EvictionPolicy 缓存满时的淘汰策略。
type EvictionPolicy int

const (
	// EvictLRU 淘汰最近最少使用的条目。
	EvictLRU EvictionPolicy = iota
	// EvictLFU 淘汰访问频率最低的条目。
	EvictLFU
	// EvictRandom 随机淘汰条目。
	EvictRandom
)

// NewMemoryCache 创建内存缓存，指定容量与淘汰策略。
func NewMemoryCache(maxSize int, eviction EvictionPolicy) *MemoryCache {
	return &MemoryCache{
		data:     make(map[string]*cacheEntry),
		maxSize:  maxSize,
		eviction: eviction,
	}
}

// Get 读取缓存；过期条目视为未命中。
func (c *MemoryCache) Get(ctx context.Context, key string) (any, bool) {
	c.mu.Lock()
	defer c.mu.Unlock()

	entry, ok := c.data[key]
	if !ok {
		return nil, false
	}

	// Check expiration
	if !entry.expiration.IsZero() && time.Now().After(entry.expiration) {
		return nil, false
	}

	entry.hits++
	entry.lastAccess = time.Now()
	return entry.value, true
}

// Set 写入缓存；满时按策略淘汰一条。
func (c *MemoryCache) Set(ctx context.Context, key string, value any, ttl time.Duration) {
	c.mu.Lock()
	defer c.mu.Unlock()

	// Evict entries if cache is full
	if len(c.data) >= c.maxSize {
		c.evict()
	}

	expiration := time.Time{}
	if ttl > 0 {
		expiration = time.Now().Add(ttl)
	}

	c.data[key] = &cacheEntry{
		value:      value,
		expiration: expiration,
		lastAccess: time.Now(),
		hits:       0,
	}
}

// Delete 删除指定键。
func (c *MemoryCache) Delete(ctx context.Context, key string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	delete(c.data, key)
}

// Clear 清空全部缓存条目。
func (c *MemoryCache) Clear() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.data = make(map[string]*cacheEntry)
}

// evict 按策略淘汰一条缓存条目。
func (c *MemoryCache) evict() {
	if len(c.data) == 0 {
		return
	}

	var keyToDelete string

	switch c.eviction {
	case EvictLRU:
		// Find least recently used entry
		var oldest time.Time
		for k, v := range c.data {
			if oldest.IsZero() || v.lastAccess.Before(oldest) {
				oldest = v.lastAccess
				keyToDelete = k
			}
		}
	case EvictLFU:
		// Find least frequently used
		var minHits int64 = -1
		for k, v := range c.data {
			if minHits == -1 || v.hits < minHits {
				minHits = v.hits
				keyToDelete = k
			}
		}
	case EvictRandom:
		// Delete first entry (Go map iteration is randomized)
		for k := range c.data {
			keyToDelete = k
			break
		}
	}

	if keyToDelete != "" {
		delete(c.data, keyToDelete)
	}
}

// GenerateCacheKey 由节点名与输入 JSON 的 SHA-256 生成缓存键。
func GenerateCacheKey(nodeName string, input any) string {
	data, err := json.Marshal(input)
	if err != nil {
		// Fall back to the type name so different inputs still produce different keys.
		return fmt.Sprintf("%s:%T", nodeName, input)
	}
	hash := sha256.Sum256(data)
	return fmt.Sprintf("%s:%s", nodeName, hex.EncodeToString(hash[:]))
}

// CachedExecutor 包装节点函数，命中缓存时跳过实际执行。
type CachedExecutor struct {
	cache       Cache
	cachePolicy *types.CachePolicy
}

// NewCachedExecutor 创建带缓存的执行器。
func NewCachedExecutor(cache Cache, policy *types.CachePolicy) *CachedExecutor {
	return &CachedExecutor{
		cache:       cache,
		cachePolicy: policy,
	}
}

// Execute 先查缓存，未命中则执行 fn 并写入缓存。
func (e *CachedExecutor) Execute(ctx context.Context, nodeName string, input any, fn func(context.Context, any) (any, error)) (any, error) {
	// Generate cache key
	var key string
	if e.cachePolicy != nil && e.cachePolicy.KeyFunc != nil {
		key = e.cachePolicy.KeyFunc(ctx, input)
	} else {
		key = GenerateCacheKey(nodeName, input)
	}

	// Check cache
	if cached, ok := e.cache.Get(ctx, key); ok {
		return cached, nil
	}

	// Execute function
	result, err := fn(ctx, input)
	if err != nil {
		return nil, err
	}

	// Cache result
	var ttl time.Duration
	if e.cachePolicy != nil && e.cachePolicy.TTL != nil {
		ttl = *e.cachePolicy.TTL
	}
	e.cache.Set(ctx, key, result, ttl)

	return result, nil
}

// NoopCache 空实现缓存，始终未命中（默认禁用缓存）。
type NoopCache struct{}

// Get 始终返回未命中。
func (n *NoopCache) Get(ctx context.Context, key string) (any, bool) {
	return nil, false
}

// Set 空操作。
func (n *NoopCache) Set(ctx context.Context, key string, value any, ttl time.Duration) {}

// Delete 空操作。
func (n *NoopCache) Delete(ctx context.Context, key string) {}

// Clear 空操作。
func (n *NoopCache) Clear() {}
