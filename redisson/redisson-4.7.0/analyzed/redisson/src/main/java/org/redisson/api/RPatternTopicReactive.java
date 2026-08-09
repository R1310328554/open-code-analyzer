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

import org.redisson.api.listener.PatternMessageListener;
import org.redisson.api.listener.PatternStatusListener;

import reactor.core.publisher.Mono;

/**
 * 基于模式的 Redis 发布/订阅 Topic Reactor API。
 * <p>各方法返回 {@link Mono}；{@link #getPatternNames()} 同步返回模式列表。
 *
 * @author Nikita Koksharov
 */
public interface RPatternTopicReactive {

    /**
     * 返回 Topic 订阅的模式（Pattern）列表
     *
     * @return 模式名称列表
     */
    List<String> getPatternNames();

    /**
     * 订阅该模式 Topic；任意匹配频道发布消息时触发 {@code MessageListener.onMessage}。
     * 
     * @param <T> type of message
     * @param type 消息类型
     * @param listener 消息监听器
     * @return 本地 JVM 唯一监听器 ID
     * @see org.redisson.api.listener.MessageListener
     */
    <T> Mono<Integer> addListener(Class<T> type, PatternMessageListener<T> listener);

    /**
     * 订阅该 Topic 的连接/订阅状态变化
     *
     * @param listener 消息监听器
     * @return 本地 JVM 唯一监听器 ID
     * @see org.redisson.api.listener.StatusListener
     */
    Mono<Integer> addListener(PatternStatusListener listener);

    /**
     * 按监听器 ID 移除 Topic 监听器
     *
     * @param listenerId 监听器 ID
     */
    Mono<Void> removeListener(int listenerId);


    /**
     * 返回当前匹配该模式的活跃频道列表
     * @return 活跃频道列表
     */
    Mono<List<String>> getActiveTopics();

}
