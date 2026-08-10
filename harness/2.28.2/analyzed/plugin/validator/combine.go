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

// validator 包提供流水线配置校验插件，支持链式组合多个校验来源。
package validator

import (
	"context"

	"github.com/drone/drone/core"
)

// Combine 组合多个 ValidateService，依次执行全部校验逻辑。
func Combine(services ...core.ValidateService) core.ValidateService {
	return &combined{services}
}

// combined 按顺序调用各校验服务，任一失败即中断并返回错误。
type combined struct {
	sources []core.ValidateService
}

// Validate 依次调用链中各校验器，首个错误即返回。
func (c *combined) Validate(ctx context.Context, req *core.ValidateArgs) error {
	for _, source := range c.sources {
		if err := source.Validate(ctx, req); err != nil {
			return err
		}
	}
	return nil
}
