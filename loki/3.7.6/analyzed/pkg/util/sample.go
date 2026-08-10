package util //nolint:revive

// util 包 UniqueSampleHash 为标签串与日志行生成 xxhash 64 位指纹，用于去重、采样或缓存键而不存储完整内容。

import (
	"github.com/cespare/xxhash/v2"
)

func UniqueSampleHash(lblString string, line []byte) uint64 {
	uniqueID := make([]byte, 0, len(lblString)+len(line)+1)
	uniqueID = append(uniqueID, lblString...)
	uniqueID = append(uniqueID, ':')
	uniqueID = append(uniqueID, line...)
	return xxhash.Sum64(uniqueID)
}
// xxhash 速度快且碰撞率低，适合高吞吐 ingester 路径的热路径哈希。
