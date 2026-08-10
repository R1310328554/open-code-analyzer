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
package io.netty.channel.kqueue;

import io.netty.channel.unix.IovArray;

/**
 * KQueue IoRegistration 附带的原生 I/O 辅助数组（懒初始化）。
 * <p>当前提供可复用的 {@link IovArray} 供 writev 聚集写。</p>
 */
final class NativeArrays {

    // 首次 write 时懒初始化
    private IovArray iovArray;

    /**
     * Return a cleared {@link IovArray} that can be used for writes.
     * <p>返回已 clear 的 IovArray，供本次 writev 使用。</p>
     */
    IovArray cleanIovArray() {
        if (iovArray == null) {
            iovArray = new IovArray();
        } else {
            iovArray.clear();
        }
        return iovArray;
    }

    void free() {
        // IoHandler destroy 时释放 IovArray 堆外内存
        if (iovArray != null) {
            iovArray.release();
            iovArray = null;
        }
    }
}
