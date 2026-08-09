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

package org.apache.rocketmq.tieredstore.common;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 分层存储选缓冲结果：封装 ByteBuffer、偏移、大小与 tagCode。
 */
public class SelectBufferResult {

    /** 堆外 Direct 缓冲，读写槽位数据。 */
    private final ByteBuffer byteBuffer;
    private final long startOffset;
    private final int size;
    /** 消息 Tag 哈希码。 */
    private final long tagCode;
    private final AtomicLong accessCount;

    public SelectBufferResult(ByteBuffer byteBuffer, long startOffset, int size, long tagCode) {
        this.startOffset = startOffset;
        this.byteBuffer = byteBuffer;
        this.size = size;
        this.tagCode = tagCode;
        this.accessCount = new AtomicLong();
    }

        /** 返回消息 ByteBuffer。 */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

        /** 返回起始物理偏移。 */
    public long getStartOffset() {
        return startOffset;
    }

        /** 返回消息大小。 */
    public int getSize() {
        return size;
    }

        /** 返回 Tag 哈希码。 */
    public long getTagCode() {
        return tagCode;
    }

        /** 返回访问计数器。 */
    public AtomicLong getAccessCount() {
        return accessCount;
    }
}
