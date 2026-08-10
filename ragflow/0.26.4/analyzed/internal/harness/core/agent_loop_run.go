// agent_loop_run.go — AgentLoop 主循环：planTurn、run 与默认事件消费。

package core

import (
	"context"
	"errors"
	"sync/atomic"
	"time"
)

// ---- AgentLoop 主循环与轮次规划 ----

// planTurn 调用 GenInput/GenResume 组装 turnPlan 与 turnRunSpec。
func (l *AgentLoop[T]) planTurn(
	ctx context.Context,
	isResume bool,
	items []T,
	pr *agentLoopPendingResume[T],
) (*turnPlan[T], error) {
	if !isResume {
		result, err := l.config.GenInput(ctx, l, items)
		if err != nil {
			return nil, err
		}
		if result == nil {
			return nil, errors.New("GenInputResult is nil")
		}
		if result.Input == nil {
			return nil, errors.New("agent input is nil")
		}
		turnCtx := ctx
		if result.RunCtx != nil {
			turnCtx = result.RunCtx
		}
		return &turnPlan[T]{
			turnCtx:   turnCtx,
			remaining: result.Remaining,
			spec: &turnRunSpec[T]{
				runCtx:   result.RunCtx,
				input:    result.Input,
				runOpts:  result.RunOpts,
				consumed: result.Consumed,
			},
		}, nil
	}
	if pr == nil {
		return nil, errors.New("resume payload is nil")
	}
	if l.config.GenResume == nil {
		return nil, errors.New("GenResume is required for resume")
	}
	resumeResult, err := l.config.GenResume(ctx, l, pr.interrupted, pr.unhandled, pr.newItems)
	if err != nil {
		return nil, err
	}
	if resumeResult == nil {
		return nil, errors.New("GenResumeResult is nil")
	}
	turnCtx := ctx
	if resumeResult.RunCtx != nil {
		turnCtx = resumeResult.RunCtx
	}
	return &turnPlan[T]{
		turnCtx:   turnCtx,
		remaining: resumeResult.Remaining,
		spec: &turnRunSpec[T]{
			runCtx:       resumeResult.RunCtx,
			runOpts:      resumeResult.RunOpts,
			resumeParams: resumeResult.ResumeParams,
			isResume:     true,
			consumed:     resumeResult.Consumed,
			resumeBytes:  pr.resumeBytes,
		},
	}, nil
}

// defaultTurnLoopOnAgentEvents 默认消费事件直至结束或首个 Err。
func defaultTurnLoopOnAgentEvents[T any](_ context.Context, _ *TurnContext[T], events *AsyncIterator[*AgentEvent]) error {
	for {
		event, ok := events.Next()
		if !ok {
			break
		}
		if event.Err != nil {
			return event.Err
		}
	}
	return nil
}

// run 主循环：加载检查点→取项→规划→PrepareAgent→runAgentAndHandleEvents。
func (l *AgentLoop[T]) run(ctx context.Context) {
	defer l.cleanup(ctx)

	if err := l.tryLoadCheckpoint(ctx); err != nil {
		l.runErr = err
		return
	}

	// 监听 ctx 取消：关闭 buffer 以唤醒阻塞的 Receive
	// Receive() unblocks.
	go func() {
		select {
		case <-ctx.Done():
			l.buffer.Close()
		case <-l.done:
		}
	}()

	for {
		if l.stopCtrl.isCommitted() {
			return
		}

		isResume := false
		var pr *agentLoopPendingResume[T]
		var items []T
		var pushBack []T

		if l.pendingResume != nil {
			isResume = true
			pr = l.pendingResume
			l.pendingResume = nil

			l.preemptCtrl.waitForPushes()
			pr.newItems = append(pr.newItems, l.buffer.TakeAll()...)

			pushBack = make([]T, 0, len(pr.interrupted)+len(pr.unhandled)+len(pr.newItems))
			pushBack = append(pushBack, pr.interrupted...)
			pushBack = append(pushBack, pr.unhandled...)
			pushBack = append(pushBack, pr.newItems...)
		} else {
			var first T
			var ok bool

			if idleFor := l.stopCtrl.idleDuration(); idleFor > 0 {
				l.buffer.ClearWakeup()
				idleTimer := time.NewTimer(idleFor)
				cancelIdle := make(chan struct{})
				go func() {
					select {
					case <-idleTimer.C:
						l.commitStop()
					case <-cancelIdle:
					}
				}()

				first, ok = l.buffer.Receive()

				// 排空 timer 通道避免与 commitStop 竞态
				if !idleTimer.Stop() {
					select {
					case <-idleTimer.C:
					default:
					}
				}
				close(cancelIdle)

				if !ok && !l.buffer.IsClosed() {
					if err := ctx.Err(); err != nil {
						l.runErr = err
						return
					}
					continue
				}

				// idle 定时器触发 commitStop 则退出循环
				if atomic.LoadInt32(&l.stopped) != 0 {
					return
				}
			} else {
				first, ok = l.buffer.Receive()
				if !ok && l.stopCtrl.idleDuration() > 0 {
					continue
				}
			}

			if !ok {
				if err := ctx.Err(); err != nil {
					l.runErr = err
				}
				return
			}

			if err := ctx.Err(); err != nil {
				l.buffer.PushFront([]T{first})
				l.runErr = err
				return
			}

			if l.stopCtrl.isCommitted() {
				l.buffer.PushFront([]T{first})
				return
			}

			l.preemptCtrl.waitForPushes()
			rest := l.buffer.TakeAll()
			items = append([]T{first}, rest...)
			pushBack = items
		}

		l.preemptCtrl.beginPlanningTurn()
		abortPlanning := func() {
			l.preemptCtrl.abortPlanningTurn().ack()
		}

		plan, err := l.planTurn(ctx, isResume, items, pr)
		if err != nil {
			abortPlanning()
			if len(pushBack) > 0 {
				l.buffer.PushFront(pushBack)
			}
			l.runErr = err
			return
		}

		if l.stopCtrl.isCommitted() {
			abortPlanning()
			if len(pushBack) > 0 {
				l.buffer.PushFront(pushBack)
			}
			return
		}

		agent, err := l.config.PrepareAgent(plan.turnCtx, l, plan.spec.consumed)
		if err != nil {
			abortPlanning()
			if len(pushBack) > 0 {
				l.buffer.PushFront(pushBack)
			}
			l.runErr = err
			return
		}

		if l.stopCtrl.isCommitted() {
			abortPlanning()
			if len(pushBack) > 0 {
				l.buffer.PushFront(pushBack)
			}
			return
		}

		l.buffer.PushFront(plan.remaining)

		runErr := l.runAgentAndHandleEvents(plan.turnCtx, agent, plan.spec)

		if runErr != nil {
			if l.capturedCancelErr != nil || l.interruptContexts != nil {
				// 故意赋值而非 append：仅保留中断轮的 consumed 项
				// turn's consumed items matter — the loop exits immediately after.
				l.interruptedItems = append([]T{}, plan.spec.consumed...)
			}
			l.runErr = runErr
			return
		}

		// 业务中断：Agent 产生 Interrupted action
		if l.interruptContexts != nil {
			l.interruptedItems = append([]T{}, plan.spec.consumed...)
			l.runErr = &InterruptError{InterruptContexts: l.interruptContexts}
			return
		}
	}
}

// pendingResume 路径合并 buffer 中晚到的 Push；plan 失败或 stop 时将 pushBack 还回队首。
