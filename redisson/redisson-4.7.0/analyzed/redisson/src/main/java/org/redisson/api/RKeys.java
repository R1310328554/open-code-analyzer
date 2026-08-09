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
import java.util.stream.Stream;

/**
 * Redis 键空间管理 API {@link RKeys}。
 * <p>封装 SCAN、过期/TTL、MIGRATE/COPY、跨库 MOVE 及键计数等操作。
 *
 * @author Nikita Koksharov
 */
public interface RKeys extends RKeysAsync {

    /**
     * 请改用 {@link #getKeys(KeysScanOptions)}。
     *
     * @param limit 键数量上限
     * @return Iterable object
     */
    @Deprecated
    Iterable<String> getKeysWithLimit(int limit);

    /**
     * 请改用 {@link #getKeys(KeysScanOptions)}。
     *
     * @param limit 键数量上限
     * @param pattern 匹配模式
     * @return Iterable object
     */
    @Deprecated
    Iterable<String> getKeysWithLimit(String pattern, int limit);

    /**
     * 将对象移动到另一 Redis 数据库。
     *
     * @param name 对象名称
     * @param database Redis 数据库编号
     * @return <code>true</code> if key was moved else <code>false</code>
     */
    boolean move(String name, int database);
    
    /**
     * 将对象从源 Redis 实例迁移到目标实例。
     * @deprecated use {@link #migrate(MigrateArgs)}  instead
     *
     * @param name 对象名称
     * @param host 目标主机
     * @param port 目标端口
     * @param database - destination database
     * @param timeout 通信最大空闲毫秒数 in any moment of the communication with the destination instance in milliseconds
     */
    @Deprecated
    void migrate(String name, String host, int port, int database, long timeout);

    /**
     * 将对象从源 Redis 实例迁移到目标实例。
     *
     * @param migrateArgs 迁移参数
     */
    void migrate(MigrateArgs migrateArgs);

    /**
     * 将对象从源 Redis 实例复制到目标实例。
     * @deprecated use {@link #migrate(MigrateArgs)}  instead
     *
     * @param name 对象名称
     * @param host 目标主机
     * @param port 目标端口
     * @param database - destination database
     * @param timeout 通信最大空闲毫秒数 in any moment of the communication with the destination instance in milliseconds
     *
     */
    @Deprecated
    void copy(String name, String host, int port, int database, long timeout);
    
    /**
     * 请改用 {@link #expire(Duration, String...)}。
     *
     * @param name 对象名称
     * @param timeToLive 过期时长
     * @param timeUnit 时间单位
     * @return <code>true</code> if the timeout was set and <code>false</code> if not
     */
    @Deprecated
    boolean expire(String name, long timeToLive, TimeUnit timeUnit);

    /**
     * Set a timeout for multiple objects. After the timeout has expired,
     * the keys will automatically be deleted.
     *
     * @param duration timeout before keys will be deleted
     * @param names object names
     * @return number of keys for which the timeout was set successfully
     */
    long expire(Duration duration, String... names);

    /**
     * 请改用 {@link #expireAt(Instant, String...)}。
     * 
     * @param name 对象名称
     * @param timestamp - expire date in milliseconds (Unix timestamp)
     * @return <code>true</code> if the timeout was set and <code>false</code> if not
     */
    @Deprecated
    boolean expireAt(String name, long timestamp);

    /**
     * Set a timeout for multiple objects. After the timeout has expired,
     * the keys will automatically be deleted.
     *
     * @param instant expiration date/time (Unix timestamp in milliseconds)
     * @param names object names
     * @return number of keys for which the timeout was set successfully
     */
    long expireAt(Instant instant, String... names);

    /**
     * Clear an expire timeout or expire date for object.
     * 
     * @param name 对象名称
     * @return <code>true</code> if timeout was removed
     *         <code>false</code> if object does not exist or does not have an associated timeout
     */
    boolean clearExpire(String name);
    
    /**
     * Rename object with <code>oldName</code> to <code>newName</code>
     * only if new key is not exists
     *
     * @param oldName - old name of object
     * @param newName - new name of object
     * @return <code>true</code> if object has been renamed successfully and <code>false</code> otherwise
     */
    boolean renamenx(String oldName, String newName);
    
    /**
     * Rename current object key to <code>newName</code>
     *
     * @param currentName - current name of object
     * @param newName - new name of object
     */
    void rename(String currentName, String newName);
    
    /**
     * Remaining time to live of Redisson object that has a timeout
     *
     * @param name of key
     * @return time in milliseconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    long remainTimeToLive(String name);

    /**
     * Update the last access time of an object. 
     * 
     * @param names of keys
     * @return count of objects were touched
     */
    long touch(String... names);
    
    /**
     * Returns amount of existing keys
     * 
     * @param names of keys
     * @return amount of existing keys
     */
    long countExists(String... names);
    
    /**
     * Get Redis object type by key
     * 
     * @param key - name of key
     * @return type of key
     */
    RType getType(String key);
    
    /**
     * Get hash slot identifier for key.
     * Available for cluster nodes only
     *
     * @param key - name of key
     * @return slot number
     */
    int getSlot(String key);

    /**
     * 请改用 {@link #getKeys(KeysScanOptions)}。
     * 
     * @param pattern 匹配模式
     * @return Iterable object
     */
    @Deprecated
    Iterable<String> getKeysByPattern(String pattern);

    /**
     * 请改用 {@link #getKeys(KeysScanOptions)}。
     *
     * @param pattern 匹配模式
     * @param count - keys loaded per request to Redis
     * @return Iterable object
     */
    @Deprecated
    Iterable<String> getKeysByPattern(String pattern, int count);

    /**
     * Get all keys using iterable. Keys traversing with SCAN operation.
     * Each SCAN operation loads up to <code>10</code> keys per request. 
     *
     * @return Iterable object
     */
    Iterable<String> getKeys();

    /**
     * Get all keys using iterable. Keys traversing with SCAN operation.
     *
     * @param options scan options
     * @return Iterable object
     */
    Iterable<String> getKeys(KeysScanOptions options);

    /**
     * 请改用 {@link #getKeys(KeysScanOptions)}。
     *
     * @param count - keys loaded per request to Redis
     * @return Iterable object
     */
    @Deprecated
    Iterable<String> getKeys(int count);

    /**
     * Use {@link #getKeys(KeysScanOptions)} instead.
     * 
     * @param pattern - match pattern
     * @return Iterable object
     */
    @Deprecated
    Stream<String> getKeysStreamByPattern(String pattern);

    /**
     * Use {@link #getKeys(KeysScanOptions)} instead.
     *
     * @param pattern - match pattern
     * @param count - keys loaded per request to Redis
     * @return Iterable object
     */
    @Deprecated
    Stream<String> getKeysStreamByPattern(String pattern, int count);
    
    /**
     * Get all keys using Stream. Keys traversing with SCAN operation.
     * Each SCAN operation loads up to <code>10</code> keys per request.
     *
     * @return Iterable object
     */
    Stream<String> getKeysStream();

    /**
     * Get all keys using Stream. Keys traversing with SCAN operation.
     * Each SCAN operation loads up to <code>10</code> keys per request.
     *
     * @return Iterable object
     */
    Stream<String> getKeysStream(KeysScanOptions options);

    /**
     * Use {@link #getKeys(KeysScanOptions)} instead.
     *
     * @param count - keys loaded per request to Redis
     * @return Iterable object
     */
    @Deprecated
    Stream<String> getKeysStream(int count);
    
    /**
     * Get random key
     *
     * @return random key
     */
    String randomKey();

    /**
     * Delete multiple objects by a key pattern.
     * <p>
     * Method executes in <b>NON atomic way</b> in cluster mode due to lua script limitations.
     * <p>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern 匹配模式 
     * @return number of removed keys
     */
    long deleteByPattern(String pattern);

    /**
     * Unlink multiple objects by a key pattern.
     * <p>
     * Method executes in <b>NON atomic way</b> in cluster mode due to lua script limitations.
     * <p>
     *  Supported glob-style patterns:
     *    h?llo subscribes to hello, hallo and hxllo
     *    h*llo subscribes to hllo and heeeello
     *    h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern 匹配模式
     * @return number of removed keys
     */
    long unlinkByPattern(String pattern);

    /**
     * Delete multiple objects
     *
     * @param objects of Redisson
     * @return number of removed keys
     */
    long delete(RObject... objects);
    
    /**
     * Delete multiple objects by name
     *
     * @param keys - object names
     * @return number of removed keys
     */
    long delete(String... keys);

    /**
     * Delete multiple objects by name.
     * Actual removal will happen later asynchronously.
     * <p>
     * Requires Redis 4.0+
     * 
     * @param keys of objects
     * @return number of removed keys
     */
    long unlink(String... keys);
    
    /**
     * Returns the number of keys in the currently-selected database
     *
     * @return count of keys
     */
    long count();

    /**
     * Swap two databases.
     */
    void swapdb(int db1, int db2);

    /**
     * Delete all keys of currently selected database
     */
    void flushdb();

    /**
     * Delete all keys of currently selected database 
     * in background without blocking server.
     * <p>
     * Requires Redis 4.0+
     * 
     */
    void flushdbParallel();

    /**
     * Delete all keys of all existing databases
     */
    void flushall();
    
    /**
     * Delete all keys of all existing databases
     * in background without blocking server.
     * <p>
     * Requires Redis 4.0+
     * 
     */
    void flushallParallel();

    /**
     * Adds global object event listener
     * which is invoked for each Redisson object.
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.SetObjectListener
     * @see org.redisson.api.listener.NewObjectListener
     * @see org.redisson.api.listener.FlushListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    int addListener(ObjectListener listener);

    /**
     * Removes global object event listener
     *
     * @param listenerId - listener id
     */
    void removeListener(int listenerId);

}
