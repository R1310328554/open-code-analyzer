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

import org.redisson.api.RFuture;

/**
 * 提供消息处理相关的异步确认操作。
 *
 * @author Nikita Koksharov
 *
 */
public interface AcknowledgmentAsync {

    /**
     * 异步确认消息已成功处理。
     *
     * @param args 确认参数
     */
    RFuture<Void> acknowledgeAsync(MessageAckArgs args);


    /**
     * 异步显式标记消息处理失败或被拒绝。
     *
     * @param args 指定待负向确认消息的参数
     */
    RFuture<Void> negativeAcknowledgeAsync(MessageNegativeAckArgs args);

}
