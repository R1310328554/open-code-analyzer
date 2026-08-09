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

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.config.AbstractRocksDBStorage;
import org.apache.rocketmq.remoting.protocol.DataVersion;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;

/**
 * Broker v2 配置 RocksDB 键值编解码辅助：按表前缀/记录前缀构造
 * DataVersion 与配置条目的 Netty {@link ByteBuf} 键值。
 */
public class ConfigHelper {

    /**
     * 从 RocksDB 加载指定表的 DataVersion。
     * <p>键布局：[table-prefix,1][table-id,2][record-prefix,1][data-version-bytes]</p>
     * <p>值布局：[state-machine-version,8][timestamp,8][counter,8]</p>
     *
     * @throws RocksDBException RocksDB 读失败时抛出
     */
    public static Optional<ByteBuf> loadDataVersion(ConfigStorage configStorage, TableId tableId)
        throws RocksDBException {
        int keyLen = 1 /* table-prefix */ + Short.BYTES /* table-id */ + 1 /* record-prefix */
            + ConfigStorage.DATA_VERSION_KEY_BYTES.length;
        ByteBuf keyBuf = AbstractRocksDBStorage.POOLED_ALLOCATOR.buffer(keyLen);
        try {
            keyBuf.writeByte(TablePrefix.TABLE.getValue());
            keyBuf.writeShort(tableId.getValue());
            keyBuf.writeByte(RecordPrefix.DATA_VERSION.getValue());
            keyBuf.writeBytes(ConfigStorage.DATA_VERSION_KEY_BYTES);
            byte[] valueByes = configStorage.get(keyBuf.nioBuffer());
            if (null != valueByes) {
                ByteBuf valueBuf = Unpooled.wrappedBuffer(valueByes);
                return Optional.of(valueBuf);
            }
        } finally {
            keyBuf.release();
        }
        return Optional.empty();
    }

    /** 递增 DataVersion 并将键值对写入 WriteBatch。 */
    public static void stampDataVersion(WriteBatch writeBatch, TableId table, DataVersion dataVersion, long stateMachineVersion)
        throws RocksDBException {
        // Increase data version
        dataVersion.nextVersion(stateMachineVersion);

        int keyLen = 1 /* table-prefix */ + Short.BYTES /* table-id */ + 1 /* record-prefix */
            + ConfigStorage.DATA_VERSION_KEY_BYTES.length;
        ByteBuf keyBuf = AbstractRocksDBStorage.POOLED_ALLOCATOR.buffer(keyLen);
        ByteBuf valueBuf = AbstractRocksDBStorage.POOLED_ALLOCATOR.buffer(Long.BYTES * 3);
        try {
            keyBuf.writeByte(TablePrefix.TABLE.getValue());
            keyBuf.writeShort(table.getValue());
            keyBuf.writeByte(RecordPrefix.DATA_VERSION.getValue());
            keyBuf.writeBytes(ConfigStorage.DATA_VERSION_KEY_BYTES);
            valueBuf.writeLong(dataVersion.getStateVersion());
            valueBuf.writeLong(dataVersion.getTimestamp());
            valueBuf.writeLong(dataVersion.getCounter().get());
            writeBatch.put(keyBuf.nioBuffer(), valueBuf.nioBuffer());
        } finally {
            keyBuf.release();
            valueBuf.release();
        }
    }

    /** 从 ByteBuf 解析 stateVersion、timestamp、counter 填入 DataVersion。 */
    public static void onDataVersionLoad(ByteBuf buf, DataVersion dataVersion) {
        if (buf.readableBytes() == 8 /* state machine version */ + 8 /* timestamp */ + 8 /* counter */) {
            long stateMachineVersion = buf.readLong();
            long timestamp = buf.readLong();
            long counter = buf.readLong();
            dataVersion.setStateVersion(stateMachineVersion);
            dataVersion.setTimestamp(timestamp);
            dataVersion.setCounter(new AtomicLong(counter));
        }
        buf.release();
    }

    /** 构造配置记录键：[table-prefix][table-id][data-prefix][name-len][name-bytes]。 */
    public static ByteBuf keyBufOf(TableId tableId, final String name) {
        Preconditions.checkNotNull(name);
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        int keyLen = 1 /* table-prefix */ + 2 /* table-id */ + 1 /* record-type-prefix */ + 2 /* name-length */ + bytes.length;
        ByteBuf keyBuf = AbstractRocksDBStorage.POOLED_ALLOCATOR.buffer(keyLen);
        keyBuf.writeByte(TablePrefix.TABLE.getValue());
        keyBuf.writeShort(tableId.getValue());
        keyBuf.writeByte(RecordPrefix.DATA.getValue());
        keyBuf.writeShort(bytes.length);
        keyBuf.writeBytes(bytes);
        return keyBuf;
    }

    /** 按序列化类型（当前仅 JSON）构造配置值 ByteBuf。 */
    public static ByteBuf valueBufOf(final Object config, SerializationType serializationType) {
        if (SerializationType.JSON == serializationType) {
            byte[] payload = JSON.toJSONBytes(config);
            ByteBuf valueBuf = AbstractRocksDBStorage.POOLED_ALLOCATOR.buffer(1 + payload.length);
            valueBuf.writeByte(SerializationType.JSON.getValue());
            valueBuf.writeBytes(payload);
            return valueBuf;
        }
        throw new RuntimeException("Unsupported serialization type: " + serializationType);
    }

    /** 读取 ByteBuf 全部可读字节为数组。 */
    public static byte[] readBytes(final ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }
}
