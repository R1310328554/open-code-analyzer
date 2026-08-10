package wal

// WAL Watcher 指数退避定时器：在 min/max 间隔间复用单个 time.Timer，
// 读成功 reset 到最小值，无新数据则 backoff 翻倍直至上限。

import "time"

// backoffTimer is a time.Timer that allows one to move between a minimum and maximum interval, using an exponential backoff
// strategy. It safely re-uses just one time.Timer instance internally.
// 封装 timer、当前/最小/最大间隔及对外只读 C channel。
type backoffTimer struct {
	timer                *time.Timer
	curr, minVal, maxVal time.Duration
	C                    <-chan time.Time
}

func newBackoffTimer(minVal, maxVal time.Duration) *backoffTimer {
	// note that the first timer created will be stopped without ever consuming it, since it's once we can omit it
	// since the timer is recycled, we can keep the channel
	t := time.NewTimer(minVal)
	return &backoffTimer{
		timer:  t,
		minVal: minVal,
		maxVal: maxVal,
		curr:   minVal,
		C:      t.C,
	}
}

// 当前间隔乘 2 并 cap 到 maxVal，然后 recycle 重置底层 Timer。
func (bt *backoffTimer) backoff() {
	bt.curr = bt.curr * 2
	if bt.curr > bt.maxVal {
		bt.curr = bt.maxVal
	}
	bt.recycle()
}

func (bt *backoffTimer) reset() {
	bt.curr = bt.minVal
	bt.recycle()
}

// Stop 并排空过期 tick，再 Reset(curr) 安全复用同一 Timer 实例。
// recycle stops and attempts to drain the time.Timer underlying channel, in order to fully recycle the instance.
func (bt *backoffTimer) recycle() {
	if !bt.timer.Stop() {
		// attempt to drain timer's channel if it has expired
		select {
		case <-bt.timer.C:
		default:
		}
	}
	// safe to call reset after checking stopping and draining the timer
	bt.timer.Reset(bt.curr)
}
