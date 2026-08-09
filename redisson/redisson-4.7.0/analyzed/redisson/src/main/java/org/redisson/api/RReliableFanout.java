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

import org.redisson.api.fanout.MessageFilter;
import org.redisson.api.fanout.FanoutPublishArgs;

import java.util.List;

/**
 * 可靠扇出（Fanout）同步 API，确保消息投递到已订阅队列。
 * <p>发布时按订阅关系将消息复制到各目标队列，支持过滤、去重与容量限制。
 *
 * @param <V> 消息载荷类型
 * @author Nikita Koksharov
 */
public interface RReliableFanout<V> extends RExpirable, RReliableFanoutAsync<V>, RDestroyable {

    /**
     * 按参数将单条消息发布到所有已订阅队列。
     *
     * @param args 单条消息发布参数
     * @return 已发布的消息；若未写入全部订阅队列则可能为 null（队列满、消息过大或去重拒绝）
     */
    Message<V> publish(FanoutPublishArgs<V> args);

    /**
     * 按参数批量发布消息到所有已订阅队列。
     *
     * @param args 批量消息发布参数
     * @return 至少写入一个订阅队列的消息列表（队列满、消息过大或去重时可能跳过部分队列）
     */
    List<Message<V>> publishMany(FanoutPublishArgs<V> args);

    /**
     * 移除指定队列名称关联的消息过滤器。
     *
     * @param name 队列名称
     */
    void removeFilter(String name);

    /**
     * 为通过本 fanout 投递到指定队列的全部消息设置过滤器。
     * <p>
     * FanoutFilter 会在各 ReliableFanout 实例间复制，发布消息时在各自节点上应用。
     *
     * @param name 队列名称
     * @param filter 消息过滤器
     */
    void setFilter(String name, MessageFilter<V> filter);

    /**
     * 检查指定名称的队列是否已订阅本 fanout。
     *
     * @param name 队列名称
     * @return 已订阅则为 {@code true}，否则 {@code false}
     */
    boolean isSubscribed(String name);

    /**
     * 将指定名称的队列订阅到本 fanout。
     *
     * @param name 队列名称
     * @return 订阅成功则为 {@code true}；已订阅则为 {@code false}
     */
    boolean subscribeQueue(String name);

    /**
     * 将指定队列订阅到本 fanout，并绑定消息过滤器。
     *
     * @param name 队列名称
     * @param filter 应用于该队列全部消息的过滤器
     * @return 订阅成功则为 {@code true}；已订阅则为 {@code false}
     */
    boolean subscribeQueue(String name, MessageFilter<V> filter);

    /**
     * 取消指定队列对本 fanout 的订阅。
     *
     * @param name 队列名称
     * @return 取消订阅成功则为 {@code true}；未订阅则为 {@code false}
     */
    boolean unsubscribe(String name);

    /**
     * 返回本 fanout 全部订阅队列的名称列表。
     *
     * @return 订阅队列名称列表
     */
    List<String> getSubscribers();

    /**
     * 返回本 fanout 的订阅队列数量。
     *
     * @return 订阅队列数量
     */
    int countSubscribers();

}
