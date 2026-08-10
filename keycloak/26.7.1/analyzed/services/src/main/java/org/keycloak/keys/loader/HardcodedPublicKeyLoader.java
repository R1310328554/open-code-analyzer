/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.keys.loader;

import java.util.Collections;

import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.JavaAlgorithm;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.keys.PublicKeyLoader;

import org.jboss.logging.Logger;

/**
 * 硬编码公钥加载器：从 PEM 编码字符串解析 RSA/EC/EdDSA 公钥供 JWT 验证使用。
 * <p>实现 {@link PublicKeyLoader}，算法无法识别或密钥为空时返回 {@link PublicKeysWrapper#EMPTY}。</p>
 *
 * @author hmlnarik
 */
public class HardcodedPublicKeyLoader implements PublicKeyLoader {

    private static final Logger logger = Logger.getLogger(HardcodedPublicKeyLoader.class);

    private final KeyWrapper keyWrapper;

    /** @param kid 密钥 ID @param encodedKey PEM 编码公钥 @param algorithm JWS 算法名称 */
    public HardcodedPublicKeyLoader(String kid, String encodedKey, String algorithm) {
        if (encodedKey != null && !encodedKey.trim().isEmpty()) {
            KeyWrapper kw = new KeyWrapper();
            kw.setKid(kid);
            kw.setUse(KeyUse.SIG);
            kw.setAlgorithm(algorithm);
            // 按算法类型从 PEM 字符串解码 RSA/EC/EdDSA 公钥
            if (JavaAlgorithm.isRSAJavaAlgorithm(algorithm)) {
                kw.setType(KeyType.RSA);
                kw.setPublicKey(PemUtils.decodePublicKey(encodedKey, KeyType.RSA));
            } else if (JavaAlgorithm.isECJavaAlgorithm(algorithm)) {
                kw.setType(KeyType.EC);
                kw.setPublicKey(PemUtils.decodePublicKey(encodedKey, KeyType.EC));
            } else if (JavaAlgorithm.isEddsaJavaAlgorithm(algorithm)) {
                kw.setType(KeyType.OKP);
                kw.setPublicKey(PemUtils.decodePublicKey(encodedKey, Algorithm.EdDSA));
                kw.setCurve(kw.getPublicKey().getAlgorithm());
            } else {
                logger.warnf("Unrecognized or invalid algorithm %s for hardcoded public key", algorithm);
                kw = null;
            }
            keyWrapper = kw;
        } else {
            keyWrapper = null;
        }
    }

    /** @return 包含单个硬编码公钥的包装，或空包装 */
    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        return keyWrapper != null
                ? new PublicKeysWrapper(Collections.singletonList(getSavedPublicKey()))
                : PublicKeysWrapper.EMPTY;
    }

    /** @return 构造时解析并缓存的公钥 */
    protected KeyWrapper getSavedPublicKey() {
        return keyWrapper;
    }
}
