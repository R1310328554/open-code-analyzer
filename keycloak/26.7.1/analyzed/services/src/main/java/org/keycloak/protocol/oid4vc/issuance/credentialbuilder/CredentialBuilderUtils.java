/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.credentialbuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.keycloak.protocol.oid4vc.model.VerifiableCredential;

/**
 * 凭证构建工具类：提供跨格式共用的辅助方法。
 */
public class CredentialBuilderUtils {

    /** 自动生成凭证 ID 时使用的 URN 模板。 */
    private static final String ID_TEMPLATE = "urn:uuid:%s";

    // 从给定 VC 读取凭证 ID，缺失则生成 urn:uuid 形式的新 ID。
    /**
     * 获取可验证凭证 ID；若未设置则生成随机 UUID URN。
     * @param verifiableCredential 可验证凭证
     * @return 凭证标识字符串
     */
    public static String createCredentialId(VerifiableCredential verifiableCredential) {
        return Optional.ofNullable(verifiableCredential.getId())
                .orElse(URI.create(String.format(ID_TEMPLATE, UUID.randomUUID())))
                .toString();
    }
}
