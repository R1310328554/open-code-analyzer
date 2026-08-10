/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.saml.processing.core.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.keycloak.common.VerificationException;
import org.keycloak.rotation.KeyLocator;
import org.keycloak.saml.SignatureAlgorithm;

import org.jboss.logging.Logger;

/**
 * SAML HTTP-Redirect 绑定的签名验证工具类。
 * <p>对 URL 查询字符串原始字节与 Base64 解码后的签名进行校验，支持按 keyId 查找密钥，失败时遍历 {@link KeyLocator} 中全部可用密钥。</p>
 *
 * @author rmartinc
 */
public class RedirectBindingSignatureUtil {

    /** 日志记录器。 */
    private static final Logger log = Logger.getLogger(RedirectBindingSignatureUtil.class);

    /** 私有构造器，禁止实例化。 */
    private RedirectBindingSignatureUtil (){
        // 工具类
    }

    /**
     * 验证 Redirect 绑定签名。
     * <p>优先使用 {@code keyId} 指定密钥；若失败则遍历 locator 中全部密钥重试。</p>
     *
     * @param sigAlg 签名算法
     * @param rawQueryBytes 待签名的原始查询字节
     * @param decodedSignature Base64 解码后的签名
     * @param locator 密钥定位器
     * @param keyId 首选密钥标识
     * @return 验证成功返回 true
     * @throws KeyManagementException 密钥管理异常
     * @throws VerificationException 验证失败异常
     */
    public static boolean validateRedirectBindingSignature(SignatureAlgorithm sigAlg, byte[] rawQueryBytes, byte[] decodedSignature,
            KeyLocator locator, String keyId) throws KeyManagementException, VerificationException {
        try {
            try {
                Key key = locator.getKey(keyId);
                if (key != null) {
                    return validateRedirectBindingSignatureForKey(sigAlg, rawQueryBytes, decodedSignature, key);
                }
            } catch (KeyManagementException ex) {
            }
        } catch (SignatureException ex) {
            log.debug("Verification failed for key %s: %s", keyId, ex);
            log.trace(ex);
        }

        log.trace("Trying hard to validate XML signature using all available keys.");

        for (Key key : locator) {
            try {
                if (validateRedirectBindingSignatureForKey(sigAlg, rawQueryBytes, decodedSignature, key)) {
                    return true;
                }
            } catch (SignatureException ex) {
                log.debug("Verification failed: %s", ex);
            }
        }

        return false;
    }

    /**
     * 使用指定密钥验证 Redirect 绑定签名。
     *
     * @param sigAlg 签名算法
     * @param rawQueryBytes 待签名的原始查询字节
     * @param decodedSignature 解码后的签名字节
     * @param key 用于验证的密钥（须为 {@link PublicKey}）
     * @return 验证成功返回 true
     * @throws SignatureException 签名操作异常
     */
    public static boolean validateRedirectBindingSignatureForKey(SignatureAlgorithm sigAlg, byte[] rawQueryBytes, byte[] decodedSignature, Key key)
      throws SignatureException {
        if (key == null) {
            return false;
        }

        if (!(key instanceof PublicKey)) {
            log.warnf("Unusable key for signature validation: %s", key);
            return false;
        }

        Signature signature = sigAlg.createSignature(); // todo plugin signature alg
        try {
            signature.initVerify((PublicKey) key);
        } catch (InvalidKeyException ex) {
            log.warnf(ex, "Unusable key for signature validation: %s", key);
            return false;
        }

        signature.update(rawQueryBytes);

        return signature.verify(decodedSignature);
    }
}
