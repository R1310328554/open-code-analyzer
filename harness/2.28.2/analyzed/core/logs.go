// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package core

import (
	"context"
	"io"
)

// Line 表示构建日志中的一行输出。
type Line struct {
	Number    int    `json:"pos"`  // 行号
	Message   string `json:"out"`  // 日志正文
	Timestamp int64  `json:"time"` // Unix 时间戳
}

// LogStore 将构建步骤日志持久化到存储后端。
type LogStore interface {
	// Find 从数据存储读取指定阶段的日志流。
	Find(ctx context.Context, stage int64) (io.ReadCloser, error)

	// Create 将读取器 r 中的日志内容写入数据存储。
	Create(ctx context.Context, stage int64, r io.Reader) error

	// Update 用读取器 r 中的内容覆盖已有日志。
	Update(ctx context.Context, stage int64, r io.Reader) error

	// Delete 从数据存储中清除指定阶段的日志。
	Delete(ctx context.Context, stage int64) error
}

// LogStream 管理构建步骤的实时日志流（内存/发布订阅）。
type LogStream interface {
	// Create 为指定步骤 ID 创建日志流。
	Create(context.Context, int64) error

	// Delete 删除指定步骤 ID 的日志流。
	Delete(context.Context, int64) error

	// Write 向日志流追加一行输出。
	Write(context.Context, int64, *Line) error

	// Tail 订阅并尾随指定步骤的实时日志。
	Tail(context.Context, int64) (<-chan *Line, <-chan error)

	// Info 返回内部流统计信息，便于监控订阅数。
	Info(context.Context) *LogStreamInfo
}

// LogStreamInfo 描述当前注册的日志流及每个流的订阅者数量。
type LogStreamInfo struct {
	// Streams 键为步骤 ID，值为正在订阅该流日志的客户端数量。
	Streams map[int64]int `json:"streams"`
}
