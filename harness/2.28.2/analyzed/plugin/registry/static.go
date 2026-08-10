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

package registry

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/drone/logger"
	"github.com/drone/drone/plugin/registry/auths"
)

// Static 从数据库静态 Secret 列表提供镜像仓库凭据。
func Static(secrets []*core.Secret) core.RegistryService {
	return &staticController{secrets: secrets}
}

// staticController 按名称索引 Secret 并解析 auths 格式。
type staticController struct {
	secrets []*core.Secret
}

// List 匹配 Pipeline.PullSecrets，校验 PR 访问权限后解析凭据。
func (c *staticController) List(ctx context.Context, in *core.RegistryArgs) ([]*core.Registry, error) {
	static := map[string]*core.Secret{}
	for _, secret := range c.secrets {
		static[secret.Name] = secret
	}

	var results []*core.Registry
	for _, name := range in.Pipeline.PullSecrets {
		logger := logger.FromContext(ctx).WithField("name", name)
		logger.Trace("registry: database: find secret")

		secret, ok := static[name]
		if !ok {
			logger.Trace("registry: database: cannot find secret")
			continue
		}

		// 密钥可限制为非 Pull Request 事件可用；PR 构建时跳过。
		if secret.PullRequest == false &&
			in.Build.Event == core.EventPullRequest {
			logger.Trace("registry: database: pull_request access denied")
			continue
		}

		logger.Trace("registry: database: secret found")
		parsed, err := auths.ParseString(secret.Data)
		if err != nil {
			logger.WithError(err).Error("registry: database: parsing error")
			return nil, err
		}

		results = append(results, parsed...)
	}
	return results, nil
}
