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

// config 包提供流水线配置获取插件，支持从仓库、Jsonnet、远程端点等多源组合加载。
package config

import (
	"context"
	"errors"

	"github.com/drone/drone/core"
)

// errNotFound 在所有配置源均未找到有效配置时返回。
var errNotFound = errors.New("configuration: not found")

// Combine 将多个 ConfigService 串联为链式查找，按顺序尝试各配置源直至获得非空配置。
func Combine(services ...core.ConfigService) core.ConfigService {
	return &combined{services}
}

// combined 按注册顺序依次调用各配置源。
type combined struct {
	sources []core.ConfigService
}

// Find 遍历配置源，返回首个含非空 Data 的配置；全部未命中则返回 errNotFound。
func (c *combined) Find(ctx context.Context, req *core.ConfigArgs) (*core.Config, error) {
	for _, source := range c.sources {
		config, err := source.Find(ctx, req)
		if err != nil {
			return nil, err
		}
		if config == nil {
			continue
		}
		if config.Data == "" {
			continue
		}
		return config, nil
	}
	return nil, errNotFound
}
