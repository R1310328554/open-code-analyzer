package bloomshipper

// Shipper 对 Store 的薄封装：批量 FetchBlocks 并对每个 BlockQuerier 执行回调后自动 Close。

import (
	"context"
	"fmt"
	"sort"

	v1 "github.com/grafana/loki/v3/pkg/storage/bloom/v1"
)

type ForEachBlockCallback func(bq *v1.BlockQuerier, bounds v1.FingerprintBounds) error

type Interface interface {
	ForEach(ctx context.Context, tenant string, blocks []BlockRef, callback ForEachBlockCallback) error
	Stop()
}

type Shipper struct {
	store StoreBase
}

func NewShipper(client StoreBase) *Shipper {
	return &Shipper{store: client}
}

// ForEach 同步拉取全部块，逐块调用 callback 并递减引用计数。
// ForEach is a convenience function that wraps the store's FetchBlocks function
// and automatically closes the block querier once the callback was run.
func (s *Shipper) ForEach(ctx context.Context, refs []BlockRef, callback ForEachBlockCallback) error {
	bqs, err := s.store.FetchBlocks(ctx, refs, WithFetchAsync(false))
	if err != nil {
		return err
	}

	if len(bqs) != len(refs) {
		return fmt.Errorf("number of response (%d) does not match number of requests (%d)", len(bqs), len(refs))
	}

	for i := range bqs {
		err := callback(bqs[i].BlockQuerier, bqs[i].Bounds)
		// close querier to decrement ref count
		bqs[i].Close()
		if err != nil {
			return err
		}
	}
	return nil
}

func (s *Shipper) Stop() {
	s.store.Stop()
}

// BlocksForMetas 从元数据列表中筛选时间区间与指纹 keyspace 内的块并按边界排序。
// BlocksForMetas returns all the blocks from all the metas listed that are within the requested bounds
func BlocksForMetas(metas []Meta, interval Interval, keyspaces []v1.FingerprintBounds) (refs []BlockRef) {
	for _, meta := range metas {
		for _, block := range meta.Blocks {
			if !isOutsideRange(block, interval, keyspaces) {
				refs = append(refs, block)
			}
		}
	}

	sort.Slice(refs, func(i, j int) bool {
		return refs[i].Bounds.Less(refs[j].Bounds)
	})

	return refs
}

// isOutsideRange 判断块是否在查询时间窗或任一指纹范围内。
// isOutsideRange tests if a given BlockRef b is outside of search boundaries
// defined by min/max timestamp and min/max fingerprint.
// Fingerprint ranges must be sorted in ascending order.
func isOutsideRange(b BlockRef, interval Interval, bounds []v1.FingerprintBounds) bool {
	// check time interval
	if !interval.Overlaps(b.Interval()) {
		return true
	}

	// check fingerprint ranges
	for _, keyspace := range bounds {
		if keyspace.Overlaps(b.Bounds) {
			return false
		}
	}

	return true
}
// Stop 转发至底层 Store，关闭 fetcher 与对象存储客户端。
