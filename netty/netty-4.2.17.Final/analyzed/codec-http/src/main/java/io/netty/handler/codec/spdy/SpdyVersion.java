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
 * Netty SPDY 编解码器支持的协议版本。
 * <p>当前仅实现 SPDY/3.1（主版本 3、次版本 1），用于 SETTINGS 与帧头协商。
 */
public enum SpdyVersion {
    /** SPDY/3.1 — 在 SPDY/3 基础上修正流控与压缩细节的最终版本。 */
    SPDY_3_1 (3, 1);

    private final int version;
    private final int minorVersion;

    SpdyVersion(int version, int minorVersion) {
        this.version = version;
        this.minorVersion = minorVersion;
    }

    /** 主版本号，写入 SPDY 帧头或 SETTINGS。 */
    public int version() {
        return version;
    }

    /** 次版本号，与 {@link #version()} 共同标识完整协议修订。 */
    public int minorVersion() {
        return minorVersion;
    }
}
