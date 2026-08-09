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
 * 流消费者组详情对象。
 * <p>
 * 包含组名称、消费者数、待处理消息数及最后投递 ID 等统计信息。
 * 
 * @author Nikita Koksharov
 *
 */
public final class StreamGroup {

    /** 消费者组名称。 */
    private final String name;
    /** 组内消费者数量。 */
    private final int consumers;
    /** 待处理消息数量。 */
    private final int pending;
    /** 最后投递的消息 ID。 */
    private final StreamMessageId lastDeliveredId;
    /** 组已读取的条目数。 */
    private final int entriesRead;
    /** 仍待投递的条目数（滞后量）。 */
    private final int lag;
    
    public StreamGroup(String name, int consumers, int pending, StreamMessageId lastDeliveredId) {
        this(name, consumers, pending, lastDeliveredId, 0, 0);
    }

    public StreamGroup(String name, int consumers, int pending, StreamMessageId lastDeliveredId, int entriesRead, int lag) {
        this.name = name;
        this.consumers = consumers;
        this.pending = pending;
        this.lastDeliveredId = lastDeliveredId;
        this.entriesRead = entriesRead;
        this.lag = lag;
    }

    /**
     * 返回该组最后投递的消息 ID。
     * 
     * @return 消息 ID 对象
     */
    public StreamMessageId getLastDeliveredId() {
        return lastDeliveredId;
    }
    
    /**
     * 返回该组当前消费者数量。
     * 
     * @return 消费者数量
     */
    public int getConsumers() {
        return consumers;
    }
    
    /**
     * 返回消费者组名称。
     * 
     * @return 组名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 返回该组的待处理消息数量。
     * 
     * @return 待处理消息数
     */
    public int getPending() {
        return pending;
    }

    /**
     * 返回该组已读取的条目数量。
     *
     * @return 已读条目数
     */
    public int getEntriesRead() {
        return entriesRead;
    }

    /**
     * 返回仍待投递的条目数量（滞后量）。
     *
     * @return 待投递条目数
     */
    public int getLag() {
        return lag;
    }

    @Override
    public String toString() {
        return "StreamGroup{" +
                "name='" + name + '\'' +
                ", consumers=" + consumers +
                ", pending=" + pending +
                ", lastDeliveredId=" + lastDeliveredId +
                ", entriesRead=" + entriesRead +
                ", lag=" + lag +
                '}';
    }
}
