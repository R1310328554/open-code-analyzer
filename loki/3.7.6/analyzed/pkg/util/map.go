package util //nolint:revive

// util 包 GenerationalMap 实现双代换入式有界 map：newgen 满时整体晋升为 oldgen 并清空，适合近似 LRU 的轻量缓存。

type GenerationalMap[K comparable, V any] struct {
	oldgen map[K]V
	newgen map[K]V

	maxSize int
	newV    func() V
	gcCb    func()
}

// NewGenMap 接受 maxSize、值工厂 newV 与代际切换时的 gcCb 回调。
// NewGenMap created which maintains at most maxSize recently used entries
func NewGenMap[K comparable, V any](maxSize int, newV func() V, gcCb func()) GenerationalMap[K, V] {
	return GenerationalMap[K, V]{
		newgen:  make(map[K]V),
		maxSize: maxSize,
		newV:    newV,
		gcCb:    gcCb,
	}
}

// GetOrCreate 优先命中 newgen，其次 oldgen，均 miss 时调用 newV 创建并写入 newgen。
func (m *GenerationalMap[K, T]) GetOrCreate(key K) T {
	v, ok := m.newgen[key]
	if !ok {
		if v, ok = m.oldgen[key]; !ok {
			v = m.newV()
		}
		m.newgen[key] = v

		if len(m.newgen) == m.maxSize {
			m.oldgen = m.newgen
			m.newgen = make(map[K]T)
			if m.gcCb != nil {
				m.gcCb()
			}
		}
	}
	return v
}
// newgen 达到 maxSize 时触发代际翻转与可选 gcCb，旧代条目仍可被单次访问命中。
