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
import io.netty.util.internal.EmptyArrays;

import javax.security.auth.Destroyable;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * <p>包装已解析的 native {@code EVP_PKEY*} 的 {@link PrivateKey} 实现：不提供 {@link #getEncoded()}，
 * 引用计数归零时调用 {@code SSL.freePrivateKey}。可通过 {@link #newKeyMaterial} 与证书链组合为 {@link OpenSslKeyMaterial}。</p>
 */
final class OpenSslPrivateKey extends AbstractReferenceCounted implements PrivateKey {

    /** native 私钥指针 {@code EVP_PKEY*}。 */
    private long privateKeyAddress;

    OpenSslPrivateKey(long privateKeyAddress) {
        this.privateKeyAddress = privateKeyAddress;
    }

    @Override
    public String getAlgorithm() {
        return "unknown";
    }

    @Override
    public String getFormat() {
        // 不支持编码导出，按 PrivateKey 规范返回 null
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return null;
    }

    /** 返回 native 私钥地址；引用计数须 &gt; 0。 */
    long privateKeyAddress() {
        if (refCnt() <= 0) {
            throw new IllegalReferenceCountException();
        }
        return privateKeyAddress;
    }

    @Override
    protected void deallocate() {
        SSL.freePrivateKey(privateKeyAddress);
        privateKeyAddress = 0;
    }

    @Override
    public OpenSslPrivateKey retain() {
        super.retain();
        return this;
    }

    @Override
    public OpenSslPrivateKey retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public OpenSslPrivateKey touch() {
        super.touch();
        return this;
    }

    @Override
    public OpenSslPrivateKey touch(Object hint) {
        return this;
    }

    /**
     * NOTE: This is a JDK8 interface/method. Due to backwards compatibility
     * reasons it's not possible to slap the {@code @Override} annotation onto
     * this method.
     *
     * @see Destroyable#destroy()
     */
    @Override
    public void destroy() {
        release(refCnt());
    }

    /**
     * NOTE: This is a JDK8 interface/method. Due to backwards compatibility
     * reasons it's not possible to slap the {@code @Override} annotation onto
     * this method.
     *
     * @see Destroyable#isDestroyed()
     */
    @Override
    public boolean isDestroyed() {
        return refCnt() == 0;
    }

    /**
     * Create a new {@link OpenSslKeyMaterial} which uses the private key that is held by {@link OpenSslPrivateKey}.
     *
     * When the material is created we increment the reference count of the enclosing {@link OpenSslPrivateKey} and
     * decrement it again when the reference count of the {@link OpenSslKeyMaterial} reaches {@code 0}.
     *
     * <p>创建 {@link OpenSslKeyMaterial} 时 retain  enclosing 私钥，材料 release 至 0 时再 release 私钥。</p>
     */
    OpenSslKeyMaterial newKeyMaterial(long certificateChain, X509Certificate[] chain) {
        return new OpenSslPrivateKeyMaterial(certificateChain, chain);
    }

    // 包内可见，仅供单元测试
    /** 与 enclosing {@link OpenSslPrivateKey} 生命周期绑定的 {@link OpenSslKeyMaterial} 实现。 */
    final class OpenSslPrivateKeyMaterial extends AbstractReferenceCounted implements OpenSslKeyMaterial {

        // Package-private for unit-test only
        /** native 证书链 {@code STACK_OF(X509)*}。 */
        long certificateChain;
        /** Java 侧证书链副本，{@link #certificateChain()} 返回 clone。 */
        private final X509Certificate[] x509CertificateChain;

        OpenSslPrivateKeyMaterial(long certificateChain, X509Certificate[] x509CertificateChain) {
            this.certificateChain = certificateChain;
            this.x509CertificateChain = x509CertificateChain == null ?
                    EmptyArrays.EMPTY_X509_CERTIFICATES : x509CertificateChain;
            OpenSslPrivateKey.this.retain();
        }

        @Override
        public X509Certificate[] certificateChain() {
            return x509CertificateChain.clone();
        }

        @Override
        public long certificateChainAddress() {
            if (refCnt() <= 0) {
                throw new IllegalReferenceCountException();
            }
            return certificateChain;
        }

        @Override
        public long privateKeyAddress() {
            if (refCnt() <= 0) {
                throw new IllegalReferenceCountException();
            }
            return OpenSslPrivateKey.this.privateKeyAddress();
        }

        @Override
        public OpenSslKeyMaterial touch(Object hint) {
            OpenSslPrivateKey.this.touch(hint);
            return this;
        }

        @Override
        public OpenSslKeyMaterial retain() {
            super.retain();
            return this;
        }

        @Override
        public OpenSslKeyMaterial retain(int increment) {
            super.retain(increment);
            return this;
        }

        @Override
        public OpenSslKeyMaterial touch() {
            OpenSslPrivateKey.this.touch();
            return this;
        }

        @Override
        protected void deallocate() {
            releaseChain();
            OpenSslPrivateKey.this.release();
        }

        private void releaseChain() {
            SSL.freeX509Chain(certificateChain);
            certificateChain = 0;
        }
    }
}
