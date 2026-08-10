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

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.internal.tcnative.SSL;
import io.netty.util.IllegalReferenceCountException;

import javax.net.ssl.SSLException;
import javax.net.ssl.X509KeyManager;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.ssl.ReferenceCountedOpenSslContext.toBIO;

/**
 * Provides {@link OpenSslKeyMaterial} for a given alias.
 *
 * <p>按 KeyManager alias 将证书链与私钥解析为 native {@link OpenSslKeyMaterial}；
 * 内置单条目 {@link MaterialCache} 避免同一 alias 重复 PEM 解析，{@link #destroy()} 后拒绝新缓存。</p>
 */
class OpenSslKeyMaterialProvider {
    /** destroy 后写入 cache 的哨兵，CAS 循环直到清完旧条目。 */
    private static final MaterialCache SENTINEL_DESTROYED = new MaterialCache(null, null, null);

    private final X509KeyManager keyManager;
    /** PKCS#8 私钥解密口令（可为 null）。 */
    private final String password;
    /** 最近一次成功解析的 alias 材料缓存（至多一条）。 */
    private final AtomicReference<MaterialCache> cache;

    OpenSslKeyMaterialProvider(X509KeyManager keyManager, String password) {
        this.keyManager = keyManager;
        this.password = password;
        cache = new AtomicReference<>();
    }

    /** 构造前校验证书链与私钥能否被 OpenSSL 解析；不可编码且允许 fallback 的私钥跳过校验。 */
    static void validateKeyMaterialSupported(X509Certificate[] keyCertChain, PrivateKey key, String keyPassword,
                                             boolean allowSignatureFallback)
            throws SSLException {
        validateSupported(keyCertChain);
        validateSupported(key, keyPassword, allowSignatureFallback);
    }

    private static void validateSupported(PrivateKey key, String password,
                                          boolean allowSignatureFallback) throws SSLException {
        if (key == null) {
            return;
        }

        // 无私钥 encoded 且启用签名 fallback 时，由 OpenSslPrivateKeyMethod 等路径处理
        if (key.getEncoded() == null && allowSignatureFallback) {
            return;
        }

        long pkeyBio = 0;
        long pkey = 0;

        try {
            pkeyBio = toBIO(UnpooledByteBufAllocator.DEFAULT, key);
            pkey = SSL.parsePrivateKey(pkeyBio, password);
        } catch (Exception e) {
            throw new SSLException("PrivateKey type not supported " + key.getFormat(), e);
        } finally {
            SSL.freeBIO(pkeyBio);
            if (pkey != 0) {
                SSL.freePrivateKey(pkey);
            }
        }
    }

    private static void validateSupported(X509Certificate[] certificates) throws SSLException {
        if (certificates == null || certificates.length == 0) {
            return;
        }

        long chainBio = 0;
        long chain = 0;
        PemEncoded encoded = null;
        try {
            encoded = PemX509Certificate.toPEM(UnpooledByteBufAllocator.DEFAULT, true, certificates);
            chainBio = toBIO(UnpooledByteBufAllocator.DEFAULT, encoded.retain());
            chain = SSL.parseX509Chain(chainBio);
        } catch (Exception e) {
            throw new SSLException("Certificate type not supported", e);
        } finally {
            SSL.freeBIO(chainBio);
            if (chain != 0) {
                SSL.freeX509Chain(chain);
            }
            if (encoded != null) {
                encoded.release();
            }
        }
    }

    /**
     * Returns the underlying {@link X509KeyManager} that is used.
     *
     * <p>返回底层 {@link X509KeyManager}，供 {@link OpenSslKeyMaterialManager} 选择 alias。</p>
     */
    X509KeyManager keyManager() {
        return keyManager;
    }

    /**
     * Returns the {@link OpenSslKeyMaterial} or {@code null} (if none) that should be used during the handshake by
     * OpenSSL.
     *
     * <p>按 alias 返回握手用材料；证书链为空返回 null。命中缓存且实例未变则复用 native 指针。</p>
     */
    OpenSslKeyMaterial chooseKeyMaterial(ByteBufAllocator allocator, String alias) throws Exception {
        X509Certificate[] certificates = keyManager.getCertificateChain(alias);
        if (certificates == null || certificates.length == 0) {
            return null;
        }

        PrivateKey key = keyManager.getPrivateKey(alias);
        MaterialCache materialCache = cache.get();
        if (materialCache != null && materialCache != SENTINEL_DESTROYED && materialCache.retain()) {
            if (materialCache.sameInstances(key, certificates)) {
                return materialCache.material(); // 已在 retain() 中增加引用计数
            } else {
                // 证书/私钥实例已变，释放旧缓存并重新解析
                materialCache.release();
            }
        }

        OpenSslKeyMaterial keyMaterial = createKeyMaterial(allocator, certificates, key);
        materialCache = new MaterialCache(key, certificates, keyMaterial);

        // 为新条目 retain 后放入 cache，并 release 被替换的旧条目
        materialCache.retain();
        MaterialCache oldMaterial = cache.getAndSet(materialCache);
        if (oldMaterial != null) {
            if (oldMaterial == SENTINEL_DESTROYED) {
                destroyCache(); // 销毁过程中插入的新条目，走 destroyCache 避免重复释放
            } else {
                oldMaterial.release();
            }
        }

        return keyMaterial;
    }

    /** 将 PEM 证书链与私钥解析为 native 指针并封装为 {@link OpenSslKeyMaterial}。 */
    private OpenSslKeyMaterial createKeyMaterial(
            ByteBufAllocator allocator, X509Certificate[] certificates, PrivateKey key)
            throws Exception {
        PemEncoded encoded = PemX509Certificate.toPEM(allocator, true, certificates);
        long chainBio = 0;
        long pkeyBio = 0;
        long chain = 0;
        long pkey = 0;
        try {
            chainBio = toBIO(allocator, encoded.retain());
            chain = SSL.parseX509Chain(chainBio);

            OpenSslKeyMaterial keyMaterial;
            if (key instanceof OpenSslPrivateKey) {
                keyMaterial = ((OpenSslPrivateKey) key).newKeyMaterial(chain, certificates);
            } else {
                pkeyBio = toBIO(allocator, key);
                pkey = key == null ? 0 : SSL.parsePrivateKey(pkeyBio, password);
                keyMaterial = new DefaultOpenSslKeyMaterial(chain, pkey, certificates);
            }

            // 所有权已移交给 OpenSslKeyMaterial，此处置 0 避免 finally 重复 free
            chain = 0;
            pkey = 0;
            return keyMaterial;
        } finally {
            SSL.freeBIO(chainBio);
            SSL.freeBIO(pkeyBio);
            if (chain != 0) {
                SSL.freeX509Chain(chain);
            }
            if (pkey != 0) {
                SSL.freePrivateKey(pkey);
            }
            encoded.release();
        }
    }

    /**
     * Will be invoked once the provider should be destroyed.
     *
     * <p>上下文销毁时调用，清空并释放缓存的 native 材料。</p>
     */
    void destroy() {
        destroyCache();
    }

    /** CAS 将 cache 设为 SENTINEL 并 release 所有已缓存材料。 */
    private void destroyCache() {
        MaterialCache oldMaterial;
        while ((oldMaterial = cache.getAndSet(SENTINEL_DESTROYED)) != SENTINEL_DESTROYED) {
            if (oldMaterial != null) {
                oldMaterial.release();
            }
        }
    }

    /** 单 alias 材料缓存：按 PrivateKey/X509Certificate[] 引用相等判断是否可复用。 */
    private static final class MaterialCache {
        private final PrivateKey key;
        private final X509Certificate[] certs;
        private final OpenSslKeyMaterial material;

        private MaterialCache(PrivateKey key, X509Certificate[] certs, OpenSslKeyMaterial material) {
            this.key = key;
            this.certs = certs;
            this.material = material;
        }

        OpenSslKeyMaterial material() {
            return material;
        }

        boolean sameInstances(PrivateKey key, X509Certificate[] certs) {
            X509Certificate[] existingCerts = this.certs;
            int length = existingCerts.length;
            if (this.key != key || length != certs.length) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (certs[i] != existingCerts[i]) {
                    return false;
                }
            }
            return true;
        }

        boolean retain() {
            if (material.refCnt() != 0) {
                try {
                    material.retain();
                    return true;
                } catch (IllegalReferenceCountException ignore) {
                    // Fall through to the `return false` below.
                }
            }
            return false;
        }

        void release() {
            material.release();
        }
    }
}
