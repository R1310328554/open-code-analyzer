// Copyright 2021 Drone IO, Inc.
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

// +build !oss

package pubsub

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"sync"

	"github.com/drone/drone/core"
	"github.com/drone/drone/service/redisdb"
)

const (
	redisPubSubEvents   = "drone-events"
	redisPubSubCapacity = 100
)

// newHubRedis 创建基于 Redis 的跨进程发布-订阅中心。
func newHubRedis(r redisdb.RedisDB) core.Pubsub {
	h := &hubRedis{
		rdb:         r,
		subscribers: make(map[chan<- *core.Message]struct{}),
	}

	go r.Subscribe(context.Background(), redisPubSubEvents, redisPubSubCapacity, h)

	return h
}

// hubRedis 通过 Redis Pub/Sub 在多个 Drone 实例间分发事件。
type hubRedis struct {
	sync.Mutex
	rdb         redisdb.RedisDB
	subscribers map[chan<- *core.Message]struct{}
}

// Publish 将消息 JSON 序列化后发布到 Redis 频道，所有实例的订阅者均可收到。
func (h *hubRedis) Publish(ctx context.Context, e *core.Message) (err error) {
	client := h.rdb.Client()

	data, err := json.Marshal(e)
	if err != nil {
		return
	}

	_, err = client.Publish(ctx, redisPubSubEvents, data).Result()
	if err != nil {
		return
	}

	return
}

// Subscribe 注册新订阅者；上下文取消时自动退订并关闭通道。
func (h *hubRedis) Subscribe(ctx context.Context) (<-chan *core.Message, <-chan error) {
	chMessage := make(chan *core.Message, redisPubSubCapacity)
	chErr := make(chan error)

	h.Lock()
	h.subscribers[chMessage] = struct{}{}
	h.Unlock()

	go func() {
		<-ctx.Done()

		h.Lock()
		delete(h.subscribers, chMessage)
		h.Unlock()

		close(chMessage)
		close(chErr)
	}()

	return chMessage, chErr
}

// Subscribers 返回当前本地订阅者数量。
func (h *hubRedis) Subscribers() (int, error) {
	h.Lock()
	n := len(h.subscribers)
	h.Unlock()

	return n, nil
}

// ProcessMessage 将 Redis 消息转发给所有本地订阅者，实现 redisdb.PubSubProcessor 接口。
func (h *hubRedis) ProcessMessage(s string) {
	message := &core.Message{}
	err := json.Unmarshal([]byte(s), message)
	if err != nil {
		// 忽略无效消息；Publish 侧已保证 JSON 编码，此处不应发生。
		_, _ = fmt.Fprintf(os.Stderr, "pubsub/redis: failed to unmarshal a message. %s
", err)
		return
	}

	h.Lock()
	for ss := range h.subscribers {
		select {
		case ss <- message:
		default: // 订阅者通道已满时丢弃消息，避免阻塞 Redis 消费
		}
	}
	h.Unlock()
}

// ProcessError 实现 redisdb.PubSubProcessor 接口，当前为空操作。
func (h *hubRedis) ProcessError(error) {}
