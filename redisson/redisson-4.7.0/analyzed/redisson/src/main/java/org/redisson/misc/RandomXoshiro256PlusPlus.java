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
/*
 * To the extent possible under law, the author has dedicated all copyright
 * and related and neighboring rights to this software to the public domain
 * worldwide. This software is distributed without any warranty.
 *
 * See <http://creativecommons.org/publicdomain/zero/1.0/>
 */
package org.redisson.misc;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 基于 xoshiro256++ 算法的 {@link Random} 实现（无第三方依赖）。
 * <p>
 * 移植自 <a href="http://xoshiro.di.unimi.it/xoshiro256plusplus.c">公开领域 C 源码</a>；
 * 本 Java 版本同样置于公有领域。使用 {@link AtomicStampedReference} 保证线程安全。
 *
 * @see <a href="http://xoshiro.di.unimi.it/">http://xoshiro.di.unimi.it/</a>
 * @author David Blackman and Sebastiano Vigna &lt;vigna@acm.org> (original C code)
 * @author Una Thompson &lt;una@unascribed.com> (Java port)
 * @author Nikita Koksharov
 */
public final class RandomXoshiro256PlusPlus extends Random {

    private static final long serialVersionUID = -2837799889588687855L;

    /** 用 {@link SecureRandom} 生成 256 位种子并构造实例。 */
    public static Random create() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] seed = new byte[32];
        secureRandom.nextBytes(seed);
        ByteBuffer bbw = ByteBuffer.wrap(seed);
        return new RandomXoshiro256PlusPlus(bbw.getLong(), bbw.getLong(), bbw.getLong(), bbw.getLong());
    }

    RandomXoshiro256PlusPlus(long s1, long s2, long s3, long s4) {
        setState(s1, s2, s3, s4);
    }

    // 用 splitmix64 将短种子扩展为完整 256 位状态；
    // 也避免全零种子导致非法状态
    ////
    // what generator is used here is unimportant, as long as it's
    // from a different family, but splitmix64 happens to be an
    // incredibly simple high-quality generator of a completely
    // different family (and is recommended by the xoshiro authors)

    private static final long SPLITMIX1_MAGIC = 0x9E3779B97F4A7C15L;

    private static long splitmix64v1(long x) {
        return (x + SPLITMIX1_MAGIC);
    }

    private static long splitmix64v2(long z) {
        z = (z ^ (z >> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >> 31);
    }

    /** 用 splitmix64 链将单个 long 种子扩展为四词状态。 */
    @Override
    public void setSeed(long seed) {
        // update haveNextNextGaussian flag in super
        super.setSeed(seed);
        int[] stamp = {0};
        stateRef = new AtomicStampedReference<>(new long[4], 0);

        long[] oldState = stateRef.get(stamp);
        long[] state = new long[4];
        long sms = splitmix64v1(seed);
        state[0] = splitmix64v2(sms);
        sms = splitmix64v1(sms);
        state[1] = splitmix64v2(sms);
        sms = splitmix64v1(sms);
        state[2] = splitmix64v2(sms);
        sms = splitmix64v1(sms);
        state[3] = splitmix64v2(sms);
        if (!stateRef.compareAndSet(oldState, state, stamp[0], 1)) {
            throw new IllegalStateException();
        }
    }

    /** 直接设置四词内部状态（CAS 更新）；全零非法。 */
    void setState(long s0, long s1, long s2, long s4) {
        if (s0 == 0 && s1 == 0 && s2 == 0 && s4 == 0)
            throw new IllegalArgumentException("xoshiro256++ state cannot be all zeroes");
        int[] stamp = {0};
        long[] oldState = stateRef.get(stamp);
        long[] state = new long[4];
        state[0] = s0;
        state[1] = s1;
        state[2] = s2;
        state[3] = s4;
        if (!stateRef.compareAndSet(oldState, state, stamp[0], 1)) {
            throw new IllegalStateException();
        }
    }

    // 通常不被调用，为 API 完整性而实现
    @Override
    protected int next(int bits) {
        return (int) (nextLong() & ((1L << bits) - 1));
    }

    @Override
    public int nextInt() {
        return (int) nextLong();
    }

    @Override
    public int nextInt(int bound) {
        return (int) nextLong(bound);
    }

    /** 返回 {@code [0, bound)} 范围内的均匀随机 long。 */
    public long nextLong(long bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        // 清除符号位保证非负，再对 bound 取模
        return (nextLong() & Long.MAX_VALUE) % bound;
    }

    @Override
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0P-53;
    }

    @Override
    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0P-24f;
    }

    @Override
    public boolean nextBoolean() {
        return (nextLong() & 1) != 0;
    }

    @Override
    public void nextBytes(byte[] buf) {
        nextBytes(buf, 0, buf.length);
    }

    public void nextBytes(byte[] buf, int ofs, int len) {
        if (ofs < 0) throw new ArrayIndexOutOfBoundsException("Offset "+ofs+" is negative");
        if (ofs >= buf.length) throw new ArrayIndexOutOfBoundsException("Offset "+ofs+" is greater than buffer length");
        if (ofs+len > buf.length) throw new ArrayIndexOutOfBoundsException("Length "+len+" with offset "+ofs+" is past end of buffer");
        int j = 8;
        long l = 0;
        for (int i = ofs; i < ofs+len; i++) {
            if (j >= 8) {
                l = nextLong();
                j = 0;
            }
            buf[i] = (byte) (l&0xFF);
            l =  l >>> 8L;
            j++;
        }
    }

    /** 带版本戳的原子状态引用，支持无锁 CAS 推进。 */
    AtomicStampedReference<long[]> stateRef;

    /** xoshiro256++ 核心步进：CAS 更新四词状态并返回随机 long。 */
    @Override
    public long nextLong() {
        while (true) {
            int[] stamp = {0};
            long[] oldState = stateRef.get(stamp);

            long[] state = Arrays.copyOf(oldState, oldState.length);
            long result = Long.rotateLeft(state[0] + state[3], 23) + state[0];

            long t = state[1] << 17;

            state[2] ^= state[0];
            state[3] ^= state[1];
            state[1] ^= state[2];
            state[0] ^= state[3];

            state[2] ^= t;

            state[3] = Long.rotateLeft(state[3], 45);

            if (stateRef.compareAndSet(oldState, state, stamp[0], stamp[0]+1)) {
                return result;
            }
        }
    }

}
