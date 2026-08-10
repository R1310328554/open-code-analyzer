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
	// ConvertArgs 表示调用流水线格式转换服务的请求参数。
	ConvertArgs struct {
		User   *User       `json:"-"`
		Repo   *Repository `json:"repo,omitempty"`
		Build  *Build      `json:"build,omitempty"`
		Config *Config     `json:"config,omitempty"`
	}

	// ConvertService 将非原生流水线配置格式转换为
	// 系统原生格式（例如 jsonnet 转 yaml）。
	ConvertService interface {
		// Convert 执行配置格式转换并返回原生 Config。
		Convert(context.Context, *ConvertArgs) (*Config, error)
	}
)
