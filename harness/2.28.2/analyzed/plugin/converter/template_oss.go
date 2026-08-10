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

// converter 包（OSS 构建）提供模板转换插件的空实现。
package converter

import (
	"context"

	"github.com/drone/drone/core"
)

// Template 在 OSS 构建中返回空操作转换服务，不执行模板展开。
func Template(templateStore core.TemplateStore, stepLimit uint64, sizeLimit uint64) core.ConvertService {
	return &templatePlugin{
		templateStore: templateStore,
	}
}

// templatePlugin OSS 构建下的模板插件桩实现。
type templatePlugin struct {
	templateStore core.TemplateStore
}

// Convert 在 OSS 构建中始终返回 nil，不进行任何配置转换。
func (p *templatePlugin) Convert(ctx context.Context, req *core.ConvertArgs) (*core.Config, error) {
	return nil, nil
}
