package client

// fnv 实现 FNV-1a 64 位哈希的底层原语：hashAdd/hashAddString/hashAddByte 供 compat 包计算标签指纹。

// hashAddString 逐字节 XOR 并乘以 prime64，与 Go 标准库 sum64a.Write 算法相同。
// hashAdd 对 string 逐字节应用 FNV-1a 更新规则。
// hashAdd adds a string to a fnv64a hash value, returning the updated hash.
func hashAddString(h uint64, s string) uint64 {
	for i := 0; i < len(s); i++ {
		h ^= uint64(s[i])
		h *= prime64
	}
	return h
}

// hashAddByte 将单字节纳入 FNV-1a 状态更新。
// hashAddByte adds a byte to a fnv64a hash value, returning the updated hash.
func hashAddByte(h uint64, b byte) uint64 {
	h ^= uint64(b)
	h *= prime64
	return h
}

// hashAdd adds a string to a fnv64a hash value, returning the updated hash.
// Note this is the same algorithm as Go stdlib `sum64a.Write()`
func hashAdd(h uint64, s string) uint64 {
	for i := 0; i < len(s); i++ {
		h ^= uint64(s[i])
		h *= prime64
	}
	return h
}
// FNV-1a 为无加密需求的快速非密码学哈希，适合标签指纹场景。
