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
package org.apache.rocketmq.store.index;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 索引文件头格式说明：
 * <pre>
 * ┌───────────────────────────────┬───────────────────────────────┬───────────────────────────────┬───────────────────────────────┬───────────────────┬───────────────────┐
 * │        Begin Timestamp        │          End Timestamp        │     Begin Physical Offset     │       End Physical Offset     │  Hash Slot Count  │    Index Count    │
 * │           (8 Bytes)           │            (8 Bytes)          │           (8 Bytes)           │           (8 Bytes)           │      (4 Bytes)    │      (4 Bytes)    │
 * ├───────────────────────────────┴───────────────────────────────┴───────────────────────────────┴───────────────────────────────┴───────────────────┴───────────────────┤
 * │                                                                      Index File Header                                                                                │
 * │
 * </pre>
 * 索引文件头大小：
 * 起始时间戳(8) + 结束时间戳(8) + 起始物理偏移(8) + 结束物理偏移(8) + 哈希槽数(4) + 索引数(4) = 40 字节
 */
/**
 * 索引文件头：维护时间戳、物理偏移及哈希槽与索引条目计数。
 */
public class IndexHeader {
    /** 索引文件头固定长度（字节）。 */
    public static final int INDEX_HEADER_SIZE = 40;
    private static int beginTimestampIndex = 0;
    private static int endTimestampIndex = 8;
    private static int beginPhyoffsetIndex = 16;
    private static int endPhyoffsetIndex = 24;
    private static int hashSlotcountIndex = 32;
    private static int indexCountIndex = 36;
    private final ByteBuffer byteBuffer;
    private final AtomicLong beginTimestamp = new AtomicLong(0);
    private final AtomicLong endTimestamp = new AtomicLong(0);
    private final AtomicLong beginPhyOffset = new AtomicLong(0);
    private final AtomicLong endPhyOffset = new AtomicLong(0);
    private final AtomicInteger hashSlotCount = new AtomicInteger(0);
    private final AtomicInteger indexCount = new AtomicInteger(1);

    /** 绑定 Mapped 缓冲区中的文件头区域。 */
    public IndexHeader(final ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    /** 从 ByteBuffer 加载各字段到原子变量。 */
    public void load() {
        this.beginTimestamp.set(byteBuffer.getLong(beginTimestampIndex));
        this.endTimestamp.set(byteBuffer.getLong(endTimestampIndex));
        this.beginPhyOffset.set(byteBuffer.getLong(beginPhyoffsetIndex));
        this.endPhyOffset.set(byteBuffer.getLong(endPhyoffsetIndex));

        this.hashSlotCount.set(byteBuffer.getInt(hashSlotcountIndex));
        this.indexCount.set(byteBuffer.getInt(indexCountIndex));

        if (this.indexCount.get() <= 0) {
            this.indexCount.set(1);
        }
    }

    /** 将内存中的字段写回 ByteBuffer。 */
    public void updateByteBuffer() {
        this.byteBuffer.putLong(beginTimestampIndex, this.beginTimestamp.get());
        this.byteBuffer.putLong(endTimestampIndex, this.endTimestamp.get());
        this.byteBuffer.putLong(beginPhyoffsetIndex, this.beginPhyOffset.get());
        this.byteBuffer.putLong(endPhyoffsetIndex, this.endPhyOffset.get());
        this.byteBuffer.putInt(hashSlotcountIndex, this.hashSlotCount.get());
        this.byteBuffer.putInt(indexCountIndex, this.indexCount.get());
    }

    /** 返回索引最早时间戳。 */
    public long getBeginTimestamp() {
        return beginTimestamp.get();
    }

    /** 设置索引最早时间戳并同步到缓冲区。 */
    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp.set(beginTimestamp);
        this.byteBuffer.putLong(beginTimestampIndex, beginTimestamp);
    }

    /** 返回索引最晚时间戳。 */
    public long getEndTimestamp() {
        return endTimestamp.get();
    }

    /** 设置索引最晚时间戳并同步到缓冲区。 */
    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp.set(endTimestamp);
        this.byteBuffer.putLong(endTimestampIndex, endTimestamp);
    }

    /** 返回索引最早物理偏移。 */
    public long getBeginPhyOffset() {
        return beginPhyOffset.get();
    }

    /** 设置索引最早物理偏移并同步到缓冲区。 */
    public void setBeginPhyOffset(long beginPhyOffset) {
        this.beginPhyOffset.set(beginPhyOffset);
        this.byteBuffer.putLong(beginPhyoffsetIndex, beginPhyOffset);
    }

    /** 返回索引最晚物理偏移。 */
    public long getEndPhyOffset() {
        return endPhyOffset.get();
    }

    /** 设置索引最晚物理偏移并同步到缓冲区。 */
    public void setEndPhyOffset(long endPhyOffset) {
        this.endPhyOffset.set(endPhyOffset);
        this.byteBuffer.putLong(endPhyoffsetIndex, endPhyOffset);
    }

    /** 返回已使用的哈希槽计数原子变量。 */
    public AtomicInteger getHashSlotCount() {
        return hashSlotCount;
    }

    /** 哈希槽计数加一并写回缓冲区。 */
    public void incHashSlotCount() {
        int value = this.hashSlotCount.incrementAndGet();
        this.byteBuffer.putInt(hashSlotcountIndex, value);
    }

    /** 返回当前索引条目数量。 */
    public int getIndexCount() {
        return indexCount.get();
    }

    /** 索引条目计数加一并写回缓冲区。 */
    public void incIndexCount() {
        int value = this.indexCount.incrementAndGet();
        this.byteBuffer.putInt(indexCountIndex, value);
    }
}
