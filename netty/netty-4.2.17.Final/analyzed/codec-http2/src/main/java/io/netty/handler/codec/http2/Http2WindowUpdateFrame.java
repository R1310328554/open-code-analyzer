/*
 * Copyright 2016 The Netty Project
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
 * HTTP/2 WINDOW_UPDATE 帧：通知对端扩大流级或连接级流量控制窗口。
 */
public interface Http2WindowUpdateFrame extends Http2StreamFrame {

    /**
     * 窗口增量（字节数）；接收方据此增加可发送额度。
     * <p>流 ID 为 0 时表示连接级窗口，否则为对应流的窗口。
     */
    int windowSizeIncrement();
}
