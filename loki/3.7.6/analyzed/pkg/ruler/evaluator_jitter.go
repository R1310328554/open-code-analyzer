package ruler

// EvaluatorWithJitter 包装底层 Evaluator，对规则查询串哈希后施加一致抖动，在固定评估周期内分散并发求值以减轻资源争用。

import (
	"context"
	"hash"
	"math"
	"sync"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"

	"github.com/grafana/loki/v3/pkg/logqlmodel"
	"github.com/grafana/loki/v3/pkg/util"
)

// EvaluatorWithJitter 用 hasher 将查询串映射为 0~maxJitter 的固定延迟。
// EvaluatorWithJitter wraps a given Evaluator. It applies a consistent jitter based on a rule's query string by hashing
// the query string to produce a 32-bit unsigned integer. From this hash, we calculate a ratio between 0 and 1 and
// multiply it by the configured max jitter. This ratio is used to delay evaluation by a consistent amount of random time.
//
// Consistent jitter is important because it allows rules to be evaluated on a regular, predictable cadence
// while also ensuring that we spread evaluations across the configured jitter window to avoid resource contention scenarios.
// inner 为实际求值器；maxJitter 上限；hasher 需并发加锁保护。
type EvaluatorWithJitter struct {
	mu sync.Mutex

	inner     Evaluator
	maxJitter time.Duration
	hasher    hash.Hash32
	logger    log.Logger
}

// NewEvaluatorWithJitter 在 maxJitter<=0 时直接返回 inner，禁用抖动。
func NewEvaluatorWithJitter(inner Evaluator, maxJitter time.Duration, hasher hash.Hash32, logger log.Logger) Evaluator {
	if maxJitter <= 0 {
		// jitter is disabled or invalid
		return inner
	}

	return &EvaluatorWithJitter{
		inner:     inner,
		maxJitter: maxJitter,
		hasher:    hasher,
		logger:    logger,
	}
}

// Eval 先 calculateJitter 再 Sleep，最后委托 inner.Eval 执行 LogQL。
func (e *EvaluatorWithJitter) Eval(ctx context.Context, qs string, now time.Time) (*logqlmodel.Result, error) {
	logger := log.With(e.logger, "query", qs, "query_hash", util.HashedQuery(qs))
	jitter := e.calculateJitter(qs, logger)

	if jitter > 0 {
		level.Debug(logger).Log("msg", "applying jitter", "jitter", jitter)
		time.Sleep(jitter)
	}

	return e.inner.Eval(ctx, qs, now)
}

func (e *EvaluatorWithJitter) calculateJitter(qs string, logger log.Logger) time.Duration {
	var h uint32

	// rules can be evaluated concurrently, so we protect the hasher with a mutex
	e.mu.Lock()
	{
		_, err := e.hasher.Write([]byte(qs))
		if err != nil {
			level.Warn(logger).Log("msg", "could not hash query to determine rule jitter", "err", err)
			return 0
		}

		h = e.hasher.Sum32()
		e.hasher.Reset()
	}
	e.mu.Unlock()

	ratio := float32(h) / math.MaxUint32
	return time.Duration(ratio * float32(e.maxJitter.Nanoseconds()))
}
// calculateJitter 将 Sum32 归一化后乘以 maxJitter 纳秒得到延迟。
