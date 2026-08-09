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

package org.apache.rocketmq.remoting.protocol.body;

import java.util.Date;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 单队列消费时间跨度：最早/最晚消息时间、消费时间及延迟。
 */
public class QueueTimeSpan {
    /** 目标 MessageQueue。 */
    private MessageQueue messageQueue;
    /** 队列中最早消息存储时间戳。 */
    private long minTimeStamp;
    /** 队列中最晚消息存储时间戳。 */
    private long maxTimeStamp;
    /** 当前消费进度对应时间戳。 */
    private long consumeTimeStamp;
    /** 消费延迟（毫秒）。 */
    private long delayTime;

    /** 返回 MessageQueue。 */
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public long getMinTimeStamp() {
        return minTimeStamp;
    }

    public void setMinTimeStamp(long minTimeStamp) {
        this.minTimeStamp = minTimeStamp;
    }

    public long getMaxTimeStamp() {
        return maxTimeStamp;
    }

    public void setMaxTimeStamp(long maxTimeStamp) {
        this.maxTimeStamp = maxTimeStamp;
    }

    public long getConsumeTimeStamp() {
        return consumeTimeStamp;
    }

    public void setConsumeTimeStamp(long consumeTimeStamp) {
        this.consumeTimeStamp = consumeTimeStamp;
    }

    /** 返回最早消息时间的可读字符串。 */
    public String getMinTimeStampStr() {
        return UtilAll.formatDate(new Date(minTimeStamp), UtilAll.YYYY_MM_DD_HH_MM_SS_SSS);
    }

    /** 返回最晚消息时间的可读字符串。 */
    public String getMaxTimeStampStr() {
        return UtilAll.formatDate(new Date(maxTimeStamp), UtilAll.YYYY_MM_DD_HH_MM_SS_SSS);
    }

    public String getConsumeTimeStampStr() {
        return UtilAll.formatDate(new Date(consumeTimeStamp), UtilAll.YYYY_MM_DD_HH_MM_SS_SSS);
    }

    /** 返回消费延迟毫秒数。 */
    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }
}
