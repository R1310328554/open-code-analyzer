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

package org.apache.rocketmq.tools.monitor;

import org.apache.rocketmq.remoting.protocol.topic.OffsetMovedEvent;

/**
 * 消息被删除（偏移量迁移）事件载体。
 * <p>由 {@link MonitorService} 订阅 {@code RMQ_SYS_OFFSET_MOVED_EVENT} 后构造并上报。
 */
public class DeleteMsgsEvent {
    /** Broker 上报的偏移量迁移详情。 */
    private OffsetMovedEvent offsetMovedEvent;
    /** 事件对应消息的存储时间戳（毫秒）。 */
    private long eventTimestamp;

    /** @return 偏移量迁移事件体 */
    public OffsetMovedEvent getOffsetMovedEvent() {
        return offsetMovedEvent;
    }

    /** @param offsetMovedEvent 偏移量迁移事件体 */
    public void setOffsetMovedEvent(OffsetMovedEvent offsetMovedEvent) {
        this.offsetMovedEvent = offsetMovedEvent;
    }

    /** @return 事件时间戳（毫秒） */
    public long getEventTimestamp() {
        return eventTimestamp;
    }

    /** @param eventTimestamp 事件时间戳（毫秒） */
    public void setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    /** @return 便于日志输出的字符串表示 */
    @Override
    public String toString() {
        return "DeleteMsgsEvent [offsetMovedEvent=" + offsetMovedEvent + ", eventTimestamp=" + eventTimestamp
            + "]";
    }
}
