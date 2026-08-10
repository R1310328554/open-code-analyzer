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

import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.ObjectUtil;

/**
 * This is a special purpose implementation of a {@link X509Certificate} which allows
 * the user to pass PEM/PKCS#8 encoded data straight into {@link OpenSslContext} without
 * having to parse and re-encode bytes in Java land.
 *
 * All methods other than what's implemented in {@link PemEncoded}'s throw
 * {@link UnsupportedOperationException}s.
 *
 * <p>将 PEM 编码的 X.509 证书链直接传入 OpenSSL，跳过 Java 侧完整证书对象解析；仅实现
 * {@link PemEncoded} 与引用计数相关 API。</p>
 *
 * @see PemEncoded
 * @see OpenSslContext
 * @see #valueOf(byte[])
 * @see #valueOf(ByteBuf)
 */
public final class PemX509Certificate extends X509Certificate implements PemEncoded {

    private static final byte[] BEGIN_CERT = "-----BEGIN CERTIFICATE-----\n".getBytes(CharsetUtil.US_ASCII);
    private static final byte[] END_CERT = "\n-----END CERTIFICATE-----\n".getBytes(CharsetUtil.US_ASCII);

    /**
     * Creates a {@link PemEncoded} value from the {@link X509Certificate}s.
     *
     * <p>单元素且已是 {@link PemEncoded} 时 retain；否则逐张编码并拼接 PEM。</p>
     */
    static PemEncoded toPEM(ByteBufAllocator allocator, boolean useDirect,
            X509Certificate... chain) throws CertificateEncodingException {

        checkNonEmpty(chain, "chain");

        // 单证书且已为 PemEncoded 时短路，避免重复编码
        if (chain.length == 1) {
            X509Certificate first = chain[0];
            if (first instanceof PemEncoded) {
                return ((PemEncoded) first).retain();
            }
        }

        boolean success = false;
        ByteBuf pem = null;
        try {
            for (X509Certificate cert : chain) {

                if (cert == null) {
                    throw new IllegalArgumentException("Null element in chain: " + Arrays.toString(chain));
                }

                if (cert instanceof PemEncoded) {
                    pem = append(allocator, useDirect, (PemEncoded) cert, chain.length, pem);
                } else {
                    pem = append(allocator, useDirect, cert, chain.length, pem);
                }
            }

            PemValue value = new PemValue(pem, false);
            success = true;
            return value;
        } finally {
            // 异常路径释放已分配的 PEM 缓冲，防止泄漏
            if (!success && pem != null) {
                pem.release();
            }
        }
    }

    /**
     * Appends the {@link PemEncoded} value to the {@link ByteBuf} (last arg) and returns it.
     * If the {@link ByteBuf} didn't exist yet it'll create it using the {@link ByteBufAllocator}.
     *
     * <p>将已有 PEM 片段 slice 写入目标缓冲；首次调用时按链长度估算 initialCapacity。</p>
     */
    private static ByteBuf append(ByteBufAllocator allocator, boolean useDirect,
            PemEncoded encoded, int count, ByteBuf pem) {

        ByteBuf content = encoded.content();

        if (pem == null) {
            // 见 append(X509Certificate...)：按单张可读字节 × count 估算容量
            pem = newBuffer(allocator, useDirect, content.readableBytes() * count);
        }

        pem.writeBytes(content.slice());
        return pem;
    }

    /**
     * Appends the {@link X509Certificate} value to the {@link ByteBuf} (last arg) and returns it.
     * If the {@link ByteBuf} didn't exist yet it'll create it using the {@link ByteBufAllocator}.
     *
     * <p>DER 编码后 Base64 并写入 BEGIN/END CERTIFICATE 行。</p>
     */
    private static ByteBuf append(ByteBufAllocator allocator, boolean useDirect,
            X509Certificate cert, int count, ByteBuf pem) throws CertificateEncodingException {

        ByteBuf encoded = Unpooled.wrappedBuffer(cert.getEncoded());
        try {
            ByteBuf base64 = SslUtils.toBase64(allocator, encoded);
            try {
                if (pem == null) {
                    // 证书 DER 大小差异大，按 (BEGIN+Base64+END)*count 近似初始容量
                    pem = newBuffer(allocator, useDirect,
                            (BEGIN_CERT.length + base64.readableBytes() + END_CERT.length) * count);
                }

                pem.writeBytes(BEGIN_CERT);
                pem.writeBytes(base64);
                pem.writeBytes(END_CERT);
            } finally {
                base64.release();
            }
        } finally {
            encoded.release();
        }

        return pem;
    }

    private static ByteBuf newBuffer(ByteBufAllocator allocator, boolean useDirect, int initialCapacity) {
        return useDirect ? allocator.directBuffer(initialCapacity) : allocator.buffer(initialCapacity);
    }

    /**
     * Creates a {@link PemX509Certificate} from raw {@code byte[]}.
     *
     * ATTENTION: It's assumed that the given argument is a PEM/PKCS#8 encoded value.
     * No input validation is performed to validate it.
     *
     * <p>假定字节为 PEM 证书文本，不做 ASN.1 校验。</p>
     */
    public static PemX509Certificate valueOf(byte[] key) {
        return valueOf(Unpooled.wrappedBuffer(key));
    }

    /**
     * Creates a {@link PemX509Certificate} from raw {@code ByteBuf}.
     *
     * ATTENTION: It's assumed that the given argument is a PEM/PKCS#8 encoded value.
     * No input validation is performed to validate it.
     *
     * <p>包装已有 PEM {@link ByteBuf}，引用计数委托给 content。</p>
     */
    public static PemX509Certificate valueOf(ByteBuf key) {
        return new PemX509Certificate(key);
    }

    /** PEM 证书字节；引用计数与 content 同步。 */
    private final ByteBuf content;

    private PemX509Certificate(ByteBuf content) {
        this.content = ObjectUtil.checkNotNull(content, "content");
    }

    @Override
    public boolean isSensitive() {
        // X.509 证书为公开材料，无需清零
        return false;
    }

    @Override
    public int refCnt() {
        return content.refCnt();
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
    public PemX509Certificate copy() {
        return replace(content.copy());
    }

    @Override
    public PemX509Certificate duplicate() {
        return replace(content.duplicate());
    }

    @Override
    public PemX509Certificate retainedDuplicate() {
        return replace(content.retainedDuplicate());
    }

    @Override
    public PemX509Certificate replace(ByteBuf content) {
        return new PemX509Certificate(content);
    }

    @Override
    public PemX509Certificate retain() {
        content.retain();
        return this;
    }

    @Override
    public PemX509Certificate retain(int increment) {
        content.retain(increment);
        return this;
    }

    @Override
    public PemX509Certificate touch() {
        content.touch();
        return this;
    }

    @Override
    public PemX509Certificate touch(Object hint) {
        content.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return content.release();
    }

    @Override
    public boolean release(int decrement) {
        return content.release(decrement);
    }

    @Override
    public byte[] getEncoded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getExtensionValue(String oid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkValidity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkValidity(Date date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigInteger getSerialNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getIssuerDN() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getSubjectDN() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNotBefore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNotAfter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getTBSCertificate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getSignature() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSigAlgName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSigAlgOID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getSigAlgParams() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean[] getIssuerUniqueID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean[] getSubjectUniqueID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean[] getKeyUsage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBasicConstraints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void verify(PublicKey key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void verify(PublicKey key, String sigProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PublicKey getPublicKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PemX509Certificate)) {
            return false;
        }

        PemX509Certificate other = (PemX509Certificate) o;
        return content.equals(other.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public String toString() {
        return content.toString(CharsetUtil.UTF_8);
    }
}
