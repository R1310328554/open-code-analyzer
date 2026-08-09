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
package org.apache.rocketmq.tieredstore.file;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.provider.FileSegment;
import org.apache.rocketmq.tieredstore.provider.FileSegmentFactory;
import org.apache.rocketmq.tieredstore.util.MessageFormatUtil;

/**
 * CommitLog 分层扁平文件：支持按时间与最小大小滚动，维护首条消息 queueOffset 缓存。
 */
public class FlatCommitLogFile extends FlatAppendFile {

    /** 读取首条 queueOffset 失败时的返回值。 */
    private static final long GET_OFFSET_ERROR = -1L;

    /** 缓存的首条消息 consume queue offset。 */
    private final AtomicLong firstOffset = new AtomicLong(GET_OFFSET_ERROR);

    /** 构造 CommitLog 文件并从 offset 0 初始化。 */
    public FlatCommitLogFile(FileSegmentFactory fileSegmentFactory, String filePath) {
        super(fileSegmentFactory, FileSegmentType.COMMIT_LOG, filePath);
        this.initOffset(0L);
    }

    /**
     * 滚动规则：① 单文件保留超过 interval 毫秒；② 达到 commitLogRollingMinimumSize。
     * 因规则 ②，实际占用可能略高于预期。
     */
    /** 满足时间与最小大小时滚动新 CommitLog 段。 */
    public boolean tryRollingFile(long interval) {
        FileSegment fileSegment = this.getFileToWrite();
        long timestamp = fileSegment.getMinTimestamp();
        if (timestamp != Long.MAX_VALUE && timestamp + interval < System.currentTimeMillis() &&
            fileSegment.getAppendPosition() >=
                fileSegmentFactory.getStoreConfig().getCommitLogRollingMinimumSize()) {
            this.rollingNewFile(this.getAppendOffset());
            return true;
        }
        return false;
    }

    /** 同步获取首条消息的 queueOffset。 */
    public long getMinOffsetFromFile() {
        return firstOffset.get() == GET_OFFSET_ERROR ?
            this.getMinOffsetFromFileAsync().join() : firstOffset.get();
    }

    /** 异步读取首条消息 queueOffset 并缓存。 */
    public CompletableFuture<Long> getMinOffsetFromFileAsync() {
        int length = MessageFormatUtil.QUEUE_OFFSET_POSITION + Long.BYTES;
        if (this.fileSegmentTable.isEmpty() ||
            this.getCommitOffset() - this.getMinOffset() < length) {
            return CompletableFuture.completedFuture(GET_OFFSET_ERROR);
        }
        return this.readAsync(this.getMinOffset(), length)
            .thenApply(buffer -> {
                firstOffset.set(MessageFormatUtil.getQueueOffset(buffer));
                return firstOffset.get();
            });
    }

    /** 过期清理后若 minOffset 变化则重置 firstOffset 缓存。 */
    @Override
    public void destroyExpiredFile(long expireTimestamp) {
        long beforeOffset = this.getMinOffset();
        super.destroyExpiredFile(expireTimestamp);
        long afterOffset = this.getMinOffset();

        if (beforeOffset != afterOffset && afterOffset > 0) {
            log.info("CommitLog min cq offset reset, filePath={}, offset={}, expireTimestamp={}, change={}-{}",
                filePath, firstOffset.get(), expireTimestamp, beforeOffset, afterOffset);
            firstOffset.set(GET_OFFSET_ERROR);
        }
    }
}
