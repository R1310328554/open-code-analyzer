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

import java.io.Serializable;
import java.util.Map;

/**
 * 消费者组待处理（pending）消息查询的汇总结果。
 * <p>
 * 包含待处理消息总数、ID 范围及各消费者的待处理计数。
 * 
 * @author Nikita Koksharov
 *
 */
public class PendingResult implements Serializable {

    private static final long serialVersionUID = -5525031552305408248L;
    
    /** 待处理消息总数。 */
    private long total;
    /** 最小待处理消息 ID。 */
    private StreamMessageId lowestId;
    /** 最大待处理消息 ID。 */
    private StreamMessageId highestId;
    /** 各消费者名称对应的待处理消息数量。 */
    private Map<String, Long> consumerNames;
    
    public PendingResult() {
    }
    
    public PendingResult(long total, StreamMessageId lowestId, StreamMessageId highestId, Map<String, Long> consumerNames) {
        super();
        this.total = total;
        this.lowestId = lowestId;
        this.highestId = highestId;
        this.consumerNames = consumerNames;
    }

    /**
     * 返回待处理消息的总数。
     * 
     * @return 消息总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 返回待处理消息中的最小 ID。
     * 
     * @return 最小消息 ID
     */
    public StreamMessageId getLowestId() {
        return lowestId;
    }

    /**
     * 返回待处理消息中的最大 ID。
     * 
     * @return 最大消息 ID
     */
    public StreamMessageId getHighestId() {
        return highestId;
    }

    /**
     * 返回按消费者名称分组的待处理消息数量映射。
     * 
     * @return 消费者名称到数量的映射
     */
    public Map<String, Long> getConsumerNames() {
        return consumerNames;
    }
    
}
