// 泛型线程安全 map：读写锁保护的 Load/Store/Items。
package syncmap

import (
	"maps"
	"sync"
)

// SyncMap 为简单的泛型线程安全 map。
// SyncMap is a simple, generic thread-safe map implementation.
type SyncMap[K comparable, V any] struct {
	mu sync.RWMutex
	m  map[K]V
}

// NewSyncMap 构造空 SyncMap。
func NewSyncMap[K comparable, V any]() *SyncMap[K, V] {
	return &SyncMap[K, V]{
		m: make(map[K]V),
	}
}

// Load 读锁下获取键值。
func (s *SyncMap[K, V]) Load(key K) (V, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	val, ok := s.m[key]
	return val, ok
}

// Store 写锁下设置键值。
func (s *SyncMap[K, V]) Store(key K, value V) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.m[key] = value
}

// Items 返回 map 的浅拷贝快照。
func (s *SyncMap[K, V]) Items() map[K]V {
	s.mu.RLock()
	defer s.mu.RUnlock()
	// 浅拷贝 map 条目。
	// shallow copy map items
	return maps.Clone(s.m)
}
