package util //nolint:revive

// 指纹哈希工具：将 Prometheus 时序指纹打散到 32 位，供分片锁选择与查询哈希使用。

import (
	"hash/fnv"

	"github.com/prometheus/common/model"
)

// HashFP 通过 XOR 移位将指纹高 48 位熵混入低 16 位，使相似指标也能均匀选 mutex。
// HashFP simply moves entropy from the most significant 48 bits of the
// fingerprint into the least significant 16 bits (by XORing) so that a simple
// MOD on the result can be used to pick a mutex while still making use of
// changes in more significant bits of the fingerprint. (The fast fingerprinting
// function we use is prone to only change a few bits for similar metrics. We
// really want to make use of every change in the fingerprint to vary mutex
// selection.)
func HashFP(fp model.Fingerprint) uint32 {
	return uint32(fp ^ (fp >> 32) ^ (fp >> 16))
}

// HashedQuery 对 LogQL/PromQL 查询串做 FNV-32 哈希，用于缓存键或分片路由。
// HashedQuery returns a unique hash value for the given `query`.
func HashedQuery(query string) uint32 {
	h := fnv.New32()
	_, _ = h.Write([]byte(query))
	return h.Sum32()
}
// 快速指纹算法对相近指标仅变动少量位，HashFP 刻意放大变化以改善并发锁分布。
