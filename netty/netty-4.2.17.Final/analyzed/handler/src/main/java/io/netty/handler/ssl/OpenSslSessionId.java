/*
 * Copyright 2021 The Netty Project
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
package io.netty.handler.ssl;

import io.netty.util.internal.EmptyArrays;

import java.util.Arrays;

/**
 * Represent the session ID used by an {@link OpenSslInternalSession}.
 *
 * <p>不可变会话 ID 包装：构造时接管 {@code byte[]} 所有权并缓存 {@link #hashCode()}，
 * 用作 {@link OpenSslSessionCache} 的 LinkedHashMap 键。</p>
 */
final class OpenSslSessionId {

    /** 会话 ID 原始字节（内部持有，不对外暴露引用）。 */
    private final byte[] id;
    /** 预计算的 hashCode，因 id 数组内容不再变化。 */
    private final int hashCode;

    /** 空会话 ID 单例，用于未分配 ID 的场景。 */
    static final OpenSslSessionId NULL_ID = new OpenSslSessionId(EmptyArrays.EMPTY_BYTES);

    OpenSslSessionId(byte[] id) {
        // We take ownership if the byte[] and so there is no need to clone it.
        this.id = id;
        // cache the hashCode as the byte[] array will never change
        this.hashCode = Arrays.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpenSslSessionId)) {
            return false;
        }

        return Arrays.equals(id, ((OpenSslSessionId) o).id);
    }

    @Override
    public String toString() {
        return "OpenSslSessionId{" +
                "id=" + Arrays.toString(id) +
                '}';
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /** 返回 id 的防御性拷贝，供 {@link javax.net.ssl.SSLSession#getId()} 等 API 使用。 */
    byte[] cloneBytes() {
        return id.clone();
    }
}
