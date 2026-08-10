package ewma

// window 表示单个 EWMA 时间窗口的状态，按指数衰减公式平滑采样值。

import (
	"math"
	"strings"
	"time"
)

// window 持有 Size、当前平滑值 value 及上次更新时间 lastUpdate。
// window represents a window size for EWMA calculations; such as a 15m window.
type window struct {
	Size time.Duration

	initialized bool
	value       float64
	lastUpdate  time.Time
}

// Name 将 Duration 格式化为 Prometheus 标签用的简短字符串（如 15m）。
// Name returns a name for the window, based on its size. Unlike
// [time.Duration.String], trailing zero units are removed, so 15m0s becomes
// 15m.
func (w *window) Name() string {
	name := w.Size.String()

	if strings.HasSuffix(name, "m0s") {
		name = name[:len(name)-2] // Trim 0s
	}
	if strings.HasSuffix(name, "h0m") {
		name = name[:len(name)-2] // Trim 0m
	}
	return name
}

// Observe 在时钟回拨或未初始化时用新样本重置；否则按 e^(-Δt/window) 衰减更新。
// Observe updates the window with a new value. Observe reinitializes the window
// the now timestamp is earlier than now timestamp on the previous call.
func (w *window) Observe(value float64, now time.Time) {
	// We'll also treat clock drift as reinitialization.
	if !w.initialized || now.Before(w.lastUpdate) {
		w.initialized = true
		w.value = value
		w.lastUpdate = now
		return
	}

	// EWMA is calculated using the formula:
	//   ewma_new = decay * ewma_old + (1 - decay) * value
	//
	// Where decay is:
	//   e^(-delta/window_size)

	delta := now.Sub(w.lastUpdate)
	decay := math.Exp(-delta.Seconds() / w.Size.Seconds())

	w.value = decay*w.value + (1-decay)*value
	w.lastUpdate = now
}

// Value returns the current EWMA value.
func (w *window) Value() float64 { return w.value }
// Value 返回当前窗口内的 EWMA 平滑结果。
