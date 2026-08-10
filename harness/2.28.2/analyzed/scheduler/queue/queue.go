// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// queue 包实现构建阶段调度队列，将待执行阶段分派给匹配的 Runner worker。
package queue

import (
	"context"
	"sync"
	"time"

	"github.com/drone/drone/core"
	"github.com/drone/drone/service/redisdb"

	"github.com/drone/drone-go/drone"
)

// queue 是阶段调度核心结构，维护 worker 池与调度循环。
type queue struct {
	sync.Mutex
	globMx redisdb.LockErr

	ready    chan struct{}
	paused   bool
	interval time.Duration
	throttle int
	store    core.StageStore
	workers  map[*worker]struct{}
	ctx      context.Context
}

// newQueue 创建由 StageStore 持久化支撑的调度队列并启动后台循环。
func newQueue(ctx context.Context, store core.StageStore) *queue {
	q := &queue{
		store:    store,
		globMx:   redisdb.LockErrNoOp{},
		ready:    make(chan struct{}, 1),
		workers:  map[*worker]struct{}{},
		interval: time.Minute,
		ctx:      ctx,
	}
	go q.start()
	return q
}

// Schedule 通知调度器有新阶段待处理（非阻塞写入 ready 信号）。
func (q *queue) Schedule(ctx context.Context, stage *core.Stage) error {
	select {
	case q.ready <- struct{}{}:
	default:
	}
	return nil
}

// Pause 暂停调度，signal 将不再分派新任务。
func (q *queue) Pause(ctx context.Context) error {
	q.Lock()
	q.paused = true
	q.Unlock()
	return nil
}

// Paused 返回队列当前是否处于暂停状态。
func (q *queue) Paused(ctx context.Context) (bool, error) {
	q.Lock()
	paused := q.paused
	q.Unlock()
	return paused, nil
}

// Resume 恢复调度并触发一次 signal。
func (q *queue) Resume(ctx context.Context) error {
	q.Lock()
	q.paused = false
	q.Unlock()

	select {
	case q.ready <- struct{}{}:
	default:
	}
	return nil
}

// Request 注册一个 worker 并阻塞等待匹配的阶段任务；context 取消时返回错误。
func (q *queue) Request(ctx context.Context, params core.Filter) (*core.Stage, error) {
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()
	w := &worker{
		kind:    params.Kind,
		typ:     params.Type,
		os:      params.OS,
		arch:    params.Arch,
		kernel:  params.Kernel,
		variant: params.Variant,
		labels:  params.Labels,
		channel: make(chan *core.Stage),
		done:    ctx.Done(),
	}
	q.Lock()
	q.workers[w] = struct{}{}
	q.Unlock()

	select {
	case q.ready <- struct{}{}:
	default:
	}

	select {
	case <-ctx.Done():
		q.Lock()
		delete(q.workers, w)
		q.Unlock()
		return nil, ctx.Err()
	case b := <-w.channel:
		return b, nil
	}
}

// signal 在全局锁保护下将未完成阶段匹配并分派给空闲 worker。
func (q *queue) signal(ctx context.Context) error {
	if err := q.globMx.LockContext(ctx); err != nil {
		return err
	}
	defer q.globMx.UnlockContext(ctx)

	q.Lock()
	count := len(q.workers)
	pause := q.paused
	q.Unlock()
	if pause {
		return nil
	}
	if count == 0 {
		return nil
	}
	items, err := q.store.ListIncomplete(ctx)
	if err != nil {
		return err
	}

	q.Lock()
	defer q.Unlock()
	for _, item := range items {
		if item.Status == core.StatusRunning {
			continue
		}
		if item.Machine != "" {
			continue
		}

		// 若阶段定义了并发上限，须确认未超限后再分派
		if withinLimits(item, items) == false {
			continue
		}

		// 若系统按仓库设置了并发节流，须确认未超限后再分派
		if shouldThrottle(item, items, item.LimitRepo) == true {
			continue
		}

	loop:
		for w := range q.workers {
			// worker 的资源 kind/type 必须与阶段匹配
			if !matchResource(w.kind, w.typ, item.Kind, item.Type) {
				continue
			}

			if w.os != "" || w.arch != "" || w.variant != "" || w.kernel != "" {
				// worker 绑定了特定平台，须与阶段平台字段一致
				if w.os != item.OS {
					continue
				}
				if w.arch != item.Arch {
					continue
				}
				// 流水线若指定 variant（如 arm6/arm7），须与 worker 一致
				if item.Variant != "" && item.Variant != w.variant {
					continue
				}
				// 流水线若指定 kernel 版本（如 1709/1803），须与 worker 一致
				if item.Kernel != "" && item.Kernel != w.kernel {
					continue
				}
			}

			if len(item.Labels) > 0 || len(w.labels) > 0 {
				if !checkLabels(item.Labels, w.labels) {
					continue
				}
			}

			// // the queue has 60 seconds to ack the item, otherwise
			// // it is eligible for processing by another worker.
			// // item.Expires = time.Now().Add(time.Minute).Unix()
			// err := q.store.Update(ctx, item)

			// if err != nil {
			// 	log.Ctx(ctx).Warn().
			// 		Err(err).
			// 		Int64("build_id", item.BuildID).
			// 		Int64("stage_id", item.ID).
			// 		Msg("cannot update queue item")
			// 	continue
			// }

			// TODO: refactor to its own unexported method
			sendWork := func() bool {
				select {
				case w.channel <- item:
					return true
				case <-w.done:
					// Worker will exit when we call the deferred q.Unlock()
				case <-time.After(q.interval):
					// Worker failed to ack before timeout
				}
				return false
			}
			if sendWork() {
				delete(q.workers, w)
				break loop
			}
		}
	}
	return nil
}

// start 是调度主循环：响应 ready 信号或定时 tick 调用 signal。
func (q *queue) start() error {
	for {
		select {
		case <-q.ctx.Done():
			return q.ctx.Err()
		case <-q.ready:
			q.signal(q.ctx)
		case <-time.After(q.interval):
			q.signal(q.ctx)
		}
	}
}

// worker 表示一个等待任务的 Runner，携带平台过滤条件与任务 channel。
type worker struct {
	kind    string
	typ     string
	os      string
	arch    string
	kernel  string
	variant string
	labels  map[string]string
	channel chan *core.Stage
	done    <-chan struct{}
}

// counter 用于统计各键出现次数（当前未使用，保留供扩展）。
type counter struct {
	counts map[string]int
}

// checkLabels 判断阶段标签与 worker 标签是否完全一致。
func checkLabels(a, b map[string]string) bool {
	if len(a) != len(b) {
		return false
	}
	for k, v := range a {
		if w, ok := b[k]; !ok || v != w {
			return false
		}
	}
	return true
}

// withinLimits 检查同名阶段并发数是否低于阶段 Limit 配置。
func withinLimits(stage *core.Stage, siblings []*core.Stage) bool {
	if stage.Limit == 0 {
		return true
	}
	count := 0
	for _, sibling := range siblings {
		if sibling.RepoID != stage.RepoID {
			continue
		}
		if sibling.ID == stage.ID {
			continue
		}
		if sibling.Name != stage.Name {
			continue
		}
		if sibling.ID < stage.ID ||
			sibling.Status == core.StatusRunning {
			count++
		}
	}
	return count < stage.Limit
}

// shouldThrottle 按仓库级 LimitRepo 判断当前阶段是否应被节流跳过。
func shouldThrottle(stage *core.Stage, siblings []*core.Stage, limit int) bool {
	// 未配置节流上限时无需节流
	if limit == 0 {
		return false
	}
	// 已在运行的阶段无法跳过
	if stage.Status == drone.StatusRunning {
		return false
	}

	count := 0
	// 统计同一仓库中 ID 更小的运行中/待处理阶段数量
	for _, sibling := range siblings {
		if sibling.RepoID != stage.RepoID {
			continue
		}
		if sibling.ID >= stage.ID {
			continue
		}
		count++
	}
	return count >= limit
}

// matchResource 比较 worker 与阶段的资源 kind/type，空值使用默认值。
func matchResource(kinda, typea, kindb, typeb string) bool {
	if kinda == "" {
		kinda = "pipeline"
	}
	if kindb == "" {
		kindb = "pipeline"
	}
	if typea == "" {
		typea = "docker"
	}
	if typeb == "" {
		typeb = "docker"
	}
	return kinda == kindb && typea == typeb
}
