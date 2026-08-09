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
 * <a href="https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm">KMP</a> 单模式搜索实现。
 * 通过 {@link AbstractSearchProcessorFactory#newKmpSearchProcessorFactory} 创建；
 * {@link #newSearchProcessor()} 返回实际扫描用的 {@link io.netty.util.ByteProcessor}。
 * @see AbstractSearchProcessorFactory
 */
public class KmpSearchProcessorFactory extends AbstractSearchProcessorFactory {

    /** KMP 部分匹配表（failure function） */
    /** KMP 部分匹配表（failure function） */
    private final int[] jumpTable;
    /** 待搜索模式的副本 */
    /** 待搜索模式的副本 */
    private final byte[] needle;

    /** 按 KMP 规则推进 {@link #currentPosition}，完整匹配时返回 false */
    /** 按 KMP 规则推进 {@link #currentPosition}，完整匹配时返回 false */
    public static class Processor implements SearchProcessor {

        private final byte[] needle;
        private final int[] jumpTable;
        private long currentPosition;

        Processor(byte[] needle, int[] jumpTable) {
            this.needle = needle;
            this.jumpTable = jumpTable;
        }

        @Override
        public boolean process(byte value) {
            while (currentPosition > 0 && PlatformDependent.getByte(needle, currentPosition) != value) {
                currentPosition = PlatformDependent.getInt(jumpTable, currentPosition);
            }
            if (PlatformDependent.getByte(needle, currentPosition) == value) {
                currentPosition++;
            }
            if (currentPosition == needle.length) {
                currentPosition = PlatformDependent.getInt(jumpTable, currentPosition);
                return false;
            }

            return true;
        }

        @Override
        public void reset() {
            currentPosition = 0;
        }
    }

    KmpSearchProcessorFactory(byte[] needle) {
        this.needle = needle.clone();
        this.jumpTable = new int[needle.length + 1];

        int j = 0;
        for (int i = 1; i < needle.length; i++) {
            while (j > 0 && needle[j] != needle[i]) {
                j = jumpTable[j];
            }
            if (needle[j] == needle[i]) {
                j++;
            }
            jumpTable[i + 1] = j;
        }
    }

    /**
     * 返回新的 {@link Processor} 实例。
     */
    @Override
    public Processor newSearchProcessor() {
        return new Processor(needle, jumpTable);
    }

}
