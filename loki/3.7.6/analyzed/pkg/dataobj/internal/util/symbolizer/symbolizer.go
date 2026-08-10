// symbolizer 字符串驻留：相同内容返回同一实例，降低重复标签等场景的内存。
// Package symbolizer provides a string interning mechanism to reduce memory usage
// by reusing identical strings.
//
// The Symbolizer maintains a cache of strings and returns the same instance
// when the same string is requested multiple times. This reduces memory usage
// when dealing with repeated strings, such as label names or values. It is not
// thread safe.
//
// When the cache exceeds the maximum size, a small percentage of entries are
// randomly discarded to keep memory usage under control.
package symbolizer

import (
	"strings"
)

// New 指定 map 初始容量与 maxSize 上限，非线程安全。
// New creates a new Symbolizer with the given initial capacity and maximum size.
func New(initialCapacity int, maxSize int) *Symbolizer {
	return &Symbolizer{
		symbols: make(map[string]string, initialCapacity),
		maxSize: maxSize,
	}
}

// Symbolizer 用 map 缓存已克隆字符串，超限时随机丢弃约百分之一条目。
type Symbolizer struct {
	symbols map[string]string
	maxSize int
}

// Get 命中缓存直接返回；未命中则 Clone 后插入，必要时随机驱逐。
// Get returns a string from the symbolizer. If the string is not in the cache,
// a clone is inserted into the cache and returned.
//
// Get may delete some values from the cache prior to inserting a new value if
// the maximum size is exceeded.
func (s *Symbolizer) Get(name string) string {
	if value, ok := s.symbols[name]; ok {
		return value
	}
	// Control maximum memory use by discarding a random 1% of symbols if the map gets too big.
	// We rely on Golang's unspecified map ordering to choose what to discard.
	if len(s.symbols) > s.maxSize {
		i := 0
		for k := range s.symbols {
			if i > s.maxSize/100 {
				break
			}
			delete(s.symbols, k)
			i++
		}
	}
	newString := strings.Clone(name)
	s.symbols[newString] = newString
	return newString
}

// Reset 清空 symbols 映射，maxSize 配置保持不变。
// Reset clears the cache and resets the Symbolizer to its initial state,
// maintaining the existing maxSize.
func (s *Symbolizer) Reset() {
	clear(s.symbols)
}
// 依赖 map 迭代顺序未定义以实现近似随机驱逐，控制峰值内存。
