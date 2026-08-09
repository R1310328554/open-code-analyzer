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

import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import org.apache.rocketmq.store.DispatchRequest;

/**
 * Dispatch 条目：扁平化存储 dispatch 请求的关键字段（Java 16 后可改用 Record）。
 */
public class DispatchEntry {
    /** 主题名 UTF-8 字节数组。 */
    public byte[] topic;
    /** 队列 ID。 */
    public int queueId;
    /** 队列逻辑偏移。 */
    public long queueOffset;
    /** CommitLog 物理偏移。 */
    public long commitLogOffset;
    /** 消息体字节长度。 */
    public int messageSize;
    /** 标签哈希码。 */
    public long tagCode;
    /** 消息存储时间戳。 */
    public long storeTimestamp;

    /** 从 {@link DispatchRequest} 构建 DispatchEntry。 */
    public static DispatchEntry from(@Nonnull DispatchRequest request) {
        DispatchEntry entry = new DispatchEntry();
        entry.topic = request.getTopic().getBytes(StandardCharsets.UTF_8);
        entry.queueId = request.getQueueId();
        entry.queueOffset = request.getConsumeQueueOffset();
        entry.commitLogOffset = request.getCommitLogOffset();
        entry.messageSize = request.getMsgSize();
        entry.tagCode = request.getTagsCode();
        entry.storeTimestamp = request.getStoreTimestamp();
        return entry;
    }
}
