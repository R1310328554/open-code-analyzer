package util //nolint:revive

// util 包 mapmerge 提供 string map 浅拷贝与 overlay 合并，不修改入参 base/overlay，返回新分配 map 供配置合并使用。

// CopyMap 对 nil 输入返回 nil，否则预分配容量后逐键拷贝字符串值。
// CopyMap makes a copy of the given map
func CopyMap(m map[string]string) map[string]string {
	var newMap = make(map[string]string, len(m))

	if m == nil {
		return nil
	}

	for k, v := range m {
		newMap[k] = v
	}

	return newMap
}

// MergeMaps 以 overlay 覆盖同键值，base 为 nil 时等价于 CopyMap(overlay)。
// MergeMaps merges the overlay map onto the base map, with overlay taking precedence
// NOTE: this treats the given base and overlay maps as immutable, and returns a copy
func MergeMaps(base map[string]string, overlay map[string]string) map[string]string {
	if base == nil {
		return CopyMap(overlay)
	}

	newMap := CopyMap(base)
	if overlay == nil {
		return newMap
	}

	for k, v := range overlay {
		newMap[k] = v
	}

	return newMap
}
// 调用方应将 base 与 overlay 视为不可变，避免共享底层 map 引发并发写问题。
