/*
 * Copyright 2022 The Netty Project
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

import javax.net.ssl.SSLEngine;


/**
 * TLS 密钥日志接口，输出格式遵循
 * <a href="https://developer.mozilla.org/en-US/docs/Mozilla/Projects/NSS/Key_Log_Format">
 *     NSS Key Log Format</a>，便于 Wireshark 等工具解密抓包流量（仅用于调试）。
 */
public interface BoringSSLKeylog {

    /**
     * BoringSSL 生成会话密钥时回调，由实现方记录一行 NSS 格式的密钥日志。
     *
     * @param engine    关联的 {@link SSLEngine}。
     * @param key       NSS 密钥日志格式的单行字符串。
     */
    void logKey(SSLEngine engine, String key);
}
