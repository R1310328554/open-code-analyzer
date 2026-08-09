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
package org.apache.rocketmq.client.consumer;

import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 单次 pull 请求的返回体：包含拉取状态、队列 offset 边界及本次拉到的消息列表。
 */
public class PullResult {
    /** 拉取结果状态。 */
    private final PullStatus pullStatus;
    /** 下次 pull 建议起始 offset。 */
    private final long nextBeginOffset;
    /** 队列最小可用 offset。 */
    private final long minOffset;
    /** 队列最大可用 offset。 */
    private final long maxOffset;
    /** 本次拉取到的消息列表（可为空）。 */
    private List<MessageExt> msgFoundList;


    /** 构造 pull 结果。 */
    public PullResult(PullStatus pullStatus, long nextBeginOffset, long minOffset, long maxOffset,
        List<MessageExt> msgFoundList) {
        super();
        this.pullStatus = pullStatus;
        this.nextBeginOffset = nextBeginOffset;
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.msgFoundList = msgFoundList;
    }

    /** 返回拉取状态。 */
    public PullStatus getPullStatus() {
        return pullStatus;
    }

    /** 返回下次 pull 起始 offset。 */
    public long getNextBeginOffset() {
        return nextBeginOffset;
    }

    /** 返回队列最小 offset。 */
    public long getMinOffset() {
        return minOffset;
    }

    /** 返回队列最大 offset。 */
    public long getMaxOffset() {
        return maxOffset;
    }

    /** 返回本次拉取到的消息。 */
    public List<MessageExt> getMsgFoundList() {
        return msgFoundList;
    }

    /** 设置消息列表（内部或测试使用）。 */
    public void setMsgFoundList(List<MessageExt> msgFoundList) {
        this.msgFoundList = msgFoundList;
    }

    @Override
    public String toString() {
        return "PullResult [pullStatus=" + pullStatus + ", nextBeginOffset=" + nextBeginOffset
            + ", minOffset=" + minOffset + ", maxOffset=" + maxOffset + ", msgFoundList="
            + (msgFoundList == null ? 0 : msgFoundList.size()) + "]";
    }
}
