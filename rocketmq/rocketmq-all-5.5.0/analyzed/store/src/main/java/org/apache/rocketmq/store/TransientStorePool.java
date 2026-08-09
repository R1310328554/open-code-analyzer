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
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import io.netty.util.internal.PlatformDependent;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.util.LibC;

/**
 *  transient 写入缓冲池：预分配 direct ByteBuffer 并 mlock，供 CommitLog 双写路径借用。
 */
public class TransientStorePool {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /** 缓冲池容量（块数）。 */
    private final int poolSize;
    /** 每块 direct buffer 大小（通常等于 mappedFileSize）。 */
    private final int fileSize;
    /** 可用 direct buffer 双端队列。 */
    private final Deque<ByteBuffer> availableBuffers;
    /** 是否执行真实 commit（否则仅写 transient 缓冲）。 */
    private volatile boolean isRealCommit = true;

    /** 构造指定容量与块大小的缓冲池。 */
    public TransientStorePool(final int poolSize, final int fileSize) {
        this.poolSize = poolSize;
        this.fileSize = fileSize;
        this.availableBuffers = new ConcurrentLinkedDeque<>();
    }

    /**
     * 重量级初始化：分配 direct buffer 并 mlock 锁定物理页。
     */
    /** 预分配并锁定全部 direct buffer。 */
    public void init() {
        for (int i = 0; i < poolSize; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fileSize);

            final long address = PlatformDependent.directBufferAddress(byteBuffer);
            Pointer pointer = new Pointer(address);
            LibC.INSTANCE.mlock(pointer, new NativeLong(fileSize));

            availableBuffers.offer(byteBuffer);
        }
    }

    /** 释放 mlock 并销毁缓冲池。 */
    public void destroy() {
        for (ByteBuffer byteBuffer : availableBuffers) {
            final long address = PlatformDependent.directBufferAddress(byteBuffer);
            Pointer pointer = new Pointer(address);
            LibC.INSTANCE.munlock(pointer, new NativeLong(fileSize));
        }
    }

    /** 归还借出的 buffer 到池首。 */
    public void returnBuffer(ByteBuffer byteBuffer) {
        byteBuffer.position(0);
        byteBuffer.limit(fileSize);
        this.availableBuffers.offerFirst(byteBuffer);
    }

    /** 从池首借出一块 buffer；余量不足 40% 时打 warn 日志。 */
    public ByteBuffer borrowBuffer() {
        ByteBuffer buffer = availableBuffers.pollFirst();
        if (availableBuffers.size() < poolSize * 0.4) {
            log.warn("TransientStorePool 剩余缓冲仅 {} 块.", availableBuffers.size());
        }
        return buffer;
    }

    /** 返回当前可用 buffer 数量。 */
    public int availableBufferNums() {
        return availableBuffers.size();
    }

    /** 是否真实 commit 到 MappedFile。 */
    public boolean isRealCommit() {
        return isRealCommit;
    }

    /** 设置是否真实 commit。 */
    public void setRealCommit(boolean realCommit) {
        isRealCommit = realCommit;
    }
}
