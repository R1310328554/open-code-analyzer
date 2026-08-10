/*
 * Copyright 2024 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.resolver.dns;

import io.netty.util.internal.MathUtil;

import java.security.SecureRandom;

/**
 * DNS 查询 ID（0–65535）分配器，在复用 ID 的同时保持一定随机性。
 * <p>分桶懒分配以控制内存；ID 归还后可通过随机插入位置再次发出，降低可预测性。</p>
 */
final class DnsQueryIdSpace {
    private static final int MAX_ID = 65535;
    private static final int BUCKETS = 4;
    // 每桶约 16KB（16384 个 short 槽位）
    // Each bucket is 16kb of size.
    private static final int BUCKET_SIZE = (MAX_ID + 1) / BUCKETS;

    // 其他桶仍有 ≥500 可用 ID 时可丢弃已满桶以省内存
    // If there are other buckets left that have at least 500 usable ids we will drop an unused bucket.
    private static final int BUCKET_DROP_THRESHOLD = 500;
    private final DnsQueryIdRange[] idBuckets = new DnsQueryIdRange[BUCKETS];
    private final SecureRandom random = new SecureRandom();

    DnsQueryIdSpace() {
        assert idBuckets.length == MathUtil.findNextPositivePowerOfTwo(idBuckets.length);
        // 初始仅分配 1 个桶，按需扩展
        // We start with 1 bucket.
        idBuckets[0] = newBucket(0, random);
    }

    private static DnsQueryIdRange newBucket(int idBucketsIdx, SecureRandom random) {
        return new DnsQueryIdRange(BUCKET_SIZE, idBucketsIdx * BUCKET_SIZE, random);
    }

    /**
     * 分配下一个可用查询 ID；耗尽时返回 {@code -1}。
     *
     * @return next id to use.
     */
    int nextId() {
        int freeIdx = -1;
        for (int bucketIdx = 0; bucketIdx < idBuckets.length; bucketIdx++) {
            DnsQueryIdRange bucket = idBuckets[bucketIdx];
            if (bucket != null) {
                int id = bucket.nextId();
                if (id != -1) {
                    return id;
                }
            } else if (freeIdx == -1 ||
                    // 随机选择空闲桶槽位，避免固定扩展顺序
                    // Let's make it somehow random which free slot is used.
                    random.nextBoolean()) {
                // We have a slot that we can use to create a new bucket if we need to.
                freeIdx = bucketIdx;
            }
        }
        if (freeIdx == -1) {
            // 无可用 ID 且无空桶槽
            // No ids left and no slot left to create a new bucket.
            return -1;
        }

        // 在空闲槽创建新桶并从中取 ID
        // We still have some slots free to store a new bucket. Let's do this now and use it to generate the next id.
        DnsQueryIdRange bucket = newBucket(freeIdx, random);
        idBuckets[freeIdx] = bucket;
        int id = bucket.nextId();
        assert id >= 0;
        return id;
    }

    /**
     * 归还 query ID 供后续查询复用。
     *
     * @param id the id.
     */
    void pushId(int id) {
        int bucketIdx = id / BUCKET_SIZE;
        if (bucketIdx >= idBuckets.length) {
            throw new IllegalArgumentException("id too large: " + id);
        }
        DnsQueryIdRange bucket = idBuckets[bucketIdx];
        assert bucket != null;
        bucket.pushId(id);

        if (bucket.usableIds() == bucket.maxUsableIds()) {
            // 桶内 ID 全部可用时，若其他桶仍有余量则丢弃本桶
            // All ids are usable in this bucket. Let's check if there are other buckets left that have still
            // some space left and if so drop this bucket.
            for (int idx = 0; idx < idBuckets.length; idx++) {
                if (idx != bucketIdx) {
                    DnsQueryIdRange otherBucket = idBuckets[idx];
                    if (otherBucket != null && otherBucket.usableIds() > BUCKET_DROP_THRESHOLD) {
                        // 释放桶内存，改由其他桶继续分配
                        // Drop bucket on the floor to reduce memory usage, there is another bucket left we can
                        // use that still has enough ids to use.
                        idBuckets[bucketIdx] = null;
                        return;
                    }
                }
            }
        }
    }

    /**
     * 返回当前仍可分配的 ID 数量。
     *
     * @return the number of ids that are left for usage.
     */
    int usableIds() {
        int usableIds = 0;
        for (DnsQueryIdRange bucket: idBuckets) {
            // If there is nothing stored in the index yet we can assume the whole bucket is usable
            usableIds += bucket == null ? BUCKET_SIZE : bucket.usableIds();
        }
        return usableIds;
    }

    /**
     * 返回理论最大 ID 容量（桶数 × 桶大小）。
     *
     * @return the maximum number of ids.
     */
    int maxUsableIds() {
        return BUCKET_SIZE * idBuckets.length;
    }

    /** 单个桶内的 ID 范围与随机栈式分配逻辑。 */
    private static final class DnsQueryIdRange {

        // 以 unsigned short 存储可用 ID 栈
        // Holds all possible ids which are stored as unsigned shorts
        private final short[] ids;
        private final int startId;
        private final SecureRandom random;
        private int count;

        DnsQueryIdRange(int bucketSize, int startId, SecureRandom random) {
            this.ids = new short[bucketSize];
            this.startId = startId;
            this.random = random;
            for (int v = startId; v < bucketSize + startId; v++) {
                pushId(v);
            }
        }

        /**
         * Returns the next ID to use for a query or {@code -1} if there is none left to use.
         *
         * @return next id to use.
         */
        int nextId() {
            assert count >= 0;
            if (count == 0) {
                return -1;
            }
            short id = ids[count - 1];
            count--;

            return id & 0xFFFF;
        }

        /**
         * Push back the id, so it can be used again for the next query.
         *
         * @param id the id.
         */
        void pushId(int id) {
            if (count == ids.length) {
                throw new IllegalStateException("overflow");
            }
            assert id <= startId + ids.length && id >= startId;
            // 随机插入归还 ID，被挤出的元素移到栈顶
            // pick a slot for our index, and whatever was in that slot before will get moved to the tail.
            int insertionPosition = random.nextInt(count + 1);
            short moveId = ids[insertionPosition];
            short insertId = (short) id;

            // Assert that the ids are different or its the same index.
            assert moveId != insertId || insertionPosition == count;

            ids[count] = moveId;
            ids[insertionPosition] = insertId;
            count++;
        }

        /**
         * Return how much more usable ids are left.
         *
         * @return the number of ids that are left for usage.
         */
        int usableIds() {
            return count;
        }

        /**
         * Return the maximum number of ids that are supported.
         *
         * @return the maximum number of ids.
         */
        int maxUsableIds() {
            return ids.length;
        }
    }
}
