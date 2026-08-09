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
package org.apache.rocketmq.store.logfile;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 共享 ByteBuffer 管理器：按 maxMessageSize 分配 DirectByteBuffer 池供并发借用。
 */
public class SharedByteBufferManager {

    /** 单例实例。 */
    private static volatile SharedByteBufferManager instance;
    private static final Object LOCK = new Object();

    /** 共享缓冲区数组。 */
    private SharedByteBuffer[] sharedByteBuffers;
    private int bufferSize;
    private int maxSharedNum;
    private volatile boolean initialized = false;

    private SharedByteBufferManager() {
        // Private constructor
    }

    /** 获取单例实例。 */
    public static SharedByteBufferManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new SharedByteBufferManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化共享缓冲区池。
     *
     * @param maxMessageSize 最大消息体大小
     * @param sharedBufferNum 共享缓冲区数量
     */
    public synchronized void init(int maxMessageSize, int sharedBufferNum) {
        if (!initialized) {
            //Reserve 64kb for encoding buffer outside body
            bufferSize = Integer.MAX_VALUE - maxMessageSize >= 64 * 1024 ?
                maxMessageSize + 64 * 1024 : Integer.MAX_VALUE;

            this.maxSharedNum = sharedBufferNum;
            this.sharedByteBuffers = new SharedByteBuffer[maxSharedNum];
            for (int i = 0; i < maxSharedNum; i++) {
                this.sharedByteBuffers[i] = new SharedByteBuffer(bufferSize);
            }
            this.initialized = true;
        }
    }

    /**
     * 随机借用一块共享缓冲区。
     *
     * @return 共享缓冲区包装
     */
    public SharedByteBuffer borrowSharedByteBuffer() {
        if (!initialized) {
            throw new IllegalStateException("SharedByteBufferManager not initialized");
        }
        int idx = ThreadLocalRandom.current().nextInt(maxSharedNum);
        return sharedByteBuffers[idx];
    }

    /**
     * 返回缓冲区大小。
     *
     * @return 字节数
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * 是否已初始化。
     *
     * @return 已初始化返回 true
     */
    public boolean isInitialized() {
        return initialized;
    }

    /** 带 ReentrantLock 的共享 DirectByteBuffer 包装。 */
    public static class SharedByteBuffer {
        private final ReentrantLock lock;
        private final ByteBuffer buffer;

        /** 分配指定大小的 DirectByteBuffer。 */
        public SharedByteBuffer(int size) {
            this.lock = new ReentrantLock();
            this.buffer = ByteBuffer.allocateDirect(size);
        }

        /** 释放锁。 */
        public void release() {
            this.lock.unlock();
        }

        /** 获取锁并返回缓冲区。 */
        public ByteBuffer acquire() {
            this.lock.lock();
            return buffer;
        }
    }
}
