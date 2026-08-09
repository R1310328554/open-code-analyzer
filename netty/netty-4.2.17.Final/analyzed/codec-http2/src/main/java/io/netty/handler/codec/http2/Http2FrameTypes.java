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
 * RFC 7540 定义的标准 HTTP/2 帧类型常量注册表。
 * <p>帧头 8 位 type 字段取值见下表；扩展帧类型由 {@link Http2FrameReader} 以 unknown frame 处理。
 */
public final class Http2FrameTypes {
    /** DATA 帧：承载应用层请求/响应体。 */
    public static final byte DATA = 0x0;
    /** HEADERS 帧：携带 HPACK 编码的头块，可开启新流。 */
    public static final byte HEADERS = 0x1;
    /** PRIORITY 帧：更新流的优先级依赖关系。 */
    public static final byte PRIORITY = 0x2;
    /** RST_STREAM 帧：异常终止单条流。 */
    public static final byte RST_STREAM = 0x3;
    /** SETTINGS 帧：协商连接级参数（帧大小、并发流数等）。 */
    public static final byte SETTINGS = 0x4;
    /** PUSH_PROMISE 帧：服务端推送预告，关联 promised stream。 */
    public static final byte PUSH_PROMISE = 0x5;
    /** PING 帧：连接保活与 RTT 测量。 */
    public static final byte PING = 0x6;
    /** GO_AWAY 帧：发起连接级优雅关闭。 */
    public static final byte GO_AWAY = 0x7;
    /** WINDOW_UPDATE 帧：扩展流控窗口。 */
    public static final byte WINDOW_UPDATE = 0x8;
    /** CONTINUATION 帧：延续 HEADERS/PUSH_PROMISE 的头块分片。 */
    public static final byte CONTINUATION = 0x9;

    private Http2FrameTypes() {
    }
}
