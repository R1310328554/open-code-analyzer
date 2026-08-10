package ring

// util/ring 包提供 Loki 哈希环辅助：FNV-32 生成 token、判断本实例地址是否落在指定 key 的复制集内。

import (
	"hash/fnv"

	"github.com/grafana/dskit/ring"

	"github.com/grafana/loki/v3/pkg/util"
)

// TokenFor 对 userID 与 labels 串联做 FNV-32，供 ingester 环分片定位。
// TokenFor generates a token used for finding ingesters from ring
func TokenFor(userID, labels string) uint32 {
	h := fnv.New32()
	_, _ = h.Write([]byte(userID))
	_, _ = h.Write([]byte(labels))
	return h.Sum32()
}

// IsInReplicationSet 以 ring.Write 取复制集并检查 address 是否在成员列表中。
// IsInReplicationSet will query the provided ring for the provided key
// and see if the provided address is in the resulting ReplicationSet
func IsInReplicationSet(r ring.ReadRing, ringKey uint32, address string) (bool, error) {
	bufDescs, bufHosts, bufZones := ring.MakeBuffersForGet()
	rs, err := r.Get(ringKey, ring.Write, bufDescs, bufHosts, bufZones)
	if err != nil {
		return false, err
	}
	return util.StringsContain(rs.GetAddresses(), address), nil
}
// MakeBuffersForGet 复用缓冲降低 Get 分配，与 dskit ring 最佳实践一致。
