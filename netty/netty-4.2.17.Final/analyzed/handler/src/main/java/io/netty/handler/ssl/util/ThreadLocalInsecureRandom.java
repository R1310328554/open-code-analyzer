/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.ssl.util;

import io.netty.util.internal.PlatformDependent;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 非安全 {@link SecureRandom}，底层使用 {@link PlatformDependent#threadLocalRandom()} 生成随机数。
 * <p>跳过熵收集以加速测试证书生成；<strong>禁止用于生产</strong>。</p>
 */
final class ThreadLocalInsecureRandom extends SecureRandom {

    private static final long serialVersionUID = -8209473337192526191L;

    /** 单例实例。 */
    private static final SecureRandom INSTANCE = new ThreadLocalInsecureRandom();

    /** 返回共享的非安全 SecureRandom。 */
    static SecureRandom current() {
        return INSTANCE;
    }

    private ThreadLocalInsecureRandom() { }

    @Override
    public String getAlgorithm() {
        return "insecure";
    }

    /** 忽略种子设置。 */
    @Override
    public void setSeed(byte[] seed) { }

    /** 忽略种子设置。 */
    @Override
    public void setSeed(long seed) { }

    @Override
    public void nextBytes(byte[] bytes) {
        random().nextBytes(bytes);
    }

    @Override
    public byte[] generateSeed(int numBytes) {
        byte[] seed = new byte[numBytes];
        random().nextBytes(seed);
        return seed;
    }

    @Override
    public int nextInt() {
        return random().nextInt();
    }

    @Override
    public int nextInt(int n) {
        return random().nextInt(n);
    }

    @Override
    public boolean nextBoolean() {
        return random().nextBoolean();
    }

    @Override
    public long nextLong() {
        return random().nextLong();
    }

    @Override
    public float nextFloat() {
        return random().nextFloat();
    }

    @Override
    public double nextDouble() {
        return random().nextDouble();
    }

    @Override
    public double nextGaussian() {
        return random().nextGaussian();
    }

    /** 获取当前线程的 ThreadLocalRandom。 */
    private static Random random() {
        return ThreadLocalRandom.current();
    }
}
