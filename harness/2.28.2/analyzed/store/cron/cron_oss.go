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

// cron 包（OSS 构建）提供 CronStore 的空实现桩。
package cron

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// New 在 OSS 构建中返回 noop CronStore。
func New(db *db.DB) core.CronStore {
	return new(noop)
}

// noop OSS 构建下的 CronStore 桩实现。
type noop struct{}

// List 空实现，返回 nil。
func (noop) List(ctx context.Context, id int64) ([]*core.Cron, error) {
	return nil, nil
}

// Ready 空实现，返回 nil。
func (noop) Ready(ctx context.Context, id int64) ([]*core.Cron, error) {
	return nil, nil
}

// Find 空实现，返回 nil。
func (noop) Find(ctx context.Context, id int64) (*core.Cron, error) {
	return nil, nil
}

// FindName 空实现，返回 nil。
func (noop) FindName(ctx context.Context, id int64, name string) (*core.Cron, error) {
	return nil, nil
}

// Create 空实现，直接返回 nil。
func (noop) Create(ctx context.Context, secret *core.Cron) error {
	return nil
}

// Update 空实现，直接返回 nil。
func (noop) Update(context.Context, *core.Cron) error {
	return nil
}

// Delete 空实现，直接返回 nil。
func (noop) Delete(context.Context, *core.Cron) error {
	return nil
}
