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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.utils.ConcurrentHashMapUtils;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;

/**
 * 基于 RocksDB 的配置键值存储：多列族、批量写入、只读/读写模式及全局实例缓存。
 */
public class ConfigRocksDBStorage extends AbstractRocksDBStorage {
    /** 列族名与字符串键使用的字符集。 */
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    /** 按 dbPath 缓存的 ConfigRocksDBStorage 单例映射。 */
    public static final ConcurrentMap<String, ConfigRocksDBStorage> STORE_MAP = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ColumnFamilyHandle> columnFamilyNameHandleMap;
    private ColumnFamilyOptions columnFamilyOptions;

    private ConfigRocksDBStorage(final String dbPath, boolean readOnly, CompressionType compressionType) {
        super(dbPath);
        this.readOnly = readOnly;
        if (compressionType != null) {
            this.compressionType = compressionType;
        }
        this.columnFamilyNameHandleMap = new ConcurrentHashMap<>();
    }

    public ConfigRocksDBStorage(final String dbPath, boolean readOnly) {
        this(dbPath, readOnly, null);
    }

    /** 使用 {@link ConfigHelper} 初始化 DB 与列族选项。 */
    protected void initOptions() {
        this.options = ConfigHelper.createConfigDBOptions();
        this.columnFamilyOptions = ConfigHelper.createConfigColumnFamilyOptions();
        this.cfOptions.add(columnFamilyOptions);
        super.initOptions();
    }

    /** 打开或创建 DB：列举列族、加载句柄并绑定默认列族。 */
    @Override
    protected boolean postLoad() {
        try {
            UtilAll.ensureDirOK(this.dbPath);

            initOptions();

            List<byte[]> columnFamilyNames = new ArrayList<>(RocksDB.listColumnFamilies(
                new Options(options, columnFamilyOptions), dbPath));
            addIfNotExists(columnFamilyNames, RocksDB.DEFAULT_COLUMN_FAMILY);

            List<ColumnFamilyDescriptor> cfDescriptors = new ArrayList<>();
            for (byte[] columnFamilyName : columnFamilyNames) {
                cfDescriptors.add(new ColumnFamilyDescriptor(columnFamilyName, columnFamilyOptions));
            }

            this.open(cfDescriptors);
            for (int i = 0; i < columnFamilyNames.size(); i++) {
                columnFamilyNameHandleMap.put(new String(columnFamilyNames.get(i), CHARSET), cfHandles.get(i));
            }
            this.defaultCFHandle = columnFamilyNameHandleMap.get(new String(RocksDB.DEFAULT_COLUMN_FAMILY, CHARSET));
        } catch (final Exception e) {
            AbstractRocksDBStorage.LOGGER.error("postLoad Failed. {}", this.dbPath, e);
            return false;
        }
        return true;
    }

    /** 关闭前释放各列族句柄。 */
    @Override
    protected void preShutdown() {
        for (final ColumnFamilyHandle columnFamilyHandle : this.columnFamilyNameHandleMap.values()) {
            if (columnFamilyHandle.isOwningHandle()) {
                columnFamilyHandle.close();
            }
        }
    }

    // 批量写入操作
    /** 向 WriteBatch 追加指定列族的 put 操作。 */
    public void writeBatchPutOperation(String cf, WriteBatch writeBatch, final byte[] key, final byte[] value) throws RocksDBException {
        writeBatch.put(getOrCreateColumnFamily(cf), key, value);
    }

    /** 批量提交（默认写入选项，可能不刷 WAL）。 */
    public void batchPut(final WriteBatch batch) throws RocksDBException {
        batchPut(this.writeOptions, batch);
    }

    /** 批量提交并写入 WAL（可靠刷盘路径）。 */
    public void batchPutWithWal(final WriteBatch batch) throws RocksDBException {
        batchPut(this.ableWalWriteOptions, batch);
    }


    // 指定列族的读写删与遍历
    /** 向指定列族写入键值（带 WAL）。 */
    public void put(String cf, final byte[] keyBytes, final int keyLen, final byte[] valueBytes) throws Exception {
        put(getOrCreateColumnFamily(cf), this.ableWalWriteOptions, keyBytes, keyLen, valueBytes, valueBytes.length);
    }

    /** 使用 ByteBuffer 向指定列族写入键值。 */
    public void put(String cf, final ByteBuffer keyBB, final ByteBuffer valueBB) throws Exception {
        put(getOrCreateColumnFamily(cf), this.ableWalWriteOptions, keyBB, valueBB);
    }

    /** 从指定列族按字节键读取值，列族不存在时返回 null。 */
    public byte[] get(String cf, final byte[] keyBytes) throws Exception {
        ColumnFamilyHandle columnFamilyHandle = columnFamilyNameHandleMap.get(cf);
        if (columnFamilyHandle == null) {
            return null;
        }
        return get(columnFamilyHandle, this.totalOrderReadOptions, keyBytes);
    }

    /** 从指定列族删除键（带 WAL）。 */
    public void delete(String cf, final byte[] keyBytes) throws Exception {
        ColumnFamilyHandle columnFamilyHandle = columnFamilyNameHandleMap.get(cf);
        if (columnFamilyHandle == null) {
            return;
        }
        delete(columnFamilyHandle, this.ableWalWriteOptions, keyBytes);
    }

    /** 遍历指定列族全部键值并对每对调用 biConsumer。 */
    public void iterate(final String cf, BiConsumer<byte[], byte[]> biConsumer) throws RocksDBException {
        if (!hold()) {
            LOGGER.warn("RocksDBKvStore[path={}] has been shut down", dbPath);
            return;
        }
        ColumnFamilyHandle columnFamilyHandle = columnFamilyNameHandleMap.get(cf);
        if (columnFamilyHandle == null) {
            return;
        }
        try (RocksIterator iterator = this.db.newIterator(columnFamilyHandle)) {
            for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                biConsumer.accept(iterator.key(), iterator.value());
            }
            iterator.status();
        }
    }

    /** 返回默认列族的全序迭代器。 */
    public RocksIterator iterator() {
        return this.db.newIterator(this.defaultCFHandle, this.totalOrderReadOptions);
    }

    /** 获取列族句柄；读写模式下不存在则动态创建。 */
    public ColumnFamilyHandle getOrCreateColumnFamily(String cf) throws RocksDBException {
        if (!columnFamilyNameHandleMap.containsKey(cf)) {
            if (readOnly) {
                String errInfo = String.format("RocksDBKvStore[path=%s] is open as read-only", dbPath);
                LOGGER.warn(errInfo);
                throw new RocksDBException(errInfo);
            }
            synchronized (this) {
                if (!columnFamilyNameHandleMap.containsKey(cf)) {
                    ColumnFamilyDescriptor columnFamilyDescriptor =
                        new ColumnFamilyDescriptor(cf.getBytes(CHARSET), columnFamilyOptions);
                    ColumnFamilyHandle columnFamilyHandle = db.createColumnFamily(columnFamilyDescriptor);
                    columnFamilyNameHandleMap.putIfAbsent(cf, columnFamilyHandle);
                    cfHandles.add(columnFamilyHandle);
                }
            }
        }
        return columnFamilyNameHandleMap.get(cf);
    }

    /** 若列表中尚无该列族名则追加。 */
    public void addIfNotExists(List<byte[]> columnFamilyNames, byte[] byteArray) {
        if (columnFamilyNames.stream().noneMatch(array -> Arrays.equals(array, byteArray))) {
            columnFamilyNames.add(byteArray);
        }
    }

    /** 按路径获取或创建 ConfigRocksDBStorage 实例（可指定压缩类型）。 */
    public static ConfigRocksDBStorage getStore(String path, boolean readOnly, CompressionType compressionType) {
        return ConcurrentHashMapUtils.computeIfAbsent(STORE_MAP, path,
            k -> new ConfigRocksDBStorage(path, readOnly, compressionType));
    }

    /** 按路径获取或创建 ConfigRocksDBStorage 实例。 */
    public static ConfigRocksDBStorage getStore(String path, boolean readOnly) {
        return getStore(path, readOnly, null);
    }

    /** 关闭并移除指定路径的存储实例。 */
    public static void shutdown(String path) {
        ConfigRocksDBStorage kvStore = STORE_MAP.remove(path);
        if (kvStore != null) {
            kvStore.shutdown();
        }
    }

    /** 关闭并销毁指定路径的 DB 文件。 */
    public static void destroy(String path) {
        ConfigRocksDBStorage kvStore = STORE_MAP.remove(path);
        if (kvStore != null) {
            kvStore.shutdown();
            kvStore.destroy();
        }
    }
}
