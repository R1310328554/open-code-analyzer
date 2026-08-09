/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.stream;

import java.util.Map;

/**
 * 流详情信息对象。
 * <p>
 * 包含流长度、基数树统计、消费者组数量及首尾条目等元数据。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public final class StreamInfo<K, V> {

    public static class Entry<K, V> {

        final StreamMessageId id;
        final Map<K, V> data;

        public Entry(StreamMessageId id, Map<K, V> data) {
            this.id = id;
            this.data = data;
        }

        /**
         * 返回该流条目的消息 ID。
         *
         * @return 消息 ID 对象
         */
        public StreamMessageId getId() {
            return id;
        }

        /**
         * 返回该流条目存储的数据。
         *
         * @return 数据映射
         */
        public Map<K, V> getData() {
            return data;
        }

    }

    /** 流当前长度。 */
    int length;
    /** 基数树分配的键数量。 */
    int radixTreeKeys;
    /** 基数树节点数量。 */
    int radixTreeNodes;
    /** 消费者组数量。 */
    int groups;
    /** 最后生成的消息 ID。 */
    StreamMessageId lastGeneratedId;
    /** 首条流条目。 */
    Entry<K, V> firstEntry;
    /** 末条流条目。 */
    Entry<K, V> lastEntry;
    /** 已删除条目的最大 ID。 */
    StreamMessageId maxDeletedEntryId;
    /** 流生命周期内写入的条目总数。 */
    int entriesAdded;
    /** 记录的首条消息 ID。 */
    StreamMessageId recordedFirstEntryId;

    /**
     * 返回流当前长度。
     *
     * @return 流长度
     */
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * 返回流基数树分配的键数量。
     *
     * @return 键数量
     */
    public int getRadixTreeKeys() {
        return radixTreeKeys;
    }
    public void setRadixTreeKeys(int radixTreeKeys) {
        this.radixTreeKeys = radixTreeKeys;
    }

    /**
     * 返回流基数树节点数量。
     *
     * @return 节点数量
     */
    public int getRadixTreeNodes() {
        return radixTreeNodes;
    }
    public void setRadixTreeNodes(int radixTreeNodes) {
        this.radixTreeNodes = radixTreeNodes;
    }

    /**
     * 返回该流所属的消费者组数量。
     *
     * @return 组数量
     */
    public int getGroups() {
        return groups;
    }
    public void setGroups(int groups) {
        this.groups = groups;
    }

    /**
     * 返回流最后使用的消息 ID。
     *
     * @return 消息 ID 对象
     */
    public StreamMessageId getLastGeneratedId() {
        return lastGeneratedId;
    }
    public void setLastGeneratedId(StreamMessageId lastGeneratedId) {
        this.lastGeneratedId = lastGeneratedId;
    }

    /**
     * 返回首条流条目。
     *
     * @return 流条目
     */
    public Entry<K, V> getFirstEntry() {
        return firstEntry;
    }
    public void setFirstEntry(Entry<K, V> firstEntry) {
        this.firstEntry = firstEntry;
    }

    /**
     * 返回末条流条目。
     *
     * @return 流条目
     */
    public Entry<K, V> getLastEntry() {
        return lastEntry;
    }
    public void setLastEntry(Entry<K, V> lastEntry) {
        this.lastEntry = lastEntry;
    }

    /**
     * 返回从流中删除的最大条目 ID。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上版本。</b>
     *
     * @return 消息 ID 对象
     */
    public StreamMessageId getMaxDeletedEntryId() {
        return maxDeletedEntryId;
    }
    public StreamInfo<K, V> setMaxDeletedEntryId(StreamMessageId maxDeletedEntryId) {
        this.maxDeletedEntryId = maxDeletedEntryId;
        return this;
    }

    /**
     * 返回流生命周期内写入的条目总数。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上版本。</b>
     *
     * @return 条目总数
     */
    public int getEntriesAdded() {
        return entriesAdded;
    }
    public StreamInfo<K, V> setEntriesAdded(int entriesAdded) {
        this.entriesAdded = entriesAdded;
        return this;
    }

    /**
     * 返回写入流的首条消息 ID。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上版本。</b>
     *
     * @return 消息 ID 对象
     */
    public StreamMessageId getRecordedFirstEntryId() {
        return recordedFirstEntryId;
    }
    public StreamInfo<K, V> setRecordedFirstEntryId(StreamMessageId recordedFirstEntryId) {
        this.recordedFirstEntryId = recordedFirstEntryId;
        return this;
    }
}
