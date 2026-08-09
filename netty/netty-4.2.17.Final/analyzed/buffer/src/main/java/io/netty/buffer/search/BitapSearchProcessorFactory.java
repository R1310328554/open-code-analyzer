/*
 * Copyright 2020 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.buffer.search;

import io.netty.util.internal.PlatformDependent;

/**
 * <a href="https://en.wikipedia.org/wiki/Bitap_algorithm">Bitap</a>（Shift-Or）单模式搜索实现。
 * 通过 {@link AbstractSearchProcessorFactory#newBitapSearchProcessorFactory} 创建；
 * {@link #newSearchProcessor()} 返回实际扫描用的 {@link io.netty.util.ByteProcessor}。
 * @see AbstractSearchProcessorFactory
 */
public class BitapSearchProcessorFactory extends AbstractSearchProcessorFactory {

    /** 各字节值在模式中的位掩码 */
    /** 各字节值在模式中的位掩码 */
    private final long[] bitMasks = new long[256];
    /** 模式完全对齐时的成功位 */
    /** 模式完全对齐时的成功位 */
    private final long successBit;

    /** 维护 {@link #currentMask} 状态机，命中时 {@link #process} 返回 false */
    /** 维护 {@link #currentMask} 状态机，命中时 {@link #process} 返回 false */
    public static class Processor implements SearchProcessor {

        private final long[] bitMasks;
        private final long successBit;
        private long currentMask;

        Processor(long[] bitMasks, long successBit) {
            this.bitMasks = bitMasks;
            this.successBit = successBit;
        }

        @Override
        public boolean process(byte value) {
            currentMask = ((currentMask << 1) | 1) & PlatformDependent.getLong(bitMasks, value & 0xffL);
            return (currentMask & successBit) == 0;
        }

        @Override
        public void reset() {
            currentMask = 0;
        }
    }

    BitapSearchProcessorFactory(byte[] needle) {
        if (needle.length > 64) {
            throw new IllegalArgumentException("Maximum supported search pattern length is 64, got " + needle.length);
        }

        long bit = 1L;
        for (byte c: needle) {
            bitMasks[c & 0xff] |= bit;
            bit <<= 1;
        }

        successBit = 1L << (needle.length - 1);
    }

    /**
     * 返回新的 {@link Processor} 实例。
     */
    @Override
    public Processor newSearchProcessor() {
        return new Processor(bitMasks, successBit);
    }

}
