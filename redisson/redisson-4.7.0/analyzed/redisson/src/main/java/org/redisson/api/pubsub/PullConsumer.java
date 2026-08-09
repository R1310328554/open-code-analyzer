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

import java.util.List;

/**
 * 按需从订阅拉取消息的拉模式消费者。
 * <p>
 * 拉模式消费者由应用主动控制消费节奏，在准备好处理时再请求消息。
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
public interface PullConsumer<V> extends PullConsumerAsync<V>, Acknowledgment {

    /**
     * 拉取并移除订阅队首消息；若订阅为空则返回 {@code null}。
     * <p>
     * 消息在调用 {@link #acknowledge(MessageAckArgs)} 或
     * {@link #negativeAcknowledge(MessageNegativeAckArgs)} 前保持未确认状态。
     *
     * @return 队首消息，或订阅为空时的 {@code null}
     * @throws OperationDisabledException 若此操作被禁用
     */
    Message<V> pull();

    /**
     * 使用指定拉取参数拉取并移除订阅队首消息。
     * <p>
     * 消息在显式确认前保持未确认状态。
     *
     * @param args 拉取参数
     * @return 队首消息，或订阅为空时的 {@code null}
     * @throws OperationDisabledException 若此操作被禁用
     */
    Message<V> pull(PullArgs args);

    /**
     * 使用指定参数批量拉取并移除多条消息。
     * <p>
     * 批量拉取比逐条拉取更高效。
     * <p>
     * 拉取到的消息须显式确认或否定确认。
     *
     * @param pargs 拉取参数
     * @return 已拉取的消息列表
     * @throws OperationDisabledException 若此操作被禁用
     */
    List<Message<V>> pullMany(PullArgs pargs);

}
