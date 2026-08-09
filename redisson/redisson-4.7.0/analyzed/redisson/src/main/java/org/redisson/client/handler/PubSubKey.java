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
package org.redisson.client.handler;

import org.redisson.client.ChannelName;

import java.util.Objects;

/**
 * Pub/Sub 命令索引键：频道名与操作类型（subscribe/message 等）的组合。
 * <p>
 * 用作 {@link CommandPubSubDecoder} 中 pending 命令的 Map 键。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubKey {

    /** 频道名称。 */
    private final ChannelName channel;
    /** 操作类型字符串（小写）。 */
    private final String operation;
    
    /**
     * @param channel 频道
     * @param operation 操作类型
     */
    public PubSubKey(ChannelName channel, String operation) {
        super();
        this.channel = channel;
        this.operation = operation;
    }
    
    /** 返回频道名。 */
    public ChannelName getChannel() {
        return channel;
    }
    
    /** 返回操作类型。 */
    public String getOperation() {
        return operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PubSubKey pubSubKey = (PubSubKey) o;
        return Objects.equals(channel, pubSubKey.channel) && Objects.equals(operation, pubSubKey.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, operation);
    }
}
