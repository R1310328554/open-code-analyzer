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
package org.apache.rocketmq.tieredstore.index;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.store.logfile.DefaultMappedFile;
import org.apache.rocketmq.store.logfile.MappedFile;
import org.apache.rocketmq.tieredstore.MessageStoreConfig;
import org.apache.rocketmq.tieredstore.common.AppendResult;
import org.apache.rocketmq.tieredstore.file.FlatAppendFile;
import org.apache.rocketmq.tieredstore.file.FlatFileFactory;
import org.apache.rocketmq.tieredstore.provider.FileSegment;
import org.apache.rocketmq.tieredstore.util.MessageStoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分层存储索引文件管理服务：维护按时间戳排序的 {@link IndexFile} 表，负责本地/远程恢复、键写入、异步查询、压缩上传与过期清理。
 */
public class IndexStoreService extends ServiceThread implements IndexService {

    private static final Logger log = LoggerFactory.getLogger(MessageStoreUtil.TIERED_STORE_LOGGER_NAME);

    /** 分层索引文件根目录名。 */
    public static final String FILE_DIRECTORY_NAME = "tiered_index_file";
    /** 索引压缩过程中的临时目录名。 */
    public static final String FILE_COMPACTED_DIRECTORY_NAME = "compacting";

    /**
     * 索引文件状态示例（timeStoreTable 中）：upload, sealed, unsealed。
     */
    /** 分层存储配置。 */
    private final MessageStoreConfig storeConfig;
    /** 按起始时间戳索引的 {@link IndexFile} 有序表。 */
    private final ConcurrentSkipListMap<Long /* timestamp */, IndexFile> timeStoreTable;
    /** 索引表读写锁，保护查询与压缩/upload 互斥。 */
    private final ReadWriteLock readWriteLock;
    /** 已完成压缩/upload 的最大时间戳游标。 */
    private final AtomicLong compactTimestamp;
    /** 远程索引 FlatFile 路径标识。 */
    private final String filePath;
    /** 分层 FlatFile 分配器。 */
    private final FlatFileFactory fileAllocator;
    /** 恢复时若表为空是否自动创建新索引文件。 */
    private final boolean autoCreateNewFile;

    /** 当前写入中的索引文件。 */
    private volatile IndexFile currentWriteFile;
    /** 远程索引追加文件句柄。 */
    private volatile FlatAppendFile flatAppendFile;

    public IndexStoreService(FlatFileFactory flatFileFactory, String filePath) {
        this(flatFileFactory, filePath, true);
    }

    public IndexStoreService(FlatFileFactory flatFileFactory, String filePath, boolean autoCreateNewFile) {
        this.storeConfig = flatFileFactory.getStoreConfig();
        this.filePath = filePath;
        this.fileAllocator = flatFileFactory;
        this.timeStoreTable = new ConcurrentSkipListMap<>();
        this.compactTimestamp = new AtomicLong(0L);
        this.readWriteLock = new ReentrantReadWriteLock();
        this.autoCreateNewFile = autoCreateNewFile;
    }

    @Override
    public void start() {
        this.recover();
        super.start();
    }

    /** 将旧版固定文件名索引转换为时间戳命名。 */
    private void doConvertOldFormatFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            MappedFile mappedFile = new DefaultMappedFile(file.getPath(), (int) file.length());
            long timestamp = mappedFile.getMappedByteBuffer().getLong(IndexStoreFile.INDEX_BEGIN_TIME_STAMP);
            if (timestamp <= 0) {
                mappedFile.destroy(TimeUnit.SECONDS.toMillis(10));
            } else {
                mappedFile.renameTo(String.valueOf(new File(file.getParent(), String.valueOf(timestamp))));
                mappedFile.shutdown(TimeUnit.SECONDS.toMillis(10));
            }
        } catch (Exception e) {
            log.error("IndexStoreService do convert old format error, file: {}", filePath, e);
        }
    }

    /** 从本地与远程恢复索引文件表。 */
    private void recover() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // 删除压缩临时目录
        UtilAll.deleteFile(new File(Paths.get(storeConfig.getStorePathRootDir(),
            FILE_DIRECTORY_NAME, FILE_COMPACTED_DIRECTORY_NAME).toString()));

        // 恢复本地索引文件
        File dir = new File(Paths.get(storeConfig.getStorePathRootDir(), FILE_DIRECTORY_NAME).toString());
        this.doConvertOldFormatFile(Paths.get(dir.getPath(), "0000").toString());
        this.doConvertOldFormatFile(Paths.get(dir.getPath(), "1111").toString());
        File[] files = dir.listFiles();

        if (files != null) {
            List<File> fileList = Arrays.asList(files);
            fileList.sort(Comparator.comparing(File::getName));

            for (File file : fileList) {
                if (file.isDirectory() || !StringUtils.isNumeric(file.getName())) {
                    continue;
                }

                try {
                    IndexFile indexFile = new IndexStoreFile(storeConfig, Long.parseLong(file.getName()));
                    timeStoreTable.put(indexFile.getTimestamp(), indexFile);
                    log.info("IndexStoreService recover load local file, timestamp: {}", indexFile.getTimestamp());
                } catch (Exception e) {
                    log.error("IndexStoreService recover, load local file error", e);
                }
            }
        }

        if (this.autoCreateNewFile && this.timeStoreTable.isEmpty()) {
            this.createNewIndexFile(System.currentTimeMillis());
        }

        if (this.timeStoreTable.isEmpty()) {
            this.setCompactTimestamp(Long.MAX_VALUE);
        } else {
            this.currentWriteFile = this.timeStoreTable.lastEntry().getValue();
            this.setCompactTimestamp(this.timeStoreTable.firstKey() - 1);
        }

        // 恢复远程已上传索引段
        this.flatAppendFile = fileAllocator.createFlatFileForIndexFile(filePath);

        for (FileSegment fileSegment : flatAppendFile.getFileSegmentList()) {
            IndexFile indexFile = new IndexStoreFile(storeConfig, fileSegment);
            IndexFile localFile = timeStoreTable.get(indexFile.getTimestamp());
            if (localFile != null) {
                localFile.destroy();
            }
            timeStoreTable.put(indexFile.getTimestamp(), indexFile);
            log.info("IndexStoreService recover load remote file, timestamp: {}, end timestamp: {}",
                indexFile.getTimestamp(), indexFile.getEndTimestamp());
        }

        log.info("IndexStoreService recover finished, total: {}, cost: {}ms, directory: {}",
            timeStoreTable.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS), dir.getAbsolutePath());
    }

    /** 创建新的未封存索引文件。 */
    public void createNewIndexFile(long timestamp) {
        try {
            this.readWriteLock.writeLock().lock();
            IndexFile indexFile = this.currentWriteFile;
            if (this.timeStoreTable.containsKey(timestamp) ||
                indexFile != null && IndexFile.IndexStatusEnum.UNSEALED.equals(indexFile.getFileStatus())) {
                return;
            }
            IndexStoreFile newStoreFile = new IndexStoreFile(storeConfig, timestamp);
            this.timeStoreTable.put(timestamp, newStoreFile);
            this.currentWriteFile = newStoreFile;
            log.info("IndexStoreService construct next file, timestamp: {}", timestamp);
        } catch (Exception e) {
            log.error("IndexStoreService construct next file, timestamp: {}", timestamp, e);
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    @VisibleForTesting
    public ConcurrentSkipListMap<Long, IndexFile> getTimeStoreTable() {
        return timeStoreTable;
    }

    @Override
    public AppendResult putKey(
        String topic, int topicId, int queueId, Set<String> keySet, long offset, int size, long timestamp) {

        if (StringUtils.isBlank(topic)) {
            return AppendResult.UNKNOWN_ERROR;
        }

        if (keySet == null || keySet.isEmpty()) {
            return AppendResult.SUCCESS;
        }

        for (int i = 0; i < 3; i++) {
            AppendResult result = this.currentWriteFile.putKey(
                topic, topicId, queueId, keySet, offset, size, timestamp);

            if (AppendResult.SUCCESS.equals(result)) {
                return AppendResult.SUCCESS;
            } else if (AppendResult.FILE_FULL.equals(result)) {
                // 用当前时间创建新文件以保证时间序
                this.createNewIndexFile(System.currentTimeMillis());
            }
        }

        log.error("IndexStoreService put key three times return error, topic: {}, topicId: {}, " +
            "queueId: {}, keySize: {}, timestamp: {}", topic, topicId, queueId, keySet.size(), timestamp);
        return AppendResult.SUCCESS;
    }

    @Override
    public CompletableFuture<List<IndexItem>> queryAsync(
        String topic, String key, int maxCount, long beginTime, long endTime) {

        if (beginTime > endTime) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        CompletableFuture<List<IndexItem>> future = new CompletableFuture<>();
        try {
            readWriteLock.readLock().lock();
            ConcurrentNavigableMap<Long, IndexFile> pendingMap =
                this.timeStoreTable.subMap(beginTime, true, endTime, true);
            List<CompletableFuture<Void>> futureList = new ArrayList<>(pendingMap.size());
            ConcurrentSkipListMap<String /* queueId-offset */, IndexItem> result = new ConcurrentSkipListMap<>();

            for (Map.Entry<Long, IndexFile> entry : pendingMap.descendingMap().entrySet()) {
                CompletableFuture<Void> completableFuture = entry.getValue()
                    .queryAsync(topic, key, maxCount, beginTime, endTime)
                    .thenAccept(itemList -> itemList.forEach(indexItem -> {
                        if (result.size() < maxCount) {
                            result.put(String.format(
                                "%d-%20d", indexItem.getQueueId(), indexItem.getOffset()), indexItem);
                        }
                    }));
                futureList.add(completableFuture);
            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .whenComplete((v, t) -> {
                    // 尽量返回部分查询结果
                    // 而非直接抛出异常中断
                    if (t != null) {
                        log.error("IndexStoreService#queryAsync, topicId={}, key={}, maxCount={}, timestamp={}-{}",
                            topic, key, maxCount, beginTime, endTime, t);
                    }
                    List<IndexItem> resultList = new ArrayList<>(result.values());
                    future.complete(resultList.subList(0, Math.min(resultList.size(), maxCount)));
                });
        } catch (Exception e) {
            log.error("IndexStoreService#queryAsync, topicId={}, key={}, maxCount={}, timestamp={}-{}",
                topic, key, maxCount, beginTime, endTime, e);
            future.completeExceptionally(e);
        } finally {
            readWriteLock.readLock().unlock();
        }
        return future;
    }

    @Override
    public void forceUpload() {
        try {
            readWriteLock.writeLock().lock();
            while (true) {
                Map.Entry<Long, IndexFile> entry =
                    this.timeStoreTable.higherEntry(this.compactTimestamp.get());
                if (entry == null) {
                    break;
                }
                if (this.doCompactThenUploadFile(entry.getValue())) {
                    this.setCompactTimestamp(entry.getValue().getTimestamp());
                    // 文件总数有限，短暂 sleep 避免 IO 过快
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            }
        } catch (Exception e) {
            log.error("IndexStoreService force upload error", e);
            throw new RuntimeException(e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /** 压缩单个索引文件并上传至远程存储。 */
    public boolean doCompactThenUploadFile(IndexFile indexFile) {
        if (IndexFile.IndexStatusEnum.UPLOAD.equals(indexFile.getFileStatus())) {
            log.error("IndexStoreService file status not correct, so skip, timestamp: {}, status: {}",
                indexFile.getTimestamp(), indexFile.getFileStatus());
            indexFile.destroy();
            return true;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        if (flatAppendFile.getCommitOffset() == flatAppendFile.getAppendOffset()) {
            ByteBuffer byteBuffer = indexFile.doCompaction();
            if (byteBuffer == null) {
                log.error("IndexStoreService found compaction buffer is null, timestamp: {}", indexFile.getTimestamp());
                return false;
            }
            flatAppendFile.rollingNewFile(Math.max(0L, flatAppendFile.getAppendOffset()));
            flatAppendFile.append(byteBuffer, indexFile.getTimestamp());
            flatAppendFile.getFileToWrite().setMinTimestamp(indexFile.getTimestamp());
            flatAppendFile.getFileToWrite().setMaxTimestamp(indexFile.getEndTimestamp());
        }
        boolean result = flatAppendFile.commitAsync().join();

        List<FileSegment> fileSegmentList = flatAppendFile.getFileSegmentList();
        FileSegment fileSegment = fileSegmentList.get(fileSegmentList.size() - 1);
        if (!result || fileSegment == null || fileSegment.getMinTimestamp() != indexFile.getTimestamp()) {
            log.warn("IndexStoreService upload compacted file error, timestamp: {}", indexFile.getTimestamp());
            return false;
        } else {
            log.info("IndexStoreService upload compacted file success, timestamp: {}", indexFile.getTimestamp());
        }

        readWriteLock.writeLock().lock();
        try {
            IndexFile storeFile = new IndexStoreFile(storeConfig, fileSegment);
            timeStoreTable.put(storeFile.getTimestamp(), storeFile);
            indexFile.destroy();
        } catch (Exception e) {
            log.error("IndexStoreService rolling file error, timestamp: {}, cost: {}ms",
                indexFile.getTimestamp(), stopwatch.elapsed(TimeUnit.MILLISECONDS), e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return true;
    }

    /** 删除过期且已上传的索引文件。 */
    public void destroyExpiredFile(long expireTimestamp) {
        // 从 timeStoreTable 删除过期条目
        readWriteLock.writeLock().lock();
        try {
            flatAppendFile.destroyExpiredFile(expireTimestamp);
            timeStoreTable.entrySet().removeIf(entry ->
                IndexFile.IndexStatusEnum.UPLOAD.equals(entry.getValue().getFileStatus()) &&
                    (flatAppendFile.getFileSegmentList().isEmpty() ||
                        entry.getKey() < flatAppendFile.getMinTimestamp()));
            int tableSize = (int) timeStoreTable.entrySet().stream()
                .filter(entry -> IndexFile.IndexStatusEnum.UPLOAD.equals(entry.getValue().getFileStatus()))
                .count();
            log.debug("IndexStoreService delete file, timestamp={}, remote={}, table={}, all={}",
                expireTimestamp, flatAppendFile.getFileSegmentList().size(), tableSize, timeStoreTable.size());
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /** 销毁本地未上传与远程索引资源。 */
    public void destroy() {
        readWriteLock.writeLock().lock();
        try {
            // 销毁本地未上传索引文件
            for (Map.Entry<Long, IndexFile> entry : timeStoreTable.entrySet()) {
                IndexFile indexFile = entry.getValue();
                if (IndexFile.IndexStatusEnum.UPLOAD.equals(indexFile.getFileStatus())) {
                    continue;
                }
                indexFile.destroy();
            }
            // 销毁远程 FlatAppendFile
            if (flatAppendFile != null) {
                flatAppendFile.destroy();
            }
        } catch (Exception e) {
            log.error("IndexStoreService destroy all file error", e);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public String getServiceName() {
        return IndexStoreService.class.getSimpleName() + "_" + this.storeConfig.getBrokerName();
    }

    /** 更新压缩进度时间戳游标。 */
    public void setCompactTimestamp(long timestamp) {
        this.compactTimestamp.set(timestamp);
        log.debug("IndexStoreService set compact timestamp to: {}", timestamp);
    }

    /** 获取下一个待压缩的已封存索引文件。 */
    protected IndexFile getNextSealedFile() {
        Map.Entry<Long, IndexFile> entry =
            this.timeStoreTable.higherEntry(this.compactTimestamp.get());
        if (entry != null && entry.getKey() < this.timeStoreTable.lastKey()) {
            return entry.getValue();
        }
        return null;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        // 等待上传完成后清空索引表
        while (!this.timeStoreTable.isEmpty()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void forceShutdown() {
        super.shutdown();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            try {
                long expireTimestamp = System.currentTimeMillis()
                    - TimeUnit.HOURS.toMillis(storeConfig.getTieredStoreFileReservedTime());
                this.destroyExpiredFile(expireTimestamp);
                IndexFile indexFile = this.getNextSealedFile();
                if (indexFile != null) {
                    if (this.doCompactThenUploadFile(indexFile)) {
                        this.setCompactTimestamp(indexFile.getTimestamp());
                        continue;
                    }
                }
            } catch (Throwable e) {
                log.error("IndexStoreService running error", e);
            }
            this.waitForRunning(TimeUnit.SECONDS.toMillis(10));
        }
        readWriteLock.writeLock().lock();
        try {
            if (autoCreateNewFile) {
                this.forceUpload();
            }
        } catch (Exception e) {
            log.error("IndexStoreService shutdown error", e);
        } finally {
            this.timeStoreTable.forEach((timestamp, file) -> file.shutdown());
            this.timeStoreTable.clear();
            readWriteLock.writeLock().unlock();
        }

        log.info(this.getServiceName() + " service shutdown");
    }
}
