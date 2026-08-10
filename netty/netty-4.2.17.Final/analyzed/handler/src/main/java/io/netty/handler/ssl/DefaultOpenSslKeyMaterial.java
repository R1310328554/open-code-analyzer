/*
 * Copyright 2018 The Netty Project
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

import io.netty.internal.tcnative.SSL;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;

import java.security.cert.X509Certificate;

/**
 * {@link OpenSslKeyMaterial} 默认实现：持有原生证书链与私钥指针，引用计数管理释放。
 */
final class DefaultOpenSslKeyMaterial extends AbstractReferenceCounted implements OpenSslKeyMaterial {

    /** 检测未正确 release 的密钥材料泄漏。 */
    private static final ResourceLeakDetector<DefaultOpenSslKeyMaterial> leakDetector =
            ResourceLeakDetectorFactory.instance().newResourceLeakDetector(DefaultOpenSslKeyMaterial.class);
    /** 本实例的泄漏跟踪句柄。 */
    private final ResourceLeakTracker<DefaultOpenSslKeyMaterial> leak;
    /** Java 侧证书链副本，供 {@link #certificateChain()} 返回克隆。 */
    private final X509Certificate[] x509CertificateChain;
    /** OpenSSL 原生 X509 证书链指针。 */
    private long chain;
    /** OpenSSL 原生 EVP_PKEY 私钥指针。 */
    private long privateKey;

    /** 包内构造：绑定原生链/私钥与 Java 证书数组。 */
    DefaultOpenSslKeyMaterial(long chain, long privateKey, X509Certificate[] x509CertificateChain) {
        this.chain = chain;
        this.privateKey = privateKey;
        this.x509CertificateChain = x509CertificateChain;
        leak = leakDetector.track(this);
    }

    @Override
    /** 返回证书链的防御性克隆。 */
    public X509Certificate[] certificateChain() {
        return x509CertificateChain.clone();
    }

    @Override
    /** 返回原生证书链地址；引用计数耗尽时抛异常。 */
    public long certificateChainAddress() {
        if (refCnt() <= 0) {
            throw new IllegalReferenceCountException();
        }
        return chain;
    }

    @Override
    /** 返回原生私钥地址。 */
    public long privateKeyAddress() {
        if (refCnt() <= 0) {
            throw new IllegalReferenceCountException();
        }
        return privateKey;
    }

    @Override
    /** 释放原生证书链与私钥并关闭泄漏跟踪。 */
    protected void deallocate() {
        SSL.freeX509Chain(chain);
        chain = 0;
        SSL.freePrivateKey(privateKey);
        privateKey = 0;
        if (leak != null) {
            boolean closed = leak.close(this);
            assert closed;
        }
    }

    @Override
    public DefaultOpenSslKeyMaterial retain() {
        if (leak != null) {
            leak.record();
        }
        super.retain();
        return this;
    }

    @Override
    public DefaultOpenSslKeyMaterial retain(int increment) {
        if (leak != null) {
            leak.record();
        }
        super.retain(increment);
        return this;
    }

    @Override
    public DefaultOpenSslKeyMaterial touch() {
        if (leak != null) {
            leak.record();
        }
        super.touch();
        return this;
    }

    @Override
    public DefaultOpenSslKeyMaterial touch(Object hint) {
        if (leak != null) {
            leak.record(hint);
        }
        return this;
    }

    @Override
    public boolean release() {
        if (leak != null) {
            leak.record();
        }
        return super.release();
    }

    @Override
    public boolean release(int decrement) {
        if (leak != null) {
            leak.record();
        }
        return super.release(decrement);
    }
}
