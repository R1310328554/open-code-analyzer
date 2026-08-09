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

import io.netty.util.internal.ObjectUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * <a href="https://www.aumasson.jp/siphash/siphash.pdf">SipHash</a> 消息认证码实现，
 * 用于 QUIC 连接 ID 生成等场景；构造后无额外堆分配。
 */
final class SipHash {

    static final int SEED_LENGTH = 16;

    // 构造完成后不再分配对象，便于热路径复用
    private final int compressionRounds;
    private final int finalizationRounds;

    // 初始状态常量，见 SipHash 论文
    private static final long INITIAL_STATE_V0 = 0x736f6d6570736575L; // "somepseu"
    private static final long INITIAL_STATE_V1 = 0x646f72616e646f6dL; // "dorandom"
    private static final long INITIAL_STATE_V2 = 0x6c7967656e657261L; // "lygenera"
    private static final long INITIAL_STATE_V3 = 0x7465646279746573L;  // "tedbytes"

    private final long initialStateV0;
    private final long initialStateV1;
    private final long initialStateV2;
    private final long initialStateV3;

    private long v0;
    private long v1;
    private long v2;
    private long v3;

    SipHash(int compressionRounds, int finalizationRounds, byte[] seed) {
        if (seed.length != SEED_LENGTH) {
            throw new IllegalArgumentException("seed must be of length " + SEED_LENGTH);
        }
        this.compressionRounds = ObjectUtil.checkPositive(compressionRounds, "compressionRounds");
        this.finalizationRounds = ObjectUtil.checkPositive(finalizationRounds, "finalizationRounds");

        // 将 16 字节 seed 拆为两个 long 初始化状态；与论文一致使用小端序
        ByteBuffer keyBuffer = ByteBuffer.wrap(seed).order(ByteOrder.LITTLE_ENDIAN);
        final long k0 = keyBuffer.getLong();
        final long k1 = keyBuffer.getLong();

        initialStateV0 = INITIAL_STATE_V0 ^ k0;
        initialStateV1 = INITIAL_STATE_V1 ^ k1;
        initialStateV2 = INITIAL_STATE_V2 ^ k0;
        initialStateV3 = INITIAL_STATE_V3 ^ k1;
    }

    /** 对输入缓冲区计算 SipHash MAC，返回 64 位哈希值。 */
    long macHash(ByteBuffer input) {
        v0 = initialStateV0;
        v1 = initialStateV1;
        v2 = initialStateV2;
        v3 = initialStateV3;
        int remaining = input.remaining();
        int position = input.position();
        int len = remaining - (remaining % Long.BYTES);
        boolean needsReverse = input.order() == ByteOrder.BIG_ENDIAN;
        for (int offset = position; offset < len; offset +=  Long.BYTES) {
            long m = input.getLong(offset);
            if (needsReverse) {
                // 大端输入需反字节序，与论文小端约定一致
                m = Long.reverseBytes(m);
            }
            v3 ^= m;
            for (int i = 0; i < compressionRounds; i++) {
                sipround();
            }
            v0 ^= m;
        }

        // 处理不足 8 字节的尾部数据
        final int left = remaining & (Long.BYTES - 1);
        long b = (long) remaining << 56;
        assert left < Long.BYTES;
        switch (left) {
            case 7:
                b |= (long) input.get(position + len + 6) << 48;
            case 6:
                b |= (long) input.get(position + len + 5) << 40;
            case 5:
                b |= (long) input.get(position + len + 4) << 32;
            case 4:
                b |= (long) input.get(position + len + 3) << 24;
            case 3:
                b |= (long) input.get(position + len + 2) << 16;
            case 2:
                b |= (long) input.get(position + len + 1) << 8;
            case 1:
                b |= input.get(position + len);
                break;
            case 0:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + left);
        }

        v3 ^= b;
        for (int i = 0; i < compressionRounds; i++) {
            sipround();
        }

        v0 ^= b;
        v2 ^= 0xFF;
        for (int i = 0; i < finalizationRounds; i++) {
            sipround();
        }

        return v0 ^ v1 ^ v2 ^ v3;
    }

    /** 执行一轮 SipHash 压缩轮（sipround）。 */
    private void sipround() {
        v0 += v1;
        v2 += v3;
        v1 = Long.rotateLeft(v1, 13);
        v3 = Long.rotateLeft(v3, 16);
        v1 ^= v0;
        v3 ^= v2;

        v0 = Long.rotateLeft(v0, 32);

        v2 += v1;
        v0 += v3;
        v1 = Long.rotateLeft(v1, 17);
        v3 = Long.rotateLeft(v3, 21);
        v1 ^= v2;
        v3 ^= v0;

        v2 = Long.rotateLeft(v2, 32);
    }
}
