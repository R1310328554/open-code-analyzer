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

package utility

// scheduled_task.go 提供周期性定时任务与节点状态上报。

import (
	"encoding/json"
	"fmt"
	"ragflow/internal/common"
	"sync/atomic"
	"time"

	"go.uber.org/zap"
)

// StatusMessage 为节点向 admin 上报的状态消息。
type StatusMessage struct {
	ID        int       `json:"id"`
	Version   string    `json:"version"`
	Timestamp time.Time `json:"timestamp"`
	NodeName  string    `json:"node_name"`
	ExtInfo   string    `json:"ext_info"`
}

func NewStatusMessage(id int, version string, nodeName string, extInfo string) *StatusMessage {
	return &StatusMessage{
		ID:        id,
		Version:   version,
		Timestamp: time.Now(),
		NodeName:  nodeName,
		ExtInfo:   extInfo,
	}
}

func StatusMessageSending() {
	// 构造并序列化状态消息
	statusMessage := NewStatusMessage(0, "v1", "ragflow", "")

	// 序列化为 JSON
	jsonData, err := json.Marshal(statusMessage)
	if err != nil {
		common.Error("Failed to marshal status message", err)
		return
	}

	// 创建指向本地 admin 的 HTTP 客户端
	client := NewHTTPClientBuilder().
		WithHost("127.0.0.1").
		WithPort(9381).
		WithSSL(false).
		WithTimeout(10 * time.Second).
		Build()

	// POST 到 /v1/admin/status
	resp, err := client.PostJSON("/v1/admin/status", jsonData)
	if err != nil {
		common.Error("Error sending status message", err)
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		common.Error("Failed to send status message", fmt.Errorf("status: %d", resp.StatusCode))
	}
}

// ScheduledTask 表示按固定间隔执行的周期性任务。
type ScheduledTask struct {
	Name      string
	Interval  time.Duration
	Job       func()
	stop      chan struct{}
	running   bool
	executing int32 // atomic flag: 0 - not executed, 1 running
}

// NewScheduledTask 创建定时任务（名称、间隔、回调）。
func NewScheduledTask(name string, interval time.Duration, job func()) *ScheduledTask {
	return &ScheduledTask{
		Name:     name,
		Interval: interval,
		Job:      job,
		stop:     make(chan struct{}),
	}
}

// Start 启动后台 goroutine 按 ticker 周期执行。
func (t *ScheduledTask) Start() {
	if t.running {
		return
	}
	t.running = true

	go func() {
		ticker := time.NewTicker(t.Interval)
		defer ticker.Stop()

		common.Info("Task started", zap.String("name", t.Name))

		for {
			select {
			case <-ticker.C:
				t.runSafely()
			case <-t.stop:
				common.Info("Task stopped", zap.String("name", t.Name))
				return
			}
		}
	}()
}

// runSafely 执行 Job，含 panic 恢复与 atomic 防重入。
func (t *ScheduledTask) runSafely() {
	// CAS 设置 executing 标志，上一轮未完成则跳过
	if !atomic.CompareAndSwapInt32(&t.executing, 0, 1) {
		common.Warn("Task skipped - previous execution still running", zap.String("name", t.Name))
		return
	}

	// 执行完毕清除 executing 标志
	defer atomic.StoreInt32(&t.executing, 0)

	defer func() {
		if r := recover(); r != nil {
			common.Fatal("Task panicked", zap.String("name", t.Name), zap.Any("recover", r))
		}
	}()

	t.Job()
}

// Stop 关闭 stop 通道并停止 ticker。
func (t *ScheduledTask) Stop() {
	if !t.running {
		return
	}
	t.running = false
	close(t.stop)
}

// IsExecuting 返回任务是否正在执行。
func (t *ScheduledTask) IsExecuting() bool {
	return atomic.LoadInt32(&t.executing) == 1
}
// scheduled_task.go — 周期性定时任务调度器，含 panic 恢复与防重入。
