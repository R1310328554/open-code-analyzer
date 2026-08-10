package util //nolint:revive

// util 包 Jitter 与 TickerWithJitter：在固定周期上叠加随机偏移，降低多实例定时任务同时唤醒的 thundering herd。

import (
	"context"
	"math/rand"
	"time"
)

type Jitter struct {
	base      time.Duration
	deviation time.Duration
}

// NewJitter 构造抖动器，dev 为半宽，实际区间为 base±dev（见 Duration 实现）。
// NewJitter returns a Jitter object that creates durations with random jitter.
func NewJitter(b time.Duration, d time.Duration) Jitter {
	return Jitter{base: b, deviation: d}
}

// Duration returns a random duration from the base duration and +/- jitter
func (j Jitter) Duration() time.Duration {
	base := j.base - j.deviation
	jitter := time.Duration(rand.Int63n(int64(float64(2 * j.deviation.Nanoseconds())))) //#nosec G404 -- Jitter does not require CSPRNG -- nosemgrep: math-random-used
	return base + jitter
}

// TickerWithJitter 暴露 C 通道与 Stop，内部 goroutine 在 ctx 取消时 drain timer。
type TickerWithJitter struct {
	C    chan time.Time
	ctx  context.Context
	stop func()
}

func (t *TickerWithJitter) Stop() {
	t.stop()
}

func (t *TickerWithJitter) start(d, dev time.Duration) {
	j := NewJitter(d, dev)
	timer := time.NewTimer(j.Duration())
	defer timer.Stop()
	for {
		select {
		case <-timer.C:
			timer.Reset(j.Duration())
			t.C <- time.Now()
		case <-t.ctx.Done():
			if !timer.Stop() {
				<-timer.C
			}
			return
		}
	}
}

// NewTickerWithJitter 后台 start 循环 Reset 随机间隔并向 C 发送 time.Now。
// NewTickerWithJitter returns a new Ticker-like object, but instead of a
// constant tick duration, it adds random +/- dev to each iteration.
func NewTickerWithJitter(d, dev time.Duration) *TickerWithJitter {
	ctx, cancel := context.WithCancel(context.Background())
	t := &TickerWithJitter{
		C:    make(chan time.Time),
		ctx:  ctx,
		stop: cancel,
	}
	go t.start(d, dev)
	return t
}
// 抖动使用 math/rand 非 CSPRNG，仅用于调度分散，不应用于安全相关随机。
