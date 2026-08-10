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

// +build oss

// card 包（OSS 构建）提供 CardStore 的空实现桩。
package card

import (
	"context"
	"io"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// New 在 OSS 构建中返回 noop CardStore，不持久化卡片数据。
func New(db *db.DB) core.CardStore {
	return new(noop)
}

// noop OSS 构建下的 CardStore 桩实现。
type noop struct{}

// Find 空实现，返回 nil。
func (noop) Find(ctx context.Context, step int64) (io.ReadCloser, error) {
	return nil, nil
}

// Create 空实现，直接返回 nil。
func (noop) Create(ctx context.Context, step int64, r io.Reader) error {
	return nil
}

// Update 空实现，直接返回 nil。
func (noop) Update(ctx context.Context, step int64, r io.Reader) error {
	return nil
}

// Delete 空实现，直接返回 nil。
func (noop) Delete(ctx context.Context, step int64) error {
	return nil
}
