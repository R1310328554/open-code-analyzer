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

// hook 包管理 SCM 仓库上的 Drone Webhook 创建与删除。
package hook

import (
	"context"
	"time"

	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

// New 构造 HookService，绑定 SCM 客户端、回调地址与监听事件列表。
func New(client *scm.Client, addr string, renew core.Renewer, events []string) core.HookService {
	return &service{client: client, addr: addr, renew: renew, events: events}
}

// service 实现 core.HookService，负责在 SCM 侧注册 Drone 回调。
type service struct {
	renew  core.Renewer
	client *scm.Client
	addr   string
	events []string
}

// Create 在指定仓库创建或替换 Drone Webhook，订阅配置的事件类型。
func (s *service) Create(ctx context.Context, user *core.User, repo *core.Repository) error {
	err := s.renew.Renew(ctx, user, false)
	if err != nil {
		return err
	}

	eventsMap := make(map[string]bool)
	for _, event := range s.events {
		eventsMap[event] = true
	}

	ctx = context.WithValue(ctx, scm.TokenKey{}, &scm.Token{
		Token:   user.Token,
		Refresh: user.Refresh,
		Expires: time.Unix(user.Expiry, 0),
	})
	hook := &scm.HookInput{
		Name:   "drone",
		Target: s.addr + "/hook",
		Secret: repo.Signer,
		Events: scm.HookEvents{
			Branch:      eventsMap["branch"],
			Deployment:  eventsMap["deployment"],
			PullRequest: eventsMap["pull_request"],
			Push:        eventsMap["push"],
			Tag:         eventsMap["tag"],
		},
	}
	return replaceHook(ctx, s.client, repo.Slug, hook)
}

// Delete 删除仓库上指向 Drone 的 Webhook。
func (s *service) Delete(ctx context.Context, user *core.User, repo *core.Repository) error {
	err := s.renew.Renew(ctx, user, false)
	if err != nil {
		return err
	}
	ctx = context.WithValue(ctx, scm.TokenKey{}, &scm.Token{
		Token:   user.Token,
		Refresh: user.Refresh,
		Expires: time.Unix(user.Expiry, 0),
	})
	return deleteHook(ctx, s.client, repo.Slug, s.addr)
}
