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

// queue 包提供基于内存的构建取消通知机制（非 Redis 部署）。
package queue

import (
	"context"
	"sync"
	"time"
)

// canceller 维护构建取消状态，并通过 channel 通知等待中的订阅者。
type canceller struct {
	sync.Mutex

	subscribers map[chan struct{}]int64
	cancelled   map[int64]time.Time
}

// newCanceller 创建内存版取消器实例。
func newCanceller() *canceller {
	return &canceller{
		subscribers: make(map[chan struct{}]int64),
		cancelled:   make(map[int64]time.Time),
	}
}

// Cancel 标记指定构建 ID 为已取消，并关闭所有监听该 ID 的订阅 channel。
func (c *canceller) Cancel(ctx context.Context, id int64) error {
	c.Lock()
	c.cancelled[id] = time.Now().Add(time.Minute * 5)
	for subscriber, build := range c.subscribers {
		if id == build {
			close(subscriber)
		}
	}
	c.collect()
	c.Unlock()
	return nil
}

// Cancelled 阻塞等待指定构建被取消；若 context 取消则返回 false。
func (c *canceller) Cancelled(ctx context.Context, id int64) (bool, error) {
	subscriber := make(chan struct{})
	c.Lock()
	c.subscribers[subscriber] = id
	c.Unlock()

	defer func() {
		c.Lock()
		delete(c.subscribers, subscriber)
		c.Unlock()
	}()

	for {
		select {
		case <-ctx.Done():
			return false, ctx.Err()
		case <-time.After(time.Minute):
			c.Lock()
			_, ok := c.cancelled[id]
			c.Unlock()
			if ok {
				return true, nil
			}
		case <-subscriber:
			return true, nil
		}
	}
}

// collect 清理已过 TTL 的取消记录，为网络抖动留出重连窗口。
func (c *canceller) collect() {
	// 已取消构建列表带 TTL，到期后才删除；
	// 这样客户端短暂断连后仍能收到取消通知。
	now := time.Now()
	for build, timestamp := range c.cancelled {
		if now.After(timestamp) {
			delete(c.cancelled, build)
		}
	}
}
