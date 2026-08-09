/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.remoting.protocol.admin;

/**
 * Topic 在单队列上的偏移范围：最小/最大逻辑偏移及最后更新时间。
 */
public class TopicOffset {
    /** 队列最小可消费偏移。 */
    private long minOffset;
    /** 队列最大已写入偏移（不含）。 */
    private long maxOffset;
    /** 偏移统计最后更新时间（毫秒）。 */
    private long lastUpdateTimestamp;

    /** 返回最小偏移。 */
    public long getMinOffset() {
        return minOffset;
    }

    /** 设置最小偏移。 */
    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }

    /** 返回最大偏移。 */
    public long getMaxOffset() {
        return maxOffset;
    }

    /** 设置最大偏移。 */
    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    /** 返回最后更新时间。 */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /** 设置最后更新时间。 */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /** 返回偏移范围的可读字符串。 */
    @Override
    public String toString() {
        return "TopicOffset{" +
                "minOffset=" + minOffset +
                ", maxOffset=" + maxOffset +
                ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                '}';
    }
}
