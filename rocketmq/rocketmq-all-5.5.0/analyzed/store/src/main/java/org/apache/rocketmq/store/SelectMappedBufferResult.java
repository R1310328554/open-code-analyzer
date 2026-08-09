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
import org.apache.rocketmq.store.logfile.MappedFile;

/**
 * Mapped 文件切片查询结果：封装起始偏移、ByteBuffer 视图及所属 MappedFile。
 */
public class SelectMappedBufferResult {

    /** 切片在 CommitLog 中的起始物理偏移。 */
    private final long startOffset;

    /** 消息内容的只读 ByteBuffer 视图。 */
    private final ByteBuffer byteBuffer;

    /** 有效数据字节长度。 */
    private int size;

    /** 数据来源 MappedFile，release 后置 null。 */
    protected MappedFile mappedFile;

    /** 数据是否仍在页缓存中（影响消费延迟统计）。 */
    private boolean isInCache = true;

    /** 构造指定偏移与缓冲区的查询结果。 */
    public SelectMappedBufferResult(long startOffset, ByteBuffer byteBuffer, int size, MappedFile mappedFile) {
        this.startOffset = startOffset;
        this.byteBuffer = byteBuffer;
        this.size = size;
        this.mappedFile = mappedFile;
    }

    /** 返回消息 ByteBuffer。 */
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    /** 返回有效数据长度。 */
    public int getSize() {
        return size;
    }

    /** 设置有效长度并调整 buffer limit。 */
    public void setSize(final int s) {
        this.size = s;
        this.byteBuffer.limit(this.size);
    }

    /** 返回所属 MappedFile。 */
    public MappedFile getMappedFile() {
        return mappedFile;
    }

    /** 释放 MappedFile 引用。 */
    public synchronized void release() {
        if (this.mappedFile != null) {
            this.mappedFile.release();
            this.mappedFile = null;
        }
    }
    /** 是否已 release（mappedFile 为 null）。 */
    public synchronized boolean hasReleased() {
        return this.mappedFile == null;
    }

    /** 返回起始物理偏移。 */
    public long getStartOffset() {
        return startOffset;
    }

    /** 对应区间是否已加载到内存（mmap 预热）。 */
    public boolean isInMem() {
        if (mappedFile == null) {
            return true;
        }
        long pos = startOffset - mappedFile.getFileFromOffset();
        return mappedFile.isLoaded(pos, size);
    }

    /** 是否在页缓存中。 */
    public boolean isInCache() {
        return isInCache;
    }

    /** 设置页缓存标志。 */
    public void setInCache(boolean inCache) {
        isInCache = inCache;
    }
}
