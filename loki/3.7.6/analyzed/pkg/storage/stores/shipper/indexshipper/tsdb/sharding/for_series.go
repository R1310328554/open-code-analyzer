package sharding

// sharding 提供 ForSeries 通用迭代接口，便于在不同索引类型上复用 series 遍历逻辑，避免各实现重复暴露相同能力并引发循环依赖。

import (
	"context"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
)

// ForSeries 按时间范围与 matcher 遍历 series，回调参数会被复用且可能并发调用。
// General purpose iteration over series. Makes it easier to build custom functionality on top of indices
// of different types without them all implementing the same feature.
// The passed callback must _not_ capture its arguments. They're reused for each call for performance.
// The passed callback may be executed concurrently,
// so any shared state must be protected by the caller.
// NB: This is a low-level API and should be used with caution.
// NB: It's possible for the callback to be called multiple times for the same series but possibly different chunks,
// such as when the Index is backed by multiple files with the same series present.
// NB(owen-d): mainly in this package to avoid circular dependencies elsewhere
// ForSeries 抽象跨索引类型的 series 迭代能力，调用方须自行保护共享状态。
type ForSeries interface {
	ForSeries(
		ctx context.Context,
		userID string,
		fpFilter index.FingerprintFilter,
		from model.Time,
		through model.Time,
		fn func(
			labels.Labels,
			model.Fingerprint,
			[]index.ChunkMeta,
		) (stop bool),
		matchers ...*labels.Matcher,
	) error
}

// ForSeriesFunc 将函数类型适配为 ForSeries 接口，便于内联轻量实现。
// function Adapter for ForSeries implementation
type ForSeriesFunc func(
	ctx context.Context,
	userID string,
	fpFilter index.FingerprintFilter,
	from model.Time,
	through model.Time,
	fn func(
		labels.Labels,
		model.Fingerprint,
		[]index.ChunkMeta,
	) (stop bool),
	matchers ...*labels.Matcher,
) error

func (f ForSeriesFunc) ForSeries(
	ctx context.Context,
	userID string,
	fpFilter index.FingerprintFilter,
	from model.Time,
	through model.Time,
	fn func(
		labels.Labels,
		model.Fingerprint,
		[]index.ChunkMeta,
	) (stop bool),
	matchers ...*labels.Matcher,
) error {
	return f(ctx, userID, fpFilter, from, through, fn, matchers...)
}
// 同一 series 可能因多文件重叠而多次回调，回调不得捕获参数引用。
