package frontend

// frontend 内部 TTL 缓存：泛型 cache 接口、带过期时间的 ttlcache 与测试用 nopcache。

import (
	"sync"
	"time"
)

// cache 抽象 Get/Set/Delete/Reset，供 assignedPartitions 等场景复用。
type cache[K comparable, V any] interface {
	// get returns the value for the key. It returns true if the key exists,
	// otherwise false.
	Get(K) (V, bool)
	// set stores the value for the key.
	Set(K, V)
	// delete removes the key. If the key does not exist, the operation is a
	// no-op.
	Delete(K)
	// reset removes all keys.
	Reset()
}

// item contains the value and expiration time for a key.
// item 保存缓存值与 expiresAt，过期判断采用 Before/Equal 边界。
type item[V any] struct {
	value     V
	expiresAt time.Time
}

func (i *item[V]) hasExpired(now time.Time) bool {
	return i.expiresAt.Before(now) || i.expiresAt.Equal(now)
}

// ttlcache 全表共用单一 TTL，Set 时惰性触发过期条目扫描。
// ttlcache is a simple, thread-safe cache with a single per-cache TTL.
type ttlcache[K comparable, V any] struct {
	items         map[K]item[V]
	ttl           time.Duration
	lastEvictedAt time.Time
	mu            sync.RWMutex
}

func newTTLCache[K comparable, V any](ttl time.Duration) *ttlcache[K, V] {
	return &ttlcache[K, V]{
		items: make(map[K]item[V]),
		ttl:   ttl,
	}
}

// Get implements the [cache] interface.
// Get 读锁下返回未过期条目；过期键视为 miss。
func (c *ttlcache[K, V]) Get(key K) (V, bool) {
	var (
		value  V
		exists bool
		now    = time.Now()
	)
	c.mu.RLock()
	defer c.mu.RUnlock()
	if item, ok := c.items[key]; ok && !item.hasExpired(now) {
		value = item.value
		exists = true
	}
	return value, exists
}

// Set implements the [cache] interface.
func (c *ttlcache[K, V]) Set(key K, value V) {
	now := time.Now()
	c.mu.Lock()
	defer c.mu.Unlock()
	c.items[key] = item[V]{
		value:     value,
		expiresAt: now.Add(c.ttl),
	}
// 距上次驱逐超过 TTL/2 时在写锁内批量清理过期键，摊销扫描成本。
	if now.Sub(c.lastEvictedAt) > c.ttl/2 {
		c.lastEvictedAt = now
		c.evictExpired(now)
	}
}

// Delete implements the [cache] interface.
func (c *ttlcache[K, V]) Delete(key K) {
	c.mu.Lock()
	defer c.mu.Unlock()
	delete(c.items, key)
}

// Reset implements the [cache] interface.
func (c *ttlcache[K, V]) Reset() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.items = make(map[K]item[V])
}

// evictExpired 遍历 map 删除已过期 item，调用方需持有写锁。
// evictExpired evicts expired items.
func (c *ttlcache[K, V]) evictExpired(now time.Time) {
	for key, item := range c.items {
		if item.hasExpired(now) {
			delete(c.items, key)
		}
	}
}

// nopcache 永不命中，用于禁用缓存或单测 stub。
// nopcache is a no-op cache. It does not store any keys. It is used in tests
// and as a stub for disabled caches.
type nopcache[K comparable, V any] struct{}

func newNopCache[K comparable, V any]() *nopcache[K, V] {
	return &nopcache[K, V]{}
}
func (c *nopcache[K, V]) Get(_ K) (V, bool) {
	var value V
	return value, false
}
func (c *nopcache[K, V]) Set(_ K, _ V) {}
func (c *nopcache[K, V]) Delete(_ K)   {}
func (c *nopcache[K, V]) Reset()       {}
// assignedPartitionsCache 缓存各 limits 实例的分区分配，减少重复 gRPC 查询。
