/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufAllocator;

import java.util.List;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;

/**
 * Implements a {@link SSLEngine} using
 * <a href="https://www.openssl.org/docs/crypto/BIO_s_bio.html#EXAMPLE">OpenSSL BIO abstractions</a>.
 * <p>
 * This class will use a finalizer to ensure native resources are automatically cleaned up. To avoid finalizers
 * and manually release the native memory see {@link ReferenceCountedOpenSslEngine}.
 *
 * <p>基于 OpenSSL BIO 的 {@link SSLEngine} 非引用计数实现，逻辑在
 * {@link ReferenceCountedOpenSslEngine}；{@link #finalize()} 在 GC 时释放泄漏的 {@code SSL*}。
 * 生产环境应使用 {@link ReferenceCountedOpenSslEngine} 并显式 {@code refCnt} 管理。</p>
 */
public final class OpenSslEngine extends ReferenceCountedOpenSslEngine {
    /** 委托父类完成 native SSL* 与 BIO 初始化。 */
    OpenSslEngine(OpenSslContext context, ByteBufAllocator alloc, String peerHost, int peerPort,
                  boolean jdkCompatibilityMode, String endpointIdentificationAlgorithm,
                  List<SNIServerName> serverNames) {
        super(context, alloc, peerHost, peerPort, jdkCompatibilityMode, false, endpointIdentificationAlgorithm,
                serverNames);
    }

    /** GC 时通过 {@link OpenSsl#releaseIfNeeded} 回收未显式 shutdown 的 native 引擎。 */
    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        try {
            OpenSsl.releaseIfNeeded(this);
        } finally {
            super.finalize();
        }
    }
}
