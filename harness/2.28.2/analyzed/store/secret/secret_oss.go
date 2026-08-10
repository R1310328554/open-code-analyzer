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

// secret 包在 OSS 构建中提供空操作的仓库密钥存储。
package secret

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
	"github.com/drone/drone/store/shared/encrypt"
)

// New 创建并返回仓库级 Secret 数据库存储。
func New(db *db.DB, enc encrypt.Encrypter) core.SecretStore {
	return new(noop)
}

// noop 是 OSS 构建中的空操作密钥存储实现。
type noop struct{}

// List 在 OSS 构建中返回 nil。
func (noop) List(ctx context.Context, id int64) ([]*core.Secret, error) {
	return nil, nil
}

func (noop) Find(ctx context.Context, id int64) (*core.Secret, error) {
	return nil, nil
}

func (noop) FindName(ctx context.Context, id int64, name string) (*core.Secret, error) {
	return nil, nil
}

func (noop) Create(ctx context.Context, secret *core.Secret) error {
	return nil
}

// Update 在 OSS 构建中为 no-op。
func (noop) Update(context.Context, *core.Secret) error {
	return nil
}

// Delete 在 OSS 构建中为 no-op。
func (noop) Delete(context.Context, *core.Secret) error {
	return nil
}
