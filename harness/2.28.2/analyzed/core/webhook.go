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
)

// Webhook 事件类型常量。
const (
	WebhookEventBuild = "build" // 构建事件
	WebhookEventRepo  = "repo"  // 仓库事件
	WebhookEventUser  = "user"  // 用户事件
)

// Webhook 动作类型常量。
const (
	WebhookActionCreated  = "created"  // 创建
	WebhookActionUpdated  = "updated"  // 更新
	WebhookActionDeleted  = "deleted"  // 删除
	WebhookActionEnabled  = "enabled"  // 启用
	WebhookActionDisabled = "disabled" // 禁用
)

type (
	// Webhook 定义出站 Webhook 集成端点配置。
	Webhook struct {
		Endpoint   string `json:"endpoint,omitempty"`    // 目标 URL
		Signer     string `json:"-"`                     // 签名密钥
		SkipVerify bool   `json:"skip_verify,omitempty"` // 是否跳过 TLS 验证
	}

	// WebhookData 封装出站 Webhook 事件载荷。
	WebhookData struct {
		Event  string      `json:"event"`            // 事件类型
		Action string      `json:"action"`           // 动作类型
		User   *User       `json:"user,omitempty"`   // 关联用户
		Repo   *Repository `json:"repo,omitempty"`   // 关联仓库
		Build  *Build      `json:"build,omitempty"`  // 关联构建
	}

	// WebhookSender 将 Webhook 载荷发送至配置的端点。
	WebhookSender interface {
		// Send 向全局 Webhook 端点发送事件。
		Send(context.Context, *WebhookData) error
	}
)
