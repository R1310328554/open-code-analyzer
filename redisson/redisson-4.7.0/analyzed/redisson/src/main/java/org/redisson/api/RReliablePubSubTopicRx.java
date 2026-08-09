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
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.redisson.api.pubsub.*;
import org.redisson.api.pubsub.event.PubSubEventListener;
import org.redisson.client.codec.Codec;

import java.util.List;
import java.util.Set;

/**
 * Reliable PubSub Topic implementation based on Stream object.
 * <p>
 * 与常规 Valkey/Redis PubSub 不同，本实现提供如下能力：
 * <ul>
 *   <li>FIFO 顺序投递消息</li>
 *   <li>消息确认（ACK）以标记处理成功</li>
 *   <li>否定确认（NACK）触发重投；未配置 DLT 时可能删除消息</li>
 *   <li>冗余与同步复制</li>
 *   <li>按 ID 或哈希在指定时间窗口内去重</li>
 *   <li>批量操作</li>
 *   <li>可配置 Topic 大小上限</li>
 *   <li>可配置单条消息大小上限</li>
 *   <li>可配置消息过期时间</li>
 *   <li>可配置消息可见性超时</li>
 *   <li>可配置消息优先级</li>
 *   <li>可配置消息延迟投递</li>
 *   <li>可配置最大投递次数</li>
 *   <li>按订阅 seek，支持回放或偏移调整</li>
 *   <li>Pull/Push 两种消费模型</li>
 *   <li>按 key 分组，保证同一消费者顺序处理</li>
 *   <li>未 ACK 消息自动重投（各消息 visibility 不同可能影响顺序）</li>
 *   <li>死信 Topic（DLT）支持</li>
 * </ul>
 *
 * @author Nikita Koksharov
 *
 */
public interface RReliablePubSubTopicRx<V> extends RExpirableRx, RDestroyable {

    /**
     * 设置本可靠 PubSub Topic 的配置。
     *
     * @param config 要应用的 Topic 配置
     */
    Completable setConfig(TopicConfig config);

    /**
     * 尝试设置本可靠 PubSub Topic 的配置。
     * <p>
     * 仅当此前未设置过配置时才会生效。
     *
     * @param config 要应用的 Topic 配置
     * @return 设置成功则为 true，已有配置则为 false
     */
    Single<Boolean> setConfigIfAbsent(TopicConfig config);

    /**
     * 返回 Topic 中可 poll 的消息总数（不含延迟与未 ACK 消息）。
     *
     * @return 消息总数
     */
    Single<Integer> size();

    /**
     * 检查 PubSub Topic 是否为空。
     * <p>
     * 就绪、延迟、未 ACK 三种状态下均无消息时视为空。
     *
     * @return 为空则为 true，否则 false
     */
    Single<Boolean> isEmpty();

    /**
     * 清空 PubSub Topic 中的全部消息。
     * <p>
     * 会清除就绪、延迟、未 ACK 等所有状态的消息。
     *
     * @return Topic 存在且已清空则为 true，否则 false
     */
    Single<Boolean> clear();

    /**
     * 检查 PubSub Topic 是否包含指定 ID 的消息。
     *
     * @param id 待检查的消息 ID
     * @return 存在则为 true，否则 false
     */
    Single<Boolean> contains(String id);

    /**
     * 检查 PubSub Topic 是否包含指定 ID 列表中的消息。
     *
     * @param ids 待检查的消息 ID 列表
     * @return 匹配的消息数量
     */
    Single<Integer> containsMany(String... ids);

    /**
     * 按参数向 PubSub Topic 添加单条消息。
     * <p>
     * 以下情况可能返回 {@code null}：
     * <ul>
     *     <li>按 ID 或哈希去重导致重复</li>
     *     <li>Topic 已达配置的大小上限</li>
     * </ul>
     *
     * @param params 待添加消息的参数
     * @return 已添加消息（含 ID 与元数据）；Topic 已满且超时未腾出空间则为 null
     * @throws  if this operation is disabled
     */
    Maybe<Message<V>> publish(PublishArgs<V> params);

    /**
     * 批量向 PubSub Topic 添加消息。
     * <p>
     * 比逐条添加更高效。
     * <p>
     * 以下情况部分消息可能未写入：
     * <ul>
     *     <li>按 ID 或哈希去重导致重复</li>
     *     <li>Topic 已达配置的大小上限</li>
     * </ul>
     *
     * @param params 待批量添加消息的参数
     * @return 已添加消息列表；Topic 已满且超时未腾出空间则为空列表
     * @throws OperationDisabledException if this operation is disabled
     */
    Single<List<Message<V>>> publishMany(PublishArgs<V> params);

    /**
     * 返回将本 Topic 配置为死信 Topic（DLT）的源 Topic 名称集合。
     * <p>
     * 仅当源 Topic 配置中指定了本 Topic 作为 DLT 时有效。
     *
     * @return 源 Topic 名称集合
     */
    Single<Set<String>> getDeadLetterTopicSources();

    /**
     * 返回 Topic 中可由 poll() 读取的全部消息（不移除），便于巡检与调试。
     *
     * @return Topic 中全部消息列表
     */
    Single<List<Message<V>>> listAll();

    /**
     * 使用指定编解码器反序列化消息头，返回 Topic 中可由 poll() 读取的全部消息。
     *
     * @param headersCodec 用于反序列化消息头的编解码器
     * @return Topic 中全部消息列表
     */
    Single<List<Message<V>>> listAll(Codec headersCodec);

    /**
     * 按 ID 返回单条消息
     *
     * @param id 消息 ID
     * @return 消息对象
     */
    Maybe<Message<V>> get(String id);

    /**
     * 按 ID 返回单条消息 applying specified codec to headers
     *
     * @param id 消息 ID
     * @param headersCodec 消息头编解码器
     * @return 消息对象
     */
    Maybe<Message<V>> get(Codec headersCodec, String id);

    /**
     * 按 ID 列表返回多条消息
     *
     * @param ids 消息 ID 列表
     * @return 消息对象
     */
    Single<List<Message<V>>> getAll(String... ids);

    /**
     * 按 ID 列表返回多条消息 applying specified codec to headers
     *
     * @param ids 消息 ID 列表
     * @param headersCodec 消息头编解码器
     * @return 消息对象
     */
    Single<List<Message<V>>> getAll(Codec headersCodec, String... ids);

    /**
     * 注册 PubSub 事件监听器
     *
     * @see org.redisson.api.pubsub.event.PublishedEventListener
     * @see org.redisson.api.pubsub.event.TopicConfigEventListener
     * @see org.redisson.api.pubsub.event.DisabledOperationEventListener
     * @see org.redisson.api.pubsub.event.EnabledOperationEventListener
     * @see org.redisson.api.pubsub.event.TopicFullEventListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    Single<String> addListener(PubSubEventListener listener);

    /**
     * 按 ID 移除监听器
     *
     * @param id 监听器 ID
     */
    Completable removeListener(String id);

    /**
     * 禁用指定的 PubSub 操作
     *
     * @param operation PubSub 操作类型
     */
    Completable disableOperation(PubSubOperation operation);

    /**
     * 启用指定的 PubSub 操作
     *
     * @param operation PubSub 操作类型
     */
    Completable enableOperation(PubSubOperation operation);

    /**
     * 按名称返回已存在的订阅。
     *
     * @param name 订阅名称
     * @return 对应订阅；不存在则为 null
     */
    Maybe<SubscriptionRx<V>> getSubscription(String name);

    /**
     * 创建自动命名的新订阅。
     * <p>
     * 每个订阅维护独立偏移量，与同 Topic 上其他订阅的消费进度互不影响。
     *
     * @return 订阅对象
     */
    Single<SubscriptionRx<V>> createSubscription();

    /**
     * 按指定配置创建新订阅。
     * <p>
     * 每个订阅维护独立偏移量，与同 Topic 上其他订阅的消费进度互不影响。
     *
     * @param config 订阅配置
     * @return 订阅对象
     */
    Single<SubscriptionRx<V>> createSubscription(SubscriptionConfig config);

    /**
     * 检查指定名称的订阅是否存在。
     *
     * @param name 订阅名称 to check
     * @return 存在则为 true，否则 false
     */
    Single<Boolean> hasSubscription(String name);

    /**
     * 移除指定名称的订阅。
     * <p>
     * 同时删除该订阅关联的全部消费者。
     *
     * @param name 订阅名称 to remove
     * @return 移除成功则为 true，不存在则为 false
     */
    Single<Boolean> removeSubscription(String name);

    /**
     * 返回本 Topic 上已注册的全部订阅名称。
     *
     * @return 订阅名称集合
     */
    Single<Set<String>> getSubscriptions();

    /**
     * 返回本 Topic 的统计信息。
     * <p>
     * 包括消息数量、吞吐量及其他运行指标。
     *
     * @return Topic 统计信息
     */
    Single<TopicStatistics> getStatistics();

}
