package logproto

// types 为 ChunkRef 等 proto 消息提供排序与 Prometheus Fingerprint 类型转换辅助。

import (
	"github.com/prometheus/common/model"
)

func (c *ChunkRef) FingerprintModel() model.Fingerprint {
	return model.Fingerprint(c.Fingerprint)
}

type ChunkRefWithSizingInfo struct {
	ChunkRef
	KB      uint32
	Entries uint32
}

// Less 按指纹、时间边界与 checksum 字典序比较，用于 chunk 列表去重排序。
// Less Compares chunks by (Fp, From, Through, checksum)
// Assumes User is equivalent
func (c *ChunkRef) Less(x ChunkRef) bool {
	if c.Fingerprint != x.Fingerprint {
		return c.Fingerprint < x.Fingerprint
	}

	if c.From != x.From {
		return c.From < x.From
	}

	if c.Through != x.Through {
		return c.Through < x.Through
	}

	return c.Checksum < x.Checksum
}
// ChunkRef 唯一标识对象存储中的一段压缩日志 chunk，User 字段区分租户。
