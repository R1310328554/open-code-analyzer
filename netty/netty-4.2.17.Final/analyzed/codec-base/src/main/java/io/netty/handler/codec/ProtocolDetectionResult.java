/*
 * Copyright 2015 The Netty Project
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
package io.netty.handler.codec;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 协议探测结果。
 *
 * @param <T> 协议标识类型
 */
public final class ProtocolDetectionResult<T> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final ProtocolDetectionResult NEEDS_MORE_DATA =
            new ProtocolDetectionResult(ProtocolDetectionState.NEEDS_MORE_DATA, null);
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final ProtocolDetectionResult INVALID =
            new ProtocolDetectionResult(ProtocolDetectionState.INVALID, null);

    /** 当前探测状态。 */
    private final ProtocolDetectionState state;
    /** 探测到的协议（仅 DETECTED 时有值）。 */
    private final T result;

    /** 返回表示尚需更多数据才能判定协议的结果。 */
    @SuppressWarnings("unchecked")
    public static <T> ProtocolDetectionResult<T> needsMoreData() {
        return NEEDS_MORE_DATA;
    }

    /** 返回表示输入数据无效、无法匹配协议的结果。 */
    @SuppressWarnings("unchecked")
    public static <T> ProtocolDetectionResult<T> invalid() {
        return INVALID;
    }

    /** 返回已识别出具体协议的结果。 */
    @SuppressWarnings("unchecked")
    public static <T> ProtocolDetectionResult<T> detected(T protocol) {
        return new ProtocolDetectionResult<T>(ProtocolDetectionState.DETECTED, checkNotNull(protocol, "protocol"));
    }

    private ProtocolDetectionResult(ProtocolDetectionState state, T result) {
        this.state = state;
        this.result = result;
    }

    /**
     * 返回 {@link ProtocolDetectionState}；若为 {@link ProtocolDetectionState#DETECTED}，
     * 可通过 {@link #detectedProtocol()} 取得协议对象。
     */
    public ProtocolDetectionState state() {
        return state;
    }

    /** 仅在 {@link #state()} 为 {@link ProtocolDetectionState#DETECTED} 时返回协议，否则 {@code null}。 */
    public T detectedProtocol() {
        return result;
    }
}
