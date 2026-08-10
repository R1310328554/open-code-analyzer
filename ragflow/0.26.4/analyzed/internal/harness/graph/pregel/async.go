// Package pregel 为 Pregel 图执行提供异步并发管道。
//
// 包含 AsyncExecutor（受控并发任务执行）、AsyncPipeline（Pregel 循环异步管线）、
// ConcurrencyLimiter（按节点限流）与 PriorityExecutor（优先级调度）。
package pregel

import (
	"context"
	"fmt"
	"sync"
	"time"

	"github.com/google/uuid"
	"ragflow/internal/harness/graph/types"
)

// AsyncExecutor 异步执行节点函数，通过 worker 池控制最大并发数。
type AsyncExecutor struct {
	maxConcurrency int // 最大并发 worker 数 // 最大并发 worker 数
	workerPool     chan struct{} // worker 令牌池 // worker 令牌池
	results        chan *asyncTaskResult // 结果广播通道 // 结果广播通道
	mu             sync.Mutex
	activeTasks    map[string]*asyncTask // 活跃任务索引 // 活跃任务索引
}

// asyncTask 表示一个异步执行任务。
type asyncTask struct {
	ID       string // 任务 UUID // 任务 UUID
	Name     string // 任务/节点名称 // 任务/节点名称
	Func     func(context.Context) (any, error)
	Context  context.Context
	Cancel   context.CancelFunc
	Priority int // 优先级（数值越大越高） // 优先级（数值越大越高）
}

// asyncTaskResult 封装异步任务的执行结果与耗时。
type asyncTaskResult struct {
	TaskID   string
	Name     string
	Output   any
	Err      error
	Duration time.Duration
}

// NewAsyncExecutor 创建异步执行器；maxConcurrency≤0 时默认 10。
func NewAsyncExecutor(maxConcurrency int) *AsyncExecutor {
	if maxConcurrency <= 0 {
		maxConcurrency = 10 // Default concurrency
	}

	exec := &AsyncExecutor{
		maxConcurrency: maxConcurrency,
		workerPool:     make(chan struct{}, maxConcurrency),
		results:        make(chan *asyncTaskResult, 100),
		activeTasks:    make(map[string]*asyncTask),
	}

	// Pre-fill worker pool
	for i := 0; i < maxConcurrency; i++ {
		exec.workerPool <- struct{}{}
	}

	return exec
}

// Execute 异步执行单个任务，返回带缓冲的结果通道。
func (e *AsyncExecutor) Execute(ctx context.Context, name string, fn func(context.Context) (any, error)) <-chan *asyncTaskResult {
	resultCh := make(chan *asyncTaskResult, 1)

	// Create cancellable context so Cancel() can stop running tasks.
	taskCtx, cancel := context.WithCancel(ctx)
	task := &asyncTask{
		ID:      uuid.New().String(),
		Name:    name,
		Func:    fn,
		Context: taskCtx,
		Cancel:  cancel,
	}

	e.mu.Lock()
	e.activeTasks[task.ID] = task
	e.mu.Unlock()

	go func() {
		defer close(resultCh)
		// Remove from activeTasks on ALL exit paths (success, ctx cancelled, etc.).
		defer func() {
			e.mu.Lock()
			delete(e.activeTasks, task.ID)
			e.mu.Unlock()
		}()

		startTime := time.Now()

		// Acquire worker slot
		select {
		case <-e.workerPool:
			defer func() { e.workerPool <- struct{}{} }()
		case <-ctx.Done():
			resultCh <- &asyncTaskResult{
				TaskID: task.ID,
				Name:   task.Name,
				Err:    ctx.Err(),
			}
			return
		}

		// Execute task
		output, err := task.Func(task.Context)

		result := &asyncTaskResult{
			TaskID:   task.ID,
			Name:     task.Name,
			Output:   output,
			Err:      err,
			Duration: time.Since(startTime),
		}

		resultCh <- result
	}()

	return resultCh
}

// ExecuteBatch 批量并发执行任务，受 worker 池容量限制。
func (e *AsyncExecutor) ExecuteBatch(ctx context.Context, tasks []asyncTask) <-chan *asyncTaskResult {
	resultCh := make(chan *asyncTaskResult, len(tasks))

	// Register all tasks in activeTasks before any goroutine starts.
	for i := range tasks {
		tasks[i].ID = uuid.New().String()
		e.mu.Lock()
		e.activeTasks[tasks[i].ID] = &tasks[i]
		e.mu.Unlock()
	}

	var wg sync.WaitGroup
	for i := range tasks {
		wg.Add(1)
		go func(task *asyncTask) {
			defer wg.Done()
			defer func() {
				e.mu.Lock()
				delete(e.activeTasks, task.ID)
				e.mu.Unlock()
			}()

			startTime := time.Now()

			// Acquire worker slot
			select {
			case <-e.workerPool:
				defer func() { e.workerPool <- struct{}{} }()
			case <-ctx.Done():
				resultCh <- &asyncTaskResult{
					TaskID: task.ID,
					Name:   task.Name,
					Err:    ctx.Err(),
				}
				return
			}

			// Execute task
			output, err := task.Func(task.Context)

			resultCh <- &asyncTaskResult{
				TaskID:   task.ID,
				Name:     task.Name,
				Output:   output,
				Err:      err,
				Duration: time.Since(startTime),
			}
		}(&tasks[i])
	}

	go func() {
		wg.Wait()
		close(resultCh)
	}()

	return resultCh
}

// ExecuteWithRetry 在异步执行中集成 RetryExecutor 重试逻辑。
func (e *AsyncExecutor) ExecuteWithRetry(ctx context.Context, name string, fn func(context.Context) (any, error), retryConfig *RetryConfig) <-chan *asyncTaskResult {
	resultCh := make(chan *asyncTaskResult, 1)

	taskCtx, cancel := context.WithCancel(ctx)
	task := &asyncTask{
		ID:      uuid.New().String(),
		Name:    name,
		Context: taskCtx,
		Cancel:  cancel,
	}

	e.mu.Lock()
	e.activeTasks[task.ID] = task
	e.mu.Unlock()

	go func() {
		defer close(resultCh)

		executor := NewRetryExecutor(retryConfig.Policy)

		startTime := time.Now()
		output, err := executor.Execute(task.Context, name, fn)

		result := &asyncTaskResult{
			TaskID:   task.ID,
			Name:     name,
			Output:   output,
			Err:      err,
			Duration: time.Since(startTime),
		}

		e.mu.Lock()
		delete(e.activeTasks, task.ID)
		e.mu.Unlock()

		resultCh <- result
	}()

	return resultCh
}

// Cancel 取消所有活跃任务（调用各任务的 context.CancelFunc）。
func (e *AsyncExecutor) Cancel() {
	e.mu.Lock()
	defer e.mu.Unlock()

	for id, task := range e.activeTasks {
		if task.Cancel != nil {
			task.Cancel()
		}
		delete(e.activeTasks, id)
	}
}

// GetActiveTaskCount 返回当前活跃任务数量。
func (e *AsyncExecutor) GetActiveTaskCount() int {
	e.mu.Lock()
	defer e.mu.Unlock()
	return len(e.activeTasks)
}

// Wait 轮询等待所有活跃任务完成，ctx 取消时返回错误。
func (e *AsyncExecutor) Wait(ctx context.Context) error {
	ticker := time.NewTicker(10 * time.Millisecond)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-ticker.C:
			if e.GetActiveTaskCount() == 0 {
				return nil
			}
		}
	}
}

// AsyncPipeline 为 Pregel 主循环提供异步执行管线，含事件/错误通道。
type AsyncPipeline struct {
	executor *AsyncExecutor // 底层异步执行器 // 底层异步执行器
	retryer  *RetryExecutor // 重试执行器 // 重试执行器

	// Stream channels
	events chan any // 管线事件通道 // 管线事件通道
	errors chan error // 管线错误通道 // 管线错误通道

	// Control
	mu      sync.RWMutex
	cancel  context.CancelFunc
	running bool // 管线是否运行中 // 管线是否运行中
}

// NewAsyncPipeline 创建异步管线，绑定 RetryExecutor。
func NewAsyncPipeline(maxConcurrency int, retryPolicy *types.RetryPolicy) *AsyncPipeline {
	return &AsyncPipeline{
		executor: NewAsyncExecutor(maxConcurrency),
		retryer:  NewRetryExecutor(retryPolicy),
		events:   make(chan any, 100),
		errors:   make(chan error, 10),
		running:  false,
	}
}

// Start 启动管线，重建事件通道并返回可取消的子 context。
func (p *AsyncPipeline) Start(ctx context.Context) context.Context {
	p.mu.Lock()
	defer p.mu.Unlock()

	if p.running {
		return ctx
	}

	// Reinitialize channels that were closed by Stop().
	p.events = make(chan any, 100)
	p.errors = make(chan error, 10)

	ctx, p.cancel = context.WithCancel(ctx)
	p.running = true

	return ctx
}

// Stop 停止管线：取消 context、取消所有任务、关闭通道。
func (p *AsyncPipeline) Stop() {
	p.mu.Lock()
	defer p.mu.Unlock()

	if !p.running {
		return
	}

	if p.cancel != nil {
		p.cancel()
	}

	p.executor.Cancel()
	close(p.events)
	close(p.errors)
	p.running = false
}

// ExecuteNode 在管线中执行节点，可选重试配置。
func (p *AsyncPipeline) ExecuteNode(ctx context.Context, name string, fn func(context.Context) (any, error), retryConfig *RetryConfig) <-chan *asyncTaskResult {
	if retryConfig != nil {
		return p.executor.ExecuteWithRetry(ctx, name, fn, retryConfig)
	}
	return p.executor.Execute(ctx, name, fn)
}

// Events 返回事件广播通道（只读）。
func (p *AsyncPipeline) Events() <-chan any {
	return p.events
}

// Errors 返回错误广播通道（只读）。
func (p *AsyncPipeline) Errors() <-chan error {
	return p.errors
}

// EmitEvent 非阻塞发送事件；通道满时丢弃。
func (p *AsyncPipeline) EmitEvent(event any) {
	p.mu.RLock()
	defer p.mu.RUnlock()

	if p.running {
		select {
		case p.events <- event:
		default:
			// Channel full, drop event
		}
	}
}

// EmitError 非阻塞发送错误；通道满时丢弃。
func (p *AsyncPipeline) EmitError(err error) {
	p.mu.RLock()
	defer p.mu.RUnlock()

	if p.running {
		select {
		case p.errors <- err:
		default:
			// Channel full, drop error
		}
	}
}

// IsRunning 返回管线是否处于运行状态。
func (p *AsyncPipeline) IsRunning() bool {
	p.mu.RLock()
	defer p.mu.RUnlock()
	return p.running
}

// ConcurrencyLimiter 为指定节点设置独立并发上限。
type ConcurrencyLimiter struct {
	limits map[string]chan struct{} // 节点→令牌通道 // 节点→令牌通道
	mu     sync.RWMutex
}

// NewConcurrencyLimiter 创建空的节点级并发限流器。
func NewConcurrencyLimiter() *ConcurrencyLimiter {
	return &ConcurrencyLimiter{
		limits: make(map[string]chan struct{}),
	}
}

// SetLimit 为节点预填充令牌通道，限制同时执行数。
func (l *ConcurrencyLimiter) SetLimit(node string, limit int) {
	l.mu.Lock()
	defer l.mu.Unlock()

	ch := make(chan struct{}, limit)
	for i := 0; i < limit; i++ {
		ch <- struct{}{}
	}
	l.limits[node] = ch
}

// Acquire 获取节点执行槽位，ctx 取消时返回错误。
func (l *ConcurrencyLimiter) Acquire(ctx context.Context, node string) error {
	l.mu.RLock()
	ch, ok := l.limits[node]
	l.mu.RUnlock()

	if !ok {
		// No limit set
		return nil
	}

	select {
	case <-ch:
		return nil
	case <-ctx.Done():
		return ctx.Err()
	}
}

// Release 归还节点执行槽位。
func (l *ConcurrencyLimiter) Release(node string) {
	l.mu.RLock()
	ch, ok := l.limits[node]
	l.mu.RUnlock()

	if !ok {
		return
	}

	select {
	case ch <- struct{}{}:
	default:
		// Channel full, shouldn't happen
	}
}

// PriorityTask 带优先级的可执行任务。
type PriorityTask struct {
	Func     func(context.Context) (any, error)
	Priority int
}

// PriorityExecutor 按优先级调度任务（简化实现）。
type PriorityExecutor struct {
	tasks chan PriorityTask
	mu    sync.Mutex
}

// NewPriorityExecutor 创建优先级执行器。
func NewPriorityExecutor(bufferSize int) *PriorityExecutor {
	return &PriorityExecutor{
		tasks: make(chan PriorityTask, bufferSize),
	}
}

// Submit 提交带优先级的任务；队列满时返回错误。
func (e *PriorityExecutor) Submit(task PriorityTask) error {
	select {
	case e.tasks <- task:
		return nil
	default:
		return fmt.Errorf("task queue full")
	}
}

// Execute 从任务队列取出并执行任务，结果写入通道。
func (e *PriorityExecutor) Execute(ctx context.Context, maxConcurrency int) <-chan any {
	resultCh := make(chan any, maxConcurrency)

	// Simple priority scheduling using multiple channels
	// In a real implementation, you'd use a priority queue
	go func() {
		defer close(resultCh)

		// This is a simplified implementation
		// A full implementation would use a heap-based priority queue
		for {
			select {
			case <-ctx.Done():
				return
			case task := <-e.tasks:
				output, err := task.Func(ctx)
				resultCh <- map[string]any{
					"output":   output,
					"error":    err,
					"priority": task.Priority,
				}
			}
		}
	}()

	return resultCh
}
