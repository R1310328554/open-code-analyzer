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

// secret 包（OSS 构建）提供外部密钥插件的空实现。
package secret

import (
	"context"

	"github.com/drone/drone/core"
)

// External 在 OSS 构建中返回空操作 SecretService，不调用外部 HTTP 端点。
func External(string, string, bool) core.SecretService {
	return new(noop)
}

// noop OSS 构建下的外部密钥插件桩实现。
type noop struct{}

// Find 空实现，始终返回 nil 表示未找到密钥。
func (noop) Find(context.Context, *core.SecretArgs) (*core.Secret, error) {
	return nil, nil
}
