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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.apache.rocketmq.tieredstore.common.AppendResult;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.metadata.MetadataStore;
import org.apache.rocketmq.tieredstore.metadata.entity.FileSegmentMetadata;
import org.apache.rocketmq.tieredstore.provider.FileSegment;
import org.apache.rocketmq.tieredstore.provider.FileSegmentFactory;
import org.apache.rocketmq.tieredstore.util.MessageStoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分层存储追加写文件抽象：管理 FileSegment 链表的恢复、滚动、追加、读取与过期清理。
 */
public class FlatAppendFile {

    /** 分层存储模块日志。 */
    protected static final Logger log = LoggerFactory.getLogger(MessageStoreUtil.TIERED_STORE_LOGGER_NAME);
    /** RPC 获取远程文件大小失败时的返回值。 */
    public static final long GET_FILE_SIZE_ERROR = -1L;

    /** 逻辑文件路径。 */
    protected final String filePath;
    /** 文件段类型（CommitLog/CQ/Index）。 */
    protected final FileSegmentType fileType;
    /** 元数据存储。 */
    protected final MetadataStore metadataStore;
    /** 文件段工厂。 */
    protected final FileSegmentFactory fileSegmentFactory;
    /** 文件段表读写锁。 */
    protected final ReentrantReadWriteLock fileSegmentLock;
    /** 按 baseOffset 排序的文件段列表。 */
    protected final CopyOnWriteArrayList<FileSegment> fileSegmentTable;

    /** 构造并执行 recover 与 recoverFileSize。 */
    protected FlatAppendFile(FileSegmentFactory fileSegmentFactory, FileSegmentType fileType, String filePath) {

        this.fileType = fileType;
        this.filePath = filePath;
        this.metadataStore = fileSegmentFactory.getMetadataStore();
        this.fileSegmentFactory = fileSegmentFactory;
        this.fileSegmentLock = new ReentrantReadWriteLock();
        this.fileSegmentTable = new CopyOnWriteArrayList<>();
        this.recover();
        this.recoverFileSize();
    }

    /** 从元数据恢复文件段列表并排序。 */
    public void recover() {
        List<FileSegment> fileSegmentList = new ArrayList<>();
        this.metadataStore.iterateFileSegment(this.filePath, this.fileType, metadata -> {
            FileSegment fileSegment = this.fileSegmentFactory.createSegment(
                this.fileType, metadata.getPath(), metadata.getBaseOffset());
            fileSegment.initPosition(metadata.getSize());
            fileSegment.setMinTimestamp(metadata.getBeginTimestamp());
            fileSegment.setMaxTimestamp(metadata.getEndTimestamp());
            fileSegmentList.add(fileSegment);
        });
        this.fileSegmentTable.addAll(fileSegmentList.stream().sorted().collect(Collectors.toList()));
    }

    /**
     * 初始化文件段时获取远程正确文件大小。
     *
     * @param fileSegment 目标文件段
     * @return 远程存在时返回实际长度，不存在返回 0，RPC 失败返回 -1
     * @see <a href="https://github.com/apache/rocketmq/issues/9544">Related GitHub Issue</a>
     */
    /** 轮询远程 RPC 直至获取有效文件大小。 */
    public long getFileCorrectSize(FileSegment fileSegment) {
        while (true) {
            long fileSize = fileSegment.getSize();
            if (fileSize != GET_FILE_SIZE_ERROR) {
                log.debug("FlatAppendFile get file correct size, filePath={} fileType={}, fileSize={}",
                    fileSegment.getPath(), fileSegment.getFileType(), fileSize);
                return fileSize;
            } else {
                log.warn("FlatAppendFile get file correct size error, filePath={}, fileType={}",
                    fileSegment.getPath(), fileSegment.getFileType());
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    log.warn("FlatAppendFile get file correct size interrupted", e);
                }
            }
        }
    }

    /** 校正最后一个文件段的 commit 位置与远程大小一致。 */
    public void recoverFileSize() {
        if (fileSegmentTable.isEmpty() || FileSegmentType.INDEX.equals(fileType)) {
            return;
        }
        FileSegment fileSegment = fileSegmentTable.get(fileSegmentTable.size() - 1);
        long fileSize = this.getFileCorrectSize(fileSegment);
        if (fileSegment.getCommitPosition() != fileSize) {
            fileSegment.initPosition(fileSize);
            flushFileSegmentMeta(fileSegment);
            log.warn("FlatAppendFile last file size not correct, filePath: {}", this.filePath);
        }
    }

    /** 表为空时创建首个文件段并刷元数据。 */
    public void initOffset(long offset) {
        if (this.fileSegmentTable.isEmpty()) {
            FileSegment fileSegment = fileSegmentFactory.createSegment(fileType, filePath, offset);
            fileSegment.initPosition(this.getFileCorrectSize(fileSegment));
            this.flushFileSegmentMeta(fileSegment);
            this.fileSegmentTable.add(fileSegment);
        }
    }

    /** 将文件段大小与时间戳写入元数据存储。 */
    public void flushFileSegmentMeta(FileSegment fileSegment) {
        FileSegmentMetadata metadata = this.metadataStore.getFileSegment(
            this.filePath, fileSegment.getFileType(), fileSegment.getBaseOffset());
        if (metadata == null) {
            metadata = new FileSegmentMetadata(
                this.filePath, fileSegment.getBaseOffset(), fileSegment.getFileType().getCode());
            metadata.setCreateTimestamp(System.currentTimeMillis());
        }
        metadata.setSize(fileSegment.getCommitPosition());
        metadata.setBeginTimestamp(fileSegment.getMinTimestamp());
        metadata.setEndTimestamp(fileSegment.getMaxTimestamp());
        this.metadataStore.updateFileSegment(metadata);
    }

    /** 返回逻辑文件路径。 */
    public String getFilePath() {
        return filePath;
    }

    /** 返回文件段类型。 */
    public FileSegmentType getFileType() {
        return fileType;
    }

    /** 返回文件段列表。 */
    public List<FileSegment> getFileSegmentList() {
        return fileSegmentTable;
    }

    /** 返回最小 baseOffset。 */
    public long getMinOffset() {
        List<FileSegment> list = this.fileSegmentTable;
        return list.isEmpty() ? 0L : list.get(0).getBaseOffset();
    }

    /** 返回最后一段 commit 偏移。 */
    public long getCommitOffset() {
        List<FileSegment> list = this.fileSegmentTable;
        return list.isEmpty() ? 0L : list.get(list.size() - 1).getCommitOffset();
    }

    /** 返回最后一段 append 偏移。 */
    public long getAppendOffset() {
        List<FileSegment> list = this.fileSegmentTable;
        return list.isEmpty() ? 0L : list.get(list.size() - 1).getAppendOffset();
    }

    /** 返回最早消息时间戳。 */
    public long getMinTimestamp() {
        List<FileSegment> list = this.fileSegmentTable;
        return list.isEmpty() ? GET_FILE_SIZE_ERROR : list.get(0).getMinTimestamp();
    }

    /** 返回最晚消息时间戳。 */
    public long getMaxTimestamp() {
        List<FileSegment> list = this.fileSegmentTable;
        return list.isEmpty() ? GET_FILE_SIZE_ERROR : list.get(list.size() - 1).getMaxTimestamp();
    }

    /** 滚动创建新文件段并刷元数据。 */
    public FileSegment rollingNewFile(long offset) {
        FileSegment fileSegment;
        fileSegmentLock.writeLock().lock();
        try {
            fileSegment = this.fileSegmentFactory.createSegment(this.fileType, this.filePath, offset);
            this.flushFileSegmentMeta(fileSegment);
            this.fileSegmentTable.add(fileSegment);
        } finally {
            fileSegmentLock.writeLock().unlock();
        }
        return fileSegment;
    }

    /** 返回当前写入目标（最后一段）。 */
    public FileSegment getFileToWrite() {
        List<FileSegment> fileSegmentList = this.fileSegmentTable;
        if (fileSegmentList.isEmpty()) {
            throw new IllegalStateException("Need to set base offset before create file segment");
        } else {
            return fileSegmentList.get(fileSegmentList.size() - 1);
        }
    }

    /** 追加写入；段满时 commit 并滚动新段。 */
    public AppendResult append(ByteBuffer buffer, long timestamp) {
        AppendResult result;
        fileSegmentLock.writeLock().lock();
        try {
            FileSegment fileSegment = this.getFileToWrite();
            result = fileSegment.append(buffer, timestamp);
            if (result == AppendResult.FILE_FULL) {
                boolean commitResult = fileSegment.commitAsync().join();
                log.info("FlatAppendFile#append not successful for the file {} is full, commit result={}",
                    fileSegment.getPath(), commitResult);
                if (commitResult) {
                    this.flushFileSegmentMeta(fileSegment);
                    return this.rollingNewFile(this.getAppendOffset()).append(buffer, timestamp);
                } else {
                    return AppendResult.UNKNOWN_ERROR;
                }
            }
        } finally {
            fileSegmentLock.writeLock().unlock();
        }
        return result;
    }

    /** 异步 commit 最后一段并刷元数据。 */
    public CompletableFuture<Boolean> commitAsync() {
        List<FileSegment> fileSegmentsList = this.fileSegmentTable;
        if (fileSegmentsList.isEmpty()) {
            return CompletableFuture.completedFuture(true);
        }
        FileSegment fileSegment = fileSegmentsList.get(fileSegmentsList.size() - 1);
        return fileSegment.commitAsync().thenApply(success -> {
            if (success) {
                this.flushFileSegmentMeta(fileSegment);
            }
            return success;
        });
    }

    /** 按全局偏移异步读取，跨段时合并缓冲区。 */
    public CompletableFuture<ByteBuffer> readAsync(long offset, int length) {
        List<FileSegment> fileSegmentList = this.fileSegmentTable;
        int index = fileSegmentList.size() - 1;
        for (; index >= 0; index--) {
            if (fileSegmentList.get(index).getBaseOffset() <= offset) {
                break;
            }
        }

        FileSegment fileSegment1 = fileSegmentList.get(index);
        FileSegment fileSegment2 = offset + length > fileSegment1.getCommitOffset() &&
            fileSegmentList.size() > index + 1 ? fileSegmentList.get(index + 1) : null;

        if (fileSegment2 == null) {
            return fileSegment1.readAsync(offset - fileSegment1.getBaseOffset(), length);
        }

        int segment1Length = (int) (fileSegment1.getCommitOffset() - offset);
        return fileSegment1.readAsync(offset - fileSegment1.getBaseOffset(), segment1Length)
            .thenCombine(fileSegment2.readAsync(0, length - segment1Length),
                (buffer1, buffer2) -> {
                    ByteBuffer buffer = ByteBuffer.allocate(buffer1.remaining() + buffer2.remaining());
                    buffer.put(buffer1).put(buffer2);
                    buffer.flip();
                    return buffer;
                });
    }

    /** 关闭所有文件段。 */
    public void shutdown() {
        fileSegmentLock.writeLock().lock();
        try {
            fileSegmentTable.forEach(FileSegment::close);
        } finally {
            fileSegmentLock.writeLock().unlock();
        }
    }

    /** 删除 maxTimestamp 早于 expireTimestamp 的过期段。 */
    public void destroyExpiredFile(long expireTimestamp) {
        fileSegmentLock.writeLock().lock();
        try {
            while (!fileSegmentTable.isEmpty()) {

                // first remove expired file from fileSegmentTable
                // then close and delete expired file
                FileSegment fileSegment = fileSegmentTable.get(0);

                if (fileSegment.getMaxTimestamp() != Long.MAX_VALUE &&
                    fileSegment.getMaxTimestamp() >= expireTimestamp) {
                    log.debug("FileSegment has not expired, filePath={}, fileType={}, " +
                            "offset={}, expireTimestamp={}, maxTimestamp={}", filePath, fileType,
                        fileSegment.getBaseOffset(), expireTimestamp, fileSegment.getMaxTimestamp());
                    break;
                }

                fileSegment.destroyFile();
                if (!fileSegment.exists()) {
                    fileSegmentTable.remove(0);
                    metadataStore.deleteFileSegment(filePath, fileType, fileSegment.getBaseOffset());
                }
            }
        } finally {
            fileSegmentLock.writeLock().unlock();
        }
    }

    /** 删除全部文件段。 */
    public void destroy() {
        this.destroyExpiredFile(Long.MAX_VALUE);
    }
}
