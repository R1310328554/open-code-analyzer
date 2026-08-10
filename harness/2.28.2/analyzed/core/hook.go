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
	"net/http"
)

// 钩子动作常量，描述 SCM Webhook 载荷中的细粒度动作。
const (
	ActionOpen   = "open"   // 打开（如 PR 创建）
	ActionClose  = "close"  // 关闭
	ActionCreate = "create" // 创建
	ActionDelete = "delete" // 删除
	ActionSync   = "sync"   // 同步
)

// Hook 表示 post-commit Webhook 的标准化载荷。
type Hook struct {
	Parent       int64             `json:"parent"`
	Trigger      string            `json:"trigger"`
	Event        string            `json:"event"`
	Action       string            `json:"action"`
	Link         string            `json:"link"`
	Timestamp    int64             `json:"timestamp"`
	Title        string            `json:"title"`
	Message      string            `json:"message"`
	Before       string            `json:"before"`
	After        string            `json:"after"`
	Ref          string            `json:"ref"`
	Fork         string            `json:"hook"`
	Source       string            `json:"source"`
	Target       string            `json:"target"`
	Author       string            `json:"author_login"`
	AuthorName   string            `json:"author_name"`
	AuthorEmail  string            `json:"author_email"`
	AuthorAvatar string            `json:"author_avatar"`
	Deployment   string            `json:"deploy_to"`
	DeploymentID int64             `json:"deploy_id"`
	Debug        bool              `json:"debug"`
	Cron         string            `json:"cron"`
	Sender       string            `json:"sender"`
	Params       map[string]string `json:"params"`
}

// HookService 在外部 SCM（如 GitHub）上注册或删除 post-commit 钩子。
type HookService interface {
	// Create 为仓库创建 Webhook。
	Create(ctx context.Context, user *User, repo *Repository) error
	// Delete 删除仓库 Webhook。
	Delete(ctx context.Context, user *User, repo *Repository) error
}

// HookParser 解析 SCM 原始 Webhook 请求并返回规范化 Hook 与仓库信息。
type HookParser interface {
	// Parse 从 HTTP 请求解析钩子，secretFunc 用于按仓库名查找签名密钥。
	Parse(req *http.Request, secretFunc func(string) string) (*Hook, *Repository, error)
}
