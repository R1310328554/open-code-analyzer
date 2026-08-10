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

// secret 包提供静态配置的流水线密钥查找实现。
package secret

import (
	"context"
	"strings"

	"github.com/drone/drone/core"
)

// Static 返回基于预置 Secret 列表的静态密钥控制器。
func Static(secrets []*core.Secret) core.SecretService {
	return &staticController{secrets: secrets}
}

// staticController 在内存 Secret 列表中按名称匹配查找。
type staticController struct {
	secrets []*core.Secret
}

// Find 按名称（不区分大小写）查找密钥，并校验 Pull Request 访问限制。
func (c *staticController) Find(ctx context.Context, in *core.SecretArgs) (*core.Secret, error) {
	for _, secret := range c.secrets {
		if !strings.EqualFold(secret.Name, in.Name) {
			continue
		}
		// 密钥可限制为非 Pull Request 事件可用；PR 构建时跳过该密钥。
		if secret.PullRequest == false &&
			in.Build.Event == core.EventPullRequest {
			continue
		}
		return secret, nil
	}
	return nil, nil
}
