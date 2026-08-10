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

// redisdb 包封装 Redis 客户端、Pub/Sub 订阅与分布式互斥锁。
package redisdb

import (
	"context"
	"fmt"
	"time"

	"github.com/go-redsync/redsync/v4"
	"github.com/go-redsync/redsync/v4/redis/goredis/v8"

	"github.com/drone/drone/cmd/drone-server/config"

	"github.com/go-redis/redis/v8"
	"github.com/sirupsen/logrus"
)

// New 根据配置创建 RedisDB 服务；连接字符串或地址未配置时返回零值。
func New(config config.Config) (srv RedisDB, err error) {
	var options *redis.Options

	if config.Redis.ConnectionString != "" {
		options, err = redis.ParseURL(config.Redis.ConnectionString)
		if err != nil {
			return
		}
	} else if config.Redis.Addr != "" {
		options = &redis.Options{
			Addr:     config.Redis.Addr,
			Password: config.Redis.Password,
			DB:       config.Redis.DB,
		}
	} else {
		return
	}

	rdb := redis.NewClient(options)

	_, err = rdb.Ping(context.Background()).Result()
	if err != nil {
		err = fmt.Errorf("redis not accessibe: %w", err)
		return
	}

	rs := redsync.New(goredis.NewPool(rdb))

	srv = redisService{
		rdb:      rdb,
		mutexGen: rs,
	}

	return
}

// RedisDB 定义 Redis 客户端、Pub/Sub 与分布式锁的抽象接口。
type RedisDB interface {
	Client() redis.Cmdable
	Subscribe(ctx context.Context, channelName string, channelSize int, proc PubSubProcessor)
	NewMutex(name string, expiry time.Duration) LockErr
}

// redisService 是 RedisDB 的默认实现。
type redisService struct {
	rdb      *redis.Client
	mutexGen *redsync.Redsync
}

// Client 返回底层 redis.Cmdable 接口供调用方执行命令。
func (r redisService) Client() redis.Cmdable {
	return r.rdb
}

// PubSubProcessor 处理 Redis Pub/Sub 消息与连接错误。
type PubSubProcessor interface {
	ProcessMessage(s string)
	ProcessError(err error)
}

// backoffDurations 定义 Pub/Sub 断线重连的递增退避间隔。
var backoffDurations = []time.Duration{
	0, time.Second, 3 * time.Second, 5 * time.Second, 10 * time.Second, 20 * time.Second,
}

// Subscribe 订阅指定 Redis 频道，通过 proc 处理消息；出错时按退避策略自动重连。
// 仅当 context 取消或超时时才会退出。
func (r redisService) Subscribe(ctx context.Context, channelName string, channelSize int, proc PubSubProcessor) {
	var connectTry int
	for {
		err := func() (err error) {
			defer func() {
				// 捕获外部 PubSubProcessor 可能触发的 panic。
				if p := recover(); p != nil {
					err = fmt.Errorf("redis pubsub: panic: %v", p)
				}
			}()

			var options []redis.ChannelOption

			if channelSize > 1 {
				options = append(options, redis.WithChannelSize(channelSize))
			}

			pubsub := r.rdb.Subscribe(ctx, channelName)
			ch := pubsub.Channel(options...)

			defer func() {
				_ = pubsub.Close()
			}()

			// 确认订阅连接可用
			err = pubsub.Ping(ctx)
			if err != nil {
				return
			}

			connectTry = 0 // 连接成功，重置重试计数

			logrus.
				WithField("try", connectTry+1).
				WithField("channel", channelName).
				Trace("redis pubsub: subscribed")

			for {
				select {
				case m, ok := <-ch:
					if !ok {
						err = fmt.Errorf("redis pubsub: channel=%s closed", channelName)
						return
					}

					proc.ProcessMessage(m.Payload)

				case <-ctx.Done():
					err = ctx.Err()
					return
				}
			}
		}()
		if err == nil {
			// 不应发生：内部循环应始终以错误退出
			continue
		}

		proc.ProcessError(err)

		if err == context.Canceled || err == context.DeadlineExceeded {
			logrus.
				WithField("channel", channelName).
				Trace("redis pubsub: finished")
			return
		}

		dur := backoffDurations[connectTry]

		logrus.
			WithError(err).
			WithField("try", connectTry+1).
			WithField("pause", dur.String()).
			WithField("channel", channelName).
			Error("redis pubsub: connection failed, reconnecting")

		time.Sleep(dur)

		if connectTry < len(backoffDurations)-1 {
			connectTry++
		}
	}
}

// NewMutex 创建带可选过期时间的 redsync 分布式互斥锁。
func (r redisService) NewMutex(name string, expiry time.Duration) LockErr {
	var options []redsync.Option
	if expiry > 0 {
		options = append(options, redsync.WithExpiry(expiry))
	}

	return r.mutexGen.NewMutex(name, options...)
}
