package client

// compat 提供与 Prometheus 一致的标签指纹算法：FastFingerprint 用于 logproto.LabelAdapter，Fingerprint 用于 labels.Labels。

import (
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logproto"
)

const (
	// offset64 为 FNV-1a 64 位哈希的初始偏移常量。
// offset64 is an offset require for the FNV (Fowler-Noll-Vo) hash function.
	offset64 = 14695981039346656037
	// prime64 为 FNV-1a 每字节乘法的 64 位质数。
// prime64 is a 64bit prime used by the FNV hash function.
	prime64 = 1099511628211
)

// hashNew 返回 FNV-1a 哈希的初始状态 offset64。
// hashNew initializes a new fnv64a hash value.
func hashNew() uint64 {
	return offset64
}

// FastFingerprint 对每个标签名/值分别哈希后 XOR 聚合，与 Prometheus 快速指纹一致。
// FastFingerprint runs the same algorithm as Prometheus labelSetToFastFingerprint()
func FastFingerprint(ls []logproto.LabelAdapter) model.Fingerprint {
	if len(ls) == 0 {
		return model.Metric(nil).FastFingerprint()
	}

	var result uint64
	for _, l := range ls {
		sum := hashNew()
		sum = hashAdd(sum, l.Name)
		sum = hashAddByte(sum, model.SeparatorByte)
		sum = hashAdd(sum, l.Value)
		result ^= sum
	}
	return model.Fingerprint(result)
}

// Fingerprint 顺序哈希全部标签并以 SeparatorByte 分隔，与 Prometheus 标准指纹一致。
// Fingerprint runs the same algorithm as Prometheus labelSetToFingerprint()
func Fingerprint(lbls labels.Labels) model.Fingerprint {
	sum := hashNew()
	lbls.Range(func(label labels.Label) {
		sum = hashAddString(sum, label.Name)
		sum = hashAddByte(sum, model.SeparatorByte)
		sum = hashAddString(sum, label.Value)
		sum = hashAddByte(sum, model.SeparatorByte)
	})
	return model.Fingerprint(sum)
}
// 指纹算法必须与 Prometheus 兼容以保证 series 路由与存储一致。
