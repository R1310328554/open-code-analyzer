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

// +build !oss

package livelog

import (
	"context"
	"encoding/json"
	"fmt"
	"strconv"
	"time"

	"github.com/drone/drone/core"
	"github.com/drone/drone/service/redisdb"

	"github.com/go-redis/redis/v8"
)

// newStreamRedis 构造基于 Redis Stream 的日志流实现。
func newStreamRedis(r redisdb.RedisDB) core.LogStream {
	return streamRedis{
		rdb: r,
	}
}

const (
	redisKeyExpiryTime = 5 * time.Hour          // 每个 Redis 键的存活时间
	redisPollTime      = 100 * time.Millisecond // XRead 阻塞间隔，避免长时间占用连接
	redisTailMaxTime   = 1 * time.Hour          // Tail 轮询的最长持续时间
	redisEntryKey      = "line"
	redisStreamPrefix  = "drone-log-"
)

// streamRedis 使用 Redis Stream 在集群间共享构建步骤日志。
type streamRedis struct {
	rdb redisdb.RedisDB
}

// Create 创建 Redis 日志流并设置过期时间；若键已存在则先删除。
func (r streamRedis) Create(ctx context.Context, id int64) error {
	// Delete if a stream already exists with the same key
	_ = r.Delete(ctx, id)

	client := r.rdb.Client()

	key := redisStreamPrefix + strconv.FormatInt(id, 10)

	addResp := client.XAdd(ctx, &redis.XAddArgs{
		Stream: key,
		ID:     "*", // auto-generate a unique incremental ID
		MaxLen: bufferSize,
		Approx: true,
		Values: map[string]interface{}{redisEntryKey: []byte{}},
	})
	if err := addResp.Err(); err != nil {
		return fmt.Errorf("livelog/redis: could not create stream with key %s", key)
	}

	res := client.Expire(ctx, key, redisKeyExpiryTime)
	if err := res.Err(); err != nil {
		return fmt.Errorf("livelog/redis: could not set expiry for key %s", key)
	}

	return nil
}

// Delete 删除指定步骤 ID 对应的 Redis 日志流。
func (r streamRedis) Delete(ctx context.Context, id int64) error {
	client := r.rdb.Client()

	key := redisStreamPrefix + strconv.FormatInt(id, 10)

	if err := r._exists(ctx, key); err != nil {
		return err
	}

	deleteResp := client.Del(ctx, key)
	if err := deleteResp.Err(); err != nil {
		return fmt.Errorf("livelog/redis: could not delete stream for step %d", id)
	}

	return nil
}

// Write 将一行日志 JSON 写入 Redis Stream。
func (r streamRedis) Write(ctx context.Context, id int64, line *core.Line) error {
	client := r.rdb.Client()

	key := redisStreamPrefix + strconv.FormatInt(id, 10)

	if err := r._exists(ctx, key); err != nil {
		return err
	}

	lineJsonData, _ := json.Marshal(line)
	addResp := client.XAdd(ctx, &redis.XAddArgs{
		Stream: key,
		ID:     "*", // auto-generate a unique incremental ID
		MaxLen: bufferSize,
		Approx: true,
		Values: map[string]interface{}{redisEntryKey: lineJsonData},
	})
	if err := addResp.Err(); err != nil {
		return err
	}

	return nil
}

// Tail 持续从 Redis Stream 读取新日志行，直至上下文取消或超时。
func (r streamRedis) Tail(ctx context.Context, id int64) (<-chan *core.Line, <-chan error) {
	client := r.rdb.Client()

	key := redisStreamPrefix + strconv.FormatInt(id, 10)

	if err := r._exists(ctx, key); err != nil {
		return nil, nil
	}

	chLines := make(chan *core.Line, bufferSize)
	chErr := make(chan error, 1)

	go func() {
		defer close(chErr)
		defer close(chLines)
		timeout := time.After(redisTailMaxTime) // polling should not last for longer than tailMaxTime

		// 从 "0" 起增量读取 Stream 条目并写入通道。
		lastID := "0"

		for {
			select {
			case <-ctx.Done():
				return
			case <-timeout:
				return
			default:
				readResp := client.XRead(ctx, &redis.XReadArgs{
					Streams: append([]string{key}, lastID),
					Block:   redisPollTime, // periodically check for ctx.Done
				})
				if readResp.Err() != nil && readResp.Err() != redis.Nil { // readResp.Err() is sometimes set to "redis: nil" instead of nil
					chErr <- readResp.Err()
					return
				}

				for _, msg := range readResp.Val() {
					messages := msg.Messages
					if len(messages) > 0 {
						lastID = messages[len(messages)-1].ID
					} else { // should not happen
						return
					}

					for _, message := range messages {
						values := message.Values
						if val, ok := values[redisEntryKey]; ok {
							var line *core.Line
							if err := json.Unmarshal([]byte(val.(string)), &line); err != nil {
								continue // ignore errors in the stream
							}
							chLines <- line
						}
					}
				}
			}
		}
	}()

	return chLines, chErr
}

// Info 扫描 Redis 中所有日志流键并返回各步骤的条目数量。
func (r streamRedis) Info(ctx context.Context) (info *core.LogStreamInfo) {
	client := r.rdb.Client()

	info = &core.LogStreamInfo{
		Streams: make(map[int64]int),
	}

	keysResp := client.Keys(ctx, redisStreamPrefix+"*")
	if err := keysResp.Err(); err != nil {
		return
	}

	for _, key := range keysResp.Val() {
		ids := key[len(redisStreamPrefix):]
		id, err := strconv.ParseInt(ids, 10, 64)
		if err != nil {
			continue
		}

		lenResp := client.XLen(ctx, key)
		if err := lenResp.Err(); err != nil {
			continue
		}

		size := int(lenResp.Val())

		info.Streams[id] = size
	}

	return
}

// _exists 校验 Redis 日志流键是否存在。
func (r streamRedis) _exists(ctx context.Context, key string) error {
	client := r.rdb.Client()

	exists := client.Exists(ctx, key)
	if exists.Err() != nil || exists.Val() == 0 {
		return fmt.Errorf("livelog/redis: log stream %s not found", key)
	}

	return nil
}
