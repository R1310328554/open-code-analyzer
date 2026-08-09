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
package org.apache.rocketmq.client.impl.consumer;

import java.util.List;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * Pull 结果扩展：在 {@link PullResult} 基础上携带 Broker 建议节点、原始二进制及 offset 增量。
 */
public class PullResultExt extends PullResult {
    /** Broker 建议下次从哪个 brokerId 拉取。 */
    private final long suggestWhichBrokerId;
    /** 原始消息二进制（解码前）。 */
    private byte[] messageBinary;

    /** 队列 offset 增量，用于修正消息 queueOffset。 */
    private final Long offsetDelta;

    /** 构造扩展 pull 结果（offsetDelta 默认为 0）。 */
    public PullResultExt(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,
        List<MessageExt> msgFoundList, final long suggestWhichBrokerId, final byte[] messageBinary) {
        this(pullStatus, nextBeginOffset, minOffset, maxOffset, msgFoundList, suggestWhichBrokerId, messageBinary, 0L);
    }

    /** 构造扩展 pull 结果，含完整 offsetDelta。 */
    public PullResultExt(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,
                         List<MessageExt> msgFoundList, final long suggestWhichBrokerId, final byte[] messageBinary, final Long offsetDelta) {
        super(pullStatus, nextBeginOffset, minOffset, maxOffset, msgFoundList);
        this.suggestWhichBrokerId = suggestWhichBrokerId;
        this.messageBinary = messageBinary;
        this.offsetDelta = offsetDelta;
    }

    /** 返回 offset 增量。 */
    public Long getOffsetDelta() {
        return offsetDelta;
    }

    /** 返回原始消息二进制。 */
    public byte[] getMessageBinary() {
        return messageBinary;
    }

    /** 设置原始消息二进制。 */
    public void setMessageBinary(byte[] messageBinary) {
        this.messageBinary = messageBinary;
    }

    /** 返回建议拉取的 Broker ID。 */
    public long getSuggestWhichBrokerId() {
        return suggestWhichBrokerId;
    }
}
