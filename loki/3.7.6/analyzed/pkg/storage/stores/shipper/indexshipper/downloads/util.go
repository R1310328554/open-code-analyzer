package downloads

// util 提供 mtxWithReadiness：在 IndexSet Init 完成前阻塞加锁，避免查询与 sync 在索引未就绪时访问 map。

import (
	"context"
	"errors"
	"sync"
	"time"
)

// mtxWithReadiness 通过关闭 ready channel 一次性标记初始化完成。
// mtxWithReadiness combines a mutex with readiness channel. It would acquire lock only when the channel is closed to mark it ready.
type mtxWithReadiness struct {
	mtx   sync.RWMutex
	ready chan struct{}
}

func newMtxWithReadiness() *mtxWithReadiness {
	return &mtxWithReadiness{
		ready: make(chan struct{}),
	}
}

func (m *mtxWithReadiness) markReady() {
	close(m.ready)
}

func (m *mtxWithReadiness) isReady() bool {
	select {
	case <-m.ready:
		return true
	default:
		return false
	}
}

// awaitReady 最多等待 30 秒直到 Init defer 中 markReady，超时返回 context 错误。
func (m *mtxWithReadiness) awaitReady(ctx context.Context) error {
	ctx, cancel := context.WithTimeoutCause(ctx, 30*time.Second, errors.New("exceeded 30 seconds in awaitReady"))
	defer cancel()

	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-m.ready:
		return nil
	}
}

// lock 先 awaitReady 再获取写锁，供 DropAllDBs 与 sync 更新 index map 使用。
func (m *mtxWithReadiness) lock(ctx context.Context) error {
	err := m.awaitReady(ctx)
	if err != nil {
		return err
	}

	m.mtx.Lock()
	return nil
}

func (m *mtxWithReadiness) unlock() {
	m.mtx.Unlock()
}

func (m *mtxWithReadiness) rLock(ctx context.Context) error {
	err := m.awaitReady(ctx)
	if err != nil {
		return err
	}

	m.mtx.RLock()
	return nil
}

func (m *mtxWithReadiness) rUnlock() {
	m.mtx.RUnlock()
}
// rLock/rUnlock 供 ForEach 并发读路径在就绪后共享遍历已打开索引文件。
