/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.crypto.fips;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.keycloak.common.crypto.PemUtilsProvider;
import org.keycloak.common.util.BouncyIntegration;
import org.keycloak.common.util.PemException;
import org.keycloak.common.util.PemUtils;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * 基于 BCFIPS 的 PEM 编解码工具，将密钥与证书编码为 PEM 字符串。
 *
 * @author <a href="mailto:david.anderson@redhat.com">David Anderson</a>
 * @version $Revision: 1 $
 */
public class BCFIPSPemUtilsProvider extends PemUtilsProvider {


    /**
     * 使用 BCFIPS 库将对象编码为 JCA PEM 字符串（不含 BEGIN/END 行）。
     *
     * @param obj
     * @return The encoded PEM string
     */
    @Override
    protected String encode(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            StringWriter writer = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
            pemWriter.writeObject(obj);
            pemWriter.flush();
            pemWriter.close();
            String s = writer.toString();
            return removeBeginEnd(s);
        } catch (Exception e) {
            throw new PemException(e);
        }
    }

    @Override
    public PublicKey decodePublicKey(String pem) {
        try {
            // 优先通过 SubjectPublicKeyInfo 解析以识别密钥类型
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemToDer(pem));
            if (publicKeyInfo != null && publicKeyInfo.getAlgorithm() != null) {
                return new JcaPEMKeyConverter().getPublicKey(publicKeyInfo);
            }
        } catch (Exception e) {
            // 解析失败时回退为强制 RSA 解码
        }

        // 无法识别类型时假定 RSA
        return decodePublicKey(pem, "RSA");
    }

    @Override
    public PrivateKey decodePrivateKey(String pem) {
        if (pem == null) {
            return null;
        }

        try {
            boolean beginEndAvailable = pem.startsWith("-----BEGIN");
            Object parsedPk;
            if (beginEndAvailable) { // BCFIPS 可根据 BEGIN 行识别 PKCS#8/RSA/EC 等格式，无需回退
                parsedPk = readPrivateKeyObject(pem);
            } else {
                try {
                    // 传统 PEM 格式（无 BEGIN 行）
                    String rsaPem = PemUtils.addRsaPrivateKeyBeginEnd(pem);
                    parsedPk = readPrivateKeyObject(rsaPem);
                } catch (IOException ioe) {
                    // 通用 PKCS#8 格式
                    pem = PemUtils.addPrivateKeyBeginEnd(pem);
                    parsedPk = readPrivateKeyObject(pem);
                }
            }

            PrivateKeyInfo privateKeyInfo;
            if (parsedPk instanceof PEMKeyPair) {
                // 已知格式密钥（如 BEGIN RSA PRIVATE KEY）
                PEMKeyPair pemKeyPair = (PEMKeyPair)parsedPk;
                privateKeyInfo = pemKeyPair.getPrivateKeyInfo();
            } else if (parsedPk instanceof PrivateKeyInfo) {
                // PKCS#8 通用私钥（BEGIN PRIVATE KEY）
                privateKeyInfo = (PrivateKeyInfo) parsedPk;
            } else {
                throw new IllegalStateException("Unknown type returned by PEMParser when parsing private key: " + parsedPk.getClass());
            }

            return new JcaPEMKeyConverter()
                    .setProvider(BouncyIntegration.PROVIDER)
                    .getPrivateKey(privateKeyInfo);
        } catch (Exception e) {
            throw new PemException(e);
        }
    }

    /** 解析带 BEGIN/END 标记的 PEM 私钥对象。 */
    private Object readPrivateKeyObject(String pemWithBeginEnd) throws IOException {
        PEMParser parser = new PEMParser(new StringReader(pemWithBeginEnd));
        return parser.readObject();
    }

}
