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

import "context"

type (
	// Config 表示流水线配置文件内容。
	Config struct {
		Data string `json:"data"` // 配置正文
		Kind string `json:"kind"` // 配置格式种类（如 yaml）
	}

	// ConfigArgs 表示获取流水线配置文件（如 .drone.yml）的请求参数。
	ConfigArgs struct {
		User   *User       `json:"-"`
		Repo   *Repository `json:"repo,omitempty"`
		Build  *Build      `json:"build,omitempty"`
		Config *Config     `json:"config,omitempty"`
	}

	// ConfigService 从外部服务解析并返回流水线配置。
	ConfigService interface {
		// Find 根据上下文参数查找并返回配置。
		Find(context.Context, *ConfigArgs) (*Config, error)
	}
)
