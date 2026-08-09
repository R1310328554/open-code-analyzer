/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import java.time.Duration;
import java.time.Instant;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.api.keys.MigrateArgs;

import java.util.concurrent.TimeUnit;

/**
 * Redis 键空间管理异步 API。
 * <p>各方法返回 {@link RFuture}。
 *
 * @author Nikita Koksharov
 */
public interface RKeysAsync {

    /**
     * 将对象移动到指定 Redis 数据库。
     *
     * @param name 对象名称
     * @param database 目标数据库编号
     * @return 见方法说明
     */
    RFuture<Boolean> moveAsync(String name, int database);
    
    /**
     * 将对象从源 Redis 实例迁移到目标实例。
     * @deprecated 已废弃， use {@link #migrateAsync(MigrateArgs)}  instead
     *
     * @param name 对象名称
     * @param host 目标主机
     * @param port 目标端口
     * @param database 目标数据库编号
     * @param timeout 通信最大空闲时间（毫秒）
     * @return 无返回值
     */
    RFuture<Void> migrateAsync(String name, String host, int port, int database, long timeout);

    /**
     * 将对象从源 Redis 实例迁移到目标实例。
     *
     * @param migrateArgs 迁移参数
     */
    RFuture<Void> migrateAsync(MigrateArgs migrateArgs);

    /**
     * 将对象从源 Redis 实例复制到目标实例。
     * in async mode
     *
     * @deprecated 已废弃， use {@link #migrateAsync(MigrateArgs)}  instead
     *
     * @param name 对象名称
     * @param host 目标主机
     * @param port 目标端口
     * @param database 目标数据库编号
     * @param timeout 通信最大空闲时间（毫秒）
     * @return 无返回值
     */
    @Deprecated
    RFuture<Void> copyAsync(String name, String host, int port, int database, long timeout);
    
    /**
     * 已废弃，请改用 {@link #expireAsync(Duration, String...)}。
     *
     * @param name 对象名称
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 见方法说明
     */
    @Deprecated
    RFuture<Boolean> expireAsync(String name, long timeToLive, TimeUnit timeUnit);

    /**
     * 为多个对象设置相对过期时长；到期后 Redis 键将自动删除。
     * the keys will automatically be deleted.
     *
     * @param duration 过期时长
     * @param names 对象名称
     * @return 成功设置过期的键数量
     */
    RFuture<Long> expireAsync(Duration duration, String... names);

    /**
     * 已废弃，请改用 {@link #expireAtAsync(Instant, String...)}。
     * 
     * @param name 对象名称
     * @param timestamp 过期时间戳
     * @return 见方法说明
     */
    @Deprecated
    RFuture<Boolean> expireAtAsync(String name, long timestamp);

    /**
     * 为多个对象设置相对过期时长；到期后 Redis 键将自动删除。
     * the keys will automatically be deleted.
     *
     * @param instant 过期时刻
     * @param names 对象名称
     * @return 成功设置过期的键数量
     */
    RFuture<Long> expireAtAsync(Instant instant, String... names);
    /**
     * 清除对象的过期时间或绝对过期时刻。
     *
     * @param name 对象名称
     * @return 见方法说明
     *         <code>false</code> if object does not exist or does not have an associated timeout
     */
    RFuture<Boolean> clearExpireAsync(String name);
    
    /**
     * 仅当新键不存在时将 {@code oldName} 重命名为 {@code newName}。
     * 仅当新键不存在时
     *
     * @param oldName 原对象名称
     * @param newName 新对象名称
     * @return 见方法说明
     */
    RFuture<Boolean> renamenxAsync(String oldName, String newName);
    
    /**
     * 将当前对象键重命名为 {@code newName}。
     *
     * @param currentName 当前对象名称
     * @param newName 新对象名称
     * @return 无返回值
     */
    RFuture<Void> renameAsync(String currentName, String newName);
    
    /**
     * 返回带过期时间的 Redisson 对象剩余存活时间。
     *
     * @param name 对象名称
     * @return 剩余毫秒数
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    RFuture<Long> remainTimeToLiveAsync(String name);
    
    /**
     * 更新对象的最后访问时间。
     * 
     * @param names 对象名称
     * @return 已 touch 的对象数量
     */
    RFuture<Long> touchAsync(String... names);
    
    /**
     * 检查给定键是否存在。
     * 
     * @param names 对象名称
     * @return 存在的键数量
     */
    RFuture<Long> countExistsAsync(String... names);

    /**
     * 通过 SCAN 迭代获取所有键（异步 Iterable）。
     * 每次 SCAN 请求最多加载 10 个键。
     *
     * @return 异步 Iterable
     */
    AsyncIterator<String> getKeysAsync();

    /**
     * 通过 SCAN 迭代获取所有键（异步 Iterable）。
     *
     * @param options SCAN 选项
     * @return 异步 Iterable
     */
    AsyncIterator<String> getKeysAsync(KeysScanOptions options);

    /**
     * 按键获取 Redis 对象类型。
     * 
     * @param key 键名
     * @return 键类型
     */
    RFuture<RType> getTypeAsync(String key);
    
    /**
     * 异步获取键在 Cluster 中的 hash slot（仅集群模式）。
     * 仅适用于 Cluster 节点
     *
     * @param key 键名
     * @return hash slot 编号
     */
    RFuture<Integer> getSlotAsync(String key);

    /**
     * 异步随机返回一个键。
     *
     * @return 随机键名
     */
    RFuture<String> randomKeyAsync();

    /**
     * 按 glob 模式批量删除对象。
     * <p>
     * Cluster 模式下因 Lua 限制以<b>非原子</b>方式执行。
     * <p>
     *  支持的 glob 模式示例：
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern 匹配模式
     * @return 已删除键数量
     */
    RFuture<Long> deleteByPatternAsync(String pattern);

    /**
     * 按 glob 模式批量异步解除链接（UNLINK）。
     * <p>
     * Cluster 模式下因 Lua 限制以<b>非原子</b>方式执行。
     * <p>
     *  支持的 glob 模式示例：
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern 匹配模式
     * @return 已删除键数量
     */
    RFuture<Long> unlinkByPatternAsync(String pattern);

    /**
     * 批量删除 Redisson 对象。
     *
     * @param objects Redisson 对象
     * @return 已删除键数量
     */
    RFuture<Long> deleteAsync(RObject... objects);
    
    /**
     * 按名称批量删除对象。
     *
     * @param keys Redis 键名
     * @return 已删除键数量
     */
    RFuture<Long> deleteAsync(String... keys);

    /**
     * 按名称批量删除对象。
     * 实际删除将异步进行。
     * <p>
     * 需要 Redis 4.0+
     * 
     * @param keys Redis 键名
     * @return 已删除键数量
     */
    RFuture<Long> unlinkAsync(String... keys);
    
    /**
     * 异步返回当前选中数据库的键数量。
     *
     * @return 键数量
     */
    RFuture<Long> countAsync();

    /**
     * 交换两个 Redis 数据库的内容。
     * <p>
     * 需要 Redis 4.0+
     *
     * @return 无返回值
     */
    RFuture<Void> swapdbAsync(int db1, int db2);

    /**
     * 清空当前选中数据库的所有键。
     *
     * @return 无返回值
     */
    RFuture<Void> flushdbAsync();

    /**
     * 清空所有数据库的所有键。
     *
     * @return 无返回值
     */
    RFuture<Void> flushallAsync();

    /**
     * 清空当前选中数据库的所有键。
     * 在后台执行，不阻塞服务器。
     * <p>
     * 需要 Redis 4.0+
     * 
     * @return 无返回值
     */
    RFuture<Void> flushdbParallelAsync();

    /**
     * 清空所有数据库的所有键。
     * 在后台执行，不阻塞服务器。
     * <p>
     * 需要 Redis 4.0+
     * 
     * @return 无返回值
     */
    RFuture<Void> flushallParallelAsync();

    /**
     * 注册全局 Redisson 对象事件监听器。
     * which is invoked for each Redisson object.
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.SetObjectListener
     * @see org.redisson.api.listener.NewObjectListener
     * @see org.redisson.api.listener.FlushListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    RFuture<Integer> addListenerAsync(ObjectListener listener);

    /**
     * 移除全局对象事件监听器。
     *
     * @param listenerId 监听器 ID
     */
    RFuture<Void> removeListenerAsync(int listenerId);

}
