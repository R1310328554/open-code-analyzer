/*
 * Copyright 2026 The Netty Project
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
package io.netty.channel.uring;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 慢路径挂起 io_uring 操作的开放寻址哈希表。
 * <p>token 为负 long（bit 63 置位），与快速路径 packed userData 区分。</p>
 * <p>使用 Fibonacci hashing 与 tombstone 处理删除与 rehash。</p>
 */
final class PendingOpMap {
    private static final float LOAD_FACTOR = 0.5f;
    // Fibonacci 哈希将单调递增 token 均匀映射到表槽
    private static final long HASH_MULTIPLIER = 0x9E3779B97F4A7C15L;
    private static final long EMPTY = 0;
    private static final long TOMBSTONE = 1;

    private long[] tokens;
    private int[] registrationIds;
    private byte[] ops;
    private long[] userDatas;
    private int mask;
    private int maxSize;
    private int size;
    private int tombstones;
    private final AtomicLong nextSequence = new AtomicLong(3);

    PendingOpMap(int initialCapacity) {
        tokens = new long[initialCapacity];
        registrationIds = new int[initialCapacity];
        ops = new byte[initialCapacity];
        userDatas = new long[initialCapacity];
        mask = initialCapacity - 1;
        maxSize = calcMaxSize(initialCapacity);
    }

    long nextToken() {
        long sequence = nextSequence.getAndIncrement();
        if (sequence <= 0) {
            // 单调序列从 3 起；以 10M/s 计溢出需约 2.9 万年，可忽略
            throw new IllegalStateException("slow path sequence overflow");
        }
        return token(sequence);
    }

    void registerNormal(long token, int registrationId, byte op, long userData) {
        for (;;) {
            int startIndex = hashIndex(token, mask);
            int index = startIndex;
            int firstTombstone = -1;
            for (;;) {
                long existing = tokens[index];
                if (existing == EMPTY) {
                    insertAt(firstTombstone == -1 ? index : firstTombstone, token, registrationId, op, userData);
                    return;
                }
                if (existing == TOMBSTONE && firstTombstone == -1) {
                    firstTombstone = index;
                }
                if ((index = probeNext(index)) == startIndex) {
                    rehash(expandCapacity(tokens.length));
                    break;
                }
            }
        }
    }

    int findSlot(long token) {
        int startIndex = hashIndex(token, mask);
        int index = startIndex;
        for (;;) {
            long existing = tokens[index];
            if (existing == EMPTY) {
                return -1;
            }
            if (existing == token) {
                return index;
            }
            if ((index = probeNext(index)) == startIndex) {
                return -1;
            }
        }
    }

    int registrationId(int slot) {
        return registrationIds[slot];
    }

    byte op(int slot) {
        return ops[slot];
    }

    long userData(int slot) {
        return userDatas[slot];
    }

    void release(int slot) {
        // 槽位存活仅由 token 决定；tombstone 时 payload 可被覆盖或 rehash 丢弃
        tokens[slot] = TOMBSTONE;
        size--;
        tombstones++;

        if (size != 0 && tombstones > size) {
            rehash(tokens.length);
        }
    }

    private void insertAt(int index, long token, int registrationId, byte op, long userData) {
        if (tokens[index] == TOMBSTONE) {
            tombstones--;
        }
        tokens[index] = token;
        registrationIds[index] = registrationId;
        ops[index] = op;
        userDatas[index] = userData;
        size++;
        if (size + tombstones > maxSize) {
            rehash(size > maxSize ? expandCapacity(tokens.length) : tokens.length);
        }
    }

    private void rehash(int newCapacity) {
        long[] oldTokens = tokens;
        int[] oldRegistrationIds = registrationIds;
        byte[] oldOps = ops;
        long[] oldUserDatas = userDatas;

        tokens = new long[newCapacity];
        registrationIds = new int[newCapacity];
        ops = new byte[newCapacity];
        userDatas = new long[newCapacity];
        mask = newCapacity - 1;
        maxSize = calcMaxSize(newCapacity);
        size = 0;
        tombstones = 0;

        for (int i = 0; i < oldTokens.length; i++) {
            long token = oldTokens[i];
            // rehash 时仅迁移有效 token（负值）
            if (token < 0) {
                insertDuringRehash(token, oldRegistrationIds[i], oldOps[i], oldUserDatas[i]);
            }
        }
    }

    private void insertDuringRehash(long token, int registrationId, byte op, long userData) {
        int index = hashIndex(token, mask);
        for (;;) {
            if (tokens[index] == EMPTY) {
                tokens[index] = token;
                registrationIds[index] = registrationId;
                ops[index] = op;
                userDatas[index] = userData;
                size++;
                return;
            }
            index = probeNext(index);
        }
    }

    /**
     * Slow-path and internal tokens are negative so they can be distinguished from packed fast-path userdata
     * by checking bit 63.
     * <p>慢路径 token 为负 long，通过 bit 63 与快速路径 userData 区分。</p>
     */
    static long token(long sequence) {
        // 不处理 sequence 回绕；Long.MAX_VALUE 溢出在此场景可忽略
        return Long.MIN_VALUE | sequence;
    }

    static long tokenSequence(long token) {
        return token & Long.MAX_VALUE;
    }

    private static int hashIndex(long key, int mask) {
        if (mask == 0) {
            return 0;
        }
        return (int) (key * HASH_MULTIPLIER >>> Long.numberOfLeadingZeros(mask));
    }

    private int probeNext(int index) {
        return index + 1 & mask;
    }

    private static int calcMaxSize(int capacity) {
        return Math.min(capacity - 1, (int) (capacity * LOAD_FACTOR));
    }

    private static int expandCapacity(int capacity) {
        int newCapacity = capacity << 1;
        if (newCapacity <= 0) {
            throw new IllegalStateException("slow path table overflow");
        }
        return newCapacity;
    }
}
