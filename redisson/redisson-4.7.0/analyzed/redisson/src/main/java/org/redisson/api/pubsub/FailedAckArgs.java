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
 * 定义订阅消息处理失败时的负向确认（nack）参数。
 *
 * @author Nikita Koksharov
 *
 */
public interface FailedAckArgs extends MessageNegativeAckArgs {

    /**
     * 指定处理失败的消息在重新投递前需等待的延迟时长。
     *
     * @param value 重新投递前的延迟时长
     * @return 参数对象
     */
    MessageNegativeAckArgs delay(Duration value);

}
