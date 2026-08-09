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
package io.netty.buffer;

import io.netty.util.internal.PlatformDependent;

import java.nio.ByteBuffer;

/**
 * 集中封装 {@link ByteBuffer} 与 {@code byte[]} 的 VarHandle 读写，
 * 避免 {@link UnpooledDirectByteBuf} 等类直接引用签名多态方法。
 * 在 VarHandle 禁用时本类不会被加载，从而规避旧版 Android 的类校验失败。
 * <p>
 * 仅当 {@code PlatformDependent.hasVarHandle()} 为真时方可调用此处方法。
 */
final class VarHandleByteBufferAccess {

    private VarHandleByteBufferAccess() {
    }

    // --- ByteBuffer：short 大端 ---
    static short getShortBE(ByteBuffer buffer, int index) {
        //noinspection DataFlowIssue
        return (short) PlatformDependent.shortBeByteBufferView().get(buffer, index);
    }

    static void setShortBE(ByteBuffer buffer, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.shortBeByteBufferView().set(buffer, index, (short) value);
    }

    // --- ByteBuffer：short 小端 ---
    static short getShortLE(ByteBuffer buffer, int index) {
        //noinspection DataFlowIssue
        return (short) PlatformDependent.shortLeByteBufferView().get(buffer, index);
    }

    static void setShortLE(ByteBuffer buffer, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.shortLeByteBufferView().set(buffer, index, (short) value);
    }

    // --- ByteBuffer：int 大端 ---
    static int getIntBE(ByteBuffer buffer, int index) {
        //noinspection DataFlowIssue
        return (int) PlatformDependent.intBeByteBufferView().get(buffer, index);
    }

    static void setIntBE(ByteBuffer buffer, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.intBeByteBufferView().set(buffer, index, value);
    }

    // int (little endian)
    static int getIntLE(ByteBuffer buffer, int index) {
        //noinspection DataFlowIssue
        return (int) PlatformDependent.intLeByteBufferView().get(buffer, index);
    }

    static void setIntLE(ByteBuffer buffer, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.intLeByteBufferView().set(buffer, index, value);
    }

    // --- ByteBuffer：long 大端 ---
    static long getLongBE(ByteBuffer buffer, int index) {
        //noinspection DataFlowIssue
        return (long) PlatformDependent.longBeByteBufferView().get(buffer, index);
    }

    static void setLongBE(ByteBuffer buffer, int index, long value) {
        //noinspection DataFlowIssue
        PlatformDependent.longBeByteBufferView().set(buffer, index, value);
    }

    // long (little endian)
    static long getLongLE(ByteBuffer buffer, int index) {
        //noinspection DataFlowIssue
        return (long) PlatformDependent.longLeByteBufferView().get(buffer, index);
    }

    static void setLongLE(ByteBuffer buffer, int index, long value) {
        //noinspection DataFlowIssue
        PlatformDependent.longLeByteBufferView().set(buffer, index, value);
    }

    // --------------------------------------------------------------------
    // 堆 byte[] 访问：同样集中 VarHandle 调用，避免 HeapByteBufUtil 等直接触达签名多态方法
    // 仅当 PlatformDependent.hasVarHandle() 为真时调用
    // --------------------------------------------------------------------

    // --- byte[]：short 大端 ---
    static short getShortBE(byte[] memory, int index) {
        //noinspection DataFlowIssue
        return (short) PlatformDependent.shortBeArrayView().get(memory, index);
    }

    static void setShortBE(byte[] memory, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.shortBeArrayView().set(memory, index, (short) value);
    }

    // short (little endian)
    static short getShortLE(byte[] memory, int index) {
        //noinspection DataFlowIssue
        return (short) PlatformDependent.shortLeArrayView().get(memory, index);
    }

    static void setShortLE(byte[] memory, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.shortLeArrayView().set(memory, index, (short) value);
    }

    // int (big endian)
    static int getIntBE(byte[] memory, int index) {
        //noinspection DataFlowIssue
        return (int) PlatformDependent.intBeArrayView().get(memory, index);
    }

    static void setIntBE(byte[] memory, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.intBeArrayView().set(memory, index, value);
    }

    // int (little endian)
    static int getIntLE(byte[] memory, int index) {
        //noinspection DataFlowIssue
        return (int) PlatformDependent.intLeArrayView().get(memory, index);
    }

    static void setIntLE(byte[] memory, int index, int value) {
        //noinspection DataFlowIssue
        PlatformDependent.intLeArrayView().set(memory, index, value);
    }

    // long (big endian)
    static long getLongBE(byte[] memory, int index) {
        //noinspection DataFlowIssue
        return (long) PlatformDependent.longBeArrayView().get(memory, index);
    }

    static void setLongBE(byte[] memory, int index, long value) {
        //noinspection DataFlowIssue
        PlatformDependent.longBeArrayView().set(memory, index, value);
    }

    // long (little endian)
    static long getLongLE(byte[] memory, int index) {
        //noinspection DataFlowIssue
        return (long) PlatformDependent.longLeArrayView().get(memory, index);
    }

    static void setLongLE(byte[] memory, int index, long value) {
        //noinspection DataFlowIssue
        PlatformDependent.longLeArrayView().set(memory, index, value);
    }
}
