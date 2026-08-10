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

// 查询计时器：可启停的 Timer 累加运行时长，TimerGroup 按名称管理多段计时。

package stats

import (
	"bytes"
	"fmt"
	"slices"
	"time"
)

// Timer 在 Start/Stop 之间累加 duration，ElapsedTime 返回自上次 Start 起的经过时间。
// A Timer that can be started and stopped and accumulates the total time it
// was running (the time between Start() and Stop()).
type Timer struct {
	name     fmt.Stringer
	created  int
	start    time.Time
	duration time.Duration
}

// Start 记录当前时刻作为段起点。
// Start the timer.
func (t *Timer) Start() *Timer {
	t.start = time.Now()
	return t
}

// Stop 将本段 elapsed 累加到 duration。
// Stop the timer.
func (t *Timer) Stop() {
	t.duration += time.Since(t.start)
}

// ElapsedTime returns the time that passed since starting the timer.
func (t *Timer) ElapsedTime() time.Duration {
	return time.Since(t.start)
}

// Duration 返回累计秒数（浮点）。
// Duration returns the duration value of the timer in seconds.
func (t *Timer) Duration() float64 {
	return t.duration.Seconds()
}

// Return a string representation of the Timer.
func (t *Timer) String() string {
	return fmt.Sprintf("%s: %s", t.name, t.duration)
}

// TimerGroup 为单次查询维护多个命名 Timer 实例。
// A TimerGroup represents a group of timers relevant to a single query.
type TimerGroup struct {
	timers map[fmt.Stringer]*Timer
}

// NewTimerGroup 初始化空的 fmt.Stringer→Timer 映射。
// NewTimerGroup constructs a new TimerGroup.
func NewTimerGroup() *TimerGroup {
	return &TimerGroup{timers: map[fmt.Stringer]*Timer{}}
}

// GetTimer 按名称获取或懒创建 Timer，creation 顺序用于 String 排序。
// GetTimer gets (and creates, if necessary) the Timer for a given code section.
func (t *TimerGroup) GetTimer(name fmt.Stringer) *Timer {
	if timer, exists := t.timers[name]; exists {
		return timer
	}
	timer := &Timer{
		name:    name,
		created: len(t.timers),
	}
	t.timers[name] = timer
	return timer
}

// Return a string representation of a TimerGroup.
func (t *TimerGroup) String() string {
	timers := make([]*Timer, 0, len(t.timers))
	for _, timer := range t.timers {
		timers = append(timers, timer)
	}
	slices.SortFunc(timers, func(a, b *Timer) int { return a.created - b.created })
	result := &bytes.Buffer{}
	for _, timer := range timers {
		fmt.Fprintf(result, "%s\n", timer)
	}
	return result.String()
}
