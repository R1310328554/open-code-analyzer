/*
 * Copyright 2017 The Netty Project
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
package io.netty.handler.codec.http2;

/**
 * {@link Http2FrameStream} 的状态变更事件，由 {@link Http2FrameCodec} 通过 user event 向上游投递。
 */
public final class Http2FrameStreamEvent {

    /** 发生变更的流。 */
    private final Http2FrameStream stream;
    /** 变更类型：状态迁移或可写性变化。 */
    private final Type type;

    /** 事件类别。 */
    public enum Type {
        /** 流状态机发生迁移（如 OPEN → HALF_CLOSED_LOCAL）。 */
        State,
        /** 流控窗口变化导致可写性改变。 */
        Writability
    }

    private Http2FrameStreamEvent(Http2FrameStream stream, Type type) {
        this.stream = stream;
        this.type = type;
    }

    public Http2FrameStream stream() {
        return stream;
    }

    public Type type() {
        return type;
    }

    /** 构造流状态变更事件。 */
    static Http2FrameStreamEvent stateChanged(Http2FrameStream stream) {
        return new Http2FrameStreamEvent(stream, Type.State);
    }

    /** 构造流可写性变更事件。 */
    static Http2FrameStreamEvent writabilityChanged(Http2FrameStream stream) {
        return new Http2FrameStreamEvent(stream, Type.Writability);
    }
}
