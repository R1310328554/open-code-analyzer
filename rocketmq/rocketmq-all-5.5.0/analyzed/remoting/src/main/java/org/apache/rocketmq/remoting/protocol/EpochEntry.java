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

package org.apache.rocketmq.remoting.protocol;

import java.util.Objects;

/**
 * Controller 选举纪元条目：记录某一 epoch 在 CommitLog 上的起止偏移。
 */
public class EpochEntry extends RemotingSerializable {

    /** 当前活跃 epoch 的 endOffset 哨兵值（表示尚未结束）。 */
    public static final long LAST_EPOCH_END_OFFSET = Long.MAX_VALUE;
    /** 选举纪元编号。 */
    private int epoch;
    /** 该 epoch 覆盖的 CommitLog 起始偏移。 */
    private long startOffset;
    /** 该 epoch 覆盖的 CommitLog 结束偏移，默认未结束。 */
    private long endOffset = LAST_EPOCH_END_OFFSET;

    /** 拷贝构造。 */
    public EpochEntry(EpochEntry entry) {
        this.epoch = entry.getEpoch();
        this.startOffset = entry.getStartOffset();
        this.endOffset = entry.getEndOffset();
    }

    /** 创建未指定结束偏移的 epoch 条目。 */
    public EpochEntry(int epoch, long startOffset) {
        this.epoch = epoch;
        this.startOffset = startOffset;
    }

    /** 创建完整起止偏移的 epoch 条目。 */
    public EpochEntry(int epoch, long startOffset, long endOffset) {
        this.epoch = epoch;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /** 返回 epoch 编号。 */
    public int getEpoch() {
        return epoch;
    }

    /** 设置 epoch 编号。 */
    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }

    /** 返回起始偏移。 */
    public long getStartOffset() {
        return startOffset;
    }

    /** 设置起始偏移。 */
    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    /** 返回结束偏移。 */
    public long getEndOffset() {
        return endOffset;
    }

    /** 设置结束偏移。 */
    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }

    /** 返回 epoch 与偏移范围的字符串。 */
    @Override
    public String toString() {
        return "EpochEntry{" +
            "epoch=" + epoch +
            ", startOffset=" + startOffset +
            ", endOffset=" + endOffset +
            '}';
    }

    /** 三字段值相等则视为同一 epoch 条目。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EpochEntry entry = (EpochEntry) o;
        return epoch == entry.epoch && startOffset == entry.startOffset && endOffset == entry.endOffset;
    }

    /** 基于 epoch 与起止偏移计算哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(epoch, startOffset, endOffset);
    }
}
