package client

// WAL WriteTo 适配器：将 TSDB 段内 series 标签与 WAL 条目重组为 api.Entry 送入 client。
// 维护 HeadSeriesRef → LabelSet 缓存，段回收时 SeriesReset 清理过期映射。

import (
	"fmt"
	"sync"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/tsdb/chunks"
	"github.com/prometheus/prometheus/tsdb/record"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"

	"github.com/grafana/loki/v3/pkg/ingester/wal"
	"github.com/grafana/loki/v3/pkg/util"
)

// 实现 wal.WriteTo：StoreSeries 缓存标签，AppendEntries 查表后写入 client channel。
// clientWriteTo implements a wal.WriteTo that re-builds entries with the stored series, and the received entries. After,
// sends each to the provided Client channel.
type clientWriteTo struct {
	series     map[chunks.HeadSeriesRef]model.LabelSet
	seriesLock sync.RWMutex

	// seriesSegment keeps track of the last segment in which the series pointed by each key in this map was seen. Keeping
	// this in a separate map avoids unnecessary contention.
	//
	// Even though it doesn't present a difference right now according to benchmarks, it will help when we introduce other
	// calls from the wal.Watcher to the wal.WriteTo like `UpdateSeriesSegment`.
	seriesSegment     map[chunks.HeadSeriesRef]int
	seriesSegmentLock sync.RWMutex

	logger   log.Logger
	toClient chan<- api.Entry
}

// 构造带空 series/segment 映射的 WriteTo，目标为 Promtail client 输入 channel。
// newClientWriteTo creates a new clientWriteTo
func newClientWriteTo(toClient chan<- api.Entry, logger log.Logger) *clientWriteTo {
	return &clientWriteTo{
		series:        make(map[chunks.HeadSeriesRef]model.LabelSet),
		seriesSegment: make(map[chunks.HeadSeriesRef]int),
		toClient:      toClient,
		logger:        logger,
	}
}

// 批量写入 series 标签与所在 segment 号，供后续 WAL 条目关联。
func (c *clientWriteTo) StoreSeries(series []record.RefSeries, segment int) {
	c.seriesLock.Lock()
	defer c.seriesLock.Unlock()
	c.seriesSegmentLock.Lock()
	defer c.seriesSegmentLock.Unlock()
	for _, seriesRec := range series {
		c.seriesSegment[seriesRec.Ref] = segment
		labels := util.MapToModelLabelSet(seriesRec.Labels.Map())
		c.series[seriesRec.Ref] = labels
	}
}

// 删除 segmentNum 及更早 segment 中的 series 缓存，配合 WAL 段删除。
// SeriesReset will delete all cache entries that were last seen in segments numbered equal or lower than segmentNum
func (c *clientWriteTo) SeriesReset(segmentNum int) {
	c.seriesLock.Lock()
	defer c.seriesLock.Unlock()
	c.seriesSegmentLock.Lock()
	defer c.seriesSegmentLock.Unlock()
	for k, v := range c.seriesSegment {
		if v <= segmentNum {
			level.Debug(c.logger).Log("msg", fmt.Sprintf("reclaiming series under segment %d", segmentNum))
			delete(c.seriesSegment, k)
			delete(c.series, k)
		}
	}
}

// 按 Ref 查标签，逐条组装 Entry 写入 toClient；未知 series 仅 debug 日志。
func (c *clientWriteTo) AppendEntries(entries wal.RefEntries) error {
	var entry api.Entry
	c.seriesLock.RLock()
	l, ok := c.series[entries.Ref]
	c.seriesLock.RUnlock()
	if ok {
		entry.Labels = l
		for _, e := range entries.Entries {
			entry.Entry = e
			c.toClient <- entry
		}
	} else {
		level.Debug(c.logger).Log("msg", "series for entry not found")
	}
	return nil
}
