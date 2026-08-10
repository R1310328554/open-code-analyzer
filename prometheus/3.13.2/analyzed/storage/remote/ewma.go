// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 指数加权移动平均（EWMA）速率估计：供 remote write 队列管理器统计样本吞吐与推送耗时。

package remote

import (
	"sync"
	"time"

	"go.uber.org/atomic"
)

// ewmaRate 以 alpha 平滑瞬时事件速率，tick 按固定 interval 调用。
// ewmaRate tracks an exponentially weighted moving average of a per-second rate.
type ewmaRate struct {
	newEvents atomic.Int64

	alpha    float64
	interval time.Duration
	lastRate float64
	init     bool
	mutex    sync.Mutex
}

// newEWMARate 每次堆分配以保证 ARM 上 int64 原子对齐（见 prometheus#2666）。
// newEWMARate always allocates a new ewmaRate, as this guarantees the atomically
// accessed int64 will be aligned on ARM.  See prometheus#2666.
func newEWMARate(alpha float64, interval time.Duration) *ewmaRate {
	return &ewmaRate{
		alpha:    alpha,
		interval: interval,
	}
}

// rate returns the per-second rate.
func (r *ewmaRate) rate() float64 {
	r.mutex.Lock()
	defer r.mutex.Unlock()
	return r.lastRate
}

// tick assumes to be called every r.interval.
// tick 读取 interval 内事件数并更新 EWMA 平滑速率。
func (r *ewmaRate) tick() {
	newEvents := r.newEvents.Swap(0)
	instantRate := float64(newEvents) / r.interval.Seconds()

	r.mutex.Lock()
	defer r.mutex.Unlock()

	switch {
	case r.init:
		r.lastRate += r.alpha * (instantRate - r.lastRate)
	case newEvents > 0:
		r.init = true
		r.lastRate = instantRate
	}
}

// inc counts one event.
// incr 原子累加 interval 内的事件计数。
func (r *ewmaRate) incr(incr int64) {
	r.newEvents.Add(incr)
}
