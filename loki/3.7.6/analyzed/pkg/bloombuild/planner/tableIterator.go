package planner

// 日表范围迭代器：在 min/max DayTime 之间按天递增，结合 schema
// 配置解析各日对应的 index table 前缀，供 planner 加载租户工作负载。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/storage/config"
)

type dayRangeIterator struct {
	min, max, cur config.DayTime
	curPeriod     config.PeriodConfig
	schemaCfg     config.SchemaConfig
	err           error
}

// newDayRangeIterator 将 cur 设为 min 前一天，Next 首次 Inc 后从 min 开始。
func newDayRangeIterator(minVal, maxVal config.DayTime, schemaCfg config.SchemaConfig) *dayRangeIterator {
	return &dayRangeIterator{min: minVal, max: maxVal, cur: minVal.Dec(), schemaCfg: schemaCfg}
}

// TotalDays 返回 min 至 max（不含 max）之间的日数，用于预分配 map 容量。
func (r *dayRangeIterator) TotalDays() int {
	offset := r.cur
	if r.cur.Before(r.min) {
		offset = r.min
	}
	return int(r.max.Sub(offset.Time) / config.ObjectStorageIndexRequiredPeriod)
}

func (r *dayRangeIterator) Next() bool {
	r.cur = r.cur.Inc()
	if !r.cur.Before(r.max) {
		return false
	}

	period, err := r.schemaCfg.SchemaForTime(r.cur.ModelTime())
	if err != nil {
		r.err = fmt.Errorf("getting schema for time (%s): %w", r.cur, err)
		return false
	}
	r.curPeriod = period

	return true
}

// At 返回当前日与其 schema 对应的 DayTable（含 index 表前缀）。
func (r *dayRangeIterator) At() config.DayTable {
	return config.NewDayTable(r.cur, r.curPeriod.IndexTables.Prefix)
}

func (r *dayRangeIterator) Err() error {
	return nil
}
