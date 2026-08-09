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

/**
 * {@link QuicChannel} 可创建流数量上限变化时触发的用户事件（单例）。
 * 应用可据此更新本地流创建策略。
 */
public final class QuicStreamLimitChangedEvent implements QuicEvent {

    /** 全局单例事件实例，避免重复分配。 */
    static final QuicStreamLimitChangedEvent INSTANCE = new QuicStreamLimitChangedEvent();

    private QuicStreamLimitChangedEvent() { }
}
