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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.subscription.SubscriptionGroupManager;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.config.AbstractRocksDBStorage;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;

/**
 * 基于 RocksDB {@link ConfigStorage} 的订阅组配置管理器：
 * 从 KV 存储加载/持久化 {@link SubscriptionGroupConfig}。
 */
public class SubscriptionGroupManagerV2 extends SubscriptionGroupManager {

    private final ConfigStorage configStorage;

    /** 绑定 broker 控制器与配置存储实例。 */
    public SubscriptionGroupManagerV2(BrokerController brokerController, ConfigStorage configStorage) {
        super(brokerController);
        this.configStorage = configStorage;
    }

    /** 加载数据版本与全部订阅组配置。 */
    @Override
    public boolean load() {
        return loadDataVersion() && loadSubscriptions();
    }

    /** 从 {@link TableId#SUBSCRIPTION_GROUP} 表读取并应用数据版本。 */
    public boolean loadDataVersion() {
        try {
            ConfigHelper.loadDataVersion(configStorage, TableId.SUBSCRIPTION_GROUP)
                .ifPresent(buf -> {
                    ConfigHelper.onDataVersionLoad(buf, dataVersion);
                });
        } catch (RocksDBException e) {
            log.error("loadDataVersion error", e);
            return false;
        }
        return true;
    }

    /** 扫描订阅组表前缀范围内的全部 KV 并反序列化。 */
    private boolean loadSubscriptions() {
        int keyLen = 1 /* table prefix */ + 2 /* table-id */ + 1 /* record-type-prefix */;
        ByteBuf beginKey = AbstractRocksDBStorage.POOLED_ALLOCATOR.buffer(keyLen);
        beginKey.writeByte(TablePrefix.TABLE.getValue());
        beginKey.writeShort(TableId.SUBSCRIPTION_GROUP.getValue());
        beginKey.writeByte(RecordPrefix.DATA.getValue());

        ByteBuf endKey = AbstractRocksDBStorage.POOLED_ALLOCATOR.buffer(keyLen);
        endKey.writeByte(TablePrefix.TABLE.getValue());
        endKey.writeShort(TableId.SUBSCRIPTION_GROUP.getValue());
        endKey.writeByte(RecordPrefix.DATA.getValue() + 1);

        try (RocksIterator iterator = configStorage.iterate(beginKey.nioBuffer(), endKey.nioBuffer())) {
            while (iterator.isValid()) {
                SubscriptionGroupConfig subscriptionGroupConfig = parseSubscription(iterator.key(), iterator.value());
                if (null != subscriptionGroupConfig) {
                    super.putSubscriptionGroupConfig(subscriptionGroupConfig);
                }
                iterator.next();
            }
        } finally {
            beginKey.release();
            endKey.release();
        }
        return true;
    }

    /** 解析 RocksDB 键值对为 {@link SubscriptionGroupConfig}（当前仅支持 JSON）。 */
    private SubscriptionGroupConfig parseSubscription(byte[] key, byte[] value) {
        ByteBuf keyBuf = Unpooled.wrappedBuffer(key);
        ByteBuf valueBuf = Unpooled.wrappedBuffer(value);
        try {
            // Skip table-prefix, table-id, record-type-prefix
            keyBuf.readerIndex(4);
            short groupNameLen = keyBuf.readShort();
            assert groupNameLen == keyBuf.readableBytes();
            CharSequence groupName = keyBuf.readCharSequence(groupNameLen, StandardCharsets.UTF_8);
            assert null != groupName;
            byte serializationType = valueBuf.readByte();
            if (SerializationType.JSON == SerializationType.valueOf(serializationType)) {
                CharSequence json = valueBuf.readCharSequence(valueBuf.readableBytes(), StandardCharsets.UTF_8);
                SubscriptionGroupConfig subscriptionGroupConfig = JSON.parseObject(json.toString(), SubscriptionGroupConfig.class);
                assert subscriptionGroupConfig != null;
                assert groupName.equals(subscriptionGroupConfig.getGroupName());
                return subscriptionGroupConfig;
            }
        } finally {
            keyBuf.release();
            valueBuf.release();
        }
        return null;
    }

    /** 强制刷 WAL 到磁盘（核心元数据变更后调用）。 */
    @Override
    public synchronized void persist() {
        try {
            configStorage.flushWAL();
        } catch (RocksDBException e) {
            log.error("Failed to flush RocksDB WAL", e);
        }
    }

    /** 查询订阅组；LMQ 组名返回默认配置而不查库。 */
    @Override
    public SubscriptionGroupConfig findSubscriptionGroupConfig(final String group) {
        if (MixAll.isLmq(group)) {
            SubscriptionGroupConfig subscriptionGroupConfig = new SubscriptionGroupConfig();
            subscriptionGroupConfig.setGroupName(group);
            return subscriptionGroupConfig;
        }
        return super.findSubscriptionGroupConfig(group);
    }

    /** 更新内存并写入 RocksDB，同时戳记数据版本。 */
    @Override
    public void updateSubscriptionGroupConfig(final SubscriptionGroupConfig config) {
        if (config == null || MixAll.isLmq(config.getGroupName())) {
            return;
        }
        super.updateSubscriptionGroupConfigWithoutPersist(config);
        ByteBuf keyBuf = ConfigHelper.keyBufOf(TableId.SUBSCRIPTION_GROUP, config.getGroupName());
        ByteBuf valueBuf = ConfigHelper.valueBufOf(config, SerializationType.JSON);
        try (WriteBatch writeBatch = new WriteBatch()) {
            writeBatch.put(keyBuf.nioBuffer(), valueBuf.nioBuffer());
            long stateMachineVersion = brokerController.getMessageStore() != null ? brokerController.getMessageStore().getStateMachineVersion() : 0;
            ConfigHelper.stampDataVersion(writeBatch, TableId.SUBSCRIPTION_GROUP, dataVersion, stateMachineVersion);
            configStorage.write(writeBatch);
            // fdatasync on core metadata change
            persist();
        } catch (RocksDBException e) {
            log.error("update subscription group config error", e);
        } finally {
            keyBuf.release();
            valueBuf.release();
        }
    }

    /** LMQ 组名恒视为存在。 */
    @Override
    public boolean containsSubscriptionGroup(String group) {
        if (MixAll.isLmq(group)) {
            return true;
        } else {
            return super.containsSubscriptionGroup(group);
        }
    }

    /** 从 RocksDB 删除键并更新内存缓存。 */
    @Override
    protected SubscriptionGroupConfig removeSubscriptionGroupConfig(String groupName) {
        ByteBuf keyBuf = ConfigHelper.keyBufOf(TableId.SUBSCRIPTION_GROUP, groupName);
        try (WriteBatch writeBatch = new WriteBatch()) {
            writeBatch.delete(ConfigHelper.readBytes(keyBuf));
            long stateMachineVersion = brokerController.getMessageStore().getStateMachineVersion();
            ConfigHelper.stampDataVersion(writeBatch, TableId.SUBSCRIPTION_GROUP, dataVersion, stateMachineVersion);
            configStorage.write(writeBatch);
        } catch (RocksDBException e) {
            log.error("Failed to remove subscription group config by group-name={}", groupName, e);
        }
        return super.removeSubscriptionGroupConfig(groupName);
    }
}
