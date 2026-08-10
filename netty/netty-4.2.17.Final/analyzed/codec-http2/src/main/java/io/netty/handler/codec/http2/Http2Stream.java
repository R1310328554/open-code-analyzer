/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.http2;

/**
 * HTTP/2 连接中的单条逻辑流，由 {@link Http2Connection} 管理生命周期与状态机。
 */
public interface Http2Stream {

    /**
     * HTTP/2 流状态机；{@code localSideOpen}/{@code remoteSideOpen} 标记本端/对端是否仍可发送。
     */
    enum State {
        /** 已分配 ID，尚未打开。 */
        IDLE(false, false),
        /** 本地发起 PUSH 预留，对端半关闭。 */
        RESERVED_LOCAL(false, false),
        /** 对端发起 PUSH 预留，本地半关闭。 */
        RESERVED_REMOTE(false, false),
        /** 双向均可收发。 */
        OPEN(true, true),
        /** 本地已 END_STREAM，对端仍可发送。 */
        HALF_CLOSED_LOCAL(false, true),
        /** 对端已 END_STREAM，本地仍可发送。 */
        HALF_CLOSED_REMOTE(true, false),
        /** 双向均已关闭。 */
        CLOSED(false, false);

        private final boolean localSideOpen;
        private final boolean remoteSideOpen;

        State(boolean localSideOpen, boolean remoteSideOpen) {
            this.localSideOpen = localSideOpen;
            this.remoteSideOpen = remoteSideOpen;
        }

        /**
         * 本端是否仍可发送（OPEN 或 HALF_CLOSED_REMOTE）。
         */
        public boolean localSideOpen() {
            return localSideOpen;
        }

        /**
         * 对端是否仍可发送（OPEN 或 HALF_CLOSED_LOCAL）。
         */
        public boolean remoteSideOpen() {
            return remoteSideOpen;
        }
    }

    /** 连接内唯一的流标识符（客户端用奇数，服务端用偶数）。 */
    int id();

    /** 当前状态机状态。 */
    State state();

    /**
     * 激活流，使其出现在 {@link Http2Connection#forEachActiveStream(Http2StreamVisitor)} 中。
     * 状态转换规则：
     * <ul>
     * <li>IDLE + {@code halfClosed=false} → {@link State#OPEN}</li>
     * <li>IDLE + {@code halfClosed=true}（本地流）→ {@link State#HALF_CLOSED_LOCAL}，{@link #isHeadersSent()} 为 true</li>
     * <li>IDLE + {@code halfClosed=true}（远端流）→ {@link State#HALF_CLOSED_REMOTE}，{@link #isHeadersReceived()} 为 true</li>
     * <li>HALF_CLOSED_REMOTE → {@link State#RESERVED_LOCAL}</li>
     * <li>HALF_CLOSED_LOCAL → {@link State#RESERVED_REMOTE}</li>
     * </ul>
     */
    Http2Stream open(boolean halfClosed) throws Http2Exception;

    /** 将流置为 CLOSED。 */
    Http2Stream close();

    /** 关闭本端发送方向；若因此双向均关闭则级联关闭。 */
    Http2Stream closeLocalSide();

    /** 关闭对端发送方向；若因此双向均关闭则级联关闭。 */
    Http2Stream closeRemoteSide();

    /** 本端是否已发送 RST_STREAM（不影响 state，仅作标记）。 */
    boolean isResetSent();

    /** 标记本端已发送 RST_STREAM。 */
    Http2Stream resetSent();

    /**
     * 绑定应用自定义属性。
     * @return The value that was previously associated with {@code key}, or {@code null} if there was none.
     */
    <V> V setProperty(Http2Connection.PropertyKey key, V value);

    /** 读取应用自定义属性。 */
    <V> V getProperty(Http2Connection.PropertyKey key);

    /** 读取并移除应用自定义属性。 */
    <V> V removeProperty(Http2Connection.PropertyKey key);

    /**
     * 标记已向对端发送头部：首次为请求/响应头，第二次为 trailers。
     * @param isInformational {@code true} if the headers contain an informational status code (for responses only).
     */
    Http2Stream headersSent(boolean isInformational);

    /** 是否已发送初始头部。 */
    boolean isHeadersSent();

    /** 是否已发送 trailers。 */
    boolean isTrailersSent();

    /**
     * 标记已收到头部：首次为请求/响应头，第二次为 trailers。
     * @param isInformational {@code true} if the headers contain an informational status code (for responses only).
     */
    Http2Stream headersReceived(boolean isInformational);

    /** 是否已收到初始头部。 */
    boolean isHeadersReceived();

    /** 是否已收到 trailers。 */
    boolean isTrailersReceived();

    /** 标记已向对端发送 PUSH_PROMISE。 */
    Http2Stream pushPromiseSent();

    /** 是否已发送 PUSH_PROMISE。 */
    boolean isPushPromiseSent();
}
