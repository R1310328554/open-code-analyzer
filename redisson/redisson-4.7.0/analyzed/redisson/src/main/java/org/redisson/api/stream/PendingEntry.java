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
 * 待处理（pending）消息列表中的单条记录。
 * <p>
 * 描述消息 ID、所属消费者、空闲时长及投递次数等元数据。
 * 
 * @author Nikita Koksharov
 *
 */
public class PendingEntry {
    
    /** 消息 ID。 */
    private StreamMessageId id;
    /** 当前持有该消息的消费者名称。 */
    private String consumerName;
    /** 自上次投递以来经过的毫秒数。 */
    private long idleTime;
    /** 消息被投递的总次数。 */
    private long deliveryCount;
    
    public PendingEntry(StreamMessageId id, String consumerName, long idleTime, long lastTimeDelivered) {
        super();
        this.id = id;
        this.consumerName = consumerName;
        this.idleTime = idleTime;
        this.deliveryCount = lastTimeDelivered;
    }
    
    /**
     * 返回消息在流中的 ID。
     * 
     * @return 消息 ID
     */
    public StreamMessageId getId() {
        return id;
    }

    /**
     * 返回当前持有该消息的消费者名称。
     * 
     * @return 消费者名称
     */
    public String getConsumerName() {
        return consumerName;
    }

    /**
     * 返回自上次投递给消费者以来经过的毫秒数。
     * 
     * @return 空闲时长（毫秒）
     */
    public long getIdleTime() {
        return idleTime;
    }

    /**
     * 返回该消息被投递的总次数。
     * 
     * @return 投递次数
     */
    public long getDeliveryCount() {
        return deliveryCount;
    }

    @Override
    public String toString() {
        return "PendingEntry{" +
                "id=" + id +
                ", consumerName='" + consumerName + '\'' +
                ", idleTime=" + idleTime +
                ", lastTimeDelivered=" + deliveryCount +
                '}';
    }
}
