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
package org.apache.rocketmq.broker.config.v2;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import io.netty.util.internal.PlatformDependent;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.config.AbstractRocksDBStorage;
import org.apache.rocketmq.common.config.ConfigHelper;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.FlushOptions;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Slice;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

/**
 * Broker 配置 RocksDB 存储层：以 KV 形式持久化 Topic、订阅组等元数据，
 * 并负责 WAL 刷盘与同步。键值布局参考 TiDB KV 映射模型。
 *
 * @see <a href="https://book.tidb.io/session1/chapter3/tidb-kv-to-relation.html">Table, Key Value Mapping</a>
 */
public class ConfigStorage extends AbstractRocksDBStorage {

    public static final String DATA_VERSION_KEY = "data_version";
    public static final byte[] DATA_VERSION_KEY_BYTES = DATA_VERSION_KEY.getBytes(StandardCharsets.UTF_8);

    private final ScheduledExecutorService scheduledExecutorService;

    /** 自上次 WAL 刷盘以来累计的写操作次数。 */
    private final AtomicInteger writeOpsCounter;

    private final AtomicLong estimateWalFileSize = new AtomicLong(0L);

    private final MessageStoreConfig messageStoreConfig;

    private final FlushSyncService flushSyncService;

    /** 在消息存储根目录下创建 {@code config/rdb} 子库并启动 WAL 刷盘后台线程。 */
    public ConfigStorage(MessageStoreConfig messageStoreConfig) {
        super(messageStoreConfig.getStorePathRootDir() + File.separator + "config" + File.separator + "rdb");
        this.messageStoreConfig = messageStoreConfig;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("config-storage-%d")
            .build();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1, threadFactory);
        writeOpsCounter = new AtomicInteger(0);
        this.flushSyncService = new FlushSyncService();
        this.flushSyncService.setDaemon(true);
    }

    /** 周期性输出 Netty 池化内存分配器指标。 */
    private void statNettyMemory() {
        PooledByteBufAllocatorMetric metric = AbstractRocksDBStorage.POOLED_ALLOCATOR.metric();
        LOGGER.info("Netty Memory Usage: {}", metric);
    }

    /** 启动 RocksDB 并调度统计任务与 {@link FlushSyncService}。 */
    @Override
    public synchronized boolean start() {
        boolean started = super.start();
        if (started) {
            scheduledExecutorService.scheduleWithFixedDelay(() -> statRocksdb(LOGGER), 1, 10, TimeUnit.SECONDS);
            scheduledExecutorService.scheduleWithFixedDelay(this::statNettyMemory, 10, 10, TimeUnit.SECONDS);
            this.flushSyncService.start();
        } else {
            LOGGER.error("Failed to start config storage");
        }
        return started;
    }

    /** 校验 Unsafe 可用性、创建目录并打开默认列族。 */
    @Override
    protected boolean postLoad() {
        if (!PlatformDependent.hasUnsafe()) {
            LOGGER.error("Unsafe not available and POOLED_ALLOCATOR cannot work correctly");
            return false;
        }
        try {
            UtilAll.ensureDirOK(this.dbPath);
            initOptions();
            List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();

            ColumnFamilyOptions defaultOptions = ConfigHelper.createConfigColumnFamilyOptions();
            this.cfOptions.add(defaultOptions);
            cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, defaultOptions));

            // Start RocksDB instance
            open(cfDescriptors);

            this.defaultCFHandle = cfHandles.get(0);
        } catch (Exception e) {
            AbstractRocksDBStorage.LOGGER.error("postLoad Failed. {}", this.dbPath, e);
            return false;
        }
        return true;
    }

    /** 关闭定时任务与 WAL 刷盘服务。 */
    @Override
    protected void preShutdown() {
        scheduledExecutorService.shutdown();
        flushSyncService.shutdown();
    }

    /** 使用 {@link ConfigHelper} 初始化配置库专用 DB 选项。 */
    protected void initOptions() {
        this.options = ConfigHelper.createConfigDBOptions();
        super.initOptions();
    }

    /** 配置 WAL 写入选项：保留 WAL、不同步 fdatasync 以避免每次写入开销。 */
    @Override
    protected void initAbleWalWriteOptions() {
        this.ableWalWriteOptions = new WriteOptions();

        // Given that fdatasync is kind of expensive, sync-WAL for every write cannot be afforded.
        this.ableWalWriteOptions.setSync(false);

        // We need WAL for config changes
        this.ableWalWriteOptions.setDisableWAL(false);

        // No fast failure on block, wait synchronously even if there is wait for the write request
        this.ableWalWriteOptions.setNoSlowdown(false);
    }

    /** 按 ByteBuffer 键读取默认列族中的配置值。 */
    public byte[] get(ByteBuffer key) throws RocksDBException {
        byte[] keyBytes = new byte[key.remaining()];
        key.get(keyBytes);
        return super.get(getDefaultCFHandle(), totalOrderReadOptions, keyBytes);
    }

    /** 批量写入并累计写操作计数与 WAL 估算大小。 */
    public void write(WriteBatch writeBatch) throws RocksDBException {
        db.write(ableWalWriteOptions, writeBatch);
        accountWriteOps(writeBatch.getDataSize());
    }

    /** 累加写次数与 WAL 数据量估算。 */
    private void accountWriteOps(long dataSize) {
        writeOpsCounter.incrementAndGet();
        estimateWalFileSize.addAndGet(dataSize);
    }

    /** 在 [beginKey, endKey) 范围内全序扫描配置键。 */
    public RocksIterator iterate(ByteBuffer beginKey, ByteBuffer endKey) {
        try (ReadOptions readOptions = new ReadOptions()) {
            readOptions.setTotalOrderSeek(true);
            readOptions.setTailing(false);
            readOptions.setAutoPrefixMode(true);
            // Use DirectSlice till the follow issue is fixed:
            // https://github.com/facebook/rocksdb/issues/13098
            //
            // readOptions.setIterateUpperBound(new DirectSlice(endKey));
            byte[] buf = new byte[endKey.remaining()];
            endKey.slice().get(buf);
            readOptions.setIterateUpperBound(new Slice(buf));

            RocksIterator iterator = db.newIterator(defaultCFHandle, readOptions);
            iterator.seek(beginKey.slice());
            return iterator;
        }
    }

    /**
     * WAL 刷盘同步后台服务：RocksDB 写入经应用缓冲、页缓存到磁盘三阶段，
     * 在 {@code manual_wal_flush} 模式下需手动调用 {@code FlushWAL}/{@code SyncWAL}。
     * <p>
     * 参见 <a href="https://rocksdb.org/blog/2017/08/25/flushwal.html">Flush And Sync WAL</a>
     */
    class FlushSyncService extends ServiceThread {

        private long lastSyncTime = 0;

        private static final long MAX_SYNC_INTERVAL_IN_MILLIS = 100;

        private final Stopwatch stopwatch = Stopwatch.createUnstarted();

        private final FlushOptions flushOptions = new FlushOptions();

        /** 返回服务线程名称。 */
        @Override
        public String getServiceName() {
            return "FlushSyncService";
        }

        /** 周期性刷 WAL，退出前执行最终同步。 */
        @Override
        public void run() {
            flushOptions.setAllowWriteStall(false);
            flushOptions.setWaitForFlush(true);
            log.info("{} service started", this.getServiceName());
            while (!this.isStopped()) {
                try {
                    this.waitForRunning(10);
                    this.flushAndSyncWAL(false);
                } catch (Exception e) {
                    log.warn("{} service has exception. ", this.getServiceName(), e);
                }
            }
            try {
                flushAndSyncWAL(true);
            } catch (Exception e) {
                log.warn("{} raised an exception while performing flush-and-sync WAL on exit",
                    this.getServiceName(), e);
            }
            flushOptions.close();
            log.info("{} service end", this.getServiceName());
        }

        /** 按写次数、时间间隔或 WAL 滚动阈值触发刷盘与同步。 */
        private void flushAndSyncWAL(boolean onExit) throws RocksDBException {
            int writeOps = writeOpsCounter.get();
            if (0 == writeOps) {
                // No write ops to flush
                return;
            }

            /*
             * Normally, when MemTables become full then immutable, RocksDB threads will automatically flush them to L0
             * SST files. The use case here is different: the MemTable may never get full and immutable given that the
             * volume of data involved is relatively small. Further, we are constantly modifying the key-value pairs and
             * generating WAL entries. The WAL file size can grow up to dozens of gigabytes without manual triggering of
             * flush.
             */
            if (ConfigStorage.this.estimateWalFileSize.get() >= messageStoreConfig.getRocksdbWalFileRollingThreshold()) {
                ConfigStorage.this.flush(flushOptions);
                estimateWalFileSize.set(0L);
            }

            // Flush and Sync WAL if we have committed enough writes
            if (writeOps >= messageStoreConfig.getRocksdbFlushWalFrequency() || onExit) {
                stopwatch.reset().start();
                ConfigStorage.this.db.flushWal(true);
                long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
                writeOpsCounter.getAndAdd(-writeOps);
                lastSyncTime = System.currentTimeMillis();
                LOGGER.debug("Flush and Sync WAL of RocksDB[{}] costs {}ms, write-ops={}", dbPath, elapsed, writeOps);
                return;
            }
            // Flush and Sync WAL if some writes are out there for a period of time
            long elapsedTime = System.currentTimeMillis() - lastSyncTime;
            if (elapsedTime > MAX_SYNC_INTERVAL_IN_MILLIS) {
                stopwatch.reset().start();
                ConfigStorage.this.db.flushWal(true);
                long elapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
                LOGGER.debug("Flush and Sync WAL of RocksDB[{}] costs {}ms, write-ops={}", dbPath, elapsed, writeOps);
                writeOpsCounter.getAndAdd(-writeOps);
                lastSyncTime = System.currentTimeMillis();
            }
        }
    }
}
