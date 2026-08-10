package bloomgateway

// Worker 侧任务处理器：按日表分组、批量拉取 Bloom 块并并发查询，
// 将过滤结果通过 Task.resCh 流式回传 Gateway。

import (
	"context"
	"time"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/concurrency"
	"github.com/grafana/dskit/multierror"
	"github.com/pkg/errors"

	iter "github.com/grafana/loki/v3/pkg/iter/v2"
	v1 "github.com/grafana/loki/v3/pkg/storage/bloom/v1"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/bloomshipper"
)

func newProcessor(id string, concurrency int, async bool, store bloomshipper.Store, logger log.Logger, metrics *workerMetrics) *processor {
	return &processor{
		id:          id,
		concurrency: concurrency,
		async:       async,
		store:       store,
		logger:      logger,
		metrics:     metrics,
	}
}

// processor 持有块查询并发度、异步下载开关与 bloomshipper Store。
type processor struct {
	id          string
	concurrency int  // concurrency at which bloom blocks are processed
	async       bool // whether blocks should be downloaded asynchronously
	store       bloomshipper.Store
	logger      log.Logger
	metrics     *workerMetrics
}

// processTasks 按日表分组依次处理，失败时向所有 Task 写入错误并关闭。
func (p *processor) processTasks(ctx context.Context, tasks []Task) error {
	tenant := tasks[0].tenant

	for ts, tasks := range group(tasks, func(t Task) config.DayTime { return t.table }) {
		err := p.processTasksForDay(ctx, tenant, ts, tasks)
		if err != nil {
			for _, task := range tasks {
				task.CloseWithError(err)
			}
			return err
		}
		for _, task := range tasks {
			task.Close()
		}
	}
	return nil
}

// processTasksForDay 合并块引用、FetchBlocks 后并发 processBlocks 并更新 Stats。
func (p *processor) processTasksForDay(ctx context.Context, _ string, _ config.DayTime, tasks []Task) error {
	var duration time.Duration

	blocksRefs := make([]bloomshipper.BlockRef, 0, len(tasks[0].blocks)*len(tasks))
	for _, task := range tasks {
		blocksRefs = append(blocksRefs, task.blocks...)
	}

	tasksByBlock := partitionTasksByBlock(tasks, blocksRefs)

	refs := make([]bloomshipper.BlockRef, 0, len(tasksByBlock))
	for _, block := range tasksByBlock {
		refs = append(refs, block.ref)
	}

	startBlocks := time.Now()
	bqs, err := p.store.FetchBlocks(
		ctx,
		refs,
		bloomshipper.WithFetchAsync(p.async),
		bloomshipper.WithIgnoreNotFound(true),
		// NB(owen-d): we relinquish bloom pages to a pool
		// after iteration for performance (alloc reduction).
		// This is safe to do here because we do not capture
		// the underlying bloom []byte outside of iteration
		bloomshipper.WithPool(p.store.Allocator()),
	)
	duration = time.Since(startBlocks)

	for _, t := range tasks {
		FromContext(t.ctx).AddBlocksFetchTime(duration)
		FromContext(t.ctx).AddProcessedBlocksTotal(len(tasksByBlock))
	}

	if err != nil {
		return err
	}

	startProcess := time.Now()
	res := p.processBlocks(ctx, bqs, tasksByBlock)
	duration = time.Since(startProcess)

	for _, t := range tasks {
		FromContext(t.ctx).AddProcessingTime(duration)
	}

	return res
}

// processBlocks 并发处理各块；块未下载则跳过并递增 blocksNotAvailable 指标。
func (p *processor) processBlocks(ctx context.Context, bqs []*bloomshipper.CloseableBlockQuerier, data []blockWithTasks) error {
	// We opportunistically close blocks during iteration to allow returning memory to the pool, etc,
	// as soon as possible, but since we exit early on error, we need to ensure we close all blocks.
	hasClosed := make([]bool, len(bqs))
	defer func() {
		for i, bq := range bqs {
			if bq != nil && !hasClosed[i] {
				_ = bq.Close()
			}
		}
	}()

	return concurrency.ForEachJob(ctx, len(bqs), p.concurrency, func(ctx context.Context, i int) error {
		blockQuerier := bqs[i]
		blockWithTasks := data[i]

		// block has not been downloaded or is otherwise not available (yet)
		// therefore no querier for this block available
		if blockQuerier == nil {
			for _, task := range blockWithTasks.tasks {
				stats := FromContext(task.ctx)
				stats.IncSkippedBlocks()
			}

			p.metrics.blocksNotAvailable.WithLabelValues(p.id).Inc()
			return nil
		}

		if !blockWithTasks.ref.Bounds.Equal(blockQuerier.Bounds) {
			return errors.Errorf("block and querier bounds differ: %s vs %s", blockWithTasks.ref.Bounds, blockQuerier.Bounds)
		}

		var errs multierror.MultiError
		errs.Add(errors.Wrap(p.processBlock(ctx, blockQuerier, blockWithTasks.tasks), "processing block"))
		errs.Add(blockQuerier.Close())
		hasClosed[i] = true
		return errs.Err()
	})
}

// processBlock 要求 V3+ schema，Fuse 多 Task 迭代器后 Run 执行 Bloom 过滤。
func (p *processor) processBlock(_ context.Context, bq *bloomshipper.CloseableBlockQuerier, tasks []Task) (err error) {
	blockQuerier := bq.BlockQuerier
	schema, err := blockQuerier.Schema()
	if err != nil {
		return err
	}

	// We require V3+ schema
	if schema.Version() < v1.V3 {
		return v1.ErrUnsupportedSchemaVersion
	}

	iters := make([]iter.PeekIterator[v1.Request], 0, len(tasks))

	for _, task := range tasks {
		// NB(owen-d): can be helpful for debugging, but is noisy
		// and don't feel like threading this through a configuration

		//sp := trace.SpanFromContext(task.ctx)
		//md, _ := blockQuerier.Metadata()
		//blk := bloomshipper.BlockRefFrom(task.tenant, task.table.String(), md)
		//sp.SetAttributes(attribute.String("process block", blk.String()), attribute.Int("series", len(task.series)))

		it := iter.NewPeekIter(task.RequestIter())
		iters = append(iters, it)
	}

	logger := log.With(p.logger, "block", bq.String())
	fq := blockQuerier.Fuse(iters, logger)

	start := time.Now()
	err = fq.Run()
	duration := time.Since(start)

	if err != nil {
		p.metrics.blockQueryLatency.WithLabelValues(p.id, labelFailure).Observe(duration.Seconds())
	} else {
		p.metrics.blockQueryLatency.WithLabelValues(p.id, labelSuccess).Observe(duration.Seconds())
	}

	for _, task := range tasks {
		stats := FromContext(task.ctx)
		stats.AddTotalProcessingTime(duration)
		stats.IncProcessedBlocks()
	}

	return err
}

// getFirstLast returns the first and last item of a fingerprint slice
// It assumes an ascending sorted list of fingerprints.
// getFirstLast 返回已排序切片的首尾元素，空切片返回零值。
func getFirstLast[T any](s []T) (T, T) {
	var zero T
	if len(s) == 0 {
		return zero, zero
	}
	return s[0], s[len(s)-1]
}

func group[K comparable, V any, S ~[]V](s S, f func(v V) K) map[K]S {
	m := make(map[K]S)
	for _, elem := range s {
		m[f(elem)] = append(m[f(elem)], elem)
	}
	return m
}
