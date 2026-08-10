// agent_loop_checkpoint.go — AgentLoop 检查点 gob 序列化与加载/保存/删除。

package core

import (
	"bytes"
	"context"
	"encoding/gob"
	"errors"
	"fmt"
)

// ---- AgentLoop 检查点序列化与生命周期 ----

type CheckPointDeleter interface {
	Delete(ctx context.Context, key string) error
}

// marshalTurnLoopCheckpoint 将检查点结构 gob 编码为字节。
func marshalTurnLoopCheckpoint[T any](c *agentLoopCheckpoint[T]) ([]byte, error) {
	buf := new(bytes.Buffer)
	if err := gob.NewEncoder(buf).Encode(c); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// unmarshalTurnLoopCheckpoint 从 gob 字节解码检查点。
func unmarshalTurnLoopCheckpoint[T any](data []byte) (*agentLoopCheckpoint[T], error) {
	var c agentLoopCheckpoint[T]
	if err := gob.NewDecoder(bytes.NewReader(data)).Decode(&c); err != nil {
		return nil, err
	}
	return &c, nil
}

// saveTurnLoopCheckpoint 序列化并写入配置 Store。
func (l *AgentLoop[T]) saveTurnLoopCheckpoint(ctx context.Context, checkPointID string, c *agentLoopCheckpoint[T]) error {
	if l.config.Store == nil {
		return errors.New("checkpoint store is nil")
	}
	data, err := marshalTurnLoopCheckpoint(c)
	if err != nil {
		return err
	}
	return l.config.Store.Set(ctx, checkPointID, data)
}

// deleteTurnLoopCheckpoint 若 Store 实现 CheckPointDeleter 则删除。
func (l *AgentLoop[T]) deleteTurnLoopCheckpoint(ctx context.Context, checkPointID string) error {
	if l.config.Store == nil {
		return nil
	}
	if deleter, ok := l.config.Store.(CheckPointDeleter); ok {
		return deleter.Delete(ctx, checkPointID)
	}
	return nil
}

// tryLoadCheckpoint 启动时加载检查点：有 Runner 状态则 pendingResume，否则 PushFront 未处理项。
func (l *AgentLoop[T]) tryLoadCheckpoint(ctx context.Context) error {
	checkPointID := l.config.CheckpointID
	if checkPointID == "" || l.config.Store == nil {
		return nil
	}

	l.loadCheckpointID = checkPointID

	data, existed, err := l.config.Store.Get(ctx, checkPointID)
	if err != nil {
		return fmt.Errorf("failed to load checkpoint[%s]: %w", checkPointID, err)
	}
	if !existed {
		return nil
	}

	var cp *agentLoopCheckpoint[T]
	if len(data) == 0 {
		return nil
	}
	cp, err = unmarshalTurnLoopCheckpoint[T](data)
	if err != nil {
		return fmt.Errorf("failed to unmarshal checkpoint[%s]: %w", checkPointID, err)
	}

	newItems := l.buffer.TakeAll()

	if cp.HasRunnerState {
		if len(cp.RunnerCheckpoint) == 0 {
			l.buffer.PushFront(newItems)
			return fmt.Errorf("checkpoint[%s] has runner state but bytes are empty", checkPointID)
		}
		l.pendingResume = &agentLoopPendingResume[T]{
			interrupted: append([]T{}, cp.CanceledItems...),
			unhandled:   append([]T{}, cp.UnhandledItems...),
			newItems:    append([]T{}, newItems...),
			resumeBytes: append([]byte{}, cp.RunnerCheckpoint...),
		}
	} else {
		items := make([]T, 0, len(cp.UnhandledItems)+len(newItems))
		items = append(items, cp.UnhandledItems...)
		items = append(items, newItems...)
		l.buffer.PushFront(items)
	}

	return nil
}

// HasRunnerState 时空字节视为损坏检查点；loadCheckpointID 供成功退出后删除旧检查点。
