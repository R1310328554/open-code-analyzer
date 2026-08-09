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
package org.redisson.api.pubsub;

import java.time.Duration;

/**
 * 定义可靠主题订阅的消费者参数。
 *
 * @author Nikita Koksharov
 *
 */
public interface ConsumerConfig {

    /**
     * 创建使用自动生成名称的消费者配置。
     *
     * @return 消费者配置
     */
    static ConsumerConfig generatedName() {
        return new ConsumerConfigParams(null);
    }

    /**
     * 创建使用指定名称的消费者配置。
     *
     * @param value 消费者名称
     * @return 消费者配置
     */
    static ConsumerConfig name(String value) {
        return new ConsumerConfigParams(value);
    }

    /**
     * 定义将消息组 ID 的所有权重新分配给新消费者的超时时间。
     * <p>
     * 具有相同 {@code groupId} 设置的消息会交付给单一“所有者”消费者。
     * 当<b>同时</b>满足以下条件时，所有权会重新分配给新消费者：
     * <ol>
     *   <li>当前所有者在此超时时间内未拉取该组 ID 的下一条待处理消息
     *       （即消息卡在该键序列的队首）。</li>
     *   <li>当前所有者在此超时时间内处于非活跃状态（未调用 {@code acknowledge()}、
     *       {@code negativeAcknowledge()}、{@code pull()} 或推送监听器）。</li>
     * </ol>
     * <p>
     * 这可防止停滞或缓慢的消费者无限期阻塞某组 ID 的消息投递，
     * 同时仍尊重正在处理其他消息的活动消费者。
     * <p>
     * 注意：{@code visibilityTimeout} 仍适用于带组 ID 的各条消息。
     * 此超时控制键级所有权，而非消息级可见性。
     *
     * @param value 超时时长
     * @return 配置对象
     */
    ConsumerConfig groupIdClaimTimeout(Duration value);

}
