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
package org.redisson.api.stream;

/**
 * {@link org.redisson.api.RStream#ack} 方法的参数对象。
 * <p>
 * 用于指定消费者组及待确认的消息 ID。
 *
 * @author seakider
 *
 */
public interface StreamAckArgs extends StreamReferencesArgs<StreamAckArgs> {

    /**
     * 指定要确认（并可选择性删除）的消息所属消费者组。
     *
     * @param groupName 消费者组名称
     * @return 参数对象
     */
    static StreamMessageIdArgs<StreamAckArgs> group(String groupName) {
        return new StreamAckParams(groupName);
    }
}
