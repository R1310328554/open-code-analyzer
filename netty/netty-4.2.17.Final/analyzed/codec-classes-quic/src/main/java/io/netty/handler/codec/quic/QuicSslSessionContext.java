/*
 * Copyright 2023 The Netty Project
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

import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSessionContext;

/**
 * 扩展 {@link SSLSessionContext}，支持 QUIC/TLS 会话票证（ticket）密钥等高级操作。
 */
public interface QuicSslSessionContext extends SSLSessionContext {

    /**
     * 设置 TLS 会话票证加密密钥：数组首元素用于加解密，其余仅解密以支持密钥轮换；
     * 轮换由调用方负责。{@code null} 时由库自动生成并轮换。
     *
     * @param keys the tickets to use.
     */
    void setTicketKeys(SslSessionTicketKey @Nullable ... keys);
}
