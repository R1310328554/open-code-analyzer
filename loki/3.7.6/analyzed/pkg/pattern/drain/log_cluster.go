package drain

// drain 包 log_cluster 表示 Drain 检测到的一条日志模式：token 模板、样本 Chunk 与体积统计。

import (
	"strings"
	"time"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/pattern/iter"
)

type LogCluster struct {
	id          int
	Size        int
	Tokens      []string
	TokenState  interface{}
	Stringer    func([]string, interface{}) string
	Volume      int64
	SampleCount int64

	Chunks Chunks
}

// String 通过 Stringer 回调 Join token，得到可查询的模式字符串。
func (c *LogCluster) String() string {
	if c.Stringer != nil {
		return c.Stringer(c.Tokens, c.TokenState)
	}
	return strings.Join(c.Tokens, " ")
}

func (c *LogCluster) append(ts model.Time, maxChunkAge time.Duration, sampleInterval time.Duration) *logproto.PatternSample {
	c.Size++
	return c.Chunks.Add(ts, maxChunkAge, sampleInterval)
}

func (c *LogCluster) merge(samples []*logproto.PatternSample) {
	c.Size += int(sumSize(samples))
	c.Chunks.merge(samples)
}

// Iterator 委托 Chunks 构建带 pattern 与 level 标签的查询迭代器。
func (c *LogCluster) Iterator(lvl string, from, through, step, sampleInterval model.Time) iter.Iterator {
	return c.Chunks.Iterator(c.String(), lvl, from, through, step, sampleInterval)
}

func (c *LogCluster) Samples() []*logproto.PatternSample {
	return c.Chunks.samples()
}

// Prune 裁剪过期 Chunk 并同步 Size，返回被移除样本。
func (c *LogCluster) Prune(olderThan time.Duration) []*logproto.PatternSample {
	prunedSamples := c.Chunks.prune(olderThan)
	c.Size = c.Chunks.size()
	return prunedSamples
}

func sumSize(samples []*logproto.PatternSample) int64 {
	var x int64
	for i := range samples {
		x += samples[i].Value
	}
	return x
}
// merge 合并外部样本并累加 Volume 相关 Size，用于跨 ingester 样本归并。
