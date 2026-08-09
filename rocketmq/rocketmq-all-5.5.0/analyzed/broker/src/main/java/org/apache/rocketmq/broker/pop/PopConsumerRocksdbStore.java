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
package org.apache.rocketmq.broker.pop;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.config.AbstractRocksDBStorage;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.store.rocksdb.RocksDBOptionsFactory;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompactRangeOptions;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.Slice;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 RocksDB 的 POP 消费状态存储：使用 popState 列族持久化 {@link PopConsumerRecord}。
 * Key 前缀为可见性超时时间戳，支持按时间范围扫描过期记录。
 */
public class PopConsumerRocksdbStore extends AbstractRocksDBStorage implements PopConsumerKVStore {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_POP_LOGGER_NAME);
    private static final byte[] COLUMN_FAMILY_NAME = "popState".getBytes(StandardCharsets.UTF_8);

    private WriteOptions writeOptions;
    private WriteOptions deleteOptions;
    protected ColumnFamilyHandle columnFamilyHandle;

    /** @param filePath RocksDB 数据目录路径 */
    public PopConsumerRocksdbStore(String filePath) {
        super(filePath);
    }

    // https://www.cnblogs.com/renjc/p/rocksdb-class-db.html
    // https://github.com/johnzeng/rocksdb-doc-cn/blob/master/doc/RocksDB-Tuning-Guide.md
    /** 初始化 DB/Write/Compact 选项，写路径开启 sync 保证 POP 状态可恢复。 */
    protected void initOptions() {
        this.options = RocksDBOptionsFactory.createDBOptions();

        this.writeOptions = new WriteOptions();
        this.writeOptions.setSync(true);
        this.writeOptions.setDisableWAL(false);
        this.writeOptions.setNoSlowdown(false);

        this.deleteOptions = new WriteOptions();
        this.deleteOptions.setSync(true);
        this.deleteOptions.setDisableWAL(false);
        this.deleteOptions.setNoSlowdown(false);

        this.compactRangeOptions = new CompactRangeOptions();
        this.compactRangeOptions.setBottommostLevelCompaction(
            CompactRangeOptions.BottommostLevelCompaction.kForce);
        this.compactRangeOptions.setAllowWriteStall(true);
        this.compactRangeOptions.setExclusiveManualCompaction(false);
        this.compactRangeOptions.setChangeLevel(true);
        this.compactRangeOptions.setTargetLevel(-1);
        this.compactRangeOptions.setMaxSubcompactions(4);
    }

    @Override
    /** 创建 popState 列族并打开数据库。 */
    protected boolean postLoad() {
        try {
            UtilAll.ensureDirOK(this.dbPath);
            initOptions();

            // init column family here
            ColumnFamilyOptions defaultOptions = RocksDBOptionsFactory.createPopCFOptions();
            ColumnFamilyOptions popStateOptions = RocksDBOptionsFactory.createPopCFOptions();
            this.cfOptions.add(defaultOptions);
            this.cfOptions.add(popStateOptions);

            List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
            cfDescriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, defaultOptions));
            cfDescriptors.add(new ColumnFamilyDescriptor(COLUMN_FAMILY_NAME, popStateOptions));
            this.open(cfDescriptors);
            this.defaultCFHandle = cfHandles.get(0);
            this.columnFamilyHandle = cfHandles.get(1);

            log.debug("PopConsumerRocksdbStore init, filePath={}", this.dbPath);
        } catch (final Exception e) {
            log.error("PopConsumerRocksdbStore init error, filePath={}", this.dbPath, e);
            return false;
        }
        return true;
    }

    /** 返回 RocksDB 数据目录。 */
    public String getFilePath() {
        return this.dbPath;
    }

    @Override
    /** 批量 Put POP 记录到 popState 列族。 */
    public void writeRecords(List<PopConsumerRecord> consumerRecordList) {
        if (!consumerRecordList.isEmpty()) {
            try (WriteBatch writeBatch = new WriteBatch()) {
                for (PopConsumerRecord record : consumerRecordList) {
                    writeBatch.put(columnFamilyHandle, record.getKeyBytes(), record.getValueBytes());
                }
                this.db.write(writeOptions, writeBatch);
            } catch (RocksDBException e) {
                throw new RuntimeException("Write record error", e);
            }
        }
    }

    @Override
    /** 批量 Delete POP 记录（ACK 或超时清理时调用）。 */
    public void deleteRecords(List<PopConsumerRecord> consumerRecordList) {
        if (!consumerRecordList.isEmpty()) {
            try (WriteBatch writeBatch = new WriteBatch()) {
                for (PopConsumerRecord record : consumerRecordList) {
                    writeBatch.delete(columnFamilyHandle, record.getKeyBytes());
                }
                this.db.write(deleteOptions, writeBatch);
            } catch (RocksDBException e) {
                throw new RuntimeException("Delete record error", e);
            }
        }
    }

    @Override
    // https://github.com/facebook/rocksdb/issues/10300
    /** 按可见性超时时间范围扫描过期记录，利用 key 前缀索引加速迭代。 */
    public List<PopConsumerRecord> scanExpiredRecords(long lower, long upper, int maxCount) {
        // In RocksDB, we can use SstPartitionerFixedPrefixFactory in cfOptions
        // and new ColumnFamilyOptions().useFixedLengthPrefixExtractor() to
        // configure prefix indexing to improve the performance of scans.
        // However, in the current implementation, this is not the bottleneck.
        List<PopConsumerRecord> consumerRecordList = new ArrayList<>();
        try (ReadOptions scanOptions = new ReadOptions()
            .setIterateLowerBound(new Slice(ByteBuffer.allocate(Long.BYTES).putLong(lower).array()))
            .setIterateUpperBound(new Slice(ByteBuffer.allocate(Long.BYTES).putLong(upper).array()));
             RocksIterator iterator = db.newIterator(this.columnFamilyHandle, scanOptions)) {
            iterator.seek(ByteBuffer.allocate(Long.BYTES).putLong(lower).array());
            while (iterator.isValid() && consumerRecordList.size() < maxCount) {
                consumerRecordList.add(PopConsumerRecord.decode(iterator.value()));
                iterator.next();
            }
        }
        return consumerRecordList;
    }

    @Override
    /** 关闭 WriteOptions 与列族句柄。 */
    protected void preShutdown() {
        if (this.writeOptions != null) {
            this.writeOptions.close();
        }
        if (this.deleteOptions != null) {
            this.deleteOptions.close();
        }
        if (this.columnFamilyHandle != null) {
            this.columnFamilyHandle.close();
        }
    }
}
