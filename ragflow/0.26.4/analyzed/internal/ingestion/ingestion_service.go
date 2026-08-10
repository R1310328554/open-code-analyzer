//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package ingestion

import (
	"context"
	"errors"
	"fmt"
	"ragflow/internal/dao"
	"ragflow/internal/engine"
	"ragflow/internal/entity"
	"ragflow/internal/ingestion/pipeline"
	"ragflow/internal/utility"
	"sync"
	"time"

	"ragflow/internal/common"

	"google.golang.org/grpc"
)

type Ingestor struct {
	id          string
	name        string
	serverAddr  string
	conn        *grpc.ClientConn
	ctx         context.Context
	cancel      context.CancelFunc
	reconnectMu sync.Mutex

	// 配置项
	maxConcurrency    int32
	supportedDocTypes []string
	version           string

	// 运行时状态
	currentTasks map[string]*TaskContext
	tasksMu      sync.RWMutex

	// ShutdownCh — 接收信号以触发优雅关闭
	ShutdownCh chan struct{}

	// 工作池
	taskChan  chan *TaskContext
	workerWg  sync.WaitGroup
	startOnce sync.Once

	ingestionTaskDAO       *dao.IngestionTaskDAO
	ingestionTaskLogDAO    *dao.IngestionTaskLogDAO
	ingestionTaskletDAO    *dao.IngestionTaskletDAO
	ingestionTaskletLogDAO *dao.IngestionTaskletLogDAO
}

type TaskLog struct {
	StartTime   time.Time              `json:"start_time"`
	EndTime     time.Time              `json:"end_time"`
	Description string                 `json:"description"`
	Details     map[string]interface{} `json:"details"`
}

type TaskContext struct {
	Ctx        context.Context
	CancelFunc context.CancelFunc
	// if tasklet is nil, this context is belonged to a task
	// if task and tasklet are both not nil, this context is belonged to a tasklet, the task is the parent task of the tasklet
	Task                   *entity.IngestionTask
	Tasklet                *entity.IngestionTasklet
	Logs                   []*TaskLog
	estimatedRemainingTime time.Duration // estimated cost in seconds to complete the task
	Progress               int32
	ErrorMessage           string
	TaskHandle             common.TaskHandle
}

func NewIngestor(name string, maxConcurrency int32, supportedTypes []string) *Ingestor {
	ctx, cancel := context.WithCancel(context.Background())
	id := utility.GenerateUUID()
	return &Ingestor{
		id:                     id,
		name:                   name,
		ctx:                    ctx,
		cancel:                 cancel,
		maxConcurrency:         maxConcurrency,
		supportedDocTypes:      supportedTypes,
		version:                "1.0.0",
		currentTasks:           make(map[string]*TaskContext),
		taskChan:               make(chan *TaskContext, maxConcurrency*2),
		ShutdownCh:             make(chan struct{}, 1),
		ingestionTaskDAO:       dao.NewIngestionTaskDAO(),
		ingestionTaskLogDAO:    dao.NewIngestionTaskLogDAO(),
		ingestionTaskletDAO:    dao.NewIngestionTaskletDAO(),
		ingestionTaskletLogDAO: dao.NewIngestionTaskletLogDAO(),
	}
}

func (e *Ingestor) ID() string {
	return e.id
}

func (e *Ingestor) Start() error {
	common.Info(fmt.Sprintf("Ingestor %s initialized", e.id))
	msgQueueEngine := engine.GetMessageQueueEngine()
	err := msgQueueEngine.InitConsumer("tasks.RAGFLOW")
	if err != nil {
		return err
	}

	// 首个任务到达时确保工作池已启动
	go e.startWorkerPool()

	for {
		var taskHandles []common.TaskHandle
		taskHandles, err = msgQueueEngine.GetMessages(4)
		if err != nil {
			common.Error("error consuming message", err)
			continue
		}
		for _, taskHandle := range taskHandles {
			taskMessage := taskHandle.GetMessage()
			common.Info(fmt.Sprintf("Received task id: %s, type: %s", taskMessage.TaskID, taskMessage.TaskType))
			if taskMessage.TaskType != common.TaskTypeIngestionTask {
				common.Info(fmt.Sprintf("task %s is not an ingestion task", taskMessage.TaskID))
				err = taskHandle.Ack()
				if err != nil {
					common.Error(fmt.Sprintf("error ack task %s", taskMessage.TaskID), err)
					return err
				}
				continue
			}
			var task *entity.IngestionTask
			task, err = e.ingestionTaskDAO.SetRunningByIngestor(taskMessage.TaskID)
			if err != nil {
				if errors.Is(err, common.ErrTaskNotFound) {
					common.Warn(fmt.Sprintf("task %s not found, skipping", taskMessage.TaskID))
					err = taskHandle.Ack()
					if err != nil {
						common.Error(fmt.Sprintf("error ack task %s", taskMessage.TaskID), err)
						return err
					}
					continue
				} else {
					common.Error(fmt.Sprintf("error setting task %s to running", taskMessage.TaskID), err)
					return err
				}
			}
			if task == nil {
				common.Info(fmt.Sprintf("task %s is already removed", taskMessage.TaskID))
				err = taskHandle.Ack()
				if err != nil {
					return err
				}
				continue
			}

			switch task.Status {
			case common.COMPLETED, common.STOPPED, common.FAILED:
				common.Info(fmt.Sprintf("task %s is already %s", taskMessage.TaskID, task.Status))
				err = taskHandle.Ack()
				if err != nil {
					common.Error(fmt.Sprintf("error nack task %s", taskMessage.TaskID), err)
					return err
				}
				continue
			case common.STOPPING, common.CREATED:
				err = fmt.Errorf("task %s is in unexpected status %s", taskMessage.TaskID, task.Status)
				return err
			case common.RUNNING:
			}

			// 构造带可取消 context 的 TaskContext
			ctx, cancel := context.WithCancel(e.ctx)
			taskCtx := &TaskContext{
				Ctx:        ctx,
				CancelFunc: cancel,
				Task:       task,
				TaskHandle: taskHandle,
			}

			// 推入 taskChan；满则拒绝任务（背压）
			select {
			case e.taskChan <- taskCtx:
				common.Info(fmt.Sprintf("Task %s queued (channel: %d/%d)", task.ID, len(e.taskChan), cap(e.taskChan)))
			default:
				common.Info(fmt.Sprintf("No available slot for task %s, failed", task.ID))

				err = taskHandle.Nack()
				if err != nil {
					common.Error(fmt.Sprintf("error nack task %s", taskMessage.TaskID), err)
					return err
				}
			}
		}
	}
}

func (e *Ingestor) startWorkerPool() {
	e.startOnce.Do(func() {
		for i := int32(0); i < e.maxConcurrency; i++ {
			e.workerWg.Add(1)
			go e.workerLoop(i)
		}
		common.Info(fmt.Sprintf("Worker pool started with %d workers", e.maxConcurrency))
	})
}

func (e *Ingestor) workerLoop(id int32) {
	defer e.workerWg.Done()
	common.Info(fmt.Sprintf("Worker %d started", id))
	for {
		select {
		case <-e.ctx.Done():
			return
		case taskCtx := <-e.taskChan:
			if taskCtx.Tasklet != nil {
				e.executeTasklet(taskCtx)
			} else {
				e.executeTask(taskCtx)
			}
		}
	}
}

func (e *Ingestor) executeTask(taskCtx *TaskContext) {
	ctx := taskCtx.Ctx
	task := taskCtx.Task
	common.Info(fmt.Sprintf("Starting task %s", task.ID))

	// Execute the canonical ingestion canvas DSL carried by the task.
	// The Go ingestion path no longer synthesizes a parallel `stages[]`
	// schema; the only accepted format is the template/canvas DSL.
	dslBytes := defaultPipelineDSL(task)
	if len(dslBytes) == 0 {
		err := fmt.Errorf("task %s missing canonical ingestion DSL in schema.pipeline or schema.dsl", task.ID)
		common.Error(fmt.Sprintf("Failed to load pipeline DSL for task %s", task.ID), err)
		e.failTask(taskCtx, err)
		return
	}
	pl, err := pipeline.NewPipelineFromDSL(dslBytes, task.ID)
	if err != nil {
		common.Error(fmt.Sprintf("Failed to compile pipeline for task %s", task.ID), err)
		e.failTask(taskCtx, err)
		return
	}

	inputs := map[string]any{
		"doc_id": task.DocumentID,
	}
	_, runErr := pl.Run(ctx, inputs)
	if runErr != nil {
		if errors.Is(runErr, context.Canceled) || errors.Is(runErr, context.DeadlineExceeded) {
			common.Info(fmt.Sprintf("Task %s cancelled: %v", task.ID, runErr))
			// STOPPED is a terminal status — the task will not be
			// re-attempted by the consumer. Ack the message so the
			// queue does not redeliver it (Nack here would race
			// with the STOPPED write and let another consumer pick
			// up a "stopped" task).
			_ = e.ingestionTaskDAO.UpdateStatus(task.ID, common.STOPPED)
			_ = e.ackOrNack(taskCtx, true)
			return
		}
		common.Error(fmt.Sprintf("Task %s pipeline failed", task.ID), runErr)
		e.failTask(taskCtx, runErr)
		return
	}

	if err = e.ingestionTaskDAO.UpdateStatus(task.ID, common.COMPLETED); err != nil {
		common.Error(fmt.Sprintf("Task %s update status failed", task.ID), err)
		_ = e.ackOrNack(taskCtx, true)
		return
	}

	common.Info(fmt.Sprintf("Task %s completed", task.ID))
	_ = e.ackOrNack(taskCtx, true)
}

// defaultPipelineDSL 返回 task schema 中的标准 canvas DSL 字节；仅接受 template/canvas 形状。
func defaultPipelineDSL(task *entity.IngestionTask) []byte {
	if task != nil && task.Schema != nil {
		if raw, ok := task.Schema["pipeline"]; ok {
			switch v := raw.(type) {
			case []byte:
				if len(v) > 0 {
					return v
				}
			case string:
				if v != "" {
					return []byte(v)
				}
			}
		}
		if raw, ok := task.Schema["dsl"]; ok {
			switch v := raw.(type) {
			case []byte:
				if len(v) > 0 {
					return v
				}
			case string:
				if v != "" {
					return []byte(v)
				}
			}
		}
	}
	return nil
}

// failTask 将任务置 FAILED 并 Ack 消息，避免 broker 无限重投递（修复 plan §8 Q3）。
func (e *Ingestor) failTask(taskCtx *TaskContext, runErr error) {
	if err := e.ingestionTaskDAO.UpdateStatus(taskCtx.Task.ID, common.FAILED); err != nil {
		common.Error(fmt.Sprintf("Task %s update status (failed) error", taskCtx.Task.ID), err)
	}
	_ = e.ackOrNack(taskCtx, true)
	common.Error(fmt.Sprintf("Task %s failed: %v", taskCtx.Task.ID, runErr), runErr)
}

// ackOrNack 统一执行后 NATS 消息处置：ack 移除，nack 重新入队。
func (e *Ingestor) ackOrNack(taskCtx *TaskContext, ack bool) error {
	if taskCtx == nil || taskCtx.TaskHandle == nil {
		return nil
	}
	var err error
	if ack {
		err = taskCtx.TaskHandle.Ack()
	} else {
		err = taskCtx.TaskHandle.Nack()
	}
	if err != nil {
		common.Error(fmt.Sprintf("Task %s ack/nack error", taskCtx.Task.ID), err)
	}
	return err
}

func (e *Ingestor) executeTasklet(taskCtx *TaskContext) {
	ctx := taskCtx.Ctx
	tasklet := taskCtx.Tasklet
	common.Info(fmt.Sprintf("Starting tasklet %s", tasklet.ID))

	latestLog, err := e.ingestionTaskletLogDAO.LatestLogByTaskletID(tasklet.ID)
	if err != nil {
		latestLog = &entity.IngestionTaskletLog{
			TaskletID: tasklet.ID,
			Checkpoint: entity.JSONMap{
				"current_step": 0,
				"total_step":   3,
			},
		}
		err = e.ingestionTaskletLogDAO.Create(latestLog)
		if err != nil {
			common.Error(fmt.Sprintf("Failed to create task log for tasklet %s", tasklet.ID), err)
			return
		}
	}

	var checkpointMap map[string]interface{}
	checkpointMap = latestLog.Checkpoint
	currentStep := checkpointMap["current_step"].(int)
	totalStep := checkpointMap["total_step"].(int)
	for i := currentStep; i < totalStep; i++ {
		select {
		case <-ctx.Done():
			// 任务已取消
			common.Info(fmt.Sprintf("Tasklet %s stopped", tasklet.ID))
			return
		case <-time.After(3000 * time.Millisecond):
			common.Info(fmt.Sprintf("Tasklet %s is running step %d", tasklet.ID, i))
			checkpointMap["current_step"] = i + 1
			latestLog.Checkpoint = checkpointMap
			err = e.ingestionTaskletLogDAO.Create(latestLog)
			if err != nil {
				common.Error(fmt.Sprintf("Failed to update task log for tasklet %s", tasklet.ID), err)
				return
			}
		}
	}

	err = e.ingestionTaskletDAO.UpdateStatus(tasklet.ID, common.STOPPED)
	if err != nil {
		common.Error(fmt.Sprintf("Tasklet %s update status failed", tasklet.ID), err)
		return
	}

	common.Info(fmt.Sprintf("Tasklet %s completed", tasklet.ID))
}

//		e.stream = stream
//
//		if err = e.sendRegister(); err != nil {
//			stream.CloseSend()
//			conn.Close()
//			common.Info(fmt.Sprintf("Reconnect register failed: %v, retrying in %v", err, backoff))
//			time.Sleep(backoff)
//			backoff *= 2
//			if backoff > maxBackoff {
//				backoff = maxBackoff
//			}
//			continue
//		}
//
//		common.Info(fmt.Sprintf("Ingestor %s reconnected to admin", e.id))
//		break
//	}
//
//	// Restart the loops on the new stream
//	go e.receiveLoop()
//	go e.heartbeatLoop()
//}

// Stop 优雅关闭 Ingestor
func (e *Ingestor) Stop() {
	common.Info(fmt.Sprintf("Stopping ingestor %s", e.id))
	e.cancel()

	// 等待所有 worker 在 ctx.Done() 后退出
	e.workerWg.Wait()
	common.Info("All tasks completed")
}
// ingestion_service.go — Ingestor 服务：消息队列消费与流水线执行。
