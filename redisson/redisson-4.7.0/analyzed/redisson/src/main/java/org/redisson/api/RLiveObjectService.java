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

import java.util.Collection;
import java.util.List;

import org.redisson.api.condition.Condition;
import org.redisson.api.condition.Conditions;

/**
 * Live Object 服务：将 Java 实体透明映射为 Redis 存储。
 * <p>实体类无需预注册；getter/setter 调用自动重定向到 Redis。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 * @author Nikita Koksharov
 */
public interface RLiveObjectService {

    /**
     * 按 ID 从 Redis 查找实体。
     *
     * 实体类需有 {@code @RId} 标注字段，且
     * 类本身需标注 {@code @REntity}。{@code @RId} 类型
     * 不可为以下类型：
     * <ol>
     * <li>An array i.e. byte[], int[], Integer[], etc.</li>
     * <li>or a RObject i.e. RedissonMap</li>
     * <li>or a Class with REntity annotation.</li>
     * </ol>
     *
     *
     * @param entityClass 实体类
     * @param id 实体 ID
     * @param <T> Entity type
     * @return 代理对象；不存在时为 null
     */
    <T> T get(Class<T> entityClass, Object id);
    
    /**
     * 按条件查找匹配的实体集合。
     * <p><strong>
     * NOTE: open-source version is slow.<br>
     * 建议使用 <a href="https://redisson.pro">Redisson PRO</a>。
     * </strong><p>
     * 用法示例：
     * <pre>
     * Collection objects = liveObjectService.find(MyObject.class, Conditions.or(Conditions.in("field", "value1", "value2"), 
     *                          Conditions.and(Conditions.eq("field2", "value2"), Conditions.eq("field3", "value5"))));
     * </pre>
     * 
     * @see Conditions
     * 
     * @param <T> Entity type
     * @param entityClass 实体类
     * @param condition 查询条件
     * @return Live Object 集合（可能为空）
     */
    <T> Collection<T> find(Class<T> entityClass, Condition condition);

    /**
     * 统计满足条件的实体数量。
     * <p><strong>
     * NOTE: open-source version is slow.<br>
     * 建议使用 <a href="https://redisson.pro">Redisson PRO</a>。
     * </strong><p>
     * 用法示例：
     * <pre>
     * long objectsAmount = liveObjectService.count(MyObject.class, Conditions.or(Conditions.in("field", "value1", "value2"),
     *                          Conditions.and(Conditions.eq("field2", "value2"), Conditions.eq("field3", "value5"))));
     * </pre>
     *
     * @see Conditions
     *
     * @param entityClass 实体类
     * @param condition 查询条件
     * @return 实体数量
     */
    long count(Class<?> entityClass, Condition condition);

    /**
     * 返回指定实体类的全部 ID 迭代器（SCAN 遍历）。
     * 通过 SCAN 遍历 ID；每次 SCAN 最多加载
     * 个键。
     *
     * @param entityClass 实体类
     * @param <K> Key type
     * @return collection of ids or empty collection.
     */
    <K> Iterable<K> findIds(Class<?> entityClass);

    /**
     * 返回指定实体类的全部 ID 迭代器（SCAN 遍历）。
     * 通过 SCAN 遍历 ID；每次 SCAN 最多加载
     * 个键。
     *
     * @param entityClass 实体类
     * @param count 每次 SCAN 加载的键数量
     * @param <K> Key type
     * @return collection of ids or empty collection.
     */
    <K> Iterable<K> findIds(Class<?> entityClass, int count);

    /**
     * 为游离对象返回代理实例，并丢弃游离对象中已有的字段值。
     * 游离对象中已有的字段值将被丢弃。
     *
     * 对象类需有字段标注
     * {@code @RId}，且该字段值非 null。
     *
     * Redis 中不存在时创建<b>空白</b>代理实例，
     * 主键与游离对象相同。
     *
     * @param <T> Entity type
     * @param detachedObject 游离（未代理）对象
     * @return 代理对象
     * @throws IllegalArgumentException if the object is is a RLiveObject instance.
     */
    <T> T attach(T detachedObject);

    /**
     * 为游离对象返回代理实例，将所有<b>非 null</b>字段值写入 Redis。
     * 将<b>非 null</b>字段值写入 Redis，不会删除
     * 字段值为 null 时 Redis 中的已有数据。
     * 
     * 对象类需有字段标注
     * {@code @RId}，且该字段值非 null。
     * 
     * Redis 中不存在时创建新 hash 键以
     * 存储；已存在则用给定对象状态覆盖 Redis 中的当前状态。
     *
     * @param <T> Entity type
     * @param detachedObject 游离（未代理）对象
     * @return 代理对象
     * @throws IllegalArgumentException if the object is is a RLiveObject instance.
     */
    <T> T merge(T detachedObject);

    /**
     * 为游离对象返回代理实例，将所有<b>非 null</b>字段值写入 Redis。
     * 将<b>非 null</b>字段值写入 Redis，不会删除
     * 字段值为 null 时 Redis 中的已有数据。
     *
     * 对象类需有字段标注
     * {@code @RId}，且该字段值非 null。
     *
     * Redis 中不存在时创建新 hash 键以
     * 存储；已存在则用给定对象状态覆盖 Redis 中的当前状态。
     *
     * @param <T> Entity type
     * @param detachedObjects 游离对象数组
     * @return 代理对象
     * @throws IllegalArgumentException if the object is is a RLiveObject instance.
     */
    <T> List<T> merge(T... detachedObjects);

    /**
     * 为游离对象返回代理实例，仅在 Redis 中不存在时写入<b>非 null</b>字段值。
     * <b>NON NULL</b> field values to the redis server. Only when the it does
     * 写入数据。
     * 
     * @param <T> Entity type
     * @param detachedObject 游离（未代理）对象
     * @return 代理对象
     */
    <T> T persist(T detachedObject);

    /**
     * 为多个游离对象返回代理实例，批量写入<b>非 null</b>字段值。
     * <b>NON NULL</b> field values.
     * <p>
     * 以批处理模式执行。
     *
     * @param <T> Entity type
     * @param detachedObjects 游离对象数组
     * @return list of proxied objects
     */
    <T> List<T> persist(T... detachedObjects);

    /**
     * 将已附加（代理）对象转为未代理的游离对象。
     *
     * @param <T> Entity type
     * @param attachedObject 已附加（代理）对象
     * @return 游离对象
     */
    <T> T detach(T attachedObject);

    /**
     * 删除已附加对象及其所有嵌套对象。
     *
     * @param <T> Entity type
     * @param attachedObject 已附加（代理）对象
     */
    <T> void delete(T attachedObject);

    /**
     * 按实体类与 ID 批量删除对象（含嵌套对象）。
     *
     * @param <T> Entity type
     * @param entityClass 实体类
     * @param ids 实体 ID 列表
     * 
     * @return 已删除对象数量
     */
    <T> long delete(Class<T> entityClass, Object... ids);
    
    /**
     * 将实例转换为 {@link RLiveObject}。
     * 
     * @param <T> type of instance
     * @param instance Live Object 实例
     * @return RLiveObject 兼容对象
     */
    <T> RLiveObject asLiveObject(T instance);

    /**
     * 将实例转换为 {@link RMap}。
     * 
     * @param <T> type of instance
     * @param <K> type of key
     * @param <V> type of value
     * @param instance Live Object 实例
     * @return RMap 兼容对象
     */
    <T, K, V> RMap<K, V> asRMap(T instance);

    /**
     * 判断实例是否为 {@link RLiveObject}。
     * 
     * @param <T> type of instance
     * @param instance Live Object 实例
     * @return 见方法说明
     */
    <T> boolean isLiveObject(T instance);
    
    /**
     * 判断 {@link RLiveObject} 是否已存在于 Redis；非 Live Object 实例返回 false。
     * the passed object is not a RLiveObject.
     * 
     * @param <T> type of instance
     * @param instance Live Object 实例
     * @return 见方法说明
     */
    <T> boolean isExists(T instance);
    
    /**
     * 预注册实体类；启动时批量注册可加速实例创建（非强制，首次使用时也会懒注册）。
     * 可加速实例创建；<b>非</b>强制，
     * 首次使用时也会懒注册。
     * 
     * 已注册类保存在类缓存中。
     * 
     * 缓存在不同 RedissonClient 实例间独立；
     * 在一个 RLiveObjectService 中注册的类，
     * 在同一 RedissonClient 创建的其他实例中也可访问。 
     * 
     * 
     * @param cls 实体类
     */
    void registerClass(Class<?> cls);
    
    /**
     * 从服务注销实体类；代理或创建失败时也会自动注销。
     * 类不再需要时注销。 
     * 
     * 代理或创建失败时会自动注销，
     * 因该类错误不可恢复。
     * 
     * 已注册类保存在类缓存中。
     * 
     * 缓存在不同 RedissonClient 实例间独立；
     * 在一个 RLiveObjectService 中注册的类， 
     * 在同一 RedissonClient 创建的其他实例中也可访问。 
     * 
     * 
     * @param cls 实体类
     */
    void unregisterClass(Class<?> cls);
    
    /**
     * 检查实体类是否已在缓存中注册。
     * 
     * 已注册类保存在类缓存中。
     * 
     * 缓存在不同 RedissonClient 实例间独立；
     * 在一个 RLiveObjectService 中注册的类， 
     * 在同一 RedissonClient 创建的其他实例中也可访问。 
     * 
     * 
     * @param cls 实体类
     * @return 见方法说明
     */
    boolean isClassRegistered(Class<?> cls);
}
