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

package org.keycloak.util;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.keycloak.common.util.Base64Url;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.PublicKeysWrapper;
import org.keycloak.jose.jwk.ECPublicJWK;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jwk.OKPPublicJWK;
import org.keycloak.jose.jwk.RSAPublicJWK;
import org.keycloak.jose.jws.crypto.HashUtils;

import org.jboss.logging.Logger;

/**
 * JSON Web Key Set（JWKS）解析与 JWK 指纹计算工具。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JWKSUtils {

    private static final Logger logger = Logger.getLogger(JWKSUtils.class.getName());

    /** JWK 指纹默认哈希算法。 */
    private static final String JWK_THUMBPRINT_DEFAULT_HASH_ALGORITHM = "SHA-256";
    /** 各密钥类型计算指纹所需的 JWK 成员字段。 */
    private static final Map<String, String[]> JWK_THUMBPRINT_REQUIRED_MEMBERS = new HashMap<>();

    static {
        JWK_THUMBPRINT_REQUIRED_MEMBERS.put(KeyType.RSA, new String[] { RSAPublicJWK.MODULUS, RSAPublicJWK.PUBLIC_EXPONENT });
        JWK_THUMBPRINT_REQUIRED_MEMBERS.put(KeyType.EC, new String[] { ECPublicJWK.CRV, ECPublicJWK.X, ECPublicJWK.Y });
        JWK_THUMBPRINT_REQUIRED_MEMBERS.put(KeyType.OKP, new String[] { OKPPublicJWK.CRV, OKPPublicJWK.X });
    }

    /**
     * @deprecated 请改用 {@link #getKeyWrappersForUse(JSONWebKeySet, JWK.Use)}
     **/
    @Deprecated
    public static Map<String, PublicKey> getKeysForUse(JSONWebKeySet keySet, JWK.Use requestedUse) {
        return getKeyWrappersForUse(keySet, requestedUse).getKeys()
                .stream()
                .collect(Collectors.toMap(KeyWrapper::getKid, keyWrapper -> (PublicKey) keyWrapper.getPublicKey()));
    }

    /**
     * 从 JWKS 中筛选指定用途（use）的密钥并包装为 {@link PublicKeysWrapper}。
     *
     * @param keySet JSON Web Key Set
     * @param requestedUse 请求的密钥用途
     * @return 匹配的公钥包装器集合
     */
    public static PublicKeysWrapper getKeyWrappersForUse(JSONWebKeySet keySet, JWK.Use requestedUse) {
        return getKeyWrappersForUse(keySet, requestedUse, false);
    }

    /**
     * 从 JWKS 中筛选指定用途的密钥。
     *
     * @param keySet JSON Web Key Set
     * @param requestedUse 请求的密钥用途
     * @param useRequestedUseWhenNull 当 JWK 缺少 use 字段时是否按 requestedUse 匹配
     * @return 匹配的公钥包装器集合
     */
    public static PublicKeysWrapper getKeyWrappersForUse(JSONWebKeySet keySet, JWK.Use requestedUse, boolean useRequestedUseWhenNull) {
        List<KeyWrapper> result = new ArrayList<>();
        for (JWK jwk : keySet.getKeys()) {
            JWKParser parser = JWKParser.create(jwk);
            if (jwk.getPublicKeyUse() == null && !useRequestedUseWhenNull) {
                logger.debugf("Ignoring JWK key '%s'. Missing required field 'use'.", jwk.getKeyId());
            } else if ((requestedUse.asString().equals(jwk.getPublicKeyUse()) || (jwk.getPublicKeyUse() == null && useRequestedUseWhenNull))
                    && parser.isKeyTypeSupported(jwk.getKeyType())) {
                try {
                    KeyWrapper keyWrapper = wrap(jwk, parser, false);
                    keyWrapper.setUse(getKeyUse(requestedUse.asString()));
                    result.add(keyWrapper);
                } catch (RuntimeException e) {
                    logger.debugf(e, "Ignoring JWK key '%s'. Failed to load key.", jwk.getKeyId());
                }
            }
        }
        return new PublicKeysWrapper(result);
    }

    private static KeyUse getKeyUse(String keyUse) {
        if (keyUse == null) {
            return null;
        } else switch (keyUse) {
            case "sig" :
                return KeyUse.SIG;
            case "enc" :
                return KeyUse.ENC;
            default :
                return null;
        }
    }

    /**
     * 从 JWKS 中返回第一个匹配指定用途且类型受支持的 JWK。
     *
     * @param keySet JSON Web Key Set
     * @param requestedUse 请求的密钥用途
     * @return 匹配的 JWK，若无则返回 null
     */
    public static JWK getKeyForUse(JSONWebKeySet keySet, JWK.Use requestedUse) {
        for (JWK jwk : keySet.getKeys()) {
            JWKParser parser = JWKParser.create(jwk);
            if (jwk.getPublicKeyUse() == null) {
                logger.debugf("Ignoring JWK key '%s'. Missing required field 'use'.", jwk.getKeyId());
            } else if (requestedUse.asString().equals(parser.getJwk().getPublicKeyUse()) && parser.isKeyTypeSupported(jwk.getKeyType())) {
                return jwk;
            }
        }

        return null;
    }

    /**
     * 将单个 JWK 解析为 {@link KeyWrapper}。
     *
     * @param jwk JSON Web Key
     * @return 密钥包装器；类型不支持时返回 null
     */
    public static KeyWrapper getKeyWrapper(JWK jwk) {
        return getKeyWrapper(jwk, false);
    }

    /**
     * 将 JWK 解析为 {@link KeyWrapper}，可选择跳过公钥 material 加载。
     *
     * @param jwk JSON Web Key
     * @param skipPublicKey 为 true 时不设置公钥（仅元数据）
     * @return 密钥包装器；类型不支持时返回 null
     */
    public static KeyWrapper getKeyWrapper(JWK jwk, boolean skipPublicKey) {
        JWKParser parser = JWKParser.create(jwk);
        if (parser.isKeyTypeSupported(jwk.getKeyType())) {
            return wrap(jwk, parser, skipPublicKey);
        } else {
            return null;
        }
    }

    private static KeyWrapper wrap(JWK jwk, JWKParser parser, boolean skipPublicKey) {
        KeyWrapper keyWrapper = new KeyWrapper();
        keyWrapper.setKid(jwk.getKeyId());
        if (jwk.getAlgorithm() != null) {
            keyWrapper.setAlgorithm(jwk.getAlgorithm());
        }
        if (jwk.getOtherClaim(OKPPublicJWK.CRV, String.class) != null) {
            keyWrapper.setCurve(jwk.getOtherClaim(OKPPublicJWK.CRV, String.class));
        }
        keyWrapper.setType(jwk.getKeyType());
        keyWrapper.setUse(getKeyUse(jwk.getPublicKeyUse()));
        if (!skipPublicKey) {
            keyWrapper.setPublicKey(parser.toPublicKey());
        }
        return keyWrapper;
    }

    /**
     * 使用默认 SHA-256 算法计算 JWK 指纹（RFC 7638）。
     *
     * @param key JSON Web Key
     * @return Base64Url 编码的指纹，失败时返回 null
     */
    public static String computeThumbprint(JWK key)  {
        return computeThumbprint(key, JWK_THUMBPRINT_DEFAULT_HASH_ALGORITHM);
    }

    /**
     * 计算 JWK 指纹（RFC 7638）。
     * TreeMap 按键名字典序排列成员，与规范中的哈希计算方式一致。
     *
     * @param key JSON Web Key
     * @param hashAlg 哈希算法名称
     * @return Base64Url 编码的指纹；不支持的密钥类型或序列化失败时返回 null
     */
    public static String computeThumbprint(JWK key, String hashAlg)  {
        String kty = key.getKeyType();
        String[] requiredMembers = JWK_THUMBPRINT_REQUIRED_MEMBERS.get(kty);

        // 例如 oct 类型，参见 RFC 7638 第 3.2 节
        if (requiredMembers == null) {
            throw new UnsupportedOperationException("Unsupported key type: " + kty);
        }

        Map<String, String> members = new TreeMap<>();
        members.put(JWK.KEY_TYPE, kty);

        try {
            for (String member : requiredMembers) {
                members.put(member, key.getOtherClaim(member, String.class));
            }

            byte[] bytes = JsonSerialization.writeValueAsBytes(members);
            byte[] hash = HashUtils.hash(hashAlg, bytes);
            return Base64Url.encode(hash);
        } catch (IOException ex) {
            logger.debugf(ex, "Failed to compute JWK thumbprint for key '%s'.", key.getKeyId());
            return null;
        }
    }

}
