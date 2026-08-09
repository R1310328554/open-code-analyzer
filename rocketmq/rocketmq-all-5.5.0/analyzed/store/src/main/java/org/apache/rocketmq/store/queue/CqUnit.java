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

package org.apache.rocketmq.store.queue;

import org.apache.rocketmq.store.ConsumeQueueExt;

import java.nio.ByteBuffer;

/**
 * 消费队列单元：映射逻辑偏移到 CommitLog 物理位置与标签码。
 */
public class CqUnit {
    /** 队列逻辑偏移。 */
    private final long queueOffset;
    /** CommitLog 中消息体字节长度。 */
    private final int size;
    /** 消息在 CommitLog 中的物理偏移。 */
    private final long pos;
    /** 批次内消息条数。 */
    private final short batchNum;
    /**
     * 注意：tagsCode 同时用作扩展文件地址。规则：1) cqExtUnit 非空时 tagsCode 须与其一致；
     * 2) cqExtUnit 为空且 tagsCode &lt; 0 表示无效地址、扩展单元读取失败。
     */
    /** 标签哈希码或扩展文件地址。 */
    private long tagsCode;
    /** 关联的扩展单元（大 tags 等场景）。 */
    private ConsumeQueueExt.CqExtUnit cqExtUnit;
    /** 指向底层映射缓冲区的引用（用于就地修正）。 */
    private final ByteBuffer nativeBuffer;
    /** 压缩偏移在 nativeBuffer 中的位置。 */
    private final int compactedOffset;

    /** 构造单条消息的 CqUnit（batchNum 默认为 1）。 */
    public CqUnit(long queueOffset, long pos, int size, long tagsCode) {
        this(queueOffset, pos, size, tagsCode, (short) 1, 0, null);
    }

    public CqUnit(long queueOffset, long pos, int size, long tagsCode, short batchNum, int compactedOffset, ByteBuffer buffer) {
        this.queueOffset = queueOffset;
        this.pos = pos;
        this.size = size;
        this.tagsCode = tagsCode;
        this.batchNum = batchNum;

        this.nativeBuffer = buffer;
        this.compactedOffset = compactedOffset;
    }

    public int getSize() {
        return size;
    }

    public long getPos() {
        return pos;
    }

    public long getTagsCode() {
        return tagsCode;
    }

    /** 返回有效 tagsCode；扩展地址或无效值时返回 null。 */
    public Long getValidTagsCodeAsLong() {
        if (!isTagsCodeValid()) {
            return null;
        }
        return tagsCode;
    }

    /** 判断 tagsCode 是否为有效标签码（非扩展地址）。 */
    public boolean isTagsCodeValid() {
        return !ConsumeQueueExt.isExtAddr(tagsCode);
    }

    public ConsumeQueueExt.CqExtUnit getCqExtUnit() {
        return cqExtUnit;
    }

    public void setCqExtUnit(ConsumeQueueExt.CqExtUnit cqExtUnit) {
        this.cqExtUnit = cqExtUnit;
    }

    public void setTagsCode(long tagsCode) {
        this.tagsCode = tagsCode;
    }

    public long getQueueOffset() {
        return queueOffset;
    }

    public short getBatchNum() {
        return batchNum;
    }

    /** 在 nativeBuffer 中修正压缩偏移字段。 */
    public void correctCompactOffset(int correctedOffset) {
        this.nativeBuffer.putInt(correctedOffset);
    }

    public int getCompactedOffset() {
        return compactedOffset;
    }

    /** 返回包含各字段的可读字符串。 */
    @Override
    public String toString() {
        return "CqUnit{" +
                "queueOffset=" + queueOffset +
                ", size=" + size +
                ", pos=" + pos +
                ", batchNum=" + batchNum +
                ", tagsCode=" + tagsCode +
                ", compactedOffset=" + compactedOffset +
                '}';
    }
}
