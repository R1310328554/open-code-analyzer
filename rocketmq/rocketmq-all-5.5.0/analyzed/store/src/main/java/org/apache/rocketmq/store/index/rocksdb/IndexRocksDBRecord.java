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
package org.apache.rocketmq.store.index.rocksdb;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageConst;

/**
 * RocksDB 索引记录：封装 Topic、Key/Tag、时间戳与物理偏移的键值编码。
 */
public class IndexRocksDBRecord {
    /** 索引键各段之间的分隔符。 */
    public static final String KEY_SPLIT = "@";
    /** 分隔符的 UTF-8 字节形式。 */
    public static final byte[] KEY_SPLIT_BYTES = KEY_SPLIT.getBytes(StandardCharsets.UTF_8);
    /** 值字段固定长度（8 字节时间戳）。 */
    private static final int VALUE_LENGTH = Long.BYTES;
    /** 消息存储时间戳。 */
    private long storeTime;
    /** 消息 Topic。 */
    private String topic;
    /** 消息业务 Key（可为空）。 */
    private String key;
    /** 消息 Tag（可为空）。 */
    private String tag;
    /** 消息唯一键。 */
    private String uniqKey;
    /** CommitLog 物理偏移。 */
    private long offsetPy;

    /** 构造一条 RocksDB 索引记录。 */
    public IndexRocksDBRecord(String topic, String key, String tag, long storeTime, String uniqKey, long offsetPy) {
        this.topic = topic;
        this.key = key;
        this.tag = tag;
        this.storeTime = storeTime;
        this.uniqKey = uniqKey;
        this.offsetPy = offsetPy;
    }

    /** 编码为 RocksDB 键字节数组，无效时返回 null。 */
    public byte[] getKeyBytes() {
        if (StringUtils.isEmpty(topic) || StringUtils.isEmpty(uniqKey) || offsetPy < 0L || storeTime <= 0L) {
            return null;
        }
        long storeTimeHour = MixAll.dealTimeToHourStamps(storeTime);
        if (storeTimeHour <= 0L) {
            return null;
        }
        String keyMiddleStr;
        if (!StringUtils.isEmpty(key)) {
            keyMiddleStr = KEY_SPLIT + topic + KEY_SPLIT + MessageConst.INDEX_KEY_TYPE + KEY_SPLIT + key + KEY_SPLIT + uniqKey + KEY_SPLIT;
        } else if (!StringUtils.isEmpty(tag)) {
            keyMiddleStr = KEY_SPLIT + topic + KEY_SPLIT + MessageConst.INDEX_TAG_TYPE + KEY_SPLIT + tag + KEY_SPLIT + uniqKey + KEY_SPLIT;
        } else {
            keyMiddleStr = KEY_SPLIT + topic + KEY_SPLIT + MessageConst.INDEX_UNIQUE_TYPE + KEY_SPLIT + uniqKey + KEY_SPLIT;
        }
        if (StringUtils.isEmpty(keyMiddleStr)) {
            return null;
        }
        byte[] keyMiddleBytes = keyMiddleStr.getBytes(StandardCharsets.UTF_8);
        int keyLength = Long.BYTES + keyMiddleBytes.length + Long.BYTES;
        return ByteBuffer.allocate(keyLength).putLong(storeTimeHour).put(keyMiddleBytes).putLong(offsetPy).array();
    }

    /** 编码为 RocksDB 值字节数组（存储时间戳）。 */
    public byte[] getValueBytes() {
        if (storeTime <= 0L) {
            return null;
        }
        return ByteBuffer.allocate(VALUE_LENGTH).putLong(storeTime).array();
    }

    /** 返回 Topic。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回业务 Key。 */
    public String getKey() {
        return key;
    }

    /** 设置业务 Key。 */
    public void setKey(String key) {
        this.key = key;
    }

    /** 返回存储时间戳。 */
    public long getStoreTime() {
        return storeTime;
    }

    /** 设置存储时间戳。 */
    public void setStoreTime(long storeTime) {
        this.storeTime = storeTime;
    }

    /** 返回唯一键。 */
    public String getUniqKey() {
        return uniqKey;
    }

    /** 设置唯一键。 */
    public void setUniqKey(String uniqKey) {
        this.uniqKey = uniqKey;
    }

    /** 返回物理偏移。 */
    public long getOffsetPy() {
        return offsetPy;
    }

    /** 设置物理偏移。 */
    public void setOffsetPy(long offsetPy) {
        this.offsetPy = offsetPy;
    }

    /** 返回 Tag。 */
    public String getTag() {
        return tag;
    }

    /** 设置 Tag。 */
    public void setTag(String tag) {
        this.tag = tag;
    }
}
