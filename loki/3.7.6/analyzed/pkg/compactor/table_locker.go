package compactor

// 索引表级互斥锁：防止压缩、保留与删除存储更新并发操作同一张表，
// 未获锁的调用方可通过 channel 等待锁释放。

import "sync"

type lockWaiterChan chan struct{}

type tableLocker struct {
	lockedTables    map[string]lockWaiterChan
	lockedTablesMtx sync.RWMutex
}

func newTableLocker() *tableLocker {
	return &tableLocker{
		lockedTables: map[string]lockWaiterChan{},
	}
}

// lockTable 尝试加锁；若表已被锁定则返回 false 及 unlock 通知 channel。
// lockTable attempts to lock a table. It returns true if the lock gets acquired for the caller.
// It also returns a channel which the caller can watch to detect unlocking of table if it was already locked by some other caller.
func (t *tableLocker) lockTable(tableName string) (bool, <-chan struct{}) {
	locked := false

	t.lockedTablesMtx.RLock()
	c, ok := t.lockedTables[tableName]
	t.lockedTablesMtx.RUnlock()
	if ok {
		return false, c
	}

	t.lockedTablesMtx.Lock()
	defer t.lockedTablesMtx.Unlock()

	c, ok = t.lockedTables[tableName]
	if !ok {
		t.lockedTables[tableName] = make(chan struct{})
		c = t.lockedTables[tableName]
		locked = true
	}

	return locked, c
}

// unlockTable close 等待 channel 并从 lockedTables 移除表条目。
func (t *tableLocker) unlockTable(tableName string) {
	t.lockedTablesMtx.Lock()
	defer t.lockedTablesMtx.Unlock()

	c, ok := t.lockedTables[tableName]
	if ok {
		close(c)
	}
	delete(t.lockedTables, tableName)
}

// isLocked 查询指定表当前是否处于锁定状态。
func (t *tableLocker) isLocked(tableName string) bool {
	t.lockedTablesMtx.Lock()
	defer t.lockedTablesMtx.Unlock()

	_, ok := t.lockedTables[tableName]
	return ok
}
