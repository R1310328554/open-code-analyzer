package sharding

// sharding 基于带统计信息的 fingerprint 序列做贪心分片：按目标 shard 字节上限合并相邻 stream，超大单 stream 单独成 shard。

import (
	"math"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/queue"
	"github.com/grafana/loki/v3/pkg/storage/stores/index/stats"
)

var (
	SizedFPsPool = queue.NewSlicePool[SizedFP](1<<8, 1<<16, 4) // 256->65536
)

// SizedFP 将 fingerprint 与 stats 绑定，供 ShardsFor 按字节预算切分查询。
type SizedFP struct {
	Fp    model.Fingerprint
	Stats stats.Stats
}

type SizedFPs []SizedFP

func (xs SizedFPs) Len() int {
	return len(xs)
}

func (xs SizedFPs) Less(i, j int) bool {
	return xs[i].Fp < xs[j].Fp
}

func (xs SizedFPs) Swap(i, j int) {
	xs[i], xs[j] = xs[j], xs[i]
}

// newShard 创建以 minFP 为下界的新 shard，Stats 初始为空以便累加。
func (xs SizedFPs) newShard(minFP model.Fingerprint) logproto.Shard {
	return logproto.Shard{
		Bounds: logproto.FPBounds{
			Min: minFP,
		},
		Stats: &stats.Stats{},
	}
}

// ShardsFor 顺序扫描排序后的 SizedFP，在 shard 容量不足时切分并更新边界。
func (xs SizedFPs) ShardsFor(targetShardBytes uint64) (res []logproto.Shard) {
	if len(xs) == 0 {
		full := xs.newShard(0)
		full.Bounds.Max = model.Fingerprint(math.MaxUint64)
		return []logproto.Shard{full}
	}

	var (
		cur = xs.newShard(0)
	)

	for _, x := range xs {

		// easy path, there's space -- continue
		if cur.SpaceFor(&x.Stats, targetShardBytes) {
			cur.Stats.Streams++
			cur.Stats.Chunks += x.Stats.Chunks
			cur.Stats.Entries += x.Stats.Entries
			cur.Stats.Bytes += x.Stats.Bytes

			cur.Bounds.Max = x.Fp
			continue
		}

		// we've hit a stream larger than the target;
		// create a shard with 1 stream
		if cur.Stats.Streams == 0 {
			cur.Stats = &stats.Stats{
				Streams: 1,
				Chunks:  x.Stats.Chunks,
				Bytes:   x.Stats.Bytes,
				Entries: x.Stats.Entries,
			}
			cur.Bounds.Max = x.Fp
			res = append(res, cur)
			cur = xs.newShard(x.Fp + 1)
			continue
		}

		// Otherwise we've hit a stream that's too large but the current shard isn't empty; create a new shard
		cur.Bounds.Max = x.Fp - 1
		res = append(res, cur)
		cur = xs.newShard(x.Fp)
		cur.Stats = &stats.Stats{
			Streams: 1,
			Chunks:  x.Stats.Chunks,
			Bytes:   x.Stats.Bytes,
			Entries: x.Stats.Entries,
		}
	}

	if cur.Stats.Streams > 0 {
		res = append(res, cur)
	}

	res[len(res)-1].Bounds.Max = model.Fingerprint(math.MaxUint64)
	return res
}
// SizedFPsPool 复用 SizedFP 切片，减少大规模分片时的堆分配开销。
