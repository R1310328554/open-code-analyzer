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
package org.apache.rocketmq.store;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拉取消息结果：封装状态、偏移区间、消息缓冲区列表及商业计费统计等。
 */
public class GetMessageResult {

    /** 映射缓冲区结果列表（含物理偏移）。 */
    private final List<SelectMappedBufferResult> messageMapedList;
    /** 消息 ByteBuffer 列表，供上层直接读取。 */
    private final List<ByteBuffer> messageBufferList;
    /** 各消息在 ConsumeQueue 中的逻辑 offset。 */
    private final List<Long> messageQueueOffset;

    /** 拉取结果状态码。 */
    private GetMessageStatus status;
    /** 建议下次拉起的起始 offset。 */
    private long nextBeginOffset;
    /** 队列最小可读 offset。 */
    private long minOffset;
    /** 队列最大可读 offset。 */
    private long maxOffset;

    /** 所有消息缓冲区总字节数。 */
    private int bufferTotalSize = 0;

    /** 本次拉取的消息条数。 */
    private int messageCount = 0;

    /** 是否建议从 Slave 拉取（主从延迟场景）。 */
    private boolean suggestPullingFromSlave = false;

    /** 商业版计费消息条数（按块折算）。 */
    private int msgCount4Commercial = 0;
    /** 商业版单条消息计费块大小（默认 4KB）。 */
    private int commercialSizePerMsg = 4 * 1024;

    /** 冷数据总字节数统计。 */
    private long coldDataSum = 0L;

    /** 被过滤掉的消息条数。 */
    private int filterMessageCount;

    /** 无匹配逻辑队列时的空结果常量。 */
    public static final GetMessageResult NO_MATCH_LOGIC_QUEUE =
        new GetMessageResult(GetMessageStatus.NO_MATCHED_LOGIC_QUEUE, 0, 0, 0, Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList());

    /** 默认构造，预分配容量 100。 */
    public GetMessageResult() {
        messageMapedList = new ArrayList<>(100);
        messageBufferList = new ArrayList<>(100);
        messageQueueOffset = new ArrayList<>(100);
    }

    /** 指定预分配容量的构造。 */
    public GetMessageResult(int resultSize) {
        messageMapedList = new ArrayList<>(resultSize);
        messageBufferList = new ArrayList<>(resultSize);
        messageQueueOffset = new ArrayList<>(resultSize);
    }

    private GetMessageResult(GetMessageStatus status, long nextBeginOffset, long minOffset, long maxOffset,
        List<SelectMappedBufferResult> messageMapedList, List<ByteBuffer> messageBufferList, List<Long> messageQueueOffset) {
        this.status = status;
        this.nextBeginOffset = nextBeginOffset;
        this.minOffset = minOffset;
        this.maxOffset = maxOffset;
        this.messageMapedList = messageMapedList;
        this.messageBufferList = messageBufferList;
        this.messageQueueOffset = messageQueueOffset;
    }

    /** 返回拉取状态。 */
    public GetMessageStatus getStatus() {
        return status;
    }

    /** 设置拉取状态。 */
    public void setStatus(GetMessageStatus status) {
        this.status = status;
    }

    public long getNextBeginOffset() {
        return nextBeginOffset;
    }

    public void setNextBeginOffset(long nextBeginOffset) {
        this.nextBeginOffset = nextBeginOffset;
    }

    public long getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public List<SelectMappedBufferResult> getMessageMapedList() {
        return messageMapedList;
    }

    public List<ByteBuffer> getMessageBufferList() {
        return messageBufferList;
    }

    /** 追加一条消息（不含队列 offset）。 */
    public void addMessage(final SelectMappedBufferResult mapedBuffer) {
        this.messageMapedList.add(mapedBuffer);
        this.messageBufferList.add(mapedBuffer.getByteBuffer());
        this.bufferTotalSize += mapedBuffer.getSize();
        this.msgCount4Commercial += (int) Math.ceil(
            mapedBuffer.getSize() /  (double)commercialSizePerMsg);
        this.messageCount++;
    }

    /** 追加一条消息并记录 ConsumeQueue offset。 */
    public void addMessage(final SelectMappedBufferResult mapedBuffer, final long queueOffset) {
        this.messageMapedList.add(mapedBuffer);
        this.messageBufferList.add(mapedBuffer.getByteBuffer());
        this.bufferTotalSize += mapedBuffer.getSize();
        this.msgCount4Commercial += (int) Math.ceil(
            mapedBuffer.getSize() /  (double)commercialSizePerMsg);
        this.messageCount++;
        this.messageQueueOffset.add(queueOffset);
    }


    /** 追加消息并按 batchNum 调整 messageCount（批量消息）。 */
    public void addMessage(final SelectMappedBufferResult mapedBuffer, final long queueOffset, final int batchNum) {
        addMessage(mapedBuffer, queueOffset);
        messageCount += batchNum - 1;
    }

    /** 释放所有映射缓冲区引用。 */
    public void release() {
        for (SelectMappedBufferResult select : this.messageMapedList) {
            select.release();
        }
    }

    public int getBufferTotalSize() {
        return bufferTotalSize;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public boolean isSuggestPullingFromSlave() {
        return suggestPullingFromSlave;
    }

    public void setSuggestPullingFromSlave(boolean suggestPullingFromSlave) {
        this.suggestPullingFromSlave = suggestPullingFromSlave;
    }

    public int getMsgCount4Commercial() {
        return msgCount4Commercial;
    }

    public void setMsgCount4Commercial(int msgCount4Commercial) {
        this.msgCount4Commercial = msgCount4Commercial;
    }

    public List<Long> getMessageQueueOffset() {
        return messageQueueOffset;
    }

    public long getColdDataSum() {
        return coldDataSum;
    }

    public void setColdDataSum(long coldDataSum) {
        this.coldDataSum = coldDataSum;
    }

    public int getFilterMessageCount() {
        return filterMessageCount;
    }

    public void setFilterMessageCount(int filterMessageCount) {
        this.filterMessageCount = filterMessageCount;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "GetMessageResult [status=" + status + ", nextBeginOffset=" + nextBeginOffset + ", minOffset="
            + minOffset + ", maxOffset=" + maxOffset + ", bufferTotalSize=" + bufferTotalSize + ", messageCount=" + messageCount
            + ", filterMessageCount=" + filterMessageCount + ", suggestPullingFromSlave=" + suggestPullingFromSlave + "]";
    }
}
