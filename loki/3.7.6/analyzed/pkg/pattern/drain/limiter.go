package drain

// drain 包 limiter 在集群驱逐率过高时临时阻断 Train，防止 LRU 抖动导致模式检测风暴。

import (
	"time"
)

type limiter struct {
	added         int64
	evicted       int64
	maxPercentage float64
	blockedUntil  time.Time
}

func newLimiter(maxPercentage float64) *limiter {
	return &limiter{
		maxPercentage: maxPercentage,
	}
}

// Allow 在 block 窗口内返回 false；否则递增 added 并在超阈值时触发 block。
func (l *limiter) Allow() bool {
	if !l.blockedUntil.IsZero() {
		if time.Now().Before(l.blockedUntil) {
			return false
		}
		l.reset()
	}
	if l.added == 0 {
		l.added++
		return true
	}
	if float64(l.evicted)/float64(l.added) > l.maxPercentage {
		l.block()
		return false
	}
	l.added++
	return true
}

// Evict 在 LRU 驱逐集群时递增 evicted，供 Allow 计算驱逐比例。
func (l *limiter) Evict() {
	l.evicted++
}

func (l *limiter) reset() {
	l.added = 0
	l.evicted = 0
	l.blockedUntil = time.Time{}
}

func (l *limiter) block() {
	l.blockedUntil = time.Now().Add(10 * time.Minute)
}
// reset 在 block 到期后清零计数，恢复模式检测。
