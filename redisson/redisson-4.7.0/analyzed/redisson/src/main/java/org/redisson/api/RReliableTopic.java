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

import org.redisson.api.listener.MessageListener;

/**
 * 基于 Redis Stream 的可靠 Topic 同步 API。
 * <p>
 * 每个订阅者实例分配独立 Redis 连接；消息投递给同一 Redis 配置下的全部监听器。
 * <p>
 * 需要 <b>Redis 5.0.0 及以上</b>。
 * <p>
 * @author Nikita Koksharov
 */
public interface RReliableTopic extends RExpirable, RReliableTopicAsync {

    /**
     * 返回 Redis Stream 中存储的消息数量。
     *
     * @return 消息数量
     */
    long size();

    /**
     * 异步向本 Topic 的全部订阅者发布消息。
     * 每个订阅者可挂载多个监听器。
     *
     * @param message 待发送消息
     * @return 收到消息的订阅者数量
     */
    long publish(Object message);

    /**
     * 订阅本 Topic。
     * 任意消息发布时触发 {@code MessageListener.onMessage}。
     * <p>
     * 消息会在所有 Topic 实例间广播，但监听器绑定在当前实例上。
     * <p>
     * 注册监听器后会启动 Watchdog。
     *
     * @see org.redisson.config.Config#setReliableTopicWatchdogTimeout(long)
     *
     * @param <M> 消息类型
     * @param type 消息类型
     * @param listener 消息监听器
     * @return 绑定在本 Topic 实例上的监听器 ID
     * @see MessageListener
     */
    <M> String addListener(Class<M> type, MessageListener<M> listener);

    /**
     * 按 ID 移除绑定在本 Topic 实例上的监听器
     *
     * @param listenerIds 监听器 ID 列表
     */
    void removeListener(String... listenerIds);

    /**
     * 移除绑定在本 Topic 实例上的全部监听器
     */
    void removeAllListeners();

    /**
     * 返回本 Topic 实例上已注册的监听器数量
     *
     * @return 监听器数量
     */
    int countListeners();

    /**
     * 返回所有 Redisson 实例上对本 Topic 的订阅者总数。
     * 每个订阅者可挂载多个监听器。
     *
     * @return 订阅者数量
     */
    int countSubscribers();

}
