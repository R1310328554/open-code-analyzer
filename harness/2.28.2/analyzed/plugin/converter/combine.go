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

// converter 包提供流水线配置格式转换插件，支持 Jsonnet 等多步链式转换。
package converter

import (
	"context"

	"github.com/drone/drone/core"
)

// Combine 将多个 ConvertService 串联；multi 为 true 时依次转换并传递中间结果，否则返回首个非空结果。
func Combine(multi bool, services ...core.ConvertService) core.ConvertService {
	return &combined{multi: multi, sources: services}
}

// combined 按顺序调用各转换器，支持单步短路或多步管道模式。
type combined struct {
	sources []core.ConvertService

	// 该特性开关可在解决多转换器链式问题后移除，参见
	// https://github.com/harness/drone/pull/2994#issuecomment-795955312
	multi bool
}

// Convert 遍历转换源；multi 模式下将每步输出写回 req.Config 供下一步使用，否则首个有效结果即返回。
func (c *combined) Convert(ctx context.Context, req *core.ConvertArgs) (*core.Config, error) {
	for _, source := range c.sources {
		config, err := source.Convert(ctx, req)
		if err != nil {
			return nil, err
		}
		if config == nil {
			continue
		}
		if config.Data == "" {
			continue
		}
		if c.multi {
			req.Config = config
		} else {
			return config, nil
		}
	}
	return req.Config, nil
}
