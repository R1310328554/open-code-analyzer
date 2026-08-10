/*
 * Copyright 2026 The Netty Project
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

import io.netty.internal.tcnative.SSLCredential;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakTracker;

/**
 * Default implementation of {@link OpenSslCredential}.
 *
 * <p>This class manages the lifecycle of a native BoringSSL {@code SSL_CREDENTIAL} object.
 *
 * <p>{@link OpenSslCredential} 的默认实现，以引用计数管理原生 BoringSSL {@code SSL_CREDENTIAL} 生命周期。</p>
 */
final class DefaultOpenSslCredential extends AbstractReferenceCounted implements OpenSslCredentialPointer {

    /** 泄漏检测器，便于排查未 release 的凭证对象。 */
    private static final ResourceLeakDetector<DefaultOpenSslCredential> leakDetector =
            ResourceLeakDetectorFactory.instance().newResourceLeakDetector(DefaultOpenSslCredential.class);

    /** 跟踪本实例的泄漏记录器。 */
    private final ResourceLeakTracker<DefaultOpenSslCredential> leak;
    /** 凭证类型（证书、私钥等）。 */
    private final CredentialType type;
    /** 原生 SSL_CREDENTIAL 指针；release 后归零。 */
    private long credential;

    /**
     * Creates a new credential instance.
     *
     * @param credential the native SSL_CREDENTIAL pointer
     * @param type the credential type
     *
     * <p>包内构造：绑定原生指针与类型并注册泄漏跟踪。</p>
     */
    DefaultOpenSslCredential(long credential, CredentialType type) {
        this.credential = credential;
        this.type = type;
        this.leak = leakDetector.track(this);
    }

    @Override
    /** 返回原生凭证地址；引用计数为 0 时抛异常。 */
    public long credentialAddress() {
        if (refCnt() <= 0) {
            throw new IllegalReferenceCountException();
        }
        return credential;
    }

    @Override
    /** 返回凭证类型枚举。 */
    public CredentialType type() {
        return type;
    }

    @Override
    /** 引用计数归零时释放原生 SSL_CREDENTIAL 并关闭泄漏跟踪。 */
    protected void deallocate() {
        try {
            SSLCredential.free(credential);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to free SSL_CREDENTIAL", e);
        } finally {
            credential = 0;
            if (leak != null) {
                boolean closed = leak.close(this);
                assert closed;
            }
        }
    }

    @Override
    public DefaultOpenSslCredential retain() {
        if (leak != null) {
            leak.record();
        }
        super.retain();
        return this;
    }

    @Override
    public DefaultOpenSslCredential retain(int increment) {
        if (leak != null) {
            leak.record();
        }
        super.retain(increment);
        return this;
    }

    @Override
    public DefaultOpenSslCredential touch() {
        if (leak != null) {
            leak.record();
        }
        super.touch();
        return this;
    }

    @Override
    public DefaultOpenSslCredential touch(Object hint) {
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
