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

/**
 * 所有 Redisson 对象的同步 API 基类接口。
 * <p>继承 {@link RObjectAsync}，提供元数据查询、DUMP/RESTORE、迁移复制及键生命周期管理等通用能力。
 *
 * @author Nikita Koksharov
 */
public interface RObject extends RObjectAsync {

    /**
     * 返回自上次读写操作以来经过的秒数。
     *
     * @return 空闲秒数
     */
    Long getIdleTime();

    /**
     * 返回该对象的引用计数。
     *
     * @return 引用计数
     */
    int getReferenceCount();

    /**
     * 返回该对象的对数访问频率计数器。
     *
     * @return 访问频率计数
     */
    int getAccessFrequency();

    /**
     * 返回 Redis 对象在服务端使用的内部编码类型
     *
     * @return 内部编码类型
     */
    ObjectEncoding getInternalEncoding();
    /**
     * 返回该对象在 Redis 内存中占用的字节数。
     * 
     * @return 内存占用字节数
     */
    long sizeInMemory();
    
    /**
     * 使用 {@link #dump()} 返回的状态快照恢复对象。
     * 
     * @param state 对象状态快照
     */
    void restore(byte[] state);
    
    /**
     * 使用 {@link #dump()} 返回的状态恢复对象，并设置存活时间（TTL）。
     * 
     * @param state 对象状态快照
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     */
    void restore(byte[] state, long timeToLive, TimeUnit timeUnit);
    
    /**
     * 恢复对象；若键已存在则覆盖替换。
     * 
     * @param state 对象状态快照
     */
    void restoreAndReplace(byte[] state);

    /**
     * 恢复并替换已存在的对象，同时设置存活时间（TTL）。
     * 
     * @param state 对象状态快照
     * @param timeToLive 存活时间
     * @param timeUnit 时间单位
     */
    void restoreAndReplace(byte[] state, long timeToLive, TimeUnit timeUnit);
    
    /**
     * 返回对象的序列化快照（DUMP 格式）
     * 
     * @return 序列化快照字节数组
     */
    byte[] dump();
    
    /**
     * 更新对象的最后访问时间。 
     * 
     * @return 成功更新访问时间则为 true，否则 false
     */
    boolean touch();
    
    /**
     * 将对象从源 Redis 实例复制到目标 Redis 实例
     *
     * @param host 目标主机
     * @param port 目标端口
     * @param database 目标数据库编号
     * @param timeout 与目标实例通信的最大空闲时间（毫秒）
     */
    void migrate(String host, int port, int database, long timeout);

    /**
     * Copy object from source Redis instance to destination Redis instance
     *
     * @param host - destination host
     * @param port - destination port
     * @param database - destination database
     * @param timeout - maximum idle time in any moment of the communication with the destination instance in milliseconds
     */
    void copy(String host, int port, int database, long timeout);

    /**
     * 将当前对象复制到指定名称的新键。
     *
     * @param destination 目标实例键名
     * @return 复制成功则为 true，否则 false
     */
    boolean copy(String destination);

    /**
     * 将当前对象复制到指定名称与数据库编号的新键。
     *
     * @param destination 目标实例键名
     * @param database 目标数据库编号
     * @return 复制成功则为 true，否则 false
     */
    boolean copy(String destination, int database);

    /**
     * 复制到指定名称的新键；若目标键已存在则覆盖。
     *
     * @param destination 目标实例键名
     * @return 复制成功则为 true，否则 false
     */
    boolean copyAndReplace(String destination);

    /**
     * 复制到指定名称与数据库的新键；若目标键已存在则覆盖。
     *
     * @param destination 目标实例键名
     * @param database 目标数据库编号
     * @return 复制成功则为 true，否则 false
     */
    boolean copyAndReplace(String destination, int database);

    /**
     * 将对象移动到另一个 Redis 数据库
     *
     * @param database Redis 数据库编号
     * @return 移动成功则为 true，否则 false
     */
    boolean move(int database);

    /**
     * 返回对象在 Redis 中的键名
     *
     * @return 对象键名
     */
    String getName();

    /**
     * 删除该对象
     * 
     * @return 存在且已删除则为 true，否则 false
     */
    boolean delete();

    /**
     * 异步删除对象；实际内存回收稍后执行。
     * <p>
     * 需要 Redis 4.0+
     * 
     * @return 存在且已删除则为 true，否则 false
     */
    boolean unlink();
    
    /**
     * 将当前对象键重命名为 {@code newName}
     *
     * @param newName 新键名
     */
    void rename(String newName);

    /**
     * 将当前对象键重命名为 {@code newName}
     * only if new key doesn't exist.
     *
     * @param newName 新键名
     * @return 重命名成功则为 true，否则 false
     */
    boolean renamenx(String newName);

    /**
     * 检查对象是否存在
     *
     * @return 存在则为 true，否则 false
     */
    boolean isExists();

    /**
     * 返回该对象使用的 {@link Codec} 编解码器
     * 
     * @return 编解码器
     */
    Codec getCodec();
    
    /**
     * 注册对象事件监听器
     * 
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * 
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    int addListener(ObjectListener listener);
    
    /**
     * 移除对象事件监听器
     * 
     * @param listenerId 监听器 ID
     */
    void removeListener(int listenerId);

}
