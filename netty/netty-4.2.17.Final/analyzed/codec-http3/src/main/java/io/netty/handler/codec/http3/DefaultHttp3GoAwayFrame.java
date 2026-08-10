/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http3;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.Objects;

/**
 * {@link Http3GoAwayFrame} 的默认实现：在控制流上宣告连接即将关闭。
 * <p>{@code id} 为最后一个仍被对端接受的 push/request 流 ID 上界，用于优雅停机。
 */
public final class DefaultHttp3GoAwayFrame implements Http3GoAwayFrame {
    /** 对端仍可安全处理的流 ID 上限（含 push 流语义）。 */
    private final long id;

    public DefaultHttp3GoAwayFrame(long id) {
        this.id = ObjectUtil.checkPositiveOrZero(id, "id");
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultHttp3GoAwayFrame that = (DefaultHttp3GoAwayFrame) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(id=" + id() + ')';
    }
}
