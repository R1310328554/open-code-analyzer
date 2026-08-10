//
// Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package syncer

// syncer.go 后台同步调度器：轮询 sync_logs 表并执行到期任务。

import (
	"context"
	"fmt"
	"ragflow/internal/common"
	"ragflow/internal/dao"
	"ragflow/internal/entity"
	"ragflow/internal/utility"
	"sync"
	"time"

	"go.uber.org/zap"
)

// Syncer 固定 worker 池周期性拉取到期同步/裁剪任务并执行。
type Syncer struct {
	id             string
	maxConcurrency int
	pollInterval   time.Duration // 每个 worker 查询到期任务的间隔

	ctx    context.Context
	cancel context.CancelFunc

	workerWg sync.WaitGroup

	// ShutdownCh 在 Stop() 完成后关闭，供外部等待优雅退出。
	ShutdownCh chan struct{}
}

// NewSyncer 创建 Syncer，指定并发 worker 数与轮询间隔。
func NewSyncer(maxConcurrency int, pollInterval time.Duration) *Syncer {
	ctx, cancel := context.WithCancel(context.Background())
	return &Syncer{
		id:             utility.GenerateUUID(),
		maxConcurrency: maxConcurrency,
		pollInterval:   pollInterval,
		ctx:            ctx,
		cancel:         cancel,
		ShutdownCh:     make(chan struct{}),
	}
}

// Start 启动 maxConcurrency 个 workerLoop 协程。
func (s *Syncer) Start() error {
	common.Info(fmt.Sprintf("Syncer %s starting with %d workers (poll every %v)",
		s.id, s.maxConcurrency, s.pollInterval))

	for i := 0; i < s.maxConcurrency; i++ {
		s.workerWg.Add(1)
		go s.workerLoop(i)
	}
	return nil
}

// Stop 取消 context 并 Wait 全部 worker 后关闭 ShutdownCh。
func (s *Syncer) Stop() {
	common.Info(fmt.Sprintf("Stopping syncer %s", s.id))
	s.cancel()
	s.workerWg.Wait()
	close(s.ShutdownCh)
	common.Info(fmt.Sprintf("Syncer %s stopped", s.id))
}

func (s *Syncer) ID() string {
	return s.id
}

// workerLoop 按 pollInterval ticker 调用 pollAndExecute 直至 ctx 取消。
func (s *Syncer) workerLoop(workerID int) {
	defer s.workerWg.Done()
	common.Debug(fmt.Sprintf("Syncer worker %d started", workerID))

	ticker := time.NewTicker(s.pollInterval)
	defer ticker.Stop()

	for {
		select {
		case <-s.ctx.Done():
			common.Debug(fmt.Sprintf("Syncer worker %d exiting (ctx cancelled)", workerID))
			return
		case <-ticker.C:
			s.pollAndExecute(workerID)
		}
	}
}

// pollAndExecute 查询到期 sync/prune 任务并选取一条执行（当前为占位日志）。
func (s *Syncer) pollAndExecute(workerID int) {
	common.Info(fmt.Sprintf("Syncer worker %d polling for due tasks", workerID))
}

// executeSyncTask 执行数据源同步任务（TODO：具体同步逻辑），完成后 markTaskDone。
func (s *Syncer) executeSyncTask(task *entity.SyncLogs) {
	common.Info("Executing sync task",
		zap.String("task_id", task.ID),
		zap.String("connector_id", task.ConnectorID),
		zap.String("kb_id", task.KbID))
	// TODO: 按连接器类型实现真实同步；当前直接标记完成。
	s.markTaskDone(task.ID, task.ConnectorID)
}

// executePruneTask 执行裁剪任务，删除过期文档（TODO），完成后 markTaskDone。
func (s *Syncer) executePruneTask(task *entity.SyncLogs) {
	common.Info("Executing prune task",
		zap.String("task_id", task.ID),
		zap.String("connector_id", task.ConnectorID),
		zap.String("kb_id", task.KbID))
	// TODO: 实现裁剪逻辑。
	s.markTaskDone(task.ID, task.ConnectorID)
}

// markTaskDone 将 SyncLogs 与 Connector 状态更新为 DONE。
func (s *Syncer) markTaskDone(taskID, connectorID string) {
	db := dao.GetDB()
	now := time.Now().Local()

	db.Model(&entity.SyncLogs{}).Where("id = ?", taskID).Updates(map[string]interface{}{
		"status":      string(entity.TaskStatusDone),
		"update_time": now,
	})
	db.Model(&entity.Connector{}).Where("id = ?", connectorID).Updates(map[string]interface{}{
		"status":      string(entity.TaskStatusDone),
		"update_time": now,
	})
}
// syncer.go — 定时轮询 sync_logs 并分发同步/裁剪任务的 worker 池。
