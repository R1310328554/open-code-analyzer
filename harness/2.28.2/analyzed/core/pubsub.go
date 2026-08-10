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

import "context"

// Message 定义构建变更事件消息，用于发布/订阅广播。
type Message struct {
	Repository string
	Visibility string
	Data       []byte
}

// Pubsub 提供发布/订阅能力，将多个发布者的消息分发给多个订阅者。
type Pubsub interface {
	// Publish 向所有订阅者发布消息。
	Publish(context.Context, *Message) error

	// Subscribe 订阅消息代理，返回消息通道与错误通道。
	Subscribe(context.Context) (<-chan *Message, <-chan error)

	// Subscribers 返回当前订阅者数量。
	Subscribers() (int, error)
}
