//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

// redis.go — Redis 客户端封装：全局单例初始化、KV/集合/有序集合、Stream 队列、分布式锁、令牌桶限流；供任务调度、缓存与会话状态共享。

package redis

import (
	"context"
	"encoding/json"
	"fmt"
	"math"
	"math/rand"
	"ragflow/internal/common"
	"strconv"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"

	"ragflow/internal/server"
)

var (
	globalClient *RedisClient
	once         sync.Once
)

// RedisClient 封装 go-redis 客户端，提供 KV、集合、Stream 与 Lua 脚本工具方法
type RedisClient struct {
	client           *redis.Client
	luaDeleteIfEqual *redis.Script
	luaTokenBucket   *redis.Script
	luaAutoIncrement *redis.Script
	config           *server.RedisConfig
}

// RedisMsg 表示从 Redis Stream 消费到的单条消息及其元数据
type RedisMsg struct {
	consumer  *redis.Client
	queueName string
	groupName string
	msgID     string
	message   map[string]interface{}
}

// Lua 脚本常量（原子删除、令牌桶）
const (
	luaDeleteIfEqualScript = `
		local current_value = redis.call('get', KEYS[1])
		if current_value and current_value == ARGV[1] then
			redis.call('del', KEYS[1])
			return 1
		end
		return 0
	`

	luaTokenBucketScript = `
		local key       = KEYS[1]
		local capacity  = tonumber(ARGV[1])
		local rate      = tonumber(ARGV[2])
		local now       = tonumber(ARGV[3])
		local cost      = tonumber(ARGV[4])

		local data = redis.call("HMGET", key, "tokens", "timestamp")
		local tokens = tonumber(data[1])
		local last_ts = tonumber(data[2])

		if tokens == nil then
			tokens = capacity
			last_ts = now
		end

		local delta = math.max(0, now - last_ts)
		tokens = math.min(capacity, tokens + delta * rate)

		if tokens < cost then
			return {0, tokens}
		end

		tokens = tokens - cost

		redis.call("HMSET", key,
			"tokens", tokens,
			"timestamp", now
		)

		redis.call("EXPIRE", key, math.ceil(capacity / rate * 2))

		return {1, tokens}
	`
)

// Init 以 sync.Once 初始化全局 Redis 客户端；Host 为空则跳过
func Init(cfg *server.RedisConfig) error {
	var initErr error
	once.Do(func() {
		if cfg.Host == "" {
			common.Info("Redis host not configured, skipping Redis initialization")
			return
		}

		client := redis.NewClient(&redis.Options{
			Addr:     fmt.Sprintf("%s:%d", cfg.Host, cfg.Port),
			Password: cfg.Password,
			DB:       cfg.DB,
		})

		// Ping 验证连接可用性
		ctx, cancel := context.WithTimeout(context.Background(), server.DefaultConnectTimeout)
		defer cancel()

		if err := client.Ping(ctx).Err(); err != nil {
			initErr = fmt.Errorf("failed to connect to Redis: %w", err)
			return
		}

		globalClient = &RedisClient{
			client:           client,
			config:           cfg,
			luaDeleteIfEqual: redis.NewScript(luaDeleteIfEqualScript),
			luaTokenBucket:   redis.NewScript(luaTokenBucketScript),
		}

		common.Info("Redis client initialized",
			zap.String("host", cfg.Host),
			zap.Int("port", cfg.Port),
			zap.Int("db", cfg.DB),
		)
	})
	return initErr
}

// Get 返回全局 RedisClient 单例（可能为 nil）
func Get() *RedisClient {
	return globalClient
}

// Close 关闭底层 go-redis 连接
func Close() error {
	if globalClient != nil && globalClient.client != nil {
		return globalClient.client.Close()
	}
	return nil
}

// IsEnabled 判断 Redis 是否已配置并成功初始化
func IsEnabled() bool {
	return globalClient != nil && globalClient.client != nil
}

// Health 通过 Ping + Set/Get 探活验证 Redis 可用
func (r *RedisClient) Health() bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	if err := r.client.Ping(ctx).Err(); err != nil {
		return false
	}

	testKey := "health_check_" + uuid.New().String()
	testValue := "yy"
	if err := r.client.Set(ctx, testKey, testValue, 3*time.Second).Err(); err != nil {
		return false
	}

	val, err := r.client.Get(ctx, testKey).Result()
	if err != nil || val != testValue {
		return false
	}
	return true
}

// Info 获取 Redis INFO 并提取版本、内存、客户端等关键指标
func (r *RedisClient) Info() map[string]interface{} {
	if r.client == nil {
		return nil
	}
	ctx := context.Background()
	infoStr, err := r.client.Info(ctx).Result()
	if err != nil {
		common.Warn("Failed to get Redis info", zap.Error(err))
		return nil
	}

	// 将 INFO 文本解析为键值 map
	info := make(map[string]string)
	lines := splitLines(infoStr)
	for _, line := range lines {
		if line == "" || line[0] == '#' {
			continue
		}
		parts := splitN(line, ":", 2)
		if len(parts) == 2 {
			info[parts[0]] = parts[1]
		}
	}

	result := map[string]interface{}{
		"redis_version":             info["redis_version"],
		"server_mode":               getServerMode(info),
		"used_memory":               info["used_memory_human"],
		"total_system_memory":       info["total_system_memory_human"],
		"mem_fragmentation_ratio":   info["mem_fragmentation_ratio"],
		"connected_clients":         parseInt(info["connected_clients"]),
		"blocked_clients":           parseInt(info["blocked_clients"]),
		"instantaneous_ops_per_sec": parseInt(info["instantaneous_ops_per_sec"]),
		"total_commands_processed":  parseInt(info["total_commands_processed"]),
	}
	return result
}

func getServerMode(info map[string]string) string {
	if mode, ok := info["server_mode"]; ok {
		return mode
	}
	return info["redis_mode"]
}

func splitLines(s string) []string {
	var lines []string
	start := 0
	for i := 0; i < len(s); i++ {
		if s[i] == '\n' {
			lines = append(lines, s[start:i])
			start = i + 1
		}
	}
	if start < len(s) {
		lines = append(lines, s[start:])
	}
	return lines
}

func splitN(s, sep string, n int) []string {
	if n <= 0 {
		return []string{s}
	}
	idx := -1
	for i := 0; i < len(s)-len(sep)+1; i++ {
		if s[i:i+len(sep)] == sep {
			idx = i
			break
		}
	}
	if idx == -1 {
		return []string{s}
	}
	return []string{s[:idx], s[idx+len(sep):]}
}

func parseInt(s string) int {
	v, _ := strconv.Atoi(s)
	return v
}

// IsAlive 仅检查 client 指针非 nil
func (r *RedisClient) IsAlive() bool {
	return r.client != nil
}

// Exist 判断键是否存在
func (r *RedisClient) Exist(key string) (bool, error) {
	if r.client == nil {
		return false, nil
	}
	ctx := context.Background()
	exists, err := r.client.Exists(ctx, key).Result()
	if err != nil {
		common.Warn("Redis Exist error", zap.String("key", key), zap.Error(err))
		return false, err
	}
	return exists > 0, nil
}

// Get 按键读取字符串；redis.Nil 时返回空串
func (r *RedisClient) Get(key string) (string, error) {
	if r.client == nil {
		return "", nil
	}
	ctx := context.Background()
	val, err := r.client.Get(ctx, key).Result()
	if err == redis.Nil {
		return "", nil
	}
	if err != nil {
		common.Warn("Redis Get error", zap.String("key", key), zap.Error(err))
		return "", err
	}
	return val, nil
}

// SetObj 将对象 JSON 序列化后写入并设置过期时间
func (r *RedisClient) SetObj(key string, obj interface{}, exp time.Duration) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	data, err := json.Marshal(obj)
	if err != nil {
		common.Warn("Redis SetObj marshal error", zap.String("key", key), zap.Error(err))
		return false
	}
	if err := r.client.Set(ctx, key, data, exp).Err(); err != nil {
		common.Warn("Redis SetObj error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// GetObj 读取 JSON 并反序列化到 dest
func (r *RedisClient) GetObj(key string, dest interface{}) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	data, err := r.client.Get(ctx, key).Result()
	if err == redis.Nil {
		return false
	}
	if err != nil {
		common.Warn("Redis GetObj error", zap.String("key", key), zap.Error(err))
		return false
	}
	if err := json.Unmarshal([]byte(data), dest); err != nil {
		common.Warn("Redis GetObj unmarshal error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// Set 写入字符串键值并设置 TTL
func (r *RedisClient) Set(key string, value string, exp time.Duration) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	if err := r.client.Set(ctx, key, value, exp).Err(); err != nil {
		common.Warn("Redis Set error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// SetNX 仅在键不存在时写入（分布式锁基础）
func (r *RedisClient) SetNX(key string, value string, exp time.Duration) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	ok, err := r.client.SetNX(ctx, key, value, exp).Result()
	if err != nil {
		common.Warn("Redis SetNX error", zap.String("key", key), zap.Error(err))
		return false
	}
	return ok
}

// GetOrCreateKey 原子获取已有键或通过 SETNX 创建新键，跨进程/协程安全
func (r *RedisClient) GetOrCreateKey(key string, value string) (string, error) {
	if r.client == nil {
		return "", nil
	}
	ctx := context.Background()
	// 先尝试读取已有值
	existingKey, err := r.client.Get(ctx, key).Result()
	if err == nil {
		common.Warn("Redis Get error", zap.String("key", key), zap.Error(err))
		// 命中已有键直接返回
		return existingKey, nil
	}

	// SETNX 仅在键不存在时写入
	// SETNX returns true if the key was set, false if it already existed
	success, err := r.client.SetNX(ctx, key, value, 0).Result()
	if err != nil {
		return "", fmt.Errorf("failed to set key in Redis: %v", err)
	}

	if success {
		// 本协程成功创建键
		return value, nil
	}

	// 并发写入失败，读取对方设置的值
	// Retrieve and return that key
	finalKey, err := r.client.Get(ctx, key).Result()
	if err != nil {
		return "", fmt.Errorf("failed to get key set by another process: %v", err)
	}

	return finalKey, nil
}

// SAdd 向集合添加成员
func (r *RedisClient) SAdd(key string, member string) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	if err := r.client.SAdd(ctx, key, member).Err(); err != nil {
		common.Warn("Redis SAdd error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// SRem 从集合移除成员
func (r *RedisClient) SRem(key string, member string) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	if err := r.client.SRem(ctx, key, member).Err(); err != nil {
		common.Warn("Redis SRem error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// SMembers 返回集合全部成员
func (r *RedisClient) SMembers(key string) ([]string, error) {
	if r.client == nil {
		return nil, nil
	}
	ctx := context.Background()
	members, err := r.client.SMembers(ctx, key).Result()
	if err != nil {
		common.Warn("Redis SMembers error", zap.String("key", key), zap.Error(err))
		return nil, err
	}
	return members, nil
}

// SIsMember 判断成员是否在集合中
func (r *RedisClient) SIsMember(key string, member string) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	ok, err := r.client.SIsMember(ctx, key, member).Result()
	if err != nil {
		common.Warn("Redis SIsMember error", zap.String("key", key), zap.Error(err))
		return false
	}
	return ok
}

// ZAdd 向有序集合添加带分数成员
func (r *RedisClient) ZAdd(key string, member string, score float64) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	if err := r.client.ZAdd(ctx, key, redis.Z{Score: score, Member: member}).Err(); err != nil {
		common.Warn("Redis ZAdd error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// ZCount 统计分数区间内的成员数量
func (r *RedisClient) ZCount(key string, min, max float64) int64 {
	if r.client == nil {
		return 0
	}
	ctx := context.Background()
	count, err := r.client.ZCount(ctx, key, fmt.Sprintf("%f", min), fmt.Sprintf("%f", max)).Result()
	if err != nil {
		common.Warn("Redis ZCount error", zap.String("key", key), zap.Error(err))
		return 0
	}
	return count
}

// ZPopMin 弹出分数最小的若干成员
func (r *RedisClient) ZPopMin(key string, count int) ([]redis.Z, error) {
	if r.client == nil {
		return nil, nil
	}
	ctx := context.Background()
	members, err := r.client.ZPopMin(ctx, key, int64(count)).Result()
	if err != nil {
		common.Warn("Redis ZPopMin error", zap.String("key", key), zap.Error(err))
		return nil, err
	}
	return members, nil
}

// ZRangeByScore 按分数区间返回成员列表
func (r *RedisClient) ZRangeByScore(key string, min, max float64) ([]string, error) {
	if r.client == nil {
		return nil, nil
	}
	ctx := context.Background()
	members, err := r.client.ZRangeByScore(ctx, key, &redis.ZRangeBy{
		Min: fmt.Sprintf("%f", min),
		Max: fmt.Sprintf("%f", max),
	}).Result()
	if err != nil {
		common.Warn("Redis ZRangeByScore error", zap.String("key", key), zap.Error(err))
		return nil, err
	}
	return members, nil
}

// ZRemRangeByScore 删除分数区间内的成员
func (r *RedisClient) ZRemRangeByScore(key string, min, max float64) int64 {
	if r.client == nil {
		return 0
	}
	ctx := context.Background()
	count, err := r.client.ZRemRangeByScore(ctx, key, fmt.Sprintf("%f", min), fmt.Sprintf("%f", max)).Result()
	if err != nil {
		common.Warn("Redis ZRemRangeByScore error", zap.String("key", key), zap.Error(err))
		return 0
	}
	return count
}

// IncrBy 将键值按增量递增
func (r *RedisClient) IncrBy(key string, increment int64) (int64, error) {
	if r.client == nil {
		return 0, nil
	}
	ctx := context.Background()
	val, err := r.client.IncrBy(ctx, key, increment).Result()
	if err != nil {
		common.Warn("Redis IncrBy error", zap.String("key", key), zap.Error(err))
		return 0, err
	}
	return val, nil
}

// DecrBy 将键值按减量递减
func (r *RedisClient) DecrBy(key string, decrement int64) (int64, error) {
	if r.client == nil {
		return 0, nil
	}
	ctx := context.Background()
	val, err := r.client.DecrBy(ctx, key, decrement).Result()
	if err != nil {
		common.Warn("Redis DecrBy error", zap.String("key", key), zap.Error(err))
		return 0, err
	}
	return val, nil
}

// GenerateAutoIncrementID 基于 Redis INCR 生成命名空间自增 ID
func (r *RedisClient) GenerateAutoIncrementID(keyPrefix string, namespace string, increment int64, ensureMinimum *int64) int64 {
	if r.client == nil {
		return -1
	}
	if keyPrefix == "" {
		keyPrefix = "id_generator"
	}
	if namespace == "" {
		namespace = "default"
	}
	if increment == 0 {
		increment = 1
	}

	redisKey := fmt.Sprintf("%s:%s", keyPrefix, namespace)
	ctx := context.Background()

	// 检查计数器键是否存在
	exists, err := r.client.Exists(ctx, redisKey).Result()
	if err != nil {
		common.Warn("Redis GenerateAutoIncrementID error", zap.Error(err))
		return -1
	}

	if exists == 0 && ensureMinimum != nil {
		startID := int64(math.Max(1, float64(*ensureMinimum)))
		r.client.Set(ctx, redisKey, startID, 0)
		return startID
	}

	// 读取当前值并与 ensureMinimum 对齐
	if ensureMinimum != nil {
		current, err := r.client.Get(ctx, redisKey).Int64()
		if err == nil && current < *ensureMinimum {
			r.client.Set(ctx, redisKey, *ensureMinimum, 0)
			return *ensureMinimum
		}
	}

	// INCRBY 递增并返回新 ID
	nextID, err := r.client.IncrBy(ctx, redisKey, increment).Result()
	if err != nil {
		common.Warn("Redis GenerateAutoIncrementID increment error", zap.Error(err))
		return -1
	}

	return nextID
}

// Transaction 通过 Pipeline SetNX 模拟事务写入
func (r *RedisClient) Transaction(key string, value string, exp time.Duration) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	pipe := r.client.Pipeline()
	pipe.SetNX(ctx, key, value, exp)
	_, err := pipe.Exec(ctx)
	if err != nil {
		common.Warn("Redis Transaction error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// QueueProduct 向 Stream 生产 JSON 消息（最多重试 3 次）
func (r *RedisClient) QueueProduct(queue string, message interface{}) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()

	for i := 0; i < 3; i++ {
		data, err := json.Marshal(message)
		if err != nil {
			common.Warn("Redis QueueProduct marshal error", zap.Error(err))
			return false
		}

		_, err = r.client.XAdd(ctx, &redis.XAddArgs{
			Stream: queue,
			Values: map[string]interface{}{"message": string(data)},
		}).Result()
		if err == nil {
			return true
		}
		common.Warn("Redis QueueProduct error", zap.String("queue", queue), zap.Error(err))
		time.Sleep(100 * time.Millisecond)
	}
	return false
}

// QueueConsumer 从 Stream 消费组读取单条消息
func (r *RedisClient) QueueConsumer(queueName, groupName, consumerName string, msgID string) (*RedisMsg, error) {
	if r.client == nil {
		return nil, nil
	}
	ctx := context.Background()

	for i := 0; i < 3; i++ {
		// 按需创建消费者组
		groups, err := r.client.XInfoGroups(ctx, queueName).Result()
		if err != nil && err.Error() != "no such key" {
			common.Warn("Redis QueueConsumer XInfoGroups error", zap.Error(err))
		}

		groupExists := false
		for _, g := range groups {
			if g.Name == groupName {
				groupExists = true
				break
			}
		}

		if !groupExists {
			err = r.client.XGroupCreateMkStream(ctx, queueName, groupName, "0").Err()
			if err != nil && err.Error() != "BUSYGROUP Consumer Group name already exists" {
				common.Warn("Redis QueueConsumer XGroupCreate error", zap.Error(err))
			}
		}

		if msgID == "" {
			msgID = ">"
		}

		messages, err := r.client.XReadGroup(ctx, &redis.XReadGroupArgs{
			Group:    groupName,
			Consumer: consumerName,
			Streams:  []string{queueName, msgID},
			Count:    1,
			Block:    5 * time.Second,
		}).Result()

		if err == redis.Nil {
			return nil, nil
		}
		if err != nil {
			common.Warn("Redis QueueConsumer XReadGroup error", zap.Error(err))
			time.Sleep(100 * time.Millisecond)
			continue
		}

		if len(messages) == 0 || len(messages[0].Messages) == 0 {
			return nil, nil
		}

		msg := messages[0].Messages[0]
		var messageData map[string]interface{}
		if msgStr, ok := msg.Values["message"].(string); ok {
			json.Unmarshal([]byte(msgStr), &messageData)
		}

		return &RedisMsg{
			consumer:  r.client,
			queueName: queueName,
			groupName: groupName,
			msgID:     msg.ID,
			message:   messageData,
		}, nil
	}
	return nil, nil
}

// Ack 确认 Stream 消息已处理
func (m *RedisMsg) Ack() bool {
	if m.consumer == nil {
		return false
	}
	ctx := context.Background()
	err := m.consumer.XAck(ctx, m.queueName, m.groupName, m.msgID).Err()
	if err != nil {
		common.Warn("RedisMsg Ack error", zap.Error(err))
		return false
	}
	return true
}

// GetMessage 返回反序列化后的消息体 map
func (m *RedisMsg) GetMessage() map[string]interface{} {
	return m.message
}

// GetMsgID 返回 Stream 消息 ID
func (m *RedisMsg) GetMsgID() string {
	return m.msgID
}

// GetPendingMsg 查询消费者组 pending 消息列表
func (r *RedisClient) GetPendingMsg(queue, groupName string) ([]redis.XPendingExt, error) {
	if r.client == nil {
		return nil, nil
	}
	ctx := context.Background()
	msgs, err := r.client.XPendingExt(ctx, &redis.XPendingExtArgs{
		Stream: queue,
		Group:  groupName,
		Start:  "-",
		End:    "+",
		Count:  10,
	}).Result()
	if err != nil {
		if err.Error() != "No such key" {
			common.Warn("Redis GetPendingMsg error", zap.Error(err))
		}
		return nil, err
	}
	return msgs, nil
}

// RequeueMsg 将指定消息重新 XAdd 并 Ack 原消息
func (r *RedisClient) RequeueMsg(queue, groupName, msgID string) {
	if r.client == nil {
		return
	}
	ctx := context.Background()

	for i := 0; i < 3; i++ {
		msgs, err := r.client.XRange(ctx, queue, msgID, msgID).Result()
		if err != nil {
			common.Warn("Redis RequeueMsg XRange error", zap.Error(err))
			time.Sleep(100 * time.Millisecond)
			continue
		}
		if len(msgs) == 0 {
			return
		}

		r.client.XAdd(ctx, &redis.XAddArgs{
			Stream: queue,
			Values: msgs[0].Values,
		})
		r.client.XAck(ctx, queue, groupName, msgID)
		return
	}
}

// QueueInfo 返回指定消费者组的 pending/consumers 等统计
func (r *RedisClient) QueueInfo(queue, groupName string) (map[string]interface{}, error) {
	if r.client == nil {
		return nil, nil
	}
	ctx := context.Background()

	for i := 0; i < 3; i++ {
		groups, err := r.client.XInfoGroups(ctx, queue).Result()
		if err != nil {
			common.Warn("Redis QueueInfo error", zap.Error(err))
			time.Sleep(100 * time.Millisecond)
			continue
		}

		for _, g := range groups {
			if g.Name == groupName {
				return map[string]interface{}{
					"name":           g.Name,
					"consumers":      g.Consumers,
					"pending":        g.Pending,
					"last_delivered": g.LastDeliveredID,
				}, nil
			}
		}
		return nil, nil
	}
	return nil, nil
}

// DeleteIfEqual 通过 Lua 原子比较删除（分布式锁释放）
func (r *RedisClient) DeleteIfEqual(key, expectedValue string) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	result, err := r.luaDeleteIfEqual.Run(ctx, r.client, []string{key}, expectedValue).Result()
	if err != nil {
		common.Warn("Redis DeleteIfEqual error", zap.Error(err))
		return false
	}
	return result.(int64) == 1
}

// Delete 删除指定键
func (r *RedisClient) Delete(key string) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	if err := r.client.Del(ctx, key).Err(); err != nil {
		common.Warn("Redis Delete error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// Expire 为键设置过期时间
func (r *RedisClient) Expire(key string, exp time.Duration) bool {
	if r.client == nil {
		return false
	}
	ctx := context.Background()
	if err := r.client.Expire(ctx, key, exp).Err(); err != nil {
		common.Warn("Redis Expire error", zap.String("key", key), zap.Error(err))
		return false
	}
	return true
}

// TTL 查询键剩余生存时间
func (r *RedisClient) TTL(key string) time.Duration {
	if r.client == nil {
		return -2
	}
	ctx := context.Background()
	ttl, err := r.client.TTL(ctx, key).Result()
	if err != nil {
		common.Warn("Redis TTL error", zap.String("key", key), zap.Error(err))
		return -2
	}
	return ttl
}

// DistributedLock 基于 SetNX + DeleteIfEqual 的分布式锁
type DistributedLock struct {
	client          *RedisClient
	lockKey         string
	lockValue       string
	timeout         time.Duration
	blockingTimeout time.Duration
}

// NewDistributedLock 创建分布式锁实例（默认随机 lockValue）
func NewDistributedLock(lockKey string, lockValue string, timeout time.Duration, blockingTimeout time.Duration) *DistributedLock {
	if globalClient == nil {
		return nil
	}
	if lockValue == "" {
		lockValue = uuid.New().String()
	}
	return &DistributedLock{
		client:          globalClient,
		lockKey:         lockKey,
		lockValue:       lockValue,
		timeout:         timeout,
		blockingTimeout: blockingTimeout,
	}
}

// Acquire 尝试获取锁（先清理过期值再 SetNX）
func (l *DistributedLock) Acquire() bool {
	if l.client == nil {
		return false
	}
	// 获取前清理可能过期的锁值
	l.client.DeleteIfEqual(l.lockKey, l.lockValue)
	return l.client.SetNX(l.lockKey, l.lockValue, l.timeout)
}

// SpinAcquire 在 ctx 取消前循环尝试获取锁
func (l *DistributedLock) SpinAcquire(ctx context.Context) error {
	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		default:
			l.client.DeleteIfEqual(l.lockKey, l.lockValue)
			if l.client.SetNX(l.lockKey, l.lockValue, l.timeout) {
				return nil
			}
			time.Sleep(10 * time.Second)
		}
	}
}

// Release 仅当 lockValue 匹配时删除键释放锁
func (l *DistributedLock) Release() bool {
	if l.client == nil {
		return false
	}
	return l.client.DeleteIfEqual(l.lockKey, l.lockValue)
}

// TokenBucket 基于 Redis Lua 的令牌桶限流器
type TokenBucket struct {
	client   *RedisClient
	key      string
	capacity float64
	rate     float64
}

// NewTokenBucket 创建令牌桶（capacity/rate 由调用方指定）
func NewTokenBucket(key string, capacity, rate float64) *TokenBucket {
	if globalClient == nil {
		return nil
	}
	return &TokenBucket{
		client:   globalClient,
		key:      key,
		capacity: capacity,
		rate:     rate,
	}
}

// Allow 消耗 cost 个令牌；Redis 不可用时 fail-open 放行
func (tb *TokenBucket) Allow(cost float64) (bool, float64) {
	if tb.client == nil || tb.client.client == nil {
		return true, 0
	}
	ctx := context.Background()
	now := float64(time.Now().Unix())

	result, err := tb.client.luaTokenBucket.Run(ctx, tb.client.client, []string{tb.key},
		tb.capacity, tb.rate, now, cost).Result()
	if err != nil {
		common.Warn("TokenBucket Allow error", zap.Error(err))
		return true, 0
	}

	values := result.([]interface{})
	allowed := values[0].(int64) == 1
	tokens := values[1].(int64)
	return allowed, float64(tokens)
}

// GetClient 暴露底层 *redis.Client 供高级场景使用
func (r *RedisClient) GetClient() *redis.Client {
	return r.client
}

// EvalTokenBucketStrict 为 fail-closed 令牌桶：Redis/Lua 失败时返回错误，供 webhook 等安全门禁拒绝流量；与 chat 路径 fail-open 的 Allow 区分。
func (r *RedisClient) EvalTokenBucketStrict(
	ctx context.Context, key string, capacity, rate float64,
) (allowed bool, err error) {
	if r == nil || r.client == nil {
		return false, fmt.Errorf("redis: not initialised")
	}
	now := float64(time.Now().Unix())
	res, err := r.luaTokenBucket.Run(ctx, r.client, []string{key},
		capacity, rate, now, 1.0).Result()
	if err != nil {
		return false, fmt.Errorf("token bucket: %w", err)
	}
	values, ok := res.([]interface{})
	if !ok || len(values) < 1 {
		return false, fmt.Errorf("token bucket: malformed reply")
	}
	allowedI, ok := values[0].(int64)
	if !ok {
		return false, fmt.Errorf("token bucket: malformed reply")
	}
	return allowedI == 1, nil
}

// RandomSleep 在 [minMs,maxMs) 毫秒间随机休眠（退避抖动）
func RandomSleep(minMs, maxMs int) {
	duration := time.Duration(rand.Intn(maxMs-minMs)+minMs) * time.Millisecond
	time.Sleep(duration)
}

// 模块小结：Redis 层为 RAGFlow 提供跨实例共享状态——任务队列 Stream、限流令牌桶、分布式锁与 JSON 缓存。Init 未配置 Host 时全局 client 为 nil，上层须通过 IsEnabled 判断。QueueConsumer 自动建组并以 5s Block 阻塞读；Health 额外做 Set/Get 探活。EvalTokenBucketStrict 与 TokenBucket.Allow 分别用于 fail-closed 与 fail-open 场景。
