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

import org.redisson.api.Message;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 基于 Project Reactor 的拉模式消费者，按需从订阅拉取消息。
 * <p>
 * 通过 {@link Mono} 在应用就绪时发起拉取，实现手动控制消费节奏。
 * <p>
 * 拉取到的消息须通过 {@link #acknowledge(MessageAckArgs)}
 * 或 {@link #negativeAcknowledge(MessageNegativeAckArgs)} 显式确认；
 * 未确认的消息在可见性超时后将自动重新投递。
 *
 * @param <V> 消息值类型
 *
 * @author Nikita Koksharov
 *
 */
public interface PullConsumerReactive<V> extends AcknowledgmentReactive, ConsumerReactive {

    /**
     * 拉取并移除订阅队首消息；若订阅为空则发出空 {@link Mono}。
     * <p>
     * 消息在显式确认前保持未确认状态。
     *
     * @return 队首消息的 {@link Mono}
     * @throws OperationDisabledException 若此操作被禁用
     */
    Mono<Message<V>> pull();

    /**
     * 使用指定拉取参数拉取并移除队首消息。
     *
     * @param args 拉取参数
     * @return 队首消息的 {@link Mono}
     * @throws OperationDisabledException 若此操作被禁用
     */
    Mono<Message<V>> pull(PullArgs args);

    /**
     * 使用指定参数批量拉取多条消息。
     * <p>
     * 批量拉取比逐条拉取更高效；拉取到的消息须显式确认。
     *
     * @param pargs 拉取参数
     * @return 消息列表的 {@link Mono}
     * @throws OperationDisabledException 若此操作被禁用
     */
    Mono<List<Message<V>>> pullMany(PullArgs pargs);

}
