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

import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * This class will use a finalizer to ensure native resources are automatically cleaned up. To avoid finalizers
 * and manually release the native memory see {@link ReferenceCountedOpenSslContext}.
 *
 * <p>OpenSSL {@link SslContext} 的非引用计数基类：{@link #newEngine0} 创建 {@link OpenSslEngine}，
 * {@link #finalize()} 通过 {@link OpenSsl#releaseIfNeeded(Object)} 在 GC 时释放 native 内存。
 * 生产环境推荐使用 {@link ReferenceCountedOpenSslContext} 子类以避免 finalizer 开销。</p>
 */
public abstract class OpenSslContext extends ReferenceCountedOpenSslContext {
    /** 将 {@link ApplicationProtocolConfig} 转为 OpenSSL 协商器后委托父类构造。 */
    OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, ApplicationProtocolConfig apnCfg,
                   int mode, Certificate[] keyCertChain,
                   ClientAuth clientAuth, String[] protocols, boolean startTls, String endpointIdentificationAlgorithm,
                   boolean enableOcsp, List<SNIServerName> serverNames, ResumptionController resumptionController,
                   Map.Entry<SslContextOption<?>, Object>[] options,
                   List<OpenSslCredential> credentials)
            throws SSLException {
        super(ciphers, cipherFilter, toNegotiator(apnCfg), mode, keyCertChain,
                clientAuth, protocols, startTls, endpointIdentificationAlgorithm, enableOcsp, false,
                serverNames, resumptionController, options, credentials);
    }

    /** 直接使用 {@link OpenSslApplicationProtocolNegotiator} 的构造路径。 */
    OpenSslContext(Iterable<String> ciphers, CipherSuiteFilter cipherFilter, OpenSslApplicationProtocolNegotiator apn,
                   int mode, Certificate[] keyCertChain,
                   ClientAuth clientAuth, String[] protocols, boolean startTls, boolean enableOcsp,
                   List<SNIServerName> serverNames, ResumptionController resumptionController,
                   Map.Entry<SslContextOption<?>, Object>[] options,
                   List<OpenSslCredential> credentials)
            throws SSLException {
        super(ciphers, cipherFilter, apn, mode, keyCertChain,
                clientAuth, protocols, startTls, null, enableOcsp, false, serverNames, resumptionController, options,
                credentials);
    }

    /** 创建带 finalizer 的 {@link OpenSslEngine} 实例（非引用计数引擎）。 */
    @Override
    final SSLEngine newEngine0(ByteBufAllocator alloc, String peerHost, int peerPort, boolean jdkCompatibilityMode) {
        return new OpenSslEngine(this, alloc, peerHost, peerPort, jdkCompatibilityMode,
                endpointIdentificationAlgorithm, serverNames);
    }

    /** GC 回收时尝试释放尚未显式 release 的 OpenSSL 上下文 native 资源。 */
    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected final void finalize() throws Throwable {
        try {
            OpenSsl.releaseIfNeeded(this);
        } finally {
            super.finalize();
        }
    }
}
