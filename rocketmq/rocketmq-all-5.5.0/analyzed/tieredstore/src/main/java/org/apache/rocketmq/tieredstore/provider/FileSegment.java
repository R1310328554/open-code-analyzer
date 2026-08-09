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
package org.apache.rocketmq.tieredstore.provider;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.rocketmq.tieredstore.MessageStoreConfig;
import org.apache.rocketmq.tieredstore.MessageStoreExecutor;
import org.apache.rocketmq.tieredstore.common.AppendResult;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.exception.TieredStoreErrorCode;
import org.apache.rocketmq.tieredstore.exception.TieredStoreException;
import org.apache.rocketmq.tieredstore.stream.FileSegmentInputStream;
import org.apache.rocketmq.tieredstore.stream.FileSegmentInputStreamFactory;
import org.apache.rocketmq.tieredstore.util.MessageStoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件段抽象基类：管理缓冲追加、异步组提交与偏移校正。
 */
public abstract class FileSegment implements Comparable<FileSegment>, FileSegmentProvider {

    private static final Logger log = LoggerFactory.getLogger(MessageStoreUtil.TIERED_STORE_LOGGER_NAME);

    /** 获取远程文件大小失败时的返回值。 */
    protected static final Long GET_FILE_SIZE_ERROR = -1L;

    /** 文件段起始逻辑偏移。 */
    protected final long baseOffset;
    /** 元数据中的相对文件路径。 */
    protected final String filePath;
    /** 文件段类型。 */
    protected final FileSegmentType fileType;
    /** 分层存储配置。 */
    protected final MessageStoreConfig storeConfig;

    /** 该类型文件段允许的最大字节数。 */
    protected final long maxSize;
    /** 异步读写线程池执行器。 */
    protected final MessageStoreExecutor executor;
    /** 保护缓冲与位置字段的可重入锁。 */
    protected final ReentrantLock fileLock = new ReentrantLock();
    /** 限制同一文件段并发提交的信号量。 */
    protected final Semaphore commitLock = new Semaphore(1);

    /** 文件段是否已关闭。 */
    protected volatile boolean closed = false;
    /** 段内最早消息存储时间戳。 */
    protected volatile long minTimestamp = Long.MAX_VALUE;
    /** 段内最晚消息存储时间戳。 */
    protected volatile long maxTimestamp = Long.MAX_VALUE;
    /** 已提交到后端的文件内偏移。 */
    protected volatile long commitPosition = 0L;
    /** 已追加到内存缓冲的文件内偏移。 */
    protected volatile long appendPosition = 0L;

    /** 待组提交的上传缓冲列表。 */
    protected volatile List<ByteBuffer> bufferList = new ArrayList<>();
    /** 当前进行中的提交输入流。 */
    protected volatile FileSegmentInputStream fileSegmentInputStream;
    /** 进行中的异步提交 Future。 */
    protected volatile CompletableFuture<Boolean> flightCommitRequest;

    public FileSegment(MessageStoreConfig storeConfig, FileSegmentType fileType,
        String filePath, long baseOffset, MessageStoreExecutor executor) {

        this.storeConfig = storeConfig;
        this.fileType = fileType;
        this.filePath = filePath;
        this.baseOffset = baseOffset;
        this.executor = executor;
        this.maxSize = this.getMaxSizeByFileType();
    }

    @Override
    public int compareTo(FileSegment o) {
        return Long.compare(this.baseOffset, o.baseOffset);
    }

    public long getBaseOffset() {
        return baseOffset;
    }

    /** 初始化提交与追加位置（恢复场景）。 */
    public void initPosition(long pos) {
        fileLock.lock();
        try {
            this.commitPosition = pos;
            this.appendPosition = pos;
        } finally {
            fileLock.unlock();
        }
    }

    public long getCommitPosition() {
        return commitPosition;
    }

    public long getAppendPosition() {
        return appendPosition;
    }

    public long getCommitOffset() {
        return baseOffset + commitPosition;
    }

    public long getAppendOffset() {
        return baseOffset + appendPosition;
    }

    public FileSegmentType getFileType() {
        return fileType;
    }

    /** 按文件段类型返回配置的最大尺寸。 */
    public long getMaxSizeByFileType() {
        switch (fileType) {
            case COMMIT_LOG:
                return storeConfig.getTieredStoreCommitLogMaxSize();
            case CONSUME_QUEUE:
                return storeConfig.getTieredStoreConsumeQueueMaxSize();
            case INDEX:
            default:
                return Long.MAX_VALUE;
        }
    }

    public long getMaxSize() {
        return maxSize;
    }

    public long getMinTimestamp() {
        return minTimestamp;
    }

    public void setMinTimestamp(long minTimestamp) {
        this.minTimestamp = minTimestamp;
    }

    public long getMaxTimestamp() {
        return maxTimestamp;
    }

    public void setMaxTimestamp(long maxTimestamp) {
        this.maxTimestamp = maxTimestamp;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        fileLock.lock();
        try {
            this.closed = true;
        } finally {
            fileLock.unlock();
        }
    }

    /** 借出并清空当前待提交缓冲列表。 */
    protected List<ByteBuffer> borrowBuffer() {
        List<ByteBuffer> temp;
        fileLock.lock();
        try {
            temp = bufferList;
            bufferList = new ArrayList<>();
        } finally {
            fileLock.unlock();
        }
        return temp;
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    /** 更新段内最小/最大消息时间戳。 */
    protected void updateTimestamp(long timestamp) {
        fileLock.lock();
        try {
            if (maxTimestamp == Long.MAX_VALUE && minTimestamp == Long.MAX_VALUE) {
                maxTimestamp = timestamp;
                minTimestamp = timestamp;
                return;
            }
            maxTimestamp = Math.max(maxTimestamp, timestamp);
            minTimestamp = Math.min(minTimestamp, timestamp);
        } finally {
            fileLock.unlock();
        }
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    /** 追加字节缓冲并更新时间与追加位置。 */
    public AppendResult append(ByteBuffer buffer, long timestamp) {
        fileLock.lock();
        try {
            if (closed) {
                return AppendResult.FILE_CLOSED;
            }
            if (appendPosition + buffer.remaining() > maxSize) {
                return AppendResult.FILE_FULL;
            }
            if (bufferList.size() >= storeConfig.getTieredStoreMaxGroupCommitCount()) {
                return AppendResult.BUFFER_FULL;
            }
            this.appendPosition += buffer.remaining();
            this.bufferList.add(buffer);
            this.updateTimestamp(timestamp);
        } finally {
            fileLock.unlock();
        }
        return AppendResult.SUCCESS;
    }

    /** 是否存在未提交的追加数据。 */
    public boolean needCommit() {
        return appendPosition > commitPosition;
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    /** 异步组提交待上传缓冲到后端存储。 */
    public CompletableFuture<Boolean> commitAsync() {
        if (closed) {
            return CompletableFuture.completedFuture(false);
        }

        if (!needCommit()) {
            return CompletableFuture.completedFuture(true);
        }

        // 获取提交锁，避免并发提交
        if (commitLock.drainPermits() <= 0) {
            return CompletableFuture.completedFuture(false);
        }

        // 处理上次提交失败后的偏移校正
        if (fileSegmentInputStream != null) {
            long fileSize = this.getSize();
            if (fileSize == GET_FILE_SIZE_ERROR) {
                log.error("FileSegment correct position error, fileName={}, commit={}, append={}, buffer={}",
                    this.getPath(), commitPosition, appendPosition, fileSegmentInputStream.getContentLength());
                releaseCommitLock();
                return CompletableFuture.completedFuture(false);
            }
            if (correctPosition(fileSize)) {
                fileSegmentInputStream = null;
            }
        }

        int bufferSize;
        if (fileSegmentInputStream != null) {
            fileSegmentInputStream.rewind();
            bufferSize = fileSegmentInputStream.available();
        } else {
            List<ByteBuffer> bufferList = this.borrowBuffer();
            bufferSize = bufferList.stream().mapToInt(ByteBuffer::remaining).sum();
            if (bufferSize == 0) {
                releaseCommitLock();
                return CompletableFuture.completedFuture(true);
            }
            fileSegmentInputStream = FileSegmentInputStreamFactory.build(
                fileType, this.getCommitOffset(), bufferList, null, bufferSize);
        }

        boolean append = fileType != FileSegmentType.INDEX;
        return flightCommitRequest =
            this.commit0(fileSegmentInputStream, commitPosition, bufferSize, append)
                .thenApply(result -> {
                    if (result) {
                        commitPosition += bufferSize;
                        fileSegmentInputStream = null;
                        return true;
                    } else {
                        fileSegmentInputStream.rewind();
                        return false;
                    }
                })
                .exceptionally(this::handleCommitException)
                .whenComplete((result, e) -> releaseCommitLock());
    }

    private boolean handleCommitException(Throwable e) {

        log.warn("FileSegment commit exception, filePath={}", this.filePath, e);

        // 提取异常根因以校正提交偏移
        Throwable rootCause = e.getCause() != null ? e.getCause() : e;

        long fileSize = rootCause instanceof TieredStoreException ?
            ((TieredStoreException) rootCause).getPosition() : this.getSize();

        long expectPosition = commitPosition + fileSegmentInputStream.getContentLength();
        if (fileSize == GET_FILE_SIZE_ERROR) {
            log.error("Get file size error after commit, FileName: {}, Commit: {}, Content: {}, Expect: {}, Append: {}",
                this.getPath(), commitPosition, fileSegmentInputStream.getContentLength(), expectPosition, appendPosition);
            return false;
        }

        if (correctPosition(fileSize)) {
            fileSegmentInputStream = null;
            return true;
        } else {
            fileSegmentInputStream.rewind();
            return false;
        }
    }

    private void releaseCommitLock() {
        if (commitLock.availablePermits() == 0) {
            commitLock.release();
        }
    }

    /**
     * 根据后端报告的文件大小校正 commitPosition；返回 true 表示可清空缓冲。
     */
    private boolean correctPosition(long fileSize) {

        // 当前存在 commit、期望与远端文件大小三个偏移
        // 保证 commit 偏移不超过期望偏移
        // 持续追加时 append 偏移会增大

        // 假定后端返回的文件大小可信，
        // 可将 commit 偏移重置为存储系统报告的大小

        long expectPosition = commitPosition + fileSegmentInputStream.getContentLength();
        commitPosition = fileSize;
        return expectPosition == fileSize;
    }

    /** 同步读取指定区间数据。 */
    public ByteBuffer read(long position, int length) {
        return readAsync(position, length).join();
    }

    /** 异步读取已提交区间内的数据。 */
    public CompletableFuture<ByteBuffer> readAsync(long position, int length) {
        CompletableFuture<ByteBuffer> future = new CompletableFuture<>();

        if (position < 0 || position >= commitPosition) {
            future.completeExceptionally(new TieredStoreException(TieredStoreErrorCode.ILLEGAL_PARAM,
                String.format("FileSegment read position illegal, filePath=%s, fileType=%s, position=%d, length=%d, commit=%d",
                    filePath, fileType, position, length, commitPosition)));
            return future;
        }

        if (length <= 0) {
            future.completeExceptionally(new TieredStoreException(TieredStoreErrorCode.ILLEGAL_PARAM,
                String.format("FileSegment read length illegal, filePath=%s, fileType=%s, position=%d, length=%d, commit=%d",
                    filePath, fileType, position, length, commitPosition)));
            return future;
        }

        int readableBytes = (int) (commitPosition - position);
        if (readableBytes < length) {
            length = readableBytes;
            log.debug("FileSegment expect request position is greater than commit position, " +
                    "file: {}, request position: {}, commit position: {}, change length from {} to {}",
                getPath(), position, commitPosition, length, readableBytes);
        }
        return this.read0(position, length);
    }
}
