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

package org.keycloak.crypto.def;

import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.keycloak.common.crypto.PemUtilsProvider;
import org.keycloak.common.util.DerUtils;
import org.keycloak.common.util.PemException;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * 基于 BouncyCastle 的 PEM 编解码实现，将密钥或证书编码为 PEM 格式字符串。
 *
 * @author <a href="mailto:david.anderson@redhat.com">David Anderson</a>
 * @version $Revision: 1 $
 */
public class BCPemUtilsProvider extends PemUtilsProvider {


    /**
     * 使用 BC 库将对象编码为 JCA PEM 字符串。
     * 
     * @param obj 待编码对象（密钥或证书）
     * @return 不含 BEGIN/END 行的 PEM 正文
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

    /**
     * 从 PEM 字符串解码公钥；优先通过 {@link SubjectPublicKeyInfo} 识别算法，失败时回退为 RSA。
     *
     * @param pem PEM 编码的公钥
     * @return 解码后的公钥
     */
    @Override
    public PublicKey decodePublicKey(String pem) {
        try {
            // 尝试通过 SubjectPublicKeyInfo 解码以识别密钥类型
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemToDer(pem));
            if (publicKeyInfo != null && publicKeyInfo.getAlgorithm() != null) {
                return new JcaPEMKeyConverter().getPublicKey(publicKeyInfo);
            }
        } catch (Exception e) {
            // PEM 解析失败，回退到强制 RSA 解码
        }

        // 无法识别算法时默认按 RSA 处理
        return decodePublicKey(pem, "RSA");
    }

    /**
     * 从 PEM 字符串解码私钥。
     *
     * @param pem PEM 编码的私钥
     * @return 解码后的私钥，输入为 null 时返回 null
     */
    @Override
    public PrivateKey decodePrivateKey(String pem) {
        if (pem == null) {
            return null;
        }

        try {
            byte[] der = pemToDer(pem);
            return DerUtils.decodePrivateKey(der);
        } catch (Exception e) {
            throw new PemException(e);
        }
    }

}
