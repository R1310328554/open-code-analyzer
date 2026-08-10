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

// registry 包提供镜像仓库凭据插件，支持从文件、数据库、外部 API 等多源组合加载。
package registry

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/drone/logger"

	"github.com/sirupsen/logrus"
)

// Combine 组合多个 RegistryService，使系统可从多个来源聚合镜像仓库凭据。
func Combine(services ...core.RegistryService) core.RegistryService {
	return &combined{services}
}

// combined 依次调用各注册表凭据源并合并结果。
type combined struct {
	sources []core.RegistryService
}

// List 遍历所有来源获取凭据并合并；任一来源出错则立即返回。
func (c *combined) List(ctx context.Context, req *core.RegistryArgs) ([]*core.Registry, error) {
	var all []*core.Registry
	for _, source := range c.sources {
		list, err := source.List(ctx, req)
		if err != nil {
			return all, err
		}
		all = append(all, list...)
	}
	// Trace 级别下输出从各来源加载的镜像仓库凭据摘要。
	logger := logger.FromContext(ctx)
	if logrus.IsLevelEnabled(logrus.TraceLevel) {
		if len(all) == 0 {
			logger.Traceln("registry: no registry credentials loaded")
		}
		for _, registry := range all {
			logger.WithField("address", registry.Address).
				Traceln("registry: registry credentials loaded")
		}
	}
	return all, nil
}
