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

package org.apache.rocketmq.tieredstore.index;

import java.nio.ByteBuffer;

/**
 * 索引项：32 字节（紧凑模式 28 字节）固定布局，含 hash、topicId、queueId、offset 等。
 */
public class IndexItem {

    /** 标准索引项字节长度。 */
    public static final int INDEX_ITEM_SIZE = 32;
    /** 紧凑索引项字节长度。 */
    public static final int COMPACT_INDEX_ITEM_SIZE = 28;

    /** 键哈希码。 */
    private final int hashCode;
    /** Topic 内部 ID。 */
    private final int topicId;
    /** 队列 ID。 */
    private final int queueId;
    /** CommitLog 物理偏移。 */
    private final long offset;
    /** 消息体大小。 */
    private final int size;
    /** 相对文件基准的时间差。 */
    private final int timeDiff;
    /** 在索引文件中的序号。 */
    private final int itemIndex;

    /** 全字段构造索引项。 */
    public IndexItem(int topicId, int queueId, long offset, int size, int hashCode, int timeDiff, int itemIndex) {
        this.hashCode = hashCode;
        this.topicId = topicId;
        this.queueId = queueId;
        this.offset = offset;
        this.size = size;
        this.timeDiff = timeDiff;
        this.itemIndex = itemIndex;
    }

    /** 从 32 或 28 字节数组反序列化。 */
    public IndexItem(byte[] bytes) {
        if (bytes == null ||
            bytes.length != INDEX_ITEM_SIZE &&
                bytes.length != COMPACT_INDEX_ITEM_SIZE) {
            throw new IllegalArgumentException("Byte array length not correct");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        hashCode = byteBuffer.getInt(0);
        topicId = byteBuffer.getInt(4);
        queueId = byteBuffer.getInt(8);
        offset = byteBuffer.getLong(12);
        size = byteBuffer.getInt(20);
        timeDiff = byteBuffer.getInt(24);
        itemIndex = bytes.length == INDEX_ITEM_SIZE ? byteBuffer.getInt(28) : 0;
    }

    /** 序列化为 32 字节 ByteBuffer。 */
    public ByteBuffer getByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.putInt(0, hashCode);
        byteBuffer.putInt(4, topicId);
        byteBuffer.putInt(8, queueId);
        byteBuffer.putLong(12, offset);
        byteBuffer.putInt(20, size);
        byteBuffer.putInt(24, timeDiff);
        byteBuffer.putInt(28, itemIndex);
        return byteBuffer;
    }

    /** 返回键哈希码。 */
    public int getHashCode() {
        return hashCode;
    }

    /** 返回 Topic ID。 */
    public int getTopicId() {
        return topicId;
    }

    /** 返回队列 ID。 */
    public int getQueueId() {
        return queueId;
    }

    /** 返回物理偏移。 */
    public long getOffset() {
        return offset;
    }

    /** 返回消息大小。 */
    public int getSize() {
        return size;
    }

    /** 返回时间差。 */
    public int getTimeDiff() {
        return timeDiff;
    }

    /** 返回项序号。 */
    public int getItemIndex() {
        return itemIndex;
    }

    /** 调试字符串。 */
    @Override
    public String toString() {
        return "IndexItem{" +
            "hashCode=" + hashCode +
            ", topicId=" + topicId +
            ", queueId=" + queueId +
            ", offset=" + offset +
            ", size=" + size +
            ", timeDiff=" + timeDiff +
            ", position=" + itemIndex +
            '}';
    }
}
