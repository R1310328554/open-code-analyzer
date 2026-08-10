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

// logs 包组合存储实现，主存储失败时回退到次级 LogStore。
package logs

import (
	"context"
	"io"

	"github.com/drone/drone/core"
)

// NewCombined 构造组合 LogStore，便于从数据库日志迁移到 S3 等外部存储。
func NewCombined(primary, secondary core.LogStore) core.LogStore {
	return &combined{
		primary:   primary,
		secondary: secondary,
	}
}

// combined 读操作主存储失败时查次级；写操作仅走主存储。
type combined struct {
	primary, secondary core.LogStore
}

// Find 先查主存储，失败则回退到次级存储。
func (s *combined) Find(ctx context.Context, step int64) (io.ReadCloser, error) {
	rc, err := s.primary.Find(ctx, step)
	if err == nil {
		return rc, err
	}
	return s.secondary.Find(ctx, step)
}

// Create 仅写入主存储。
func (s *combined) Create(ctx context.Context, step int64, r io.Reader) error {
	return s.primary.Create(ctx, step, r)
}

// Update 仅更新主存储。
func (s *combined) Update(ctx context.Context, step int64, r io.Reader) error {
	return s.primary.Update(ctx, step, r)
}

// Delete 先删主存储，失败再尝试次级存储。
func (s *combined) Delete(ctx context.Context, step int64) error {
	err := s.primary.Delete(ctx, step)
	if err != nil {
		err = s.secondary.Delete(ctx, step)
	}
	return err
}
