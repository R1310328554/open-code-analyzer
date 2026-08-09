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

import java.util.function.Supplier;

/**
 * 向 CommitLog 追加消息后的写入结果封装。
 */
public class AppendMessageResult {
    /** 写入状态码。 */
    private AppendMessageStatus status;
    /** 本次写入在文件中的起始物理偏移。 */
    private long wroteOffset;
    /** 本次写入字节数。 */
    private int wroteBytes;
    /** 消息 ID（可能延迟由 supplier 生成）。 */
    private String msgId;
    /** 延迟生成 msgId 的供应器。 */
    private Supplier<String> msgIdSupplier;
    /** 消息存储时间戳。 */
    private long storeTimestamp;
    /** 对应 ConsumeQueue 逻辑偏移（逐条递增）。 */
    private long logicsOffset;
    /** 页缓存相关耗时（纳秒或毫秒，取决于调用方）。 */
    private long pagecacheRT = 0;

    /** 本次写入包含的消息条数（批量时为多条）。 */
    private int msgNum = 1;

    public AppendMessageResult(AppendMessageStatus status) {
        this(status, 0, 0, "", 0, 0, 0);
    }

    public AppendMessageResult(AppendMessageStatus status, long wroteOffset, int wroteBytes, String msgId,
        long storeTimestamp, long logicsOffset, long pagecacheRT) {
        this.status = status;
        this.wroteOffset = wroteOffset;
        this.wroteBytes = wroteBytes;
        this.msgId = msgId;
        this.storeTimestamp = storeTimestamp;
        this.logicsOffset = logicsOffset;
        this.pagecacheRT = pagecacheRT;
    }

    public AppendMessageResult(AppendMessageStatus status, long wroteOffset, int wroteBytes, long storeTimestamp) {
        this.status = status;
        this.wroteOffset = wroteOffset;
        this.wroteBytes = wroteBytes;
        this.storeTimestamp = storeTimestamp;
    }

    public AppendMessageResult(AppendMessageStatus status, long wroteOffset, int wroteBytes, Supplier<String> msgIdSupplier,
            long storeTimestamp, long logicsOffset, long pagecacheRT) {
        this.status = status;
        this.wroteOffset = wroteOffset;
        this.wroteBytes = wroteBytes;
        this.msgIdSupplier = msgIdSupplier;
        this.storeTimestamp = storeTimestamp;
        this.logicsOffset = logicsOffset;
        this.pagecacheRT = pagecacheRT;
    }

    public AppendMessageResult(AppendMessageStatus status, long wroteOffset, int wroteBytes, Supplier<String> msgIdSupplier,
            long storeTimestamp, long logicsOffset, long pagecacheRT, int msgNum) {
        this.status = status;
        this.wroteOffset = wroteOffset;
        this.wroteBytes = wroteBytes;
        this.msgIdSupplier = msgIdSupplier;
        this.storeTimestamp = storeTimestamp;
        this.logicsOffset = logicsOffset;
        this.pagecacheRT = pagecacheRT;
        this.msgNum = msgNum;
    }

    public long getPagecacheRT() {
        return pagecacheRT;
    }

    public void setPagecacheRT(final long pagecacheRT) {
        this.pagecacheRT = pagecacheRT;
    }

    /** 是否写入成功（status 为 PUT_OK）。 */
    public boolean isOk() {
        return this.status == AppendMessageStatus.PUT_OK;
    }

    public AppendMessageStatus getStatus() {
        return status;
    }

    public void setStatus(AppendMessageStatus status) {
        this.status = status;
    }

    public long getWroteOffset() {
        return wroteOffset;
    }

    public void setWroteOffset(long wroteOffset) {
        this.wroteOffset = wroteOffset;
    }

    public int getWroteBytes() {
        return wroteBytes;
    }

    public void setWroteBytes(int wroteBytes) {
        this.wroteBytes = wroteBytes;
    }

    public String getMsgId() {
        if (msgId == null && msgIdSupplier != null) {
            msgId = msgIdSupplier.get();
        }
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public long getStoreTimestamp() {
        return storeTimestamp;
    }

    public void setStoreTimestamp(long storeTimestamp) {
        this.storeTimestamp = storeTimestamp;
    }

    public long getLogicsOffset() {
        return logicsOffset;
    }

    public void setLogicsOffset(long logicsOffset) {
        this.logicsOffset = logicsOffset;
    }

    public int getMsgNum() {
        return msgNum;
    }

    public void setMsgNum(int msgNum) {
        this.msgNum = msgNum;
    }

    @Override
    public String toString() {
        return "AppendMessageResult{" +
            "status=" + status +
            ", wroteOffset=" + wroteOffset +
            ", wroteBytes=" + wroteBytes +
            ", msgId='" + msgId + '\'' +
            ", storeTimestamp=" + storeTimestamp +
            ", logicsOffset=" + logicsOffset +
            ", pagecacheRT=" + pagecacheRT +
            ", msgNum=" + msgNum +
            '}';
    }
}
