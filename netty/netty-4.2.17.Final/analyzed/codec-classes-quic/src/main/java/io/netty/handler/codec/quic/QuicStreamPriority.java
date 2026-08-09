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
package io.netty.handler.codec.quic;

import io.netty.util.internal.ObjectUtil;

import java.util.Objects;

/**
 * {@link QuicStreamChannel} 的优先级描述，含紧急度与是否增量传输。
 */
public final class QuicStreamPriority {

    private final int urgency;
    private final boolean incremental;

    /**
     * 创建流优先级实例。
     *
     * @param urgency       the urgency of the stream.
     * @param incremental   {@code true} if incremental.
     */
    public QuicStreamPriority(int urgency, boolean incremental) {
        this.urgency = ObjectUtil.checkInRange(urgency, 0, Byte.MAX_VALUE, "urgency");
        this.incremental = incremental;
    }

    /**
     * 流紧急度，数值越小越优先发送。
     *
     * @return  the urgency.
     */
    public int urgency() {
        return urgency;
    }

    /**
     * 是否为增量流（{@code true} 表示增量，{@code false} 表示非增量）。
     *
     * @return  if incremental.
     */
    public boolean isIncremental() {
        return incremental;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuicStreamPriority that = (QuicStreamPriority) o;
        return urgency == that.urgency && incremental == that.incremental;
    }

    @Override
    public int hashCode() {
        return Objects.hash(urgency, incremental);
    }

    @Override
    public String toString() {
        return "QuicStreamPriority{" +
                "urgency=" + urgency +
                ", incremental=" + incremental +
                '}';
    }
}
