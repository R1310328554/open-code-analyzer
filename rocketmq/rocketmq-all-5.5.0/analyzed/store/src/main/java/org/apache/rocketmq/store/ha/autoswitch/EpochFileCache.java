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

package org.apache.rocketmq.store.ha.autoswitch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.CheckpointFile;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.EpochEntry;

/**
 * Epoch 文件缓存：维护 Epoch 到 StartOffset 的有序映射。
 */
public class EpochFileCache {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);
    /** 保护 epochMap 的读写锁。 */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = this.readWriteLock.readLock();
    private final Lock writeLock = this.readWriteLock.writeLock();
    /** Epoch -> EpochEntry 有序表。 */
    private final TreeMap<Integer, EpochEntry> epochMap;
    /** 持久化 checkpoint 文件句柄。 */
    private CheckpointFile<EpochEntry> checkpoint;

    /** 内存模式，不写盘。 */
    public EpochFileCache() {
        this.epochMap = new TreeMap<>();
    }

    /** 指定 checkpoint 文件路径。 */
    public EpochFileCache(final String path) {
        this.epochMap = new TreeMap<>();
        this.checkpoint = new CheckpointFile<>(path, new EpochEntrySerializer());
    }

    /** 从磁盘加载 epoch 条目到缓存。 */
    public boolean initCacheFromFile() {
        this.writeLock.lock();
        try {
            final List<EpochEntry> entries = this.checkpoint.read();
            initEntries(entries);
            return true;
        } catch (final IOException e) {
            log.error("Error happen when init epoch entries from epochFile", e);
            return false;
        } finally {
            this.writeLock.unlock();
        }
    }

    /** 用给定条目初始化并刷盘。 */
    public void initCacheFromEntries(final List<EpochEntry> entries) {
        this.writeLock.lock();
        try {
            initEntries(entries);
            flush();
        } finally {
            this.writeLock.unlock();
        }
    }

    /** 重建 epochMap 并链接相邻 entry 的 endOffset。 */
    private void initEntries(final List<EpochEntry> entries) {
        this.epochMap.clear();
        EpochEntry preEntry = null;
        for (final EpochEntry entry : entries) {
            this.epochMap.put(entry.getEpoch(), entry);
            if (preEntry != null) {
                preEntry.setEndOffset(entry.getStartOffset());
            }
            preEntry = entry;
        }
    }

    /** 返回 epoch 条目数量。 */
    public int getEntrySize() {
        this.readLock.lock();
        try {
            return this.epochMap.size();
        } finally {
            this.readLock.unlock();
        }
    }

    /** 追加新 epoch 条目并刷盘。 */
    public boolean appendEntry(final EpochEntry entry) {
        this.writeLock.lock();
        try {
            if (!this.epochMap.isEmpty()) {
                final EpochEntry lastEntry = this.epochMap.lastEntry().getValue();
                if (lastEntry.getEpoch() >= entry.getEpoch() || lastEntry.getStartOffset() >= entry.getStartOffset()) {
                    log.error("The appending entry's lastEpoch or endOffset {} is not bigger than lastEntry {}, append failed", entry, lastEntry);
                    return false;
                }
                lastEntry.setEndOffset(entry.getStartOffset());
            }
            this.epochMap.put(entry.getEpoch(), new EpochEntry(entry));
            flush();
            return true;
        } finally {
            this.writeLock.unlock();
        }
    }

    /** 设置最后一个 epoch 条目的 endOffset。 */
    public void setLastEpochEntryEndOffset(final long endOffset) {
        this.writeLock.lock();
        try {
            if (!this.epochMap.isEmpty()) {
                final EpochEntry lastEntry = this.epochMap.lastEntry().getValue();
                if (lastEntry.getStartOffset() <= endOffset) {
                    lastEntry.setEndOffset(endOffset);
                }
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    /** 返回首个 epoch 条目的副本。 */
    public EpochEntry firstEntry() {
        this.readLock.lock();
        try {
            if (this.epochMap.isEmpty()) {
                return null;
            }
            return new EpochEntry(this.epochMap.firstEntry().getValue());
        } finally {
            this.readLock.unlock();
        }
    }

    /** 返回最后一个 epoch 条目的副本。 */
    public EpochEntry lastEntry() {
        this.readLock.lock();
        try {
            if (this.epochMap.isEmpty()) {
                return null;
            }
            return new EpochEntry(this.epochMap.lastEntry().getValue());
        } finally {
            this.readLock.unlock();
        }
    }

    /** 返回最大 epoch 值，空则 -1。 */
    public int lastEpoch() {
        final EpochEntry entry = lastEntry();
        if (entry != null) {
            return entry.getEpoch();
        }
        return -1;
    }

    /** 按 epoch 查询条目副本。 */
    public EpochEntry getEntry(final int epoch) {
        this.readLock.lock();
        try {
            if (this.epochMap.containsKey(epoch)) {
                final EpochEntry entry = this.epochMap.get(epoch);
                return new EpochEntry(entry);
            }
            return null;
        } finally {
            this.readLock.unlock();
        }
    }

    /** 按 CommitLog 偏移查找所属 epoch 条目。 */
    public EpochEntry findEpochEntryByOffset(final long offset) {
        this.readLock.lock();
        try {
            if (!this.epochMap.isEmpty()) {
                for (Map.Entry<Integer, EpochEntry> entry : this.epochMap.entrySet()) {
                    if (entry.getValue().getStartOffset() <= offset && entry.getValue().getEndOffset() > offset) {
                        return new EpochEntry(entry.getValue());
                    }
                }
            }
            return null;
        } finally {
            this.readLock.unlock();
        }
    }

    /** 返回严格大于给定 epoch 的下一条目。 */
    public EpochEntry nextEntry(final int epoch) {
        this.readLock.lock();
        try {
            final Map.Entry<Integer, EpochEntry> entry = this.epochMap.ceilingEntry(epoch + 1);
            if (entry != null) {
                return new EpochEntry(entry.getValue());
            }
            return null;
        } finally {
            this.readLock.unlock();
        }
    }

    /** 返回全部 epoch 条目副本列表。 */
    public List<EpochEntry> getAllEntries() {
        this.readLock.lock();
        try {
            final ArrayList<EpochEntry> result = new ArrayList<>(this.epochMap.size());
            this.epochMap.forEach((key, value) -> result.add(new EpochEntry(value)));
            return result;
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * 与 compareCache 比对，找到一致点偏移。
     *
     * @return 一致偏移，无则 -1
     */
    public long findConsistentPoint(final EpochFileCache compareCache) {
        this.readLock.lock();
        try {
            long consistentOffset = -1;
            final Map<Integer, EpochEntry> descendingMap = new TreeMap<>(this.epochMap).descendingMap();
            final Iterator<Map.Entry<Integer, EpochEntry>> iter = descendingMap.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry<Integer, EpochEntry> curLocalEntry = iter.next();
                final EpochEntry compareEntry = compareCache.getEntry(curLocalEntry.getKey());
                if (compareEntry != null && compareEntry.getStartOffset() == curLocalEntry.getValue().getStartOffset()) {
                    consistentOffset = Math.min(curLocalEntry.getValue().getEndOffset(), compareEntry.getEndOffset());
                    break;
                }
            }
            return consistentOffset;
        } finally {
            this.readLock.unlock();
        }
    }

    /** 截断 epoch >= truncateEpoch 的后缀条目。 */
    public void truncateSuffixByEpoch(final int truncateEpoch) {
        Predicate<EpochEntry> predict = entry -> entry.getEpoch() >= truncateEpoch;
        doTruncateSuffix(predict);
    }

    /** 截断 startOffset >= truncateOffset 的后缀条目。 */
    public void truncateSuffixByOffset(final long truncateOffset) {
        Predicate<EpochEntry> predict = entry -> entry.getStartOffset() >= truncateOffset;
        doTruncateSuffix(predict);
    }

    /** 按谓词删除后缀并重置末条 endOffset 为 MAX。 */
    private void doTruncateSuffix(Predicate<EpochEntry> predict) {
        this.writeLock.lock();
        try {
            this.epochMap.entrySet().removeIf(entry -> predict.test(entry.getValue()));
            final EpochEntry entry = lastEntry();
            if (entry != null) {
                entry.setEndOffset(Long.MAX_VALUE);
            }
            flush();
        } finally {
            this.writeLock.unlock();
        }
    }

    /** 截断 endOffset <= truncateOffset 的前缀条目。 */
    public void truncatePrefixByOffset(final long truncateOffset) {
        Predicate<EpochEntry> predict = entry -> entry.getEndOffset() <= truncateOffset;
        this.writeLock.lock();
        try {
            this.epochMap.entrySet().removeIf(entry -> predict.test(entry.getValue()));
            flush();
        } finally {
            this.writeLock.unlock();
        }
    }

    /** 将 epochMap 写入 checkpoint 文件。 */
    private void flush() {
        this.writeLock.lock();
        try {
            if (this.checkpoint != null) {
                final ArrayList<EpochEntry> entries = new ArrayList<>(this.epochMap.values());
                this.checkpoint.write(entries);
            }
        } catch (final IOException e) {
            log.error("Error happen when flush epochEntries to epochCheckpointFile", e);
        } finally {
            this.writeLock.unlock();
        }
    }

    /** EpochEntry 与 checkpoint 行格式互转。 */
    static class EpochEntrySerializer implements CheckpointFile.CheckpointSerializer<EpochEntry> {

        /** 格式化为 epoch-startOffset 行。 */
        @Override
        public String toLine(EpochEntry entry) {
            if (entry != null) {
                return String.format("%d-%d", entry.getEpoch(), entry.getStartOffset());
            } else {
                return null;
            }
        }

        /** 从 checkpoint 行解析 EpochEntry。 */
        @Override
        public EpochEntry fromLine(String line) {
            final String[] arr = line.split("-");
            if (arr.length == 2) {
                final int epoch = Integer.parseInt(arr[0]);
                final long startOffset = Long.parseLong(arr[1]);
                return new EpochEntry(epoch, startOffset);
            }
            return null;
        }
    }
}
