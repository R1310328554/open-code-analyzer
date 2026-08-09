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

import org.apache.rocketmq.store.logfile.MappedFile;

/**
 * 批量消息偏移索引：映射 CommitLog 物理位置与批次大小。
 */
public class BatchOffsetIndex {

    /** 索引条目所在的映射文件。 */
    private final MappedFile mappedFile;
    /** 索引在映射文件内的字节偏移。 */
    private final int indexPos;
    /** 消息在 CommitLog 中的物理偏移。 */
    private final long msgOffset;
    /** 本批次包含的消息条数。 */
    private final short batchSize;
    /** 消息存储时间戳。 */
    private final long storeTimestamp;

    /** 构造批量偏移索引条目。 */
    public BatchOffsetIndex(MappedFile file, int pos, long msgOffset, short size, long storeTimestamp) {
        mappedFile = file;
        indexPos = pos;
        this.msgOffset = msgOffset;
        batchSize = size;
        this.storeTimestamp = storeTimestamp;
    }

    /** 返回映射文件。 */
    public MappedFile getMappedFile() {
        return mappedFile;
    }

    /** 返回索引在文件内的位置。 */
    public int getIndexPos() {
        return indexPos;
    }

    /** 返回 CommitLog 物理偏移。 */
    public long getMsgOffset() {
        return msgOffset;
    }

    /** 返回批次大小。 */
    public short getBatchSize() {
        return batchSize;
    }

    /** 返回存储时间戳。 */
    public long getStoreTimestamp() {
        return storeTimestamp;
    }
}
