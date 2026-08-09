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
 * 提供消息处理相关的 Reactive 确认操作。
 *
 * @author Nikita Koksharov
 *
 */
public interface AcknowledgmentReactive {

    /**
     * 确认消息已成功处理。
     *
     * @param args 确认参数
     */
    Mono<Void> acknowledge(MessageAckArgs args);


    /**
     * 显式标记消息处理失败或被拒绝。
     *
     * @param args 指定待负向确认消息的参数
     */
    Mono<Void> negativeAcknowledge(MessageNegativeAckArgs args);

}
