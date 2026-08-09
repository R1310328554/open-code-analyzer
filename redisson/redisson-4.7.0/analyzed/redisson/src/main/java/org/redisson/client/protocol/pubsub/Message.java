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
package org.redisson.client.protocol.pubsub;

import org.redisson.client.ChannelName;

/**
 * Pub/Sub 消息通用接口。
 * <p>
 * 所有订阅推送类型的公共父接口，提供消息所属
 * {@link ChannelName}（频道或模式）的访问能力。
 *
 * @author Nikita Koksharov
 *
 */
public interface Message {

    /** 返回此消息关联的 Redis 频道（或模式）名称。 */
    ChannelName getChannel();
    
}
