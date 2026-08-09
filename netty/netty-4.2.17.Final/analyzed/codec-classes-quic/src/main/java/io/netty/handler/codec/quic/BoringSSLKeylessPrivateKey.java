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

import io.netty.util.internal.EmptyArrays;

import java.security.PrivateKey;

/**
 * Keyless TLS 场景下的占位 {@link PrivateKey}。
 * <p>
 * 私钥材料不在 JVM 内，{@link #getEncoded()} 始终返回空数组；实际签名由
 * {@link BoringSSLAsyncPrivateKeyMethod} 在 BoringSSL 回调中异步完成。
 */
final class BoringSSLKeylessPrivateKey implements PrivateKey {

    /** 单例占位私钥，供 {@link BoringSSLKeylessManagerFactory} 的虚拟 KeyStore 使用。 */
    static final BoringSSLKeylessPrivateKey INSTANCE = new BoringSSLKeylessPrivateKey();

    private BoringSSLKeylessPrivateKey() {
    }

    @Override
    public String getAlgorithm() {
        return "keyless";
    }

    @Override
    public String getFormat() {
        return "keyless";
    }

    @Override
    public byte[] getEncoded() {
        // 无本地密钥材料，编码为空
        return EmptyArrays.EMPTY_BYTES;
    }
}
