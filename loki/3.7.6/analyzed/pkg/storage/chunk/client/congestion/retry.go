package congestion

// retry 实现 Retrier 策略：NoopRetrier 单次执行；LimitedRetrier 在可重试错误上循环至多 limit+1 次，超限返回 RetriesExceeded。

import (
	"context"
	"errors"
	"io"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
)

var RetriesExceeded = errors.New("retries exceeded")

type NoopRetrier struct{}

func NewNoopRetrier(Config) *NoopRetrier {
	return &NoopRetrier{}
}

func (n *NoopRetrier) Do(fn DoRequestFunc, _ IsRetryableErrFunc, _ func(), _ func()) (io.ReadCloser, int64, error) {
	// don't retry, just execute the given function once
	return fn(0)
}

func (n *NoopRetrier) withLogger(log.Logger) Retrier { return n }

// LimitedRetrier 执行首次请求加最多 limit 次重试；limit=0 等价于 NoopRetrier。
// LimitedRetrier executes the initial request plus a configurable limit of subsequent retries.
// limit=0 is equivalent to NoopRetrier
type LimitedRetrier struct {
	limit  int
	logger log.Logger
}

func NewLimitedRetrier(cfg Config) *LimitedRetrier {
	return &LimitedRetrier{limit: cfg.Retry.Limit}
}

// Do 在可重试错误时调用 onError 触发 AIMD 乘性降速，成功时 onSuccess 触发加性增。
func (l *LimitedRetrier) Do(fn DoRequestFunc, isRetryable IsRetryableErrFunc, onSuccess func(), onError func()) (io.ReadCloser, int64, error) {
	// i = 0 is initial request
	// i > 0 is retry
	for i := 0; i <= l.limit; i++ {
		rc, sz, err := fn(i)

		if err != nil {
			if !isRetryable(err) {
				if !errors.Is(err, context.Canceled) {
					level.Debug(l.logger).Log("msg", "store error is not retryable", "err", err)
				}
				return rc, sz, err
			}

			level.Debug(l.logger).Log("msg", "error is retryable", "err", err)
			// TODO(dannyk): consider this more carefully
			// only decrease rate-limit if error is retryable, otherwise all errors (context cancelled, dial errors, timeouts, etc)
			// which may be mostly client-side would inappropriately reduce throughput
			onError()
			continue
		}

		onSuccess()
		return rc, sz, err
	}

	return nil, 0, RetriesExceeded
}

func (l *LimitedRetrier) withLogger(logger log.Logger) Retrier {
	l.logger = logger
	return l
}
// 不可重试错误（含 context.Canceled）立即返回；仅对可重试存储错误才继续循环。
