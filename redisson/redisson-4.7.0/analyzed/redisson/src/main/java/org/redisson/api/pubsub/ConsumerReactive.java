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

import reactor.core.publisher.Mono;

/**
 * 订阅内消息消费者的基础接口（Project Reactor 响应式风格）。
 * <p>
 * 消费者负责处理订阅中的消息，可为按需拉取的 {@link PullConsumer}，
 * 或事件驱动的 {@link PushConsumer}。
 *
 * @author Nikita Koksharov
 *
 */
public interface ConsumerReactive {

    /**
     * 返回此消费者的名称。
     *
     * @return 消费者名称
     */
    String getName();

    /**
     * 返回此消费者的统计信息。
     *
     * @return 统计信息对象
     */
    Mono<ConsumerStatistics> getStatistics();

}
