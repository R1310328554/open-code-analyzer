package util //nolint:revive

// util 包提供 chunk 客户端通用工具：并行拉取 chunk、索引并行查询及目录/权限辅助函数。

import (
	"context"
	"sync"

	"github.com/go-kit/log/level"
	"go.opentelemetry.io/otel"
	attribute "go.opentelemetry.io/otel/attribute"

	"github.com/grafana/loki/v3/pkg/storage/chunk"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

var tracer = otel.Tracer("pkg/storage/chunk/client/util")

// decodeContextPool 复用 DecodeContext，避免并行解码时频繁分配。
var decodeContextPool = sync.Pool{
	New: func() interface{} {
		return chunk.NewDecodeContext()
	},
}

// GetParallelChunks 以最多 maxParallel 个 worker 并行调用 f 拉取 chunk，遇错仍返回已成功的部分结果。
// GetParallelChunks fetches chunks in parallel (up to maxParallel).
// GetParallelChunks 启动 worker 从 queuedChunks 消费任务，错误与成功结果经 channel 汇总。
func GetParallelChunks(ctx context.Context, maxParallel int, chunks []chunk.Chunk, f func(context.Context, *chunk.DecodeContext, chunk.Chunk) (chunk.Chunk, error)) ([]chunk.Chunk, error) {
	ctx, sp := tracer.Start(ctx, "GetParallelChunks")
	defer sp.End()

	sp.SetAttributes(attribute.Int("requested", len(chunks)))

	if ctx.Err() != nil {
		return nil, ctx.Err()
	}

	queuedChunks := make(chan chunk.Chunk)

	go func() {
		for _, c := range chunks {
			queuedChunks <- c
		}
		close(queuedChunks)
	}()

	processedChunks := make(chan chunk.Chunk)
	errors := make(chan error)

	for i := 0; i < min(maxParallel, len(chunks)); i++ {
		go func() {
			decodeContext := decodeContextPool.Get().(*chunk.DecodeContext)
			for c := range queuedChunks {
				c, err := f(ctx, decodeContext, c)
				if err != nil {
					errors <- err
				} else {
					processedChunks <- c
				}
			}
			decodeContextPool.Put(decodeContext)
		}()
	}

	result := make([]chunk.Chunk, 0, len(chunks))
	var lastErr error
	for i := 0; i < len(chunks); i++ {
		select {
		case chunk := <-processedChunks:
			result = append(result, chunk)
		case err := <-errors:
			lastErr = err
		}
	}

	sp.SetAttributes(attribute.Int("fetched", len(result)))
	if lastErr != nil {
		level.Error(util_log.Logger).Log("msg", "error fetching chunks", "err", lastErr)
	}

	// Return any chunks we did receive: a partial result may be useful
	return result, lastErr
}
// OpenTelemetry span 记录 requested/fetched 数量；最后一个错误会记录日志但不丢弃已拉取块。
