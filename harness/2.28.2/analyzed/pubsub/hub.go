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

package pubsub

import (
	"context"
	"sync"

	"github.com/drone/drone/core"
)

// hub 内存版发布-订阅中心，维护本地订阅者集合。
type hub struct {
	sync.Mutex

	subs map[*subscriber]struct{}
}

// newHub 创建新的内存发布-订阅实例。
func newHub() core.Pubsub {
	return &hub{
		subs: map[*subscriber]struct{}{},
	}
}

// Publish 向所有本地订阅者广播消息。
func (h *hub) Publish(ctx context.Context, e *core.Message) error {
	h.Lock()
	for s := range h.subs {
		s.publish(e)
	}
	h.Unlock()
	return nil
}

// Subscribe 注册新订阅者，返回消息通道与错误通道；上下文取消时自动退订。
func (h *hub) Subscribe(ctx context.Context) (<-chan *core.Message, <-chan error) {
	h.Lock()
	s := &subscriber{
		handler: make(chan *core.Message, 100),
		quit:    make(chan struct{}),
	}
	h.subs[s] = struct{}{}
	h.Unlock()
	errc := make(chan error)
	go func() {
		defer close(errc)
		select {
		case <-ctx.Done():
			h.Lock()
			delete(h.subs, s)
			h.Unlock()
			s.close()
		}
	}()
	return s.handler, errc
}

// Subscribers 返回当前活跃订阅者数量。
func (h *hub) Subscribers() (int, error) {
	h.Lock()
	c := len(h.subs)
	h.Unlock()
	return c, nil
}
