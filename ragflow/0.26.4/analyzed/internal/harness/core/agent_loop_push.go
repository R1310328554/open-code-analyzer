// agent_loop_push.go — AgentLoop Push 入队：普通/抢占/策略三种路径。

package core

import (
	"context"
	"sync/atomic"
	"time"
)

// ---- AgentLoop Push 操作 ----

// appendLate 循环已停止时将项记入 lateItems（TakeLateItems 前可追加）。
func (l *AgentLoop[T]) appendLate(item T) {
	l.lateMu.Lock()
	defer l.lateMu.Unlock()
	if l.lateSealed {
		panic("AgentLoop: Push called after TakeLateItems")
	}
	l.lateItems = append(l.lateItems, item)
}

// Push 将元素加入循环缓冲等待处理。
// 循环已停止返回 false；抢占模式返回 ack 通道。
func (l *AgentLoop[T]) Push(item T, opts ...PushOption[T]) (bool, <-chan struct{}) {
	cfg := &pushConfig[T]{}
	for _, opt := range opts {
		opt(cfg)
	}

	if cfg.pushStrategy != nil {
		return l.pushWithStrategy(item, cfg)
	}

	return l.pushWithConfig(item, cfg)
}

// pushWithStrategy 快照目标轮后由策略决定如何入队与是否抢占
// （策略可返回新的 PushOption）。
//
// 空闲时 snapshot.ctx 为 nil，策略收到 context.TODO()
// 无法感知调用方取消或 deadline；需 ctx 时请用闭包传入
// （尚未提供带 ctx 的 Push 重载）。
// that accepts ctx (not yet available; pass via closure instead).
// pushWithStrategy 执行自定义 pushStrategy 后再走抢占或普通入队。
func (l *AgentLoop[T]) pushWithStrategy(item T, cfg *pushConfig[T]) (bool, <-chan struct{}) {
	strategy := cfg.pushStrategy

	snapshot := l.preemptCtrl.beginPush()
	defer l.preemptCtrl.endPush()

	runCtx := snapshot.ctx
	if runCtx == nil {
		runCtx = context.TODO()
	}
	var tc *TurnContext[T]
	if snapshot.tc != nil {
		tc = snapshot.tc.(*TurnContext[T])
	}
	realOpts := strategy(runCtx, tc)
	cfg = &pushConfig[T]{}
	for _, opt := range realOpts {
		opt(cfg)
	}
	cfg.pushStrategy = nil

	if !cfg.preempt {
		if !l.buffer.TrySend(item) {
			l.appendLate(item)
			return false, nil
		}
		return true, nil
	}

	if atomic.LoadInt32(&l.stopped) != 0 {
		l.appendLate(item)
		return false, nil
	}

	if !l.buffer.TrySend(item) {
		l.appendLate(item)
		return false, nil
	}

	ack := make(chan struct{})
	if atomic.LoadInt32(&l.started) == 0 {
		close(ack)
		return true, ack
	}

	if cfg.preemptDelay > 0 {
		go func() {
			select {
			case <-time.After(cfg.preemptDelay):
				l.preemptCtrl.requestPreempt(snapshot, ack, cfg.agentCancelOpts...)
			case <-l.done:
				close(ack)
			}
		}()
	} else {
		l.preemptCtrl.requestPreempt(snapshot, ack, cfg.agentCancelOpts...)
	}
	return true, ack
}

// pushWithConfig 按 cfg.preempt 决定是否发起抢占。
func (l *AgentLoop[T]) pushWithConfig(item T, cfg *pushConfig[T]) (bool, <-chan struct{}) {
	if atomic.LoadInt32(&l.stopped) != 0 {
		l.appendLate(item)
		return false, nil
	}

	if cfg.preempt {
		snapshot := l.preemptCtrl.beginPush()
		defer l.preemptCtrl.endPush()

		if !l.buffer.TrySend(item) {
			l.appendLate(item)
			return false, nil
		}

		ack := make(chan struct{})
		if atomic.LoadInt32(&l.started) == 0 {
			close(ack)
			return true, ack
		}

		if cfg.preemptDelay > 0 {
			go func() {
				select {
				case <-time.After(cfg.preemptDelay):
					l.preemptCtrl.requestPreempt(snapshot, ack, cfg.agentCancelOpts...)
				case <-l.done:
					close(ack)
				}
			}()
		} else {
			l.preemptCtrl.requestPreempt(snapshot, ack, cfg.agentCancelOpts...)
		}
		return true, ack
	}

	if !l.buffer.TrySend(item) {
		l.appendLate(item)
		return false, nil
	}
	return true, nil
}

// 未启动时 ack 立即 close；preemptDelay 在 goroutine 中延迟 requestPreempt。
