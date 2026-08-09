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
package org.apache.rocketmq.common.config;

import com.google.common.base.Strings;
import java.io.File;
import org.apache.rocketmq.common.UtilAll;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.DataBlockIndexType;
import org.rocksdb.IndexType;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.LRUCache;
import org.rocksdb.RateLimiter;
import org.rocksdb.SkipListMemTableConfig;
import org.rocksdb.Statistics;
import org.rocksdb.StatsLevel;
import org.rocksdb.StringAppendOperator;
import org.rocksdb.WALRecoveryMode;
import org.rocksdb.util.SizeUnit;

/**
 * RocketMQ 配置 RocksDB 的列族与 DB 选项工厂：块缓存、Compaction、WAL 刷盘策略等。
 */
public class ConfigHelper {
    /** 创建配置存储专用列族选项（块表、Bloom 过滤、Level Compaction 等）。 */
    public static ColumnFamilyOptions createConfigColumnFamilyOptions() {
        BlockBasedTableConfig blockBasedTableConfig = new BlockBasedTableConfig().
            setFormatVersion(5).
            setIndexType(IndexType.kBinarySearch).
            setDataBlockIndexType(DataBlockIndexType.kDataBlockBinarySearch).
            setBlockSize(32 * SizeUnit.KB).
            setFilterPolicy(new BloomFilter(16, false)).
            // 是否将索引/过滤块放入块缓存。
            setCacheIndexAndFilterBlocks(true).
            setCacheIndexAndFilterBlocksWithHighPriority(true).
            setPinL0FilterAndIndexBlocksInCache(false).
            setPinTopLevelIndexAndFilter(true).
            setBlockCache(new LRUCache(4 * SizeUnit.MB, 8, false)).
            setWholeKeyFiltering(true);

        ColumnFamilyOptions options = new ColumnFamilyOptions();
        return options.setMaxWriteBufferNumber(4).
            setWriteBufferSize(64 * SizeUnit.MB).
            setMinWriteBufferNumberToMerge(1).
            setTableFormatConfig(blockBasedTableConfig).
            setMemTableConfig(new SkipListMemTableConfig()).
            setCompressionType(CompressionType.NO_COMPRESSION).
            setNumLevels(7).
            setCompactionStyle(CompactionStyle.LEVEL).
            setLevel0FileNumCompactionTrigger(4).
            setLevel0SlowdownWritesTrigger(8).
            setLevel0StopWritesTrigger(12).
            // Compaction 目标文件大小。
            setTargetFileSizeBase(64 * SizeUnit.MB).
            setTargetFileSizeMultiplier(2).
            // L1 层文件总大小的上限（字节）。
            setMaxBytesForLevelBase(256 * SizeUnit.MB).
            setMaxBytesForLevelMultiplier(2).
            setMergeOperator(new StringAppendOperator()).
            setInplaceUpdateSupport(true);
    }

    /** 创建配置库 DB 选项：手动 WAL 刷盘、限速、Direct IO 等。 */
    public static DBOptions createConfigDBOptions() {
        // 调参参考 RocksDB Tuning Guide 及内部 JStorm 实践。
        // and http://gitlab.alibaba-inc.com/aloha/aloha/blob/branch_2_5_0/jstorm-core/src/main/java/com/alibaba/jstorm/cache/rocksdb/RocksDbOptionsFactory.java
        DBOptions options = new DBOptions();
        Statistics statistics = new Statistics();
        statistics.setStatsLevel(StatsLevel.EXCEPT_DETAILED_TIMERS);
        return options.
            setDbLogDir(getDBLogDir()).
            setInfoLogLevel(InfoLogLevel.INFO_LEVEL).
            setWalRecoveryMode(WALRecoveryMode.SkipAnyCorruptedRecords).
            /*
             * 启用手动 WAL 刷盘，在可靠性与性能间折中：
             * 对 Topic/订阅等关键元数据每次写入都会 flush-and-sync；
             * 对 commit/pull 位点推进则按 N 次写入（默认 1024）或写入老化批量刷盘，类似 OS 页缓存机制。
             */
            setManualWalFlush(true).
            // 仅在有多个列族时生效。
            // https://github.com/facebook/rocksdb/issues/4180
            // setMaxTotalWalSize(1024 * SizeUnit.MB).
            setDbWriteBufferSize(128 * SizeUnit.MB).
            setBytesPerSync(SizeUnit.MB).
            setWalBytesPerSync(SizeUnit.MB).
            setCreateIfMissing(true).
            setCreateMissingColumnFamilies(true).
            setMaxOpenFiles(-1).
            setMaxLogFileSize(SizeUnit.GB).
            setKeepLogFileNum(5).
            setMaxManifestFileSize(SizeUnit.GB).
            setAllowConcurrentMemtableWrite(false).
            setStatistics(statistics).
            setStatsDumpPeriodSec(600).
            setMaxBackgroundJobs(32).
            setMaxSubcompactions(4).
            setParanoidChecks(true).
            setDelayedWriteRate(16 * SizeUnit.MB).
            setRateLimiter(new RateLimiter(100 * SizeUnit.MB)).
            setUseDirectIoForFlushAndCompaction(true).
            setUseDirectReads(true);
    }

    /** 在用户目录、临时目录或 /data 下递归创建并返回 RocketMQ RocksDB 日志目录。 */
    public static String getDBLogDir() {
        String[] rootPaths = new String[] {
            System.getProperty("user.home"),
            System.getProperty("java.io.tmpdir"),
            File.separator + "data"
        };
        for (String rootPath : rootPaths) {
            // 参考 Bazel 测试百科：并非所有目录在测试环境中可写。
            // 跳过不存在或不可写的根路径。
            if (Strings.isNullOrEmpty(rootPath)) {
                continue;
            }
            File rootPathFile = new File(rootPath);
            if (!rootPathFile.exists() || !rootPathFile.canWrite()) {
                continue;
            }
            String logDirectory = rootPath + File.separator + "logs" + File.separator + "rocketmqlogs";
            // 递归创建日志目录。
            UtilAll.ensureDirOK(logDirectory);
            return logDirectory;
        }
        throw new RuntimeException("Failed to get log directory");
    }
}
