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

// hook 包内部工具函数，用于查找、创建与删除 SCM Webhook。
package hook

import (
	"context"
	"net/url"

	"github.com/drone/go-scm/scm"
)

// replaceHook 先删除同目标地址的旧 Hook，再创建新 Hook。
func replaceHook(ctx context.Context, client *scm.Client, repo string, hook *scm.HookInput) error {
	if err := deleteHook(ctx, client, repo, hook.Target); err != nil {
		return err
	}
	_, _, err := client.Repositories.CreateHook(ctx, repo, hook)
	return err
}

// deleteHook 按回调 URL 主机名查找并删除匹配的 Webhook。
func deleteHook(ctx context.Context, client *scm.Client, repo, target string) error {
	u, _ := url.Parse(target)
	h, err := findHook(ctx, client, repo, u.Host)
	if err != nil {
		return err
	}
	if h == nil {
		return nil
	}
	_, err = client.Repositories.DeleteHook(ctx, repo, h.ID)
	return err
}

// findHook 列出仓库 Hook 并按目标 URL 主机名匹配返回。
func findHook(ctx context.Context, client *scm.Client, repo, host string) (*scm.Hook, error) {
	hooks, _, err := client.Repositories.ListHooks(ctx, repo, scm.ListOptions{Size: 100})
	if err != nil {
		return nil, err
	}
	for _, hook := range hooks {
		u, err := url.Parse(hook.Target)
		if err != nil {
			continue
		}
		if u.Host == host {
			return hook, nil
		}
	}
	return nil, nil
}
