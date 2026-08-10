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

import (
	"context"
	"errors"
	"regexp"

	"github.com/drone/drone-yaml/yaml"
)

var (
	// errSecretNameInvalid 表示密钥名称格式无效。
	errSecretNameInvalid = errors.New("Invalid Secret Name")
	// errSecretDataInvalid 表示密钥值为空或无效。
	errSecretDataInvalid = errors.New("Invalid Secret Value")
)

type (
	// Secret 表示构建运行时注入的密钥变量，如密码或令牌。
	Secret struct {
		ID              int64  `json:"id,omitempty"`
		RepoID          int64  `json:"repo_id,omitempty"`
		Namespace       string `json:"namespace,omitempty"`
		Name            string `json:"name,omitempty"`
		Type            string `json:"type,omitempty"`
		Data            string `json:"data,omitempty"`
		PullRequest     bool   `json:"pull_request,omitempty"`
		PullRequestPush bool   `json:"pull_request_push,omitempty"`
	}

	// SecretArgs 提供向远程密钥服务请求密钥所需的上下文参数。
	SecretArgs struct {
		Name  string         `json:"name"`
		Repo  *Repository    `json:"repo,omitempty"`
		Build *Build         `json:"build,omitempty"`
		Conf  *yaml.Manifest `json:"-"`
	}

	// SecretStore 管理仓库级别的密钥存储。
	SecretStore interface {
		// List 从数据存储中返回指定仓库的密钥列表。
		List(context.Context, int64) ([]*Secret, error)

		// Find 按 ID 从数据存储中查询密钥。
		Find(context.Context, int64) (*Secret, error)

		// FindName 按仓库 ID 与名称从数据存储中查询密钥。
		FindName(context.Context, int64, string) (*Secret, error)

		// Create 将新密钥持久化到数据存储。
		Create(context.Context, *Secret) error

		// Update 将更新后的密钥持久化到数据存储。
		Update(context.Context, *Secret) error

		// Delete 从数据存储中删除密钥。
		Delete(context.Context, *Secret) error
	}

	// GlobalSecretStore 管理系统级全局密钥，对所有仓库可见。
	GlobalSecretStore interface {
		// List 从数据存储中返回指定命名空间的密钥列表。
		List(ctx context.Context, namespace string) ([]*Secret, error)

		// ListAll 从数据存储中返回所有命名空间的密钥列表。
		ListAll(ctx context.Context) ([]*Secret, error)

		// Find 按 ID 从数据存储中查询密钥。
		Find(ctx context.Context, id int64) (*Secret, error)

		// FindName 按命名空间与名称从数据存储中查询密钥。
		FindName(ctx context.Context, namespace, name string) (*Secret, error)

		// Create 将新密钥持久化到数据存储。
		Create(ctx context.Context, secret *Secret) error

		// Update 将更新后的密钥持久化到数据存储。
		Update(ctx context.Context, secret *Secret) error

		// Delete 从数据存储中删除密钥。
		Delete(ctx context.Context, secret *Secret) error
	}

	// SecretService 从外部密钥插件服务获取密钥。
	SecretService interface {
		// Find 从全局远程服务按名称返回密钥。
		Find(context.Context, *SecretArgs) (*Secret, error)
	}
)

// Validate 校验密钥的必填字段与名称格式。
func (s *Secret) Validate() error {
	switch {
	case len(s.Name) == 0:
		return errSecretNameInvalid
	case len(s.Data) == 0:
		return errSecretDataInvalid
	case slugRE.MatchString(s.Name):
		return errSecretNameInvalid
	default:
		return nil
	}
}

// Copy 复制密钥元数据，不包含敏感值（Data 字段）。
func (s *Secret) Copy() *Secret {
	return &Secret{
		ID:              s.ID,
		RepoID:          s.RepoID,
		Namespace:       s.Namespace,
		Name:            s.Name,
		Type:            s.Type,
		PullRequest:     s.PullRequest,
		PullRequestPush: s.PullRequestPush,
	}
}

// slugRE 匹配非法 slug 字符（仅允许字母、数字、连字符、下划线与点）。
var slugRE = regexp.MustCompile("[^a-zA-Z0-9-_.]+")
