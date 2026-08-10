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
	"github.com/drone/drone/core"
)

// Memoize 在 OSS 构建中直接返回空操作配置服务，不启用 LRU 缓存层。
func Memoize(base core.ConfigService) core.ConfigService {
	return new(noop)
}
