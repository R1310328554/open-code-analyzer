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

// pubsub 包内部定义单个订阅者的消息分发与生命周期管理。
package pubsub

import (
	"sync"

	"github.com/drone/drone/core"
)

// subscriber 表示一个 pubsub 订阅者，持有带缓冲的事件通道与退出信号。
type subscriber struct {
	sync.Mutex

	handler chan *core.Message
	quit    chan struct{}
	done    bool
}

// publish 向订阅者发送事件；若订阅者已关闭或通道已满则丢弃新消息。
func (s *subscriber) publish(event *core.Message) {
	select {
	case <-s.quit:
	case s.handler <- event:
	default:
		// 事件写入带缓冲通道。若消费者处理过慢导致通道已满，
		// 较新的消息将被忽略。
	}
}

// close 关闭订阅者，仅首次调用时发送退出信号。
func (s *subscriber) close() {
	s.Lock()
	if s.done == false {
		close(s.quit)
		s.done = true
	}
	s.Unlock()
}
