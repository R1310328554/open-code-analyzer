/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.timeout;

import io.netty.channel.Channel;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * {@link IdleStateHandler} 在 {@link Channel} 空闲时触发的用户事件。
 */
public class IdleStateEvent {
    /** 首次读空闲事件。 */
    public static final IdleStateEvent FIRST_READER_IDLE_STATE_EVENT =
            new DefaultIdleStateEvent(IdleState.READER_IDLE, true);
    /** 读空闲事件（非首次）。 */
    public static final IdleStateEvent READER_IDLE_STATE_EVENT =
            new DefaultIdleStateEvent(IdleState.READER_IDLE, false);
    /** 首次写空闲事件。 */
    public static final IdleStateEvent FIRST_WRITER_IDLE_STATE_EVENT =
            new DefaultIdleStateEvent(IdleState.WRITER_IDLE, true);
    /** 写空闲事件（非首次）。 */
    public static final IdleStateEvent WRITER_IDLE_STATE_EVENT =
            new DefaultIdleStateEvent(IdleState.WRITER_IDLE, false);
    /** 首次读写均空闲事件。 */
    public static final IdleStateEvent FIRST_ALL_IDLE_STATE_EVENT =
            new DefaultIdleStateEvent(IdleState.ALL_IDLE, true);
    /** 读写均空闲事件（非首次）。 */
    public static final IdleStateEvent ALL_IDLE_STATE_EVENT =
            new DefaultIdleStateEvent(IdleState.ALL_IDLE, false);

    /** 空闲类型。 */
    private final IdleState state;
    /** 是否为该类型的首次空闲。 */
    private final boolean first;

    /**
     * 供子类使用的构造器。
     *
     * @param state the {@link IdleStateEvent} which triggered the event.
     * @param first {@code true} if its the first idle event for the {@link IdleStateEvent}.
     */
    protected IdleStateEvent(IdleState state, boolean first) {
        this.state = ObjectUtil.checkNotNull(state, "state");
        this.first = first;
    }

    /**
     * 返回空闲状态类型。
     */
    public IdleState state() {
        return state;
    }

    /**
     * 若为该 {@link IdleState} 的首次空闲事件则返回 {@code true}。
     */
    public boolean isFirst() {
        return first;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + '(' + state + (first ? ", first" : "") + ')';
    }

    /** 预分配字符串表示的默认实现，避免重复拼接。 */
    private static final class DefaultIdleStateEvent extends IdleStateEvent {
        private final String representation;

        DefaultIdleStateEvent(IdleState state, boolean first) {
            super(state, first);
            this.representation = "IdleStateEvent(" + state + (first ? ", first" : "") + ')';
        }

        @Override
        public String toString() {
            return representation;
        }
    }
}
