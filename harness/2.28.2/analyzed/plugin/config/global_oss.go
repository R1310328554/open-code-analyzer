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

package config

import (
	"context"
	"time"

	"github.com/drone/drone/core"
)

// Global 在 OSS 构建中返回空操作配置服务，不从远程端点拉取配置。
func Global(string, string, bool, time.Duration) core.ConfigService {
	return new(noop)
}

// noop 是始终返回 nil 的桩 ConfigService。
type noop struct{}

// Find 空实现，不返回任何配置。
func (noop) Find(context.Context, *core.ConfigArgs) (*core.Config, error) {
	return nil, nil
}
