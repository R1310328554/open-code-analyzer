/*
 * Copyright 2017 The Netty Project
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
 * 表示连接前言（RFC 7540 §3.5）与初始 SETTINGS 帧已写入完成。
 * <p>客户端发送前言；服务器接收前言。客户端应在处理此事件之前避免发送应用数据。
 */
public final class Http2ConnectionPrefaceAndSettingsFrameWrittenEvent {
    /** 单例事件对象，通过 reference equality 识别。 */
    static final Http2ConnectionPrefaceAndSettingsFrameWrittenEvent INSTANCE =
            new Http2ConnectionPrefaceAndSettingsFrameWrittenEvent();

    private Http2ConnectionPrefaceAndSettingsFrameWrittenEvent() {
    }
}
