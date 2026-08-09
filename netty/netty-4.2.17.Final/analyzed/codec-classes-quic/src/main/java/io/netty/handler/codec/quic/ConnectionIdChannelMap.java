/*
 * Copyright 2025 The Netty Project
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
package io.netty.handler.codec.quic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * QUIC Connection ID 到 {@link QuicheQuicChannel} 的映射表。
 * <p>
 * 使用 SipHash 1-3 作为哈希函数，降低
 * <a href="https://github.com/ncc-pbottine/QUIC-Hash-Dos-Advisory">哈希拒绝服务攻击</a> 风险。
 */
final class ConnectionIdChannelMap {
    private static final SecureRandom random = new SecureRandom();

    private final Map<ConnectionIdKey, QuicheQuicChannel> channelMap = new HashMap<>();
    private final SipHash sipHash;

    ConnectionIdChannelMap() {
        byte[] seed = new byte[SipHash.SEED_LENGTH];
        random.nextBytes(seed);
        // 与 Rust/quiche 默认一致，采用 SipHash 1-3
        sipHash = new SipHash(1, 3, seed);
    }

    /** 由 Connection ID 计算 SipHash 并构造复合键（哈希 + 原始 CID）。 */
    private ConnectionIdKey key(ByteBuffer cid) {
        long hash = sipHash.macHash(cid);
        return new ConnectionIdKey(hash, cid);
    }

    @Nullable
    QuicheQuicChannel put(ByteBuffer cid, QuicheQuicChannel channel) {
        return channelMap.put(key(cid), channel);
    }

    @Nullable
    QuicheQuicChannel remove(ByteBuffer cid) {
        return channelMap.remove(key(cid));
    }

    @Nullable
    QuicheQuicChannel get(ByteBuffer cid) {
        return channelMap.get(key(cid));
    }

    void clear() {
        channelMap.clear();
    }

    /** Map 键：SipHash 值与 Connection ID 字节内容共同决定相等性。 */
    private static final class ConnectionIdKey implements Comparable<ConnectionIdKey> {
        private final long hash;
        private final ByteBuffer key;

        ConnectionIdKey(long hash, ByteBuffer key) {
            this.hash = hash;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConnectionIdKey that = (ConnectionIdKey) o;
            return hash == that.hash && Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return (int) hash;
        }

        @Override
        public int compareTo(@NotNull ConnectionIdChannelMap.ConnectionIdKey o) {
            int result = Long.compare(hash, o.hash);
            return result != 0 ? result : key.compareTo(o.key);
        }
    }
}
