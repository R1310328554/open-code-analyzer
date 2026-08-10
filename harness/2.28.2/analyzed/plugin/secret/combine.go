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

// secret 包提供流水线密钥插件，支持从数据库、加密 YAML、外部 API 等多源链式查找。
package secret

import (
	"context"
	"strings"

	"github.com/drone/drone/core"
)

// Combine 组合多个 SecretService，按链式顺序查找流水线密钥。
func Combine(services ...core.SecretService) core.SecretService {
	return &combined{services}
}

// combined 依次调用各密钥来源，跳过空结果与 Docker 内部配置名。
type combined struct {
	sources []core.SecretService
}

// Find 查找密钥；忽略 docker_auth_config 等系统保留名称。
func (c *combined) Find(ctx context.Context, in *core.SecretArgs) (*core.Secret, error) {
	// .docker/config.json 为内部专用，不向构建环境暴露。
	if isDockerConfig(in.Name) {
		return nil, nil
	}

	for _, source := range c.sources {
		secret, err := source.Find(ctx, in)
		if err != nil {
			return nil, err
		}
		if secret == nil {
			continue
		}
		// Secret 非 nil 但 Data 为空表示远端返回 204，继续尝试下一来源。
		if secret.Data == "" {
			continue
		}
		return secret, nil
	}
	return nil, nil
}

// isDockerConfig 判断名称是否为 Docker 内部配置变量（不向流水线暴露）。
func isDockerConfig(name string) bool {
	return strings.EqualFold(name, "docker_auth_config") ||
		strings.EqualFold(name, ".dockerconfigjson") ||
		strings.EqualFold(name, ".dockerconfig")
}
