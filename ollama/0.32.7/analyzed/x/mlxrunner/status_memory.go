// MLX 内存状态缓存：异步刷新 Active+Cache 内存供 /v1/status 使用。
package mlxrunner

import (
	"context"
	"log/slog"
	"sync"
	"time"
)

const statusMemoryRefreshWait = 50 * time.Millisecond

// statusMemoryRefreshFunc 读取当前 MLX 内存字节数。
type statusMemoryRefreshFunc func() (uint64, error)

// statusMemoryCache 避免 health 同步阻塞 MLX worker，异步刷新内存遥测。
// statusMemoryCache keeps health checks from depending synchronously on the
// serialized MLX worker while still refreshing memory telemetry opportunistically.
// statusMemoryCache 缓存 memory 与 refreshedAt，支持 in-flight 去重。
type statusMemoryCache struct {
	done    <-chan struct{}
	wait    time.Duration
	refresh statusMemoryRefreshFunc

	mu          sync.Mutex
	memory      uint64
	refreshedAt time.Time
	inFlight    chan struct{}
}

// newStatusMemoryCache 创建带初始值与刷新回调的缓存。
func newStatusMemoryCache(ctx context.Context, memory uint64, refreshedAt time.Time, wait time.Duration, refresh statusMemoryRefreshFunc) *statusMemoryCache {
	return &statusMemoryCache{
		done:        ctx.Done(),
		wait:        wait,
		refresh:     refresh,
		memory:      memory,
		refreshedAt: refreshedAt,
	}
}

// Memory 等待刷新或超时后返回缓存内存值。
func (c *statusMemoryCache) Memory() uint64 {
	done := c.startRefresh()
	if c.wait <= 0 {
		<-done
		memory, _ := c.snapshot()
		return memory
	}

	timer := time.NewTimer(c.wait)
	defer timer.Stop()

	select {
	case <-done:
	case <-timer.C:
		memory, refreshedAt := c.snapshot()
		if refreshedAt.IsZero() {
			slog.Debug("using cached MLX memory status before first refresh")
		} else {
			slog.Debug("using cached MLX memory status", "stale", time.Since(refreshedAt))
		}
		return memory
	case <-c.done:
	}

	memory, _ := c.snapshot()
	return memory
}

// startRefresh 启动或复用进行中的异步刷新。
func (c *statusMemoryCache) startRefresh() chan struct{} {
	c.mu.Lock()
	if c.inFlight != nil {
		done := c.inFlight
		c.mu.Unlock()
		return done
	}

	refreshDone := make(chan struct{})
	c.inFlight = refreshDone
	refresh := c.refresh
	lifecycleDone := c.done
	c.mu.Unlock()

	go func() {
		memory, err := refresh()
		now := time.Now()

		c.mu.Lock()
		defer c.mu.Unlock()
		defer close(refreshDone)

		if err != nil {
			select {
			case <-lifecycleDone:
			default:
				slog.Debug("failed to refresh MLX memory status", "error", err)
			}
			c.inFlight = nil
			return
		}

		c.memory = memory
		c.refreshedAt = now
		c.inFlight = nil
	}()

	return refreshDone
}

// snapshot 返回当前 memory 与 refreshedAt。
func (c *statusMemoryCache) snapshot() (uint64, time.Time) {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.memory, c.refreshedAt
}
