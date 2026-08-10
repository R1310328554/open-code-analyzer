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

package org.keycloak.keys;

import org.keycloak.jose.jwk.JWK;

/**
 * 公钥缓存键工具：为客户端与身份提供者生成 {@link PublicKeyStorageProvider} 使用的模型键。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PublicKeyStorageUtils {

    /** 客户端公钥缓存默认用途：签名（{@link org.keycloak.jose.jwk.JWK.Use#SIG}）。 */
    static final JWK.Use DEFAULT_KEYUSE = JWK.Use.SIG;

    /** 使用默认签名用途生成客户端公钥缓存键。 */
    public static String getClientModelCacheKey(String realmId, String clientUuid) {
        return getClientModelCacheKey(realmId, clientUuid, DEFAULT_KEYUSE);
    }

    /**
     * 生成身份提供者公钥缓存键。
     *
     * @param realmId realm ID
     * @param idpInternalId IdP 内部 ID
     * @return 缓存键字符串
     */
    public static String getIdpModelCacheKey(String realmId, String idpInternalId) {
        return realmId + "::idp::" + idpInternalId;
    }

    /**
     * 按指定 JWK 用途生成客户端公钥缓存键。
     *
     * @param realmId realm ID
     * @param clientUuid 客户端 UUID
     * @param keyUse 密钥用途（签名/加密）
     * @return 缓存键字符串
     */
    public static String getClientModelCacheKey(String realmId, String clientUuid, JWK.Use keyUse) {
        return realmId + "::client::" + clientUuid + "::keyuse::" + keyUse;
    }

}
