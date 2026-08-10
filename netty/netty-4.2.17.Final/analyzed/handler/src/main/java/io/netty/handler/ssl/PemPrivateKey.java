/*
 * Copyright 2016 The Netty Project
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

import java.security.PrivateKey;

import javax.security.auth.Destroyable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.CharsetUtil;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.ObjectUtil;

/**
 * This is a special purpose implementation of a {@link PrivateKey} which allows the
 * user to pass PEM/PKCS#8 encoded key material straight into {@link OpenSslContext}
 * without having to parse and re-encode bytes in Java land.
 *
 * All methods other than what's implemented in {@link PemEncoded} and {@link Destroyable}
 * throw {@link UnsupportedOperationException}s.
 *
 * <p>将 PKCS#8/PEM 私钥字节直接交给 {@link OpenSslContext}，避免 Java 侧解析再编码；除
 * {@link PemEncoded}/{@link Destroyable} 外的方法均不支持。</p>
 *
 * @see PemEncoded
 * @see OpenSslContext
 * @see #valueOf(byte[])
 * @see #valueOf(ByteBuf)
 */
public final class PemPrivateKey extends AbstractReferenceCounted implements PrivateKey, PemEncoded {
    private static final long serialVersionUID = 7978017465645018936L;

    private static final byte[] BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n".getBytes(CharsetUtil.US_ASCII);
    private static final byte[] END_PRIVATE_KEY = "\n-----END PRIVATE KEY-----\n".getBytes(CharsetUtil.US_ASCII);

    /** {@link PrivateKey#getFormat()} 返回值，标识 PKCS#8。 */
    private static final String PKCS8_FORMAT = "PKCS#8";

    /**
     * Creates a {@link PemEncoded} value from the {@link PrivateKey}.
     *
     * <p>若已是 {@link PemEncoded} 则 retain；否则 getEncoded 后包装为 PEM {@link PemValue}。</p>
     */
    static PemEncoded toPEM(ByteBufAllocator allocator, boolean useDirect, PrivateKey key) {
        // 已为 PEM 编码时直接 retain，避免重复 Base64 与拷贝
        if (key instanceof PemEncoded) {
            return ((PemEncoded) key).retain();
        }

        byte[] bytes = key.getEncoded();
        if (bytes == null) {
            throw new IllegalArgumentException(key.getClass().getName() + " does not support encoding");
        }

        return toPEM(allocator, useDirect, bytes);
    }

    /** 将 DER PKCS#8 字节编码为标准 PEM {@link PemValue}（敏感，释放时清零）。 */
    static PemEncoded toPEM(ByteBufAllocator allocator, boolean useDirect, byte[] bytes) {
        ByteBuf encoded = Unpooled.wrappedBuffer(bytes);
        try {
            ByteBuf base64 = SslUtils.toBase64(allocator, encoded);
            try {
                int size = BEGIN_PRIVATE_KEY.length + base64.readableBytes() + END_PRIVATE_KEY.length;

                boolean success = false;
                final ByteBuf pem = useDirect ? allocator.directBuffer(size) : allocator.buffer(size);
                try {
                    pem.writeBytes(BEGIN_PRIVATE_KEY);
                    pem.writeBytes(base64);
                    pem.writeBytes(END_PRIVATE_KEY);

                    PemValue value = new PemValue(pem, true);
                    success = true;
                    return value;
                } finally {
                    // 构造失败时须释放已分配的 PEM ByteBuf，防止泄漏
                    if (!success) {
                        SslUtils.zerooutAndRelease(pem);
                    }
                }
            } finally {
                SslUtils.zerooutAndRelease(base64);
            }
        } finally {
            SslUtils.zerooutAndRelease(encoded);
        }
    }

    /**
     * Creates a {@link PemPrivateKey} from raw {@code byte[]}.
     *
     * ATTENTION: It's assumed that the given argument is a PEM/PKCS#8 encoded value.
     * No input validation is performed to validate it.
     *
     * <p>假定入参已是 PEM/PKCS#8 文本，不做格式校验。</p>
     */
    public static PemPrivateKey valueOf(byte[] key) {
        return valueOf(Unpooled.wrappedBuffer(key));
    }

    /**
     * Creates a {@link PemPrivateKey} from raw {@code ByteBuf}.
     *
     * ATTENTION: It's assumed that the given argument is a PEM/PKCS#8 encoded value.
     * No input validation is performed to validate it.
     *
     * <p>从 {@link ByteBuf} 包装 PEM 私钥，不拷贝内容。</p>
     */
    public static PemPrivateKey valueOf(ByteBuf key) {
        return new PemPrivateKey(key);
    }

    /** PEM/PKCS#8 原始字节（引用计数由本对象管理）。 */
    private final ByteBuf content;

    private PemPrivateKey(ByteBuf content) {
        this.content = ObjectUtil.checkNotNull(content, "content");
    }

    @Override
    public boolean isSensitive() {
        return true;
    }

    @Override
    public ByteBuf content() {
        int count = refCnt();
        if (count <= 0) {
            throw new IllegalReferenceCountException(count);
        }

        return content;
    }

    @Override
    public PemPrivateKey copy() {
        return replace(content.copy());
    }

    @Override
    public PemPrivateKey duplicate() {
        return replace(content.duplicate());
    }

    @Override
    public PemPrivateKey retainedDuplicate() {
        return replace(content.retainedDuplicate());
    }

    @Override
    public PemPrivateKey replace(ByteBuf content) {
        return new PemPrivateKey(content);
    }

    @Override
    public PemPrivateKey touch() {
        content.touch();
        return this;
    }

    @Override
    public PemPrivateKey touch(Object hint) {
        content.touch(hint);
        return this;
    }

    @Override
    public PemPrivateKey retain() {
        return (PemPrivateKey) super.retain();
    }

    @Override
    public PemPrivateKey retain(int increment) {
        return (PemPrivateKey) super.retain(increment);
    }

    @Override
    protected void deallocate() {
        // 私钥敏感：释放前清零底层 ByteBuf
        SslUtils.zerooutAndRelease(content);
    }

    @Override
    public byte[] getEncoded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAlgorithm() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFormat() {
        return PKCS8_FORMAT;
    }

    /**
     * NOTE: This is a JDK8 interface/method. Due to backwards compatibility
     * reasons it's not possible to slap the {@code @Override} annotation onto
     * this method.
     *
     * @see Destroyable#destroy()
     *
     * <p>通过 release 至 0 销毁密钥材料（JDK8 {@link Destroyable} 兼容）。</p>
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
}
