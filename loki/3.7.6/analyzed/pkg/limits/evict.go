package limits

// evictor 按固定间隔触发 usageStore 等 evictable 对象的过期流清理。

import (
	"context"
	"time"

	"github.com/coder/quartz"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
)

type evictable interface {
	evict(context.Context) error
}

// evictor 使用 quartz Ticker 在 interval 周期内调用 evict，ctx 取消时停止。
// evictor runs scheduled evictions.
type evictor struct {
	ctx       context.Context
	interval  time.Duration
	evictable evictable
	logger    log.Logger

	// Used for tests.
	clock quartz.Clock
}

// newEvictor 构造定时驱逐器，clock 可在测试中替换为 fake clock。
// newEvictor returns a new evictor over the interval.
func newEvictor(ctx context.Context, interval time.Duration, evictable evictable, logger log.Logger) (*evictor, error) {
	return &evictor{
		ctx:       ctx,
		interval:  interval,
		evictable: evictable,
		logger:    logger,
		clock:     quartz.NewReal(),
	}, nil
}

// Run 阻塞直到 ctx 结束；单次 tick 超时时间为 interval。
// Runs the scheduler loop until the context is canceled.
func (e *evictor) Run() error {
	t := e.clock.TickerFunc(e.ctx, e.interval, e.doTick)
	return t.Wait()
}

func (e *evictor) doTick() error {
	ctx, cancel := context.WithTimeout(e.ctx, e.interval)
	defer cancel()
	if err := e.evictable.evict(ctx); err != nil {
		level.Warn(e.logger).Log("failed to run eviction", "err", err.Error())
	}
	return nil
}
// 驱逐间隔由 Config.EvictionInterval 配置，与 ActiveWindow 配合控制内存占用。
