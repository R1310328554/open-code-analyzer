/*
 * Copyright 2015 The Netty Project
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
package io.netty.handler.ssl.util;

import io.netty.util.internal.ObjectUtil;

import javax.security.cert.CertificateException;
import javax.security.cert.CertificateExpiredException;
import javax.security.cert.CertificateNotYetValidException;
import javax.security.cert.X509Certificate;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Date;

/**
 * 延迟解析 DER 的 legacy {@link javax.security.cert.X509Certificate} 包装。
 * <p>供仍依赖 javax.security.cert API 的路径使用；首次访问证书字段时才调用
 * {@link javax.security.cert.X509Certificate#getInstance(byte[])}。</p>
 */
public final class LazyJavaxX509Certificate extends X509Certificate {
    private final byte[] bytes;
    private X509Certificate wrapped;

    /**
     * Creates a new instance which will lazy parse the given bytes. Be aware that the bytes will not be cloned.
     * <p>构造时不解析；{@code bytes} 不会被复制。</p>
     */
    public LazyJavaxX509Certificate(byte[] bytes) {
        this.bytes = ObjectUtil.checkNotNull(bytes, "bytes");
    }

    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        unwrap().checkValidity();
    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        unwrap().checkValidity(date);
    }

    @Override
    public int getVersion() {
        return unwrap().getVersion();
    }

    @Override
    public BigInteger getSerialNumber() {
        return unwrap().getSerialNumber();
    }

    @Override
    public Principal getIssuerDN() {
        return unwrap().getIssuerDN();
    }

    @Override
    public Principal getSubjectDN() {
        return unwrap().getSubjectDN();
    }

    @Override
    public Date getNotBefore() {
        return unwrap().getNotBefore();
    }

    @Override
    public Date getNotAfter() {
        return unwrap().getNotAfter();
    }

    @Override
    public String getSigAlgName() {
        return unwrap().getSigAlgName();
    }

    @Override
    public String getSigAlgOID() {
        return unwrap().getSigAlgOID();
    }

    @Override
    public byte[] getSigAlgParams() {
        return unwrap().getSigAlgParams();
    }

    @Override
    public byte[] getEncoded() {
        return bytes.clone();
    }

    /**
     * Return the underyling {@code byte[]} without cloning it first. This {@code byte[]} <strong>must</strong> never
     * be mutated.
     * <p>返回内部 DER 字节数组（不克隆）；<strong>禁止</strong>修改。</p>
     */
    byte[] getBytes() {
        return bytes;
    }

    @Override
    public void verify(PublicKey key)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
                   SignatureException {
        unwrap().verify(key);
    }

    @Override
    public void verify(PublicKey key, String sigProvider)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
                   SignatureException {
        unwrap().verify(key, sigProvider);
    }

    @Override
    public String toString() {
        return unwrap().toString();
    }

    @Override
    public PublicKey getPublicKey() {
        return unwrap().getPublicKey();
    }

    private X509Certificate unwrap() {
        X509Certificate wrapped = this.wrapped;
        if (wrapped == null) {
            try {
                // 双重检查锁外单线程首次解析即可（字段非 volatile，与 LazyX509Certificate 策略一致）
                wrapped = this.wrapped = X509Certificate.getInstance(bytes);
            } catch (CertificateException e) {
                throw new IllegalStateException(e);
            }
        }
        return wrapped;
    }
}
