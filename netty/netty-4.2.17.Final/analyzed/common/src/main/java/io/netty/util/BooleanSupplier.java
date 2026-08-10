/*
 * Copyright 2016 The Netty Project
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
package io.netty.util;

/**
 * Represents a supplier of {@code boolean}-valued results.
 *
 * <p>可抛异常的布尔供应器，用于事件循环中轮询 I/O 就绪等场景（如 {@code epollWait} 回调）。</p>
 */
public interface BooleanSupplier {
    /**
     * Gets a boolean value.
     * @return a boolean value.
     * @throws Exception If an exception occurs.
     *
     * <p>返回当前布尔结果；实现可抛出受检异常。</p>
     */
    boolean get() throws Exception;

    /**
     * A supplier which always returns {@code false} and never throws.
     *
     * <p>恒为 {@code false} 的单例，避免 lambda 分配。</p>
     */
    BooleanSupplier FALSE_SUPPLIER = new BooleanSupplier() {
        @Override
        public boolean get() {
            return false;
        }
    };

    /**
     * A supplier which always returns {@code true} and never throws.
     *
     * <p>恒为 {@code true} 的单例。</p>
     */
    BooleanSupplier TRUE_SUPPLIER = new BooleanSupplier() {
        @Override
        public boolean get() {
            return true;
        }
    };
}
