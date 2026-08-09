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
package org.apache.rocketmq.tieredstore.metadata.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import java.util.Objects;

/**
 * 文件段元数据：描述分层存储中单个文件段的路径、类型、偏移、状态与时间戳。
 */
public class FileSegmentMetadata {

    /** 新建状态，尚未封存。 */
    public static final int STATUS_NEW = 0;
    /** 已封存，不再追加写入。 */
    public static final int STATUS_SEALED = 1;
    /** 已标记删除。 */
    public static final int STATUS_DELETED = 2;

    /** 文件段在元数据中的相对路径。 */
    @JSONField(ordinal = 1)
    private String path;

    /** 文件段类型编码，对应 {@link org.apache.rocketmq.tieredstore.common.FileSegmentType}。 */
    @JSONField(ordinal = 2)
    private int type;

    /** 文件段起始逻辑偏移。 */
    @JSONField(ordinal = 3)
    private long baseOffset;

    /** 当前状态：新建、封存或删除。 */
    @JSONField(ordinal = 4)
    private int status;

    /** 文件段字节大小。 */
    @JSONField(ordinal = 5)
    private long size;

    /** 创建时间戳（毫秒）。 */
    @JSONField(ordinal = 6)
    private long createTimestamp;

    /** 段内最早消息存储时间戳。 */
    @JSONField(ordinal = 7)
    private long beginTimestamp;

    /** 段内最晚消息存储时间戳。 */
    @JSONField(ordinal = 8)
    private long endTimestamp;

    /** 封存时间戳（毫秒）。 */
    @JSONField(ordinal = 9)
    private long sealTimestamp;

    // fastjson 反序列化使用的默认构造器
    @SuppressWarnings("unused")
    public FileSegmentMetadata() {
    }

    public FileSegmentMetadata(String path, long baseOffset, int type) {
        this.path = path;
        this.baseOffset = baseOffset;
        this.type = type;
        this.status = STATUS_NEW;
    }

    /** 标记为已封存并记录封存时间。 */
    public void markSealed() {
        this.status = STATUS_SEALED;
        this.sealTimestamp = System.currentTimeMillis();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getBaseOffset() {
        return baseOffset;
    }

    public void setBaseOffset(long baseOffset) {
        this.baseOffset = baseOffset;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getSealTimestamp() {
        return sealTimestamp;
    }

    public void setSealTimestamp(long sealTimestamp) {
        this.sealTimestamp = sealTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FileSegmentMetadata metadata = (FileSegmentMetadata) o;
        return size == metadata.size
            && baseOffset == metadata.baseOffset
            && status == metadata.status
            && path.equals(metadata.path)
            && type == metadata.type
            && createTimestamp == metadata.createTimestamp
            && beginTimestamp == metadata.beginTimestamp
            && endTimestamp == metadata.endTimestamp
            && sealTimestamp == metadata.sealTimestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path, baseOffset, status, size, createTimestamp, beginTimestamp, endTimestamp, sealTimestamp);
    }
}
