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

// chunk 写入任务队列：分段环形缓冲替代大容量 channel，空闲时释放段引用以便 GC 回收内存。

package chunks

import "sync"

// writeJobQueue 用等长 segment 链表模拟有界 channel，push/pop 通过 Cond 阻塞同步。
// writeJobQueue is similar to buffered channel of chunkWriteJob, but manages its own buffers
// to avoid using a lot of memory when it's empty. It does that by storing elements into segments
// of equal size (segmentSize). When segment is not used anymore, reference to it are removed,
// so it can be treated as a garbage.
type writeJobQueue struct {
	maxSize     int
	segmentSize int

	mtx            sync.Mutex            // protects all following variables
	pushed, popped *sync.Cond            // signalled when something is pushed into the queue or popped from it
	first, last    *writeJobQueueSegment // pointer to first and last segment, if any
	size           int                   // total size of the queue
	closed         bool                  // after closing the queue, nothing can be pushed to it
}

// writeJobQueueSegment 保存固定容量 segment 及段内读写游标与下一段指针。
type writeJobQueueSegment struct {
	segment             []chunkWriteJob
	nextRead, nextWrite int                   // index of next read and next write in this segment.
	nextSegment         *writeJobQueueSegment // next segment, if any
}

// newWriteJobQueue 校验参数并初始化 pushed/popped 条件变量。
func newWriteJobQueue(maxSize, segmentSize int) *writeJobQueue {
	if maxSize <= 0 || segmentSize <= 0 {
		panic("invalid queue")
	}

	q := &writeJobQueue{
		maxSize:     maxSize,
		segmentSize: segmentSize,
	}

	q.pushed = sync.NewCond(&q.mtx)
	q.popped = sync.NewCond(&q.mtx)
	return q
}

// close 标记队列关闭并 Broadcast 唤醒所有阻塞的 push/pop。
func (q *writeJobQueue) close() {
	q.mtx.Lock()
	defer q.mtx.Unlock()

	q.closed = true

	// Unblock all blocked goroutines.
	q.pushed.Broadcast()
	q.popped.Broadcast()
}

// push 在队列满时等待 pop 腾出空间；关闭后返回 false。
// push blocks until there is space available in the queue, and then adds job to the queue.
// If queue is closed or gets closed while waiting for space, push returns false.
func (q *writeJobQueue) push(job chunkWriteJob) bool {
	q.mtx.Lock()
	defer q.mtx.Unlock()

	// Wait until queue has more space or is closed.
	for !q.closed && q.size >= q.maxSize {
		q.popped.Wait()
	}

	if q.closed {
		return false
	}

	// Check if this segment has more space for writing, and create new one if not.
	if q.last == nil || q.last.nextWrite >= q.segmentSize {
		prevLast := q.last
		q.last = &writeJobQueueSegment{
			segment: make([]chunkWriteJob, q.segmentSize),
		}

		if prevLast != nil {
			prevLast.nextSegment = q.last
		}
		if q.first == nil {
			q.first = q.last
		}
	}

	q.last.segment[q.last.nextWrite] = job
	q.last.nextWrite++
	q.size++
	q.pushed.Signal()
	return true
}

// pop FIFO 取任务；关闭后会排空剩余元素再返回 false。
// pop returns first job from the queue, and true.
// If queue is empty, pop blocks until there is a job (returns true), or until queue is closed (returns false).
// If queue was already closed, pop first returns all remaining elements from the queue (with true value), and only then returns false.
func (q *writeJobQueue) pop() (chunkWriteJob, bool) {
	q.mtx.Lock()
	defer q.mtx.Unlock()

	// wait until something is pushed to the queue, or queue is closed.
	for q.size == 0 {
		if q.closed {
			return chunkWriteJob{}, false
		}

		q.pushed.Wait()
	}

	res := q.first.segment[q.first.nextRead]
	q.first.segment[q.first.nextRead] = chunkWriteJob{} // clear just-read element
	q.first.nextRead++
	q.size--

	// If we have read all possible elements from first segment, we can drop it.
	if q.first.nextRead >= q.segmentSize {
		q.first = q.first.nextSegment
		if q.first == nil {
			q.last = nil
		}
	}

	q.popped.Signal()
	return res, true
}

// length 返回当前队列中待处理任务总数。
// length returns number of all jobs in the queue.
func (q *writeJobQueue) length() int {
	q.mtx.Lock()
	defer q.mtx.Unlock()

	return q.size
}
