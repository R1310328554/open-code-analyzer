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

import java.util.concurrent.TimeUnit;

import org.redisson.client.codec.Codec;

import reactor.core.publisher.Mono;

/**
 * 所有 Redisson 对象的 Reactor 响应式 API 基类接口。
 * <p>各方法返回 {@link Mono}；{@link #getName()} 与 {@link #getCodec()} 为同步方法。
 *
 * @author Nikita Koksharov
 */
public interface RObjectReactive {

    /**
     * 返回自上次读写操作以来经过的秒数。
     *
     * @return 空闲秒数
     */
    Mono<Long> getIdleTime();

    /**
     * 返回该对象的引用计数。
     *
     * @return 引用计数
     */
    Mono<Integer> getReferenceCount();

    /**
     * 返回该对象的对数访问频率计数器。
     *
     * @return 访问频率计数
     */
    Mono<Integer> getAccessFrequency();

    /**
     * 返回 Redis 对象在服务端使用的内部编码类型
     *
     * @return 内部编码类型
     */
    Mono<ObjectEncoding> getInternalEncoding();

    String getName();
    
    Codec getCodec();
    
    /**
     * 返回该对象在 Redis 内存中占用的字节数。 
     * 
     * @return 内存占用字节数
     */
    Mono<Long> sizeInMemory();
    
    /**
     * 使用 {@link #dump()} 返回的状态快照恢复对象。
     * 
     * @param state 对象状态快照
     * @return 无返回值
     */
    Mono<Void> restore(byte[] state);
    
    /**
     * 使用 {@link #dump()} 返回的状态恢复对象，并设置存活时间（TTL）。
     * 
     * @param state 对象状态快照
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 无返回值
     */
    Mono<Void> restore(byte[] state, long timeToLive, TimeUnit timeUnit);
    
    /**
     * 恢复对象；若键已存在则覆盖替换。
     * 
     * @param state 对象状态快照
     * @return 无返回值
     */
    Mono<Void> restoreAndReplace(byte[] state);
    
    /**
     * 恢复并替换已存在的对象，同时设置存活时间（TTL）。
     * 
     * @param state 对象状态快照
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     * @return 无返回值
     */
    Mono<Void> restoreAndReplace(byte[] state, long timeToLive, TimeUnit timeUnit);

    /**
     * 返回对象的序列化快照（DUMP 格式）
     * 
     * @return 序列化快照字节数组
     */
    Mono<byte[]> dump();
    
    /**
     * 更新对象的最后访问时间。 
     * 
     * @return 成功更新访问时间则为 true，否则 false
     */
    Mono<Boolean> touch();    
    
    /**
     * 异步删除对象；实际内存回收稍后执行。
     * <p>
     * 需要 Redis 4.0+
     * 
     * @return 存在且已删除则为 true，否则 false
     */
    Mono<Boolean> unlink();
    
    /**
     * 将对象从源 Redis 实例复制到目标 Redis 实例
     *
     * @param host 目标主机
     * @param port 目标端口
     * @param database 目标数据库编号
     * @param timeout 与目标实例通信的最大空闲时间（毫秒）
     * @return 无返回值
     */
    Mono<Void> copy(String host, int port, int database, long timeout);

    /**
     * 将当前对象复制到指定名称的新键。
     *
     * @param destination 目标实例键名
     * @return 复制成功则为 true，否则 false
     */
    Mono<Boolean> copy(String destination);

    /**
     * 将当前对象复制到指定名称与数据库编号的新键。
     *
     * @param destination 目标实例键名
     * @param database 目标数据库编号
     * @return 复制成功则为 true，否则 false
     */
    Mono<Boolean> copy(String destination, int database);

    /**
     * 复制到指定名称的新键；若目标键已存在则覆盖。
     *
     * @param destination 目标实例键名
     * @return 复制成功则为 true，否则 false
     */
    Mono<Boolean> copyAndReplace(String destination);

    /**
     * 复制到指定名称与数据库的新键；若目标键已存在则覆盖。
     *
     * @param destination 目标实例键名
     * @param database 目标数据库编号
     * @return 复制成功则为 true，否则 false
     */
    Mono<Boolean> copyAndReplace(String destination, int database);

    /**
     * 将对象从源 Redis 实例迁移到目标实例
     *
     * @param host 目标主机
     * @param port 目标端口
     * @param database 目标数据库编号
     * @param timeout 与目标实例通信的最大空闲时间（毫秒）
     * @return 无返回值
     */
    Mono<Void> migrate(String host, int port, int database, long timeout);

    /**
     * 将对象移动到另一个 Redis 数据库 in  mode
     *
     * @param database Redis 数据库编号
     * @return <code>true</code> if key was moved <code>false</code> if not
     */
    Mono<Boolean> move(int database);

    /**
     * 删除对象
     *
     * @return <code>true</code> if object was deleted <code>false</code> if not
     */
    Mono<Boolean> delete();

    /**
     * 将当前对象键重命名为 {@code newName}
     * in  mode
     *
     * @param newName 新键名
     * @return 无返回值
     */
    Mono<Void> rename(String newName);

    /**
     * 将当前对象键重命名为 {@code newName}
     * in  mode only if new key is not exists
     *
     * @param newName 新键名
     * @return 重命名成功则为 true，否则 false
     */
    Mono<Boolean> renamenx(String newName);

    /**
     * 检查对象是否存在
     *
     * @return 存在则为 true，否则 false
     */
    Mono<Boolean> isExists();

    /**
     * 注册对象事件监听器
     * 
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * 
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    Mono<Integer> addListener(ObjectListener listener);

    /**
     * 移除对象事件监听器
     * 
     * @param listenerId 监听器 ID
     * @return 无返回值
     */
    Mono<Void> removeListener(int listenerId);
    
}
