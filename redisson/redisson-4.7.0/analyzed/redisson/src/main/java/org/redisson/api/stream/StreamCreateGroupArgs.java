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
 * {@link org.redisson.api.RStream#createGroup(StreamCreateGroupArgs)} 方法的参数对象。
 * <p>
 * 用于配置消费者组名称、起始消息 ID 及是否自动创建流等选项。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamCreateGroupArgs {

    /**
     * 设置 entries_read 参数。
     *
     * @param amount entries_read 值
     * @return 参数对象
     */
    StreamCreateGroupArgs entriesRead(int amount);

    /**
     * 若流不存在则自动创建。
     *
     * @return 参数对象
     */
    StreamCreateGroupArgs makeStream();

    /**
     * 设置流消息 ID。
     * <p>
     * 仅该 ID 之后的新消息对该组消费者可见。
     * <p>
     * {@link StreamMessageId#NEWEST} 表示自创建组时起的新消息
     * <p>
     * {@link StreamMessageId#ALL} 表示创建组前后所有消息
     *
     * @param id 流消息 ID
     * @return 参数对象
     */
    StreamCreateGroupArgs id(StreamMessageId id);

    /**
     * 定义消费者组名称。
     * <p>
     * 仅新消息对该组消费者可见。
     *
     * @param value 组名称
     * @return 参数对象
     */
    static StreamCreateGroupArgs name(String value) {
        return new StreamCreateGroupParams(value);
    }

}
