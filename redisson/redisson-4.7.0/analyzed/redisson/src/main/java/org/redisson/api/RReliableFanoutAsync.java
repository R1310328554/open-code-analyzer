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
 * 可靠的扇出（Fanout）异步 API，确保消息投递到已订阅队列。
 * <p>各方法返回 {@link RFuture}。
 *
 * @param <V> 消息载荷类型
 *
 * @author Nikita Koksharov
 */
public interface RReliableFanoutAsync<V> extends RExpirableAsync {

    /**
     * 按给定参数向所有已订阅队列发布单条消息。
     *
     * @param args 单条消息的发布参数
     * @return 已发布消息；若未写入全部订阅队列则可能为 null（队列已满、消息超限或去重拒绝等）
     */
    RFuture<Message<V>> publishAsync(FanoutPublishArgs<V> args);

    /**
     * 按给定参数向所有已订阅队列批量发布消息。
     *
     * @param args 批量消息的发布参数
     * @return 至少写入一个订阅队列的消息列表（未写入的条目因队列满、消息超限或去重而被跳过）
     */
    RFuture<List<Message<V>>> publishManyAsync(FanoutPublishArgs<V> args);

    /**
     * 移除指定队列名称上的消息过滤器。
     *
     * @param name 队列名称
     */
    RFuture<Void> removeFilterAsync(String name);

    /**
     * 设置通过本扇出发布到队列时应用于全部消息的过滤器。
     * <p>
     * FanoutFilter 会在所有 ReliableFanout 实例间复制，并在发布时各自生效。
     *
     * @param name 队列名称
     * @param filter 应用于消息的过滤器
     */
    RFuture<Void> setFilterAsync(String name, MessageFilter<V> filter);

    /**
     * 检查指定名称的队列是否已订阅本扇出。
     *
     * @param name 队列名称
     * @return 已订阅则为 true，否则 false
     */
    RFuture<Boolean> isSubscribedAsync(String name);

    /**
     * 将指定名称的队列订阅到本扇出。
     *
     * @param name 队列名称
     * @return 新订阅成功则为 true，已订阅则为 false
     */
    RFuture<Boolean> subscribeQueueAsync(String name);

    /**
     * 将指定名称的队列订阅到本扇出，并绑定消息过滤器。
     *
     * @param name 队列名称
     * @param filter 应用于本扇出全部发布消息的过滤器
     * @return 新订阅成功则为 true，已订阅则为 false
     */
    RFuture<Boolean> subscribeQueueAsync(String name, MessageFilter<V> filter);

    /**
     * 取消指定名称队列对本扇出的订阅。
     *
     * @param name 队列名称
     * @return 取消订阅成功则为 true，未订阅则为 false
     */
    RFuture<Boolean> unsubscribeAsync(String name);

    /**
     * 返回本扇出全部订阅者的队列名称列表。
     *
     * @return 订阅者名称列表
     */
    RFuture<List<String>> getSubscribersAsync();

    /**
     * 返回本扇出的订阅者数量。
     *
     * @return 订阅者数量
     */
    RFuture<Integer> countSubscribersAsync();

}
