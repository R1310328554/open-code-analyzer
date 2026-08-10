package util //nolint:revive

// util 包 PriorityQueue 基于 container/heap 实现线程安全优先级队列：同 Key 去重、Cond 阻塞 Dequeue，可选 Gauge 跟踪队列长度。

import (
	"container/heap"
	"sync"

	"github.com/prometheus/client_golang/prometheus"
)

// PriorityQueue 用 hit map 防止同一 Op.Key 重复入队，Priority 越大越先出队。
// PriorityQueue is a priority queue.
type PriorityQueue struct {
	lock        sync.Mutex
	cond        *sync.Cond
	closing     bool
	closed      bool
	hit         map[string]struct{}
	queue       queue
	lengthGauge prometheus.Gauge
}

// Op 接口抽象待调度任务，Key 用于去重，Priority 为 int64 优先级。
// Op is an operation on the priority queue.
type Op interface {
	Key() string
	Priority() int64 // The larger the number the higher the priority.
}

type queue []Op

func (q queue) Len() int           { return len(q) }
func (q queue) Less(i, j int) bool { return q[i].Priority() > q[j].Priority() }
func (q queue) Swap(i, j int)      { q[i], q[j] = q[j], q[i] }

// Push and Pop use pointer receivers because they modify the slice's length,
// not just its contents.
func (q *queue) Push(x interface{}) {
	*q = append(*q, x.(Op))
}

func (q *queue) Pop() interface{} {
	old := *q
	n := len(old)
	x := old[n-1]
	*q = old[0 : n-1]
	return x
}

// NewPriorityQueue makes a new priority queue.
func NewPriorityQueue(lengthGauge prometheus.Gauge) *PriorityQueue {
	pq := &PriorityQueue{
		hit:         map[string]struct{}{},
		lengthGauge: lengthGauge,
	}
	pq.cond = sync.NewCond(&pq.lock)
	heap.Init(&pq.queue)
	return pq
}

// Length returns the length of the queue.
func (pq *PriorityQueue) Length() int {
	pq.lock.Lock()
	defer pq.lock.Unlock()
	return len(pq.queue)
}

// Close 设置 closing 并 Broadcast；空队列时 Dequeue 返回 nil 表示优雅结束。
// Close signals that the queue should be closed when it is empty.
// A closed queue will not accept new items.
func (pq *PriorityQueue) Close() {
	pq.lock.Lock()
	defer pq.lock.Unlock()
	pq.closing = true
	pq.cond.Broadcast()
}

// DiscardAndClose closes the queue and removes all the items from it.
func (pq *PriorityQueue) DiscardAndClose() {
	pq.lock.Lock()
	defer pq.lock.Unlock()
	pq.closed = true
	pq.queue = nil
	pq.hit = map[string]struct{}{}
	pq.cond.Broadcast()
}

// Enqueue 在 closed 前 panic；重复 Key 返回 false 且不改变堆顺序。
// Enqueue adds an operation to the queue in priority order. Returns
// true if added; false if the operation was already on the queue.
func (pq *PriorityQueue) Enqueue(op Op) bool {
	pq.lock.Lock()
	defer pq.lock.Unlock()

	if pq.closed {
		panic("enqueue on closed queue")
	}

	_, enqueued := pq.hit[op.Key()]
	if enqueued {
		return false
	}

	pq.hit[op.Key()] = struct{}{}
	heap.Push(&pq.queue, op)
	pq.cond.Broadcast()
	if pq.lengthGauge != nil {
		pq.lengthGauge.Inc()
	}
	return true
}

// Dequeue 在 closing/closed 且队列为空时置 closed=true 并返回 nil。
// Dequeue will return the op with the highest priority; block if queue is
// empty; returns nil if queue is closed.
func (pq *PriorityQueue) Dequeue() Op {
	pq.lock.Lock()
	defer pq.lock.Unlock()

	for len(pq.queue) == 0 && (!pq.closing && !pq.closed) {
		pq.cond.Wait()
	}

	if len(pq.queue) == 0 && (pq.closing || pq.closed) {
		pq.closed = true
		return nil
	}

	op := heap.Pop(&pq.queue).(Op)
	delete(pq.hit, op.Key())
	if pq.lengthGauge != nil {
		pq.lengthGauge.Dec()
	}
	return op
}
// DiscardAndClose 立即清空堆与 hit，适合取消全部待处理任务并唤醒阻塞消费者。
