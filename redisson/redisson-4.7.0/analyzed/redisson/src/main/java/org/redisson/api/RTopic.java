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

import java.util.List;

import org.redisson.api.listener.MessageListener;
import org.redisson.api.listener.StatusListener;

/**
 * 分布式主题；消息会投递到 Redis 集群中所有订阅该主题的消息监听器。
 *
 * @author Nikita Koksharov
 *
 */
public interface RTopic extends RTopicAsync {

    /**
     * 获取主题关联的频道名称列表。
     *
     * @return 频道名称列表
     */
    List<String> getChannelNames();

    /**
     * 向该主题的所有订阅者发布消息。
     *
     * @param message 待发送消息
     * @return 收到消息的客户端数量
     */
    long publish(Object message);

    /**
     * 订阅该主题；有消息发布时调用 {@code MessageListener.onMessage}。
     *
     * @param <M> 消息类型
     * @param type 消息类型
     * @param listener 消息监听器
     * @return 本地唯一监听器 ID
     * @see org.redisson.api.listener.MessageListener
     */
    <M> int addListener(Class<M> type, MessageListener<? extends M> listener);

    /**
     * 订阅该主题的状态变更。
     *
     * @param listener 消息监听器
     * @return 监听器 ID
     * @see org.redisson.api.listener.StatusListener
     */
    int addListener(StatusListener listener);

    /**
     * 按监听器实例移除监听器。
     *
     * @param listener 监听器实例
     */
    void removeListener(MessageListener<?> listener);
    
    /**
     * 按监听器 ID 移除对该主题的订阅。
     *
     * @param listenerIds 监听器 ID 列表
     */
    void removeListener(Integer... listenerIds);

    /**
     * 移除该主题上的全部监听器。
     */
    void removeAllListeners();

    /**
     * 返回该主题上已注册的监听器数量。
     * 
     * @return 监听器数量
     */
    int countListeners();
    
    /**
     * 返回所有 Redisson 实例上该主题的订阅者数量；每个订阅者可注册多个监听器。
     * 
     * @return 订阅者数量
     */
    long countSubscribers();
}
