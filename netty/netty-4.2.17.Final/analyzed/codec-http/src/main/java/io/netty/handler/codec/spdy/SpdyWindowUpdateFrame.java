/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.spdy;

/**
 * SPDY WINDOW_UPDATE 帧：为连接或单条流增加可发送字节窗口（流控信用）。
 * <p>接收方消费 DATA 后通过本帧告知对端可再发送的字节增量；
 * stream ID 为 0 时作用于连接级窗口，非零时仅更新对应流。
 */
public interface SpdyWindowUpdateFrame extends SpdyFrame {

    /** 目标流 ID；0 表示连接级窗口更新。 */
    int streamId();

    /** 设置流 ID，不可为负数。 */
    SpdyWindowUpdateFrame setStreamId(int streamID);

    /**
     * 返回窗口增量（Delta-Window-Size），即本次释放的可发送字节数。
     */
    int deltaWindowSize();

    /**
     * 设置窗口增量，必须为正数。
     */
    SpdyWindowUpdateFrame setDeltaWindowSize(int deltaWindowSize);
}
