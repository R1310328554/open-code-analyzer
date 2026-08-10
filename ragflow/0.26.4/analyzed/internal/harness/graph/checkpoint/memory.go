// memory.go — 内存检查点存储：实现 BaseCheckpointer，flat map 格式。

// Package checkpoint 提供 LangGraph Go 检查点实现。
//
// MemorySaver 以 flat map 实现 BaseCheckpointer，适合测试与单机。
// CheckpointManager provides rich versioning and conflict detection for *Checkpoint structs.
// See checkpoint.go for the full versioned API.
package checkpoint

import (
	"context"
	"fmt"
	"sync"
	"time"

	"github.com/google/uuid"
	"ragflow/internal/harness/graph/constants"
)

// MemorySaver 线程安全的内存检查点保存器。
type MemorySaver struct {
	mu          sync.RWMutex
	checkpoints map[string]map[string]interface{}
	versions    map[string][]checkpointEntry
}

type checkpointEntry struct {
	ID         string
	ThreadID   string
	Checkpoint map[string]interface{}
	Metadata   map[string]interface{}
	CreatedAt  time.Time
	ParentID   string
}

// NewMemorySaver 创建空内存 saver。
func NewMemorySaver() *MemorySaver {
	return &MemorySaver{
		checkpoints: make(map[string]map[string]interface{}),
		versions:    make(map[string][]checkpointEntry),
	}
}

// Get 按 thread_id 或 checkpoint_id 读取检查点（深拷贝）。
func (s *MemorySaver) Get(ctx context.Context, config map[string]interface{}) (map[string]interface{}, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	threadID, ok := config[constants.ConfigKeyThreadID].(string)
	if !ok {
		return nil, fmt.Errorf("thread_id is required")
	}

	if checkpointID, ok := config[constants.ConfigKeyCheckpointID].(string); ok {
		versions := s.versions[threadID]
		for _, entry := range versions {
			if entry.ID == checkpointID {
				cp := deepCopyMap(entry.Checkpoint)
				return cp, nil
			}
		}
		return nil, fmt.Errorf("checkpoint not found: %s", checkpointID)
	}

	versions := s.versions[threadID]
	if len(versions) == 0 {
		return nil, nil
	}

	return deepCopyMap(versions[len(versions)-1].Checkpoint), nil
}

// Put 追加新版本并更新线程最新快照。
func (s *MemorySaver) Put(ctx context.Context, config map[string]interface{}, checkpoint map[string]interface{}) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	threadID, ok := config[constants.ConfigKeyThreadID].(string)
	if !ok {
		return fmt.Errorf("thread_id is required")
	}

	checkpointID := uuid.New().String()
	if id, ok := config[constants.ConfigKeyCheckpointID].(string); ok {
		checkpointID = id
	}

	entry := checkpointEntry{
		ID:         checkpointID,
		ThreadID:   threadID,
		Checkpoint: deepCopyMap(checkpoint),
		Metadata:   deepCopyMap(config),
		CreatedAt:  time.Now(),
	}

	if parentID, ok := config["parent_checkpoint_id"].(string); ok {
		entry.ParentID = parentID
	}

	s.versions[threadID] = append(s.versions[threadID], entry)
	s.checkpoints[threadID] = deepCopyMap(checkpoint)
	return nil
}

// List 倒序返回最近 limit 条版本摘要。
func (s *MemorySaver) List(ctx context.Context, config map[string]interface{}, limit int) ([]map[string]interface{}, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	threadID, ok := config[constants.ConfigKeyThreadID].(string)
	if !ok {
		return nil, fmt.Errorf("thread_id is required")
	}

	versions := s.versions[threadID]
	if limit <= 0 || limit > len(versions) {
		limit = len(versions)
	}

	result := make([]map[string]interface{}, 0, limit)
	for i := len(versions) - 1; i >= len(versions)-limit && i >= 0; i-- {
		entry := versions[i]
		result = append(result, map[string]interface{}{
			constants.ConfigKeyCheckpointID: entry.ID,
			constants.ConfigKeyThreadID:     entry.ThreadID,
			"metadata":                      deepCopyMap(entry.Metadata),
			"created_at":                    entry.CreatedAt,
			"parent_id":                     entry.ParentID,
		})
	}

	return result, nil
}

// GetState 返回 CheckpointState（含元数据）。
func (s *MemorySaver) GetState(ctx context.Context, config map[string]interface{}) (*CheckpointState, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	threadID, ok := config[constants.ConfigKeyThreadID].(string)
	if !ok {
		return nil, fmt.Errorf("thread_id is required")
	}

	checkpointID, ok := config[constants.ConfigKeyCheckpointID].(string)
	if !ok {
		versions := s.versions[threadID]
		if len(versions) == 0 {
			return nil, nil
		}
		entry := versions[len(versions)-1]
		return &CheckpointState{
			Checkpoint: deepCopyMap(entry.Checkpoint),
			Metadata:   deepCopyMap(entry.Metadata),
		}, nil
	}

	versions := s.versions[threadID]
	for _, entry := range versions {
		if entry.ID == checkpointID {
			return &CheckpointState{
				Checkpoint: deepCopyMap(entry.Checkpoint),
				Metadata:   deepCopyMap(entry.Metadata),
			}, nil
		}
	}

	return nil, fmt.Errorf("checkpoint not found: %s", checkpointID)
}

// CheckpointState 检查点数据与配置元数据的组合。
type CheckpointState struct {
	Checkpoint map[string]interface{}
	Metadata   map[string]interface{}
}

// 与 CheckpointManager（*Checkpoint 结构体）互补：MemorySaver 面向 map 契约。
