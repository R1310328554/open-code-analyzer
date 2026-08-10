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

	"github.com/drone/drone-yaml/yaml"
)

const (
	// RegistryPull 策略：允许从镜像仓库拉取镜像。
	RegistryPull = "pull"

	// RegistryPush 策略：允许向镜像仓库推送，Pull Request 事件除外。
	RegistryPush = "push"

	// RegistryPushPullRequest 策略：允许向镜像仓库推送，
	// 包括 Pull Request 在内的所有事件类型。
	RegistryPushPullRequest = "push-pull-request"
)

type (
	// Registry 表示带凭证的 Docker 镜像仓库配置。
	Registry struct {
		Address  string `json:"address"`
		Username string `json:"username"`
		Password string `json:"password"`
		Policy   string `json:"policy"`
	}

	// RegistryArgs 提供向远程服务请求镜像仓库凭证所需的上下文参数。
	RegistryArgs struct {
		Repo     *Repository    `json:"repo,omitempty"`
		Build    *Build         `json:"build,omitempty"`
		Conf     *yaml.Manifest `json:"-"`
		Pipeline *yaml.Pipeline `json:"-"`
	}

	// RegistryService 从外部插件服务获取镜像仓库凭证。
	RegistryService interface {
		// List 从全局远程镜像仓库插件返回凭证列表。
		List(context.Context, *RegistryArgs) ([]*Registry, error)
	}
)
