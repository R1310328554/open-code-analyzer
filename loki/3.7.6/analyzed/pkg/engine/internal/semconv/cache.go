package semconv

// IdentifierCache 按 FQN 字符串缓存 Identifier，避免重复解析与分配；非线程安全。
// IdentifierCache caches Identifier objects for a given FQN string to
// reduce memory allocations. IdentifierCache is not thread-safe.
type IdentifierCache struct {
	cache map[string]cacheRecord
}

// cacheRecord 同时缓存解析结果与错误，保证同一 FQN 重复 Parse 行为一致。
type cacheRecord struct {
	ident *Identifier
	err   error
}

// NewIdentifierCache 创建空缓存，通常在单 goroutine 计划构建阶段使用。
func NewIdentifierCache() *IdentifierCache {
	return &IdentifierCache{
		cache: make(map[string]cacheRecord),
	}
}

// ParseFQN 命中缓存直接返回，未命中则调用 ParseFQN 并写入 map。
func (c *IdentifierCache) ParseFQN(fqn string) (*Identifier, error) {
	rec, ok := c.cache[fqn]
	if !ok {
		ident, err := ParseFQN(fqn)
		rec = cacheRecord{
			ident: ident,
			err:   err,
		}
		c.cache[fqn] = rec
	}

	return rec.ident, rec.err
}
// 计划器在热路径上应复用同一 IdentifierCache 实例以降低 GC 压力。
