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
	"encoding/json"
	"io"
)

// CardInput 表示流水线步骤上报的可视化卡片输入。
type CardInput struct {
	Schema string          `json:"schema"` // 卡片数据模式标识
	Data   json.RawMessage `json:"data"`   // 卡片 JSON 载荷
}

// CardStore 管理仓库构建步骤关联的卡片数据。
type CardStore interface {
	// Find 从数据存储读取指定步骤的卡片数据流。
	Find(ctx context.Context, step int64) (io.ReadCloser, error)

	// Create 将读取器 r 中的卡片内容写入数据存储。
	Create(ctx context.Context, step int64, r io.Reader) error

	// Update 用读取器 r 中的内容覆盖已有卡片数据。
	Update(ctx context.Context, step int64, r io.Reader) error

	// Delete 从数据存储中清除指定步骤的卡片数据。
	Delete(ctx context.Context, step int64) error
}
