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

// +build oss

package webhook

import (
	"context"

	"github.com/drone/drone/core"
)

// New 在 OSS 构建中返回空操作 Webhook 发送器，不推送任何事件。
func New(Config) core.WebhookSender {
	return new(noop)
}

// noop OSS 构建下的 Webhook 桩实现。
type noop struct{}

// Send 空实现，不发送任何 Webhook 请求。
func (noop) Send(context.Context, *core.WebhookData) error {
	return nil
}
