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
package io.netty.util.internal;

import java.nio.ByteBuffer;

/**
 * Encapsulates a direct {@link ByteBuffer} and its mechanism for immediate deallocation, if any.
 * <p>封装堆外 {@link ByteBuffer} 及其显式释放机制；调用 {@link #clean()} 后不得再访问缓冲区。</p>
 */
public interface CleanableDirectBuffer {
    /**
     * Get the buffer instance.
     * <p>
     * Note: the buffer must not be accessed after the {@link #clean()} method has been called.
     * <p>在 {@link #clean()} 调用之后禁止访问返回的缓冲区。</p>
     *
     * @return The {@link ByteBuffer} instance.
     */
    ByteBuffer buffer();

    /**
     * Deallocate the buffer. This method can only be called once per instance,
     * and all usages of the buffer must have ceased before this method is called,
     * and the buffer must not be accessed again after this method has been called.
     * <p>释放底层堆外内存；每实例仅可调用一次，且调用前须停止一切读写。</p>
     */
    void clean();

    /**
     * @return {@code true} if the {@linkplain #memoryAddress() native memory address} is available,
     * otherwise {@code false}.
     * <p>是否可安全调用 {@link #memoryAddress()} 获取本机地址。</p>
     */
    default boolean hasMemoryAddress() {
        return false;
    }

    /**
     * Get the native memory address, but only if {@link #hasMemoryAddress()} returns true,
     * otherwise this may return an unspecified value or throw an exception.
     * <p>仅当 {@link #hasMemoryAddress()} 为真时地址有效；否则返回值未定义或可能抛异常。</p>
     * @return The native memory address of this buffer, if available.
     */
    default long memoryAddress() {
        return 0;
    }
}
