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
import java.util.List;

/**
 * 按索引查询消息的结果容器：聚合多条 SelectMappedBufferResult 及索引元数据。
 */
public class QueryMessageResult {

    /** 查询命中的 Mapped 缓冲区结果列表。 */
    private final List<SelectMappedBufferResult> messageMapedList =
        new ArrayList<>(100);

    /** 对应消息内容的 ByteBuffer 列表。 */
    private final List<ByteBuffer> messageBufferList = new ArrayList<>(100);
    /** 索引最后更新时间戳。 */
    private long indexLastUpdateTimestamp;
    /** 索引最后更新时的物理偏移量。 */
    private long indexLastUpdatePhyoffset;

    /** 所有消息缓冲区的总字节数。 */
    private int bufferTotalSize = 0;

    /** 追加一条查询结果并累加总大小。 */
    public void addMessage(final SelectMappedBufferResult mapedBuffer) {
        this.messageMapedList.add(mapedBuffer);
        this.messageBufferList.add(mapedBuffer.getByteBuffer());
        this.bufferTotalSize += mapedBuffer.getSize();
    }

    /** 释放所有 Mapped 缓冲区引用。 */
    public void release() {
        for (SelectMappedBufferResult select : this.messageMapedList) {
            select.release();
        }
    }

    /** 返回索引最后更新时间戳。 */
    public long getIndexLastUpdateTimestamp() {
        return indexLastUpdateTimestamp;
    }

    /** 设置索引最后更新时间戳。 */
    public void setIndexLastUpdateTimestamp(long indexLastUpdateTimestamp) {
        this.indexLastUpdateTimestamp = indexLastUpdateTimestamp;
    }

    /** 返回索引最后更新的物理偏移量。 */
    public long getIndexLastUpdatePhyoffset() {
        return indexLastUpdatePhyoffset;
    }

    /** 设置索引最后更新的物理偏移量。 */
    public void setIndexLastUpdatePhyoffset(long indexLastUpdatePhyoffset) {
        this.indexLastUpdatePhyoffset = indexLastUpdatePhyoffset;
    }

    /** 返回消息 ByteBuffer 列表。 */
    public List<ByteBuffer> getMessageBufferList() {
        return messageBufferList;
    }

    /** 返回缓冲区总字节数。 */
    public int getBufferTotalSize() {
        return bufferTotalSize;
    }

    /** 返回 Mapped 缓冲区结果列表。 */
    public List<SelectMappedBufferResult> getMessageMapedList() {
        return messageMapedList;
    }
}
