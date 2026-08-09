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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.tieredstore.MessageStoreConfig;
import org.apache.rocketmq.tieredstore.MessageStoreExecutor;
import org.apache.rocketmq.tieredstore.metadata.MetadataStore;
import org.apache.rocketmq.tieredstore.metadata.entity.TopicMetadata;
import org.apache.rocketmq.tieredstore.util.MessageStoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分层 FlatMessageFile 仓库：加载恢复、过期调度删除与按 MessageQueue 管理文件。
 */
public class FlatFileStore {

    /** 分层存储日志。 */
    private static final Logger log = LoggerFactory.getLogger(MessageStoreUtil.TIERED_STORE_LOGGER_NAME);

    /** 元数据存储。 */
    private final MetadataStore metadataStore;
    /** 存储配置。 */
    private final MessageStoreConfig storeConfig;
    /** 异步任务执行器。 */
    private final MessageStoreExecutor executor;
    /** 扁平文件工厂。 */
    private final FlatFileFactory flatFileFactory;
    /** MessageQueue → FlatMessageFile 映射。 */
    private final ConcurrentMap<MessageQueue, FlatMessageFile> flatFileConcurrentMap;

    /** 构造并初始化工厂与并发映射表。 */
    public FlatFileStore(MessageStoreConfig storeConfig, MetadataStore metadataStore, MessageStoreExecutor executor) {
        this.storeConfig = storeConfig;
        this.metadataStore = metadataStore;
        this.executor = executor;
        this.flatFileFactory = new FlatFileFactory(metadataStore, storeConfig, executor);
        this.flatFileConcurrentMap = new ConcurrentHashMap<>();
    }

    /** 清空映射并 recover 全部 Topic/Queue 文件。 */
    public boolean load() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            this.flatFileConcurrentMap.clear();
            this.recover();
            log.info("FlatFileStore recover finished, total cost={}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            long costTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            log.info("FlatFileStore recover error, total cost={}ms", costTime);
            LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME)
                .error("FlatFileStore recover error, total cost={}ms", costTime, e);
            return false;
        }
        return true;
    }

    /** 并发恢复各 Topic 下所有 Queue 的 FlatMessageFile。 */
    public void recover() {
        Semaphore semaphore = new Semaphore(storeConfig.getTieredStoreMaxPendingLimit() / 4);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        metadataStore.iterateTopic(topicMetadata -> {
            semaphore.acquireUninterruptibly();
            futures.add(this.recoverAsync(topicMetadata)
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("FlatFileStore recover file error, topic={}", topicMetadata.getTopic(), throwable);
                    }
                    semaphore.release();
                }));
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /** 异步恢复单个 Topic 下全部 Queue。 */
    public CompletableFuture<Void> recoverAsync(TopicMetadata topicMetadata) {
        return CompletableFuture.runAsync(() -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            AtomicLong queueCount = new AtomicLong();
            metadataStore.iterateQueue(topicMetadata.getTopic(), queueMetadata -> {
                FlatMessageFile flatFile = this.computeIfAbsent(new MessageQueue(
                    topicMetadata.getTopic(), storeConfig.getBrokerName(), queueMetadata.getQueue().getQueueId()));
                queueCount.incrementAndGet();
                log.debug("FlatFileStore recover file, topicId={}, topic={}, queueId={}, cost={}ms",
                    flatFile.getTopicId(), flatFile.getMessageQueue().getTopic(),
                    flatFile.getMessageQueue().getQueueId(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            });
            log.info("FlatFileStore recover file, topic={}, total={}, cost={}ms",
                topicMetadata.getTopic(), queueCount.get(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }, executor.bufferCommitExecutor);
    }

    /** 定时任务：按保留小时数删除各文件过期段。 */
    public void scheduleDeleteExpireFile() {
        if (!storeConfig.isTieredStoreDeleteFileEnable()) {
            return;
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        ImmutableList<FlatMessageFile> fileList = this.deepCopyFlatFileToList();
        for (FlatMessageFile flatFile : fileList) {
            flatFile.getFileLock().lock();
            try {
                flatFile.destroyExpiredFile(System.currentTimeMillis() -
                    TimeUnit.HOURS.toMillis(flatFile.getFileReservedHours()));
            } catch (Exception e) {
                log.error("FlatFileStore delete expire file error", e);
            } finally {
                flatFile.getFileLock().unlock();
            }
        }
        log.info("FlatFileStore schedule delete expired file, count={}, cost={}ms",
            fileList.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    /** 返回元数据存储。 */
    public MetadataStore getMetadataStore() {
        return metadataStore;
    }

    /** 返回存储配置。 */
    public MessageStoreConfig getStoreConfig() {
        return storeConfig;
    }

    /** 返回扁平文件工厂。 */
    public FlatFileFactory getFlatFileFactory() {
        return flatFileFactory;
    }

    /** 获取或创建指定 Queue 的 FlatMessageFile。 */
    public FlatMessageFile computeIfAbsent(MessageQueue messageQueue) {
        return flatFileConcurrentMap.computeIfAbsent(messageQueue,
            mq -> new FlatMessageFile(flatFileFactory, mq.getTopic(), mq.getQueueId()));
    }

    /** 返回已存在的 FlatMessageFile，不存在则 null。 */
    public FlatMessageFile getFlatFile(MessageQueue messageQueue) {
        return flatFileConcurrentMap.get(messageQueue);
    }

    /** 深拷贝当前全部 FlatMessageFile 列表。 */
    public ImmutableList<FlatMessageFile> deepCopyFlatFileToList() {
        return ImmutableList.copyOf(flatFileConcurrentMap.values());
    }

    /** 关闭全部 FlatMessageFile。 */
    public void shutdown() {
        flatFileConcurrentMap.values().forEach(FlatMessageFile::shutdown);
    }

    /** 移除并销毁指定 Queue 的文件。 */
    public void destroyFile(MessageQueue mq) {
        if (mq == null) {
            return;
        }

        FlatMessageFile flatFile = flatFileConcurrentMap.remove(mq);
        if (flatFile != null) {
            flatFile.shutdown();
            flatFile.destroy();
        }
        log.info("FlatFileStore destroy file, topic={}, queueId={}", mq.getTopic(), mq.getQueueId());
    }

    /** 关闭并销毁全部文件后清空映射。 */
    public void destroy() {
        this.shutdown();
        flatFileConcurrentMap.values().forEach(FlatMessageFile::destroy);
        flatFileConcurrentMap.clear();
    }
}
