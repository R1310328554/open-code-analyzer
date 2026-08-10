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

// queue 包（非 OSS 构建）提供基于 Redis 的分布式构建取消通知。
package queue

import (
	"context"
	"fmt"
	"os"
	"strconv"
	"sync"
	"time"

	"github.com/drone/drone/service/redisdb"

	"github.com/go-redis/redis/v8"
)

const (
	redisPubSubCancel       = "drone-cancel"
	redisCancelValuePrefix  = "drone-cancel-"
	redisCancelValueTimeout = 5 * time.Minute
	redisCancelValue        = "canceled"
)

// newCancellerRedis 创建 Redis 版取消器并订阅取消 pub/sub 频道。
func newCancellerRedis(r redisdb.RedisDB) *cancellerRedis {
	h := &cancellerRedis{
		rdb:         r,
		subscribers: make(map[*cancelSubscriber]struct{}),
	}

	go r.Subscribe(context.Background(), redisPubSubCancel, 1, h)

	return h
}

// cancellerRedis 通过 Redis pub/sub 与键值存储实现跨节点取消广播。
type cancellerRedis struct {
	rdb         redisdb.RedisDB
	subscribers map[*cancelSubscriber]struct{}
	sync.Mutex
}

// cancelSubscriber 表示等待特定构建取消通知的本地订阅者。
type cancelSubscriber struct {
	id int64
	ch chan<- error
}

// Cancel 向所有订阅者（Runner）广播指定构建 ID 的取消事件。
func (c *cancellerRedis) Cancel(ctx context.Context, id int64) (err error) {
	client := c.rdb.Client()

	ids := strconv.FormatInt(id, 10)

	// 向所有等待中的 Runner 发布取消事件
	_, err = client.Publish(ctx, redisPubSubCancel, ids).Result()
	if err != nil {
		return
	}

	// 写入带过期时间的键，供当前未监听的 Runner 稍后查询
	_, err = client.Set(ctx, redisCancelValuePrefix+ids, redisCancelValue, redisCancelValueTimeout).Result()
	if err != nil {
		return
	}

	return
}

// Cancelled 阻塞等待指定构建被取消；context 取消时中止等待。
func (c *cancellerRedis) Cancelled(ctx context.Context, id int64) (isCancelled bool, err error) {
	client := c.rdb.Client()

	ids := strconv.FormatInt(id, 10)

	// 先检查构建是否已被取消

	result, err := client.Get(ctx, redisCancelValuePrefix+ids).Result()
	if err != nil && err != redis.Nil {
		return
	}

	isCancelled = err != redis.Nil && result == redisCancelValue
	if isCancelled {
		return
	}

	// 若尚未取消，则订阅取消事件直至 context 结束或收到取消通知

	ch := make(chan error)
	sub := &cancelSubscriber{id: id, ch: ch}

	c.Lock()
	c.subscribers[sub] = struct{}{}
	c.Unlock()

	select {
	case err = <-ch:
		// 构建已取消或发生错误时，订阅者由其他 goroutine 从集合中移除
		isCancelled = err != nil
	case <-ctx.Done():
		// context 取消时须在此处移除订阅者
		c.Lock()
		delete(c.subscribers, sub)
		c.Unlock()
	}

	return
}

// ProcessMessage 处理 Redis 取消消息，通知监听该构建 ID 的所有本地订阅者。
// 实现 redisdb.PubSubProcessor 接口，由 Subscribe 内部调用。
func (c *cancellerRedis) ProcessMessage(s string) {
	id, err := strconv.ParseInt(s, 10, 64)
	if err != nil {
		// 忽略非法消息；Cancel 方法保证消息均为整数字符串，此情况不应发生
		_, _ = fmt.Fprintf(os.Stderr, "canceller/redis: message is not an integer: %s\n", s)
		return
	}

	c.Lock()
	for ss := range c.subscribers {
		if ss.id == id {
			ss.ch <- nil
			close(ss.ch)
			delete(c.subscribers, ss)
		}
	}
	c.Unlock()
}

// ProcessError 向所有订阅者报告错误并清空订阅集合。
// 每个订阅者只接收一条消息，出错时清空集合以避免遗漏通知。
// 实现 redisdb.PubSubProcessor 接口，由 Subscribe 内部调用。
func (c *cancellerRedis) ProcessError(err error) {
	c.Lock()
	for ss := range c.subscribers {
		ss.ch <- err
		close(ss.ch)
		delete(c.subscribers, ss)
	}
	c.Unlock()
}
