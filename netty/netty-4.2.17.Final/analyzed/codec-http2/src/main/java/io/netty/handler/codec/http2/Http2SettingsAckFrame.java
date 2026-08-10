/*
 * Copyright 2019 The Netty Project
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
 * 对已收到的 {@link Http2SettingsFrame} 的 ACK 确认帧。
 * <p>
 * 按 <a href="https://tools.ietf.org/html/rfc7540#section-6.5">HTTP/2 协议</a>，ACK 必须按序生效；
 * 本 ACK 对应最早收到且尚未确认的那条 {@link Http2SettingsFrame}。
 */
public interface Http2SettingsAckFrame extends Http2Frame {
    /** 单例：SETTINGS ACK 帧无载荷，所有实例语义相同。 */
    Http2SettingsAckFrame INSTANCE = new DefaultHttp2SettingsAckFrame();

    @Override
    String name();
}
