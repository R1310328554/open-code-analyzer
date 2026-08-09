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

import java.time.Duration;
import java.util.Map;

/**
 * 队列消息的流式参数配置接口。
 * <p>支持优先级、延迟投递、去重、TTL、投递次数限制及自定义 headers。
 *
 * @author Nikita Koksharov
 * @param <V> 载荷类型
 */
public interface MessageArgs<V> {

    /**
     * 设置消息优先级，取值 <code>0</code>～<code>9</code>。
     * <p>
     * <code>0</code> 为最低，<code>9</code> 为最高；默认 <code>0</code>。
     *
     * @param priority 优先级
     * @return 参数构建器
     */
    MessageArgs<V> priority(int priority);

    /**
     * 设置消息可消费前的延迟时长。
     * <p>
     * <code>0</code> 表示不延迟；未设置时使用队列级 delay，队列也未设置则默认为 <code>0</code>。
     *
     * @param interval 延迟时长
     * @return 参数构建器
     */
    MessageArgs<V> delay(Duration interval);

    /**
     * 在指定时间窗口内按载荷哈希去重。
     * <p>
     * 窗口内相同哈希的消息视为重复，不会入队。
     * </p>
     *
     * @param interval 去重时间窗口
     * @return 参数构建器
     */
    MessageArgs<V> deduplicationByHash(Duration interval);

    /**
     * 在指定时间窗口内按自定义 ID 去重。
     * <p>
     * 窗口内相同 ID 的消息视为重复，不会入队。
     * </p>
     *
     * @param id 自定义标识
     * @param interval 去重时间窗口
     * @return 参数构建器
     */
    MessageArgs<V> deduplicationById(Object id, Duration interval);

    /**
     * 设置消息存活时间（TTL）。
     * <p>
     * 超时且未被消费时从队列移除。
     * <p>
     * <code>0</code> 表示不过期；未设置时使用队列 timeToLive，队列也未设置则默认为 <code>0</code>。
     *
     * @param value 存活时长
     * @return 参数构建器
     */
    MessageArgs<V> timeToLive(Duration value);

    /**
     * 设置消息最大投递次数。
     * <p>
     * 处理失败时可重投，最多达到指定次数。
     * </p>
     * 最小值为 <code>1</code>；未设置时使用队列 deliveryLimit，队列也未设置则默认为 <code>10</code>。
     *
     * @param count 最大投递次数
     * @return 参数构建器
     */
    MessageArgs<V> deliveryLimit(int count);

    /**
     * 添加单条消息 header。
     *
     * @param key header 键
     * @param value header 值
     * @return 参数构建器
     */
    MessageArgs<V> header(String key, Object value);

    /**
     * 批量添加消息 headers。
     *
     * @param entries header 键值对映射
     * @return 参数构建器
     */
    MessageArgs<V> headers(Map<String, Object> entries);

    /**
     * 指定消息载荷并创建参数构建器。
     *
     * @param value 消息载荷
     * @return 参数构建器
     */
    static <V> MessageArgs<V> payload(V value) {
        return new MessageParams<V>(value);
    }

}
