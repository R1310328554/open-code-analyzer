// agent_loop_bridge.go — 内存桥接 CheckPointStore，在单轮内暂存 Runner 检查点字节。

package core

import (
	"context"
	"sync"
)

const bridgeCheckpointID = "__adk_turnloop_bridge_cp__"

// bridgeStore 最小 CheckPointStore，桥接 AgentLoop 与 Runner 检查点
// 不直接使用持久化 Store，仅在轮次内暂存。
type bridgeStore struct {
	cpID string
	data []byte
	mu   sync.RWMutex
}

// newBridgeStore 创建空桥接 Store。
func newBridgeStore() *bridgeStore {
	return &bridgeStore{cpID: bridgeCheckpointID}
}

// newResumeBridgeStore 用已有字节初始化恢复用桥接 Store。
func newResumeBridgeStore(cpID string, data []byte) *bridgeStore {
	return &bridgeStore{cpID: cpID, data: append([]byte{}, data...)}
}

// Get 按 key 返回检查点副本；key 不匹配或为空则不存在。
func (s *bridgeStore) Get(_ context.Context, key string) ([]byte, bool, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if key != s.cpID {
		return nil, false, nil
	}
	if len(s.data) == 0 {
		return nil, false, nil
	}
	return append([]byte{}, s.data...), true, nil
}

// Set 写入检查点字节（深拷贝）。
func (s *bridgeStore) Set(_ context.Context, key string, data []byte) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if key == s.cpID {
		s.data = append([]byte{}, data...)
	}
	return nil
}

// Delete 清除匹配 key 的检查点数据。
func (s *bridgeStore) Delete(_ context.Context, key string) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if key == s.cpID {
		s.data = nil
	}
	return nil
}

var _ CheckPointStore = (*bridgeStore)(nil)
var _ CheckPointDeleter = (*bridgeStore)(nil)

// bridgeCheckpointID 为固定内部 key；实现 CheckPointStore 与 CheckPointDeleter。
