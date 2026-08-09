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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.time.Duration;
import java.time.Instant;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.api.keys.MigrateArgs;

import java.util.concurrent.TimeUnit;

/**
 * Redis 键空间管理 RxJava API。
 * <p>各方法返回 {@link io.reactivex.rxjava3.core.Single} 或 {@link io.reactivex.rxjava3.core.Completable}。
 *
 * @author Nikita Koksharov
 */
public interface RKeysRx {

    /**
     * 将对象移动到指定 Redis 数据库。
     *
     * @param name 对象名称
     * @param database 目标数据库编号
     * @return 见方法说明
     */
    Single<Boolean> move(String name, int database);
    
    /**
     * 将对象从源 Redis 实例迁移到目标实例。
     * @deprecated 已废弃， use {@link #migrate(MigrateArgs)}  instead
     *
     * @param name 对象名称
     * @param host 目标主机
     * @param port 目标端口
     * @param database 目标数据库编号
     * @param timeout 通信最大空闲时间（毫秒）
     * @return 无返回值
     */
    @Deprecated
    Completable migrate(String name, String host, int port, int database, long timeout);
    /**
     * 将对象从源 Redis 实例迁移到目标实例。
     *
     * @param migrateArgs 迁移参数
     */
    Completable migrate(MigrateArgs migrateArgs);
    /**
     * 将对象从源 Redis 实例复制到目标实例。
     * @deprecated 已废弃， use {@link #migrate(MigrateArgs)}  instead
     *
     * @param name 对象名称
     * @param host 目标主机
     * @param port 目标端口
     * @param database 目标数据库编号
     * @param timeout 通信最大空闲时间（毫秒）
     * @return 无返回值
     */
    @Deprecated
    Completable copy(String name, String host, int port, int database, long timeout);
    
    /**
     * 已废弃，请改用 {@link #expire(Duration, String...)}。
     *
     * @param name 对象名称
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 见方法说明
     */
    @Deprecated
    Single<Boolean> expire(String name, long timeToLive, TimeUnit timeUnit);

    /**
     * 为多个对象设置相对过期时长；到期后 Redis 键将自动删除。
     * the keys will automatically be deleted.
     *
     * @param duration 过期时长
     * @param names 对象名称
     * @return 成功设置过期的键数量
     */
    Single<Long> expire(Duration duration, String... names);

    /**
     * 已废弃，请改用 {@link #expireAt(Instant, String...)}。
     * 
     * @param name 对象名称
     * @param timestamp 过期时间戳
     * @return 见方法说明
     */
    @Deprecated
    Single<Boolean> expireAt(String name, long timestamp);

    /**
     * 为多个对象设置相对过期时长；到期后 Redis 键将自动删除。
     * the keys will automatically be deleted.
     *
     * @param instant 过期时刻
     * @param names 对象名称
     * @return 成功设置过期的键数量
     */
    Single<Long> expireAt(Instant instant, String... names);

    /**
     * 清除对象的过期时间或绝对过期时刻。
     * 
     * @param name 对象名称
     * @return 见方法说明
     *         <code>false</code> if object does not exist or does not have an associated timeout
     */
    Single<Boolean> clearExpire(String name);
    
    /**
     * 仅当新键不存在时将 {@code oldName} 重命名为 {@code newName}。
     * 仅当新键不存在时
     *
     * @param oldName 原对象名称
     * @param newName 新对象名称
     * @return 见方法说明
     */
    Single<Boolean> renamenx(String oldName, String newName);
    
    /**
     * 将当前对象键重命名为 {@code newName}。
     *
     * @param currentName 当前对象名称
     * @param newName 新对象名称
     * @return 无返回值
     */
    Completable rename(String currentName, String newName);
    
    /**
     * 返回带过期时间的 Redisson 对象剩余存活时间。
     *
     * @param name 对象名称
     * @return 剩余毫秒数
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    Single<Long> remainTimeToLive(String name);

    /**
     * 更新对象的最后访问时间。
     * 
     * @param names 对象名称
     * @return 已 touch 的对象数量
     */
    Single<Long> touch(String... names);
    
    /**
     * 检查给定键是否存在。
     * 
     * @param names 对象名称
     * @return 存在的键数量
     */
    Single<Long> countExists(String... names);
    
    /**
     * 按键获取 Redis 对象类型。
     * 
     * @param key 键名
     * @return 键类型
     */
    Single<RType> getType(String key);
    
    /**
     * 以增量迭代方式加载键（SCAN 遍历）。
     * Each SCAN operation loads up to 10 keys per request.
     *
     * @return keys
     */
    Flowable<String> getKeys();

    /**
     * 通过 SCAN 迭代获取所有键（异步 Iterable）。
     *
     * @param options SCAN 选项
     * @return Iterable
     */
    Flowable<String> getKeys(KeysScanOptions options);
    
    /**
     * 已废弃，请改用 {@link #getKeys(KeysScanOptions)}。
     *
     * @param count 返回数量
     * @return keys
     */
    @Deprecated
    Flowable<String> getKeys(int count);

    /**
     * 已废弃，请改用 {@link #getKeys(KeysScanOptions)}。
     *
     * @param pattern 匹配模式
     * @return keys
     */
    @Deprecated
    Flowable<String> getKeysByPattern(String pattern);

    /**
     * 已废弃，请改用 {@link #getKeys(KeysScanOptions)}。
     *
     * @param pattern 匹配模式
     * @param count 返回数量
     * @return keys
     */
    @Deprecated
    Flowable<String> getKeysByPattern(String pattern, int count);
    
    /**
     * 获取键在 Cluster 中的 hash slot（仅集群模式）。
     * 仅适用于 Cluster 节点.
     *
     * Uses <code>KEYSLOT</code> Redis command.
     *
     * @param key 键名
     * @return slot number
     */
    Single<Integer> getSlot(String key);

    /**
     * 随机返回一个键。
     *
     * Uses <code>RANDOM_KEY</code> Redis command.
     *
     * @return 随机键名
     */
    Maybe<String> randomKey();

    /**
     * 按 glob 模式批量删除对象。
     *
     * Uses Lua script.
     *
     *  支持的 glob 模式示例：
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern 匹配模式
     * @return deleted objects amount
     */
    Single<Long> deleteByPattern(String pattern);

    /**
     * 按 glob 模式批量异步解除链接（UNLINK）。
     *
     * Uses Lua script.
     *
     *  支持的 glob 模式示例：
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern 匹配模式
     * @return deleted objects amount
     */
    Single<Long> unlinkByPattern(String pattern);

    /**
     * 按名称批量删除对象。
     *
     * Uses <code>DEL</code> Redis command.
     *
     * @param keys Redis 键名
     * @return deleted objects amount
     */
    Single<Long> delete(String... keys);

    /**
     * 按名称批量删除对象。
     * 实际删除将异步进行。
     * <p>
     * 需要 Redis 4.0+
     * 
     * @param keys Redis 键名
     * @return 已删除键数量
     */
    Single<Long> unlink(String... keys);
    
    /**
     * 返回当前选中数据库的键数量。
     *
     * @return count of keys
     */
    Single<Long> count();
    
    /**
     * 清空当前选中数据库的所有键。
     *
     * Uses <code>FLUSHDB</code> Redis command.
     * 
     * @return 无返回值
     */
    Completable flushdb();

    /**
     * 交换两个 Redis 数据库的内容。
     * <p>
     * 需要 Redis 4.0+
     *
     * @return 无返回值
     */
    Completable swapdb(int db1, int db2);

    /**
     * 清空所有数据库的所有键。
     *
     * Uses <code>FLUSHALL</code> Redis command.
     *
     * @return 无返回值
     */
    Completable flushall();

    /**
     * 清空当前选中数据库的所有键。
     * 在后台执行，不阻塞服务器。
     * <p>
     * 需要 Redis 4.0+
     *
     * @return 无返回值
     */
    Completable flushdbParallel();

    /**
     * 清空所有数据库的所有键。
     * 在后台执行，不阻塞服务器。
     * <p>
     * 需要 Redis 4.0+
     *
     * @return 无返回值
     */
    Completable flushallParallel();

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
    Single<Integer> addListener(ObjectListener listener);

    /**
     * 移除全局对象事件监听器。
     *
     * @param listenerId 监听器 ID
     */
    Completable removeListener(int listenerId);

}
