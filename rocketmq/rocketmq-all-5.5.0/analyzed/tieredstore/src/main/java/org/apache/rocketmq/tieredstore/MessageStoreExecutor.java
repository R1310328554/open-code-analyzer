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
package org.apache.rocketmq.tieredstore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.utils.ThreadUtils;

/**
 * 分层存储线程池单例：管理缓冲提交/拉取与文件回收执行器。
 */
public class MessageStoreExecutor {

    /** 缓冲提交线程池任务队列。 */
    public final BlockingQueue<Runnable> bufferCommitThreadPoolQueue;
    public final BlockingQueue<Runnable> bufferFetchThreadPoolQueue;
    public final BlockingQueue<Runnable> fileRecyclingThreadPoolQueue;

    /** 分层存储通用调度线程池。 */
    public final ScheduledExecutorService commonExecutor;
    /** 缓冲提交线程池。 */
    public final ExecutorService bufferCommitExecutor;
    public final ExecutorService bufferFetchExecutor;
    public final ExecutorService fileRecyclingExecutor;

    private static class SingletonHolder {
        private static final MessageStoreExecutor INSTANCE = new MessageStoreExecutor();
    }

        /** 返回单例执行器。 */
    public static MessageStoreExecutor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public MessageStoreExecutor() {
        this(10000);
    }

    public MessageStoreExecutor(int maxQueueCapacity) {

        this.commonExecutor = ThreadUtils.newScheduledThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            new ThreadFactoryImpl("TieredCommonExecutor_"));

        this.bufferCommitThreadPoolQueue = new LinkedBlockingQueue<>(maxQueueCapacity);
        this.bufferCommitExecutor = ThreadUtils.newThreadPoolExecutor(
            Math.max(16, Runtime.getRuntime().availableProcessors() * 4),
            Math.max(16, Runtime.getRuntime().availableProcessors() * 4),
            TimeUnit.MINUTES.toMillis(1), TimeUnit.MILLISECONDS,
            this.bufferCommitThreadPoolQueue,
            new ThreadFactoryImpl("BufferCommitExecutor_"));

        this.bufferFetchThreadPoolQueue = new LinkedBlockingQueue<>(maxQueueCapacity);
        this.bufferFetchExecutor = ThreadUtils.newThreadPoolExecutor(
            Math.max(16, Runtime.getRuntime().availableProcessors() * 4),
            Math.max(16, Runtime.getRuntime().availableProcessors() * 4),
            TimeUnit.MINUTES.toMillis(1), TimeUnit.MILLISECONDS,
            this.bufferFetchThreadPoolQueue,
            new ThreadFactoryImpl("BufferFetchExecutor_"));

        this.fileRecyclingThreadPoolQueue = new LinkedBlockingQueue<>(maxQueueCapacity);
        this.fileRecyclingExecutor = ThreadUtils.newThreadPoolExecutor(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            TimeUnit.MINUTES.toMillis(1), TimeUnit.MILLISECONDS,
            this.fileRecyclingThreadPoolQueue,
            new ThreadFactoryImpl("BufferFetchExecutor_"));
    }

    private void shutdownExecutor(ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
        }
    }

        /** 关闭全部线程池。 */
    public void shutdown() {
        this.shutdownExecutor(this.commonExecutor);
        this.shutdownExecutor(this.bufferCommitExecutor);
        this.shutdownExecutor(this.bufferFetchExecutor);
        this.shutdownExecutor(this.fileRecyclingExecutor);
    }
}
