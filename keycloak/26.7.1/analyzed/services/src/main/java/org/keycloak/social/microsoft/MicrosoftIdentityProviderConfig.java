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

package org.keycloak.social.microsoft;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Microsoft 身份提供者配置。
 * <p>扩展 OIDC 配置，支持可选的 Azure AD 租户 ID。</p>
 */
public class MicrosoftIdentityProviderConfig extends OIDCIdentityProviderConfig {

    /** 从领域 IdP 模型构造配置。 */
    public MicrosoftIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    /** 创建空配置实例。 */
    public MicrosoftIdentityProviderConfig() {

    }

    /**
     * 获取 Azure AD 租户 ID。
     * <p>未配置或为空时返回 {@code null}，表示使用多租户端点。</p>
     */
    public String getTenantId() {
        String tenantId = getConfig().get("tenantId");

        return tenantId == null || tenantId.isEmpty() ? null : tenantId;
    }

    /** 设置 Azure AD 租户 ID。 */
    public void setTenantId(final String tenantId) {
        getConfig().put("tenantId", tenantId);
    }
}
