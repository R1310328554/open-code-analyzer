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

import org.redisson.api.queue.*;
import org.redisson.api.queue.event.QueueEventListener;
import org.redisson.client.codec.Codec;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * Reliable queue reactive implementation based on Stream object.
 * <p>
 * 与常规 队列不同，本实现提供如下能力：
 * <ul>
 *   <li>消息确认（ACK）以标记处理成功</li>
 *   <li>否定确认（NACK）触发重投；未配置 DLQ 时可能删除消息</li>
 *   <li>冗余与同步复制</li>
 *   <li>按 ID 或哈希在指定时间窗口内去重</li>
 *   <li>批量操作</li>
 *   <li>可配置队列大小上限</li>
 *   <li>可配置单条消息大小上限</li>
 *   <li>可配置消息过期时间</li>
 *   <li>可配置消息可见性超时</li>
 *   <li>可配置消息优先级</li>
 *   <li>可配置消息延迟投递</li>
 *   <li>可配置最大投递次数</li>
 *   <li>未 ACK 消息自动重投</li>
 *   <li>死信队列（DLQ）支持</li>
 * </ul>
 *
 * @author Nikita Koksharov
 *
 */
public interface RReliableQueueReactive<V> extends RExpirableRx {

    /**
     * 设置本可靠队列的配置。
     *
     * @param config 要应用的队列配置
     */
    Mono<Void> setConfig(QueueConfig config);

    /**
     * 尝试设置本可靠队列的配置。
     * <p>
     * 仅当此前未设置过配置时才会生效。
     *
     * @param config 要应用的队列配置
     * @return 设置成功则为 true，已有配置则为 false
     */
    Mono<Boolean> setConfigIfAbsent(QueueConfig config);

    /**
     * 返回队列中可 poll 的消息总数（不含延迟与未 ACK 消息）。
     *
     * @return 消息总数
     */
    Mono<Integer> size();

    /**
     * 返回队列中延迟投递的消息数量。
     * <p>
     * 延迟消息尚未到达可消费时间。
     *
     * @return 延迟消息数量
     */
    Mono<Integer> countDelayedMessages();

    /**
     * 返回队列中未确认（未 ACK）的消息数量。
     * <p>
     * 这类消息已投递给消费者，但尚未确认处理成功。
     *
     * @return 未 ACK 消息数量
     */
    Mono<Integer> countUnacknowledgedMessages();

    /**
     * 清空队列中的全部消息。
     * <p>
     * 会清除就绪、延迟、未 ACK 等所有状态的消息。
     *
     * @return 队列存在且已清空则为 true，否则 false
     */
    Mono<Boolean> clear();

    /**
     * 取出并移除队首消息；队列为空时返回 {@code null}。
     * <p>
     * 取出的消息处于未 ACK 状态，需调用 {@link #acknowledge(QueueAckArgs)} 或 {@link #negativeAcknowledge(QueueNegativeAckArgs)} 确认或否定确认。
     *
     * @return 队首消息；队列为空则为 null
     * @throws OperationDisabledException if this operation is disabled
     */
    Mono<Message<V>> poll();

    /**
     * 按 poll 参数取出并移除队首消息。
     * <p>
     * 取出的消息处于未 ACK 状态，需调用 {@link #acknowledge(QueueAckArgs)} 或 {@link #negativeAcknowledge(QueueNegativeAckArgs)} 确认或否定确认。
     *
     * @param args poll 参数
     * @return 队首消息；队列为空则为 null
     * @throws OperationDisabledException if this operation is disabled
     */
    Mono<Message<V>> poll(QueuePollArgs args);

    /**
     * 按 poll 参数批量取出并移除消息。
     * <p>
     * 比逐条 poll 更高效。
     * <p>
     * 取出的消息处于未 ACK 状态，需调用 {@link #acknowledge(QueueAckArgs)} 或 {@link #negativeAcknowledge(QueueNegativeAckArgs)} 确认或否定确认。
     *
     * @param pargs poll 参数
     * @return 取出的消息列表
     * @throws OperationDisabledException if this operation is disabled
     */
    Mono<List<Message<V>>> pollMany(QueuePollArgs pargs);

    /**
     * 确认消息已成功处理。
     * <p>
     * ACK 后消息将从队列永久移除，不再重投。
     *
     * @param args 确认（ACK）参数
     */
    Mono<Void> acknowledge(QueueAckArgs args);

    /**
     * 检查队列是否包含指定 ID 的消息。
     *
     * @param id 待检查的消息 ID
     * @return 存在则为 true，否则 false
     */
    Mono<Boolean> contains(String id);

    /**
     * 检查队列是否包含指定 ID 列表中的消息。
     *
     * @param ids 待检查的消息 ID 列表
     * @return 匹配的消息数量
     */
    Mono<Integer> containsMany(String... ids);

    /**
     * 从队列移除指定消息。
     * <p>
     * 可删除就绪、延迟、未 ACK 任意状态的消息。
     *
     * @param args 移除参数
     * @return 移除成功则为 true，未找到则为 false
     */
    Mono<Boolean> remove(QueueRemoveArgs args);

    /**
     * 通过 Mono 批量从队列移除多条消息。
     *
     * @param args 移除参数
     * @return 成功移除的消息数量
     */
    Mono<Integer> removeMany(QueueRemoveArgs args);

    /**
     * 在队列之间移动消息。
     *
     * @param args 移动参数
     * @return 成功移动的消息数量
     */
    Mono<Integer> move(QueueMoveArgs args);

    /**
     * 按参数向队列添加单条消息。
     * <p>
     * 以下情况可能返回 {@code null}：
     * <ul>
     *     <li>按 ID 或哈希去重导致重复</li>
     *     <li>队列已达配置的大小上限</li>
     * </ul>
     *
     * @param params 待添加消息的参数
     * @return the added message with its assigned ID and metadata or {@code null} if nothing was added
     * @throws OperationDisabledException if this operation is disabled
     */
    Mono<Message<V>> add(QueueAddArgs<V> params);

    /**
     * 通过 Mono 批量向队列添加消息。
     * <p>
     * 比逐条添加更高效。
     * <p>
     * 以下情况部分消息可能未写入：
     * <ul>
     *     <li>按 ID 或哈希去重导致重复</li>
     *     <li>队列已达配置的大小上限</li>
     * </ul>
     *
     * @param params 待批量添加消息的参数
     * @return a list of added messages with their assigned IDs and metadata
     * @throws OperationDisabledException if this operation is disabled
     */
    Mono<List<Message<V>>> addMany(QueueAddArgs<V> params);

    /**
     * 返回将本队列配置为死信队列（DLQ）的源队列名称集合。
     * <p>
     * 仅当源队列配置中指定了本队列作为 DLQ 时有效。
     *
     * @return 源队列名称集合
     */
    Mono<Set<String>> getDeadLetterQueueSources();

    /**
     * 返回队列中可由 poll() 读取的全部消息（不移除），便于巡检与调试。
     *
     * @return 队列中全部消息列表
     */
    Mono<List<Message<V>>> listAll();

    /**
     * 使用指定编解码器反序列化消息头，返回队列中可由 poll() 读取的全部消息。
     *
     * @param headersCodec 用于反序列化消息头的编解码器
     * @return 队列中全部消息列表
     */
    Mono<List<Message<V>>> listAll(Codec headersCodec);

    /**
     * 按 ID 返回单条消息
     *
     * @param id 消息 ID
     * @return 消息对象
     */
    Mono<Message<V>> get(String id);

    /**
     * 按 ID 返回单条消息 applying specified codec to headers
     *
     * @param id 消息 ID
     * @param headersCodec 消息头编解码器
     * @return 消息对象
     */
    Mono<Message<V>> get(Codec headersCodec, String id);

    /**
     * 按 ID 列表返回多条消息
     *
     * @param ids 消息 ID 列表
     * @return 消息对象
     */
    Mono<List<Message<V>>> getAll(String... ids);

    /**
     * 按 ID 列表返回多条消息 applying specified codec to headers
     *
     * @param ids 消息 ID 列表
     * @param headersCodec 消息头编解码器
     * @return 消息对象
     */
    Mono<List<Message<V>>> getAll(Codec headersCodec, String... ids);

    /**
     * 显式否定确认（NACK）消息，标记为处理失败或拒绝。
     *
     * @param args 否定确认（NACK）参数
     */
    Mono<Void> negativeAcknowledge(QueueNegativeAckArgs args);

    /**
     * 注册队列事件监听器
     *
     * @see org.redisson.api.queue.event.AddedEventListener
     * @see org.redisson.api.queue.event.PolledEventListener
     * @see org.redisson.api.queue.event.RemovedEventListener
     * @see org.redisson.api.queue.event.AcknowledgedEventListener
     * @see org.redisson.api.queue.event.NegativelyAcknowledgedEventListener
     * @see org.redisson.api.queue.event.ConfigEventListener
     * @see org.redisson.api.queue.event.DisabledOperationEventListener
     * @see org.redisson.api.queue.event.EnabledOperationEventListener
     * @see org.redisson.api.queue.event.FullEventListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    Mono<String> addListener(QueueEventListener listener);

    /**
     * 按 ID 移除监听器
     *
     * @param id 监听器 ID
     */
    Mono<Void> removeListener(String id);

    /**
     * 禁用指定的队列操作
     *
     * @param operation 队列操作类型
     */
    Mono<Void> disableOperation(QueueOperation operation);

    /**
     * 启用指定的队列操作
     *
     * @param operation 队列操作类型
     */
    Mono<Void> enableOperation(QueueOperation operation);

}
