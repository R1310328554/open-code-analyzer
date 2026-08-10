// orderedmap 包提供保持插入顺序的泛型有序映射，封装 wk8/go-ordered-map/v2。
// Package orderedmap provides a generic ordered map that maintains insertion order.
// It wraps github.com/wk8/go-ordered-map/v2 to encapsulate the dependency.
package orderedmap

import (
	"encoding/json"
	"iter"

	orderedmap "github.com/wk8/go-ordered-map/v2"
)

// Map 是保持插入顺序的泛型有序映射包装。
// Map is a generic ordered map that maintains insertion order.
type Map[K comparable, V any] struct {
	om *orderedmap.OrderedMap[K, V]
}

// New 创建空有序映射。
// New creates a new empty ordered map.
func New[K comparable, V any]() *Map[K, V] {
	return &Map[K, V]{
		om: orderedmap.New[K, V](),
	}
}

// Get 按键取值，nil 映射安全返回零值。
// Get retrieves a value by key.
func (m *Map[K, V]) Get(key K) (V, bool) {
	if m == nil || m.om == nil {
		var zero V
		return zero, false
	}
	return m.om.Get(key)
}

// Set 设置键值；已存在键更新值但保持迭代顺序，新键追加到末尾。
// Set sets a key-value pair. If the key already exists, its value is updated
// but its position in the iteration order is preserved. If the key is new,
// it is appended to the end.
func (m *Map[K, V]) Set(key K, value V) {
	if m == nil {
		return
	}
	if m.om == nil {
		m.om = orderedmap.New[K, V]()
	}
	m.om.Set(key, value)
}

// Len 返回条目数量。
// Len returns the number of entries.
func (m *Map[K, V]) Len() int {
	if m == nil || m.om == nil {
		return 0
	}
	return m.om.Len()
}

// All 按插入顺序迭代全部键值对。
// All returns an iterator over all key-value pairs in insertion order.
func (m *Map[K, V]) All() iter.Seq2[K, V] {
	return func(yield func(K, V) bool) {
		if m == nil || m.om == nil {
			return
		}
		for pair := m.om.Oldest(); pair != nil; pair = pair.Next() {
			if !yield(pair.Key, pair.Value) {
				return
			}
		}
	}
}

// ToMap 转为普通 map（不保留顺序）。
// ToMap converts to a regular Go map.
// Note: The resulting map does not preserve order.
func (m *Map[K, V]) ToMap() map[K]V {
	if m == nil || m.om == nil {
		return nil
	}
	result := make(map[K]V, m.om.Len())
	for pair := m.om.Oldest(); pair != nil; pair = pair.Next() {
		result[pair.Key] = pair.Value
	}
	return result
}

// MarshalJSON 序列化 JSON 并保持键顺序。
// MarshalJSON implements json.Marshaler. The JSON output preserves key order.
func (m *Map[K, V]) MarshalJSON() ([]byte, error) {
	if m == nil || m.om == nil {
		return []byte("null"), nil
	}
	return json.Marshal(m.om)
}

// UnmarshalJSON 反序列化 JSON，插入顺序与输入键序一致。
// UnmarshalJSON implements json.Unmarshaler. The insertion order matches the
// order of keys in the JSON input.
func (m *Map[K, V]) UnmarshalJSON(data []byte) error {
	m.om = orderedmap.New[K, V]()
	return json.Unmarshal(data, &m.om)
}
