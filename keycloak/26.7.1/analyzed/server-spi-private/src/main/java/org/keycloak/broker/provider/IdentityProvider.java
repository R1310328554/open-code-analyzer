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
package org.keycloak.broker.provider;

import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.broker.provider.util.IdentityProviderTypeUtil;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;

/**
 * 身份联邦提供者核心 SPI 接口，扩展 {@link Provider} 并暴露 {@link IdentityProviderModel} 配置。
 * <p>提供导出、映射器兼容、密钥重载与类型判定等默认能力。</p>
 *
 * @author Pedro Igor
 */
public interface IdentityProvider<C extends IdentityProviderModel> extends Provider {

    C getConfig();

    /**
     * 以指定格式导出身份提供者表示（如 SAML EntityDescriptor）。
     *
     * Export a representation of the IdentityProvider in a specific format.  For example, a SAML EntityDescriptor
     *
     * @return
     */
    default Response export(UriInfo uriInfo, RealmModel realm, String format) {
        return Response.noContent().build();
    }

    /**
     * 检查映射器是否与本身份提供者兼容（{@link IdentityProviderMapper#ANY_PROVIDER} 或 providerId 匹配）。
     *
     * Checks whether a mapper is supported for this Identity Provider.
     */
    default boolean isMapperSupported(IdentityProviderMapper mapper) {
        List<String> compatibleIdps = Arrays.asList(mapper.getCompatibleProviders());
        return compatibleIdps.contains(IdentityProviderMapper.ANY_PROVIDER)
                || compatibleIdps.contains(getConfig().getProviderId());
    }

    /**
     * 若配置允许，从 JWKS 或元数据端点重新加载 IdP 公钥（OIDC/SAML 等）。
     *
     * Reload keys for the identity provider if permitted in it.For example OIDC or
     * SAML providers will reload the keys from the jwks or metadata endpoint.
     * @return true if reloaded, false if not
     */
    default boolean reloadKeys() {
        return false;
    }

    /**
     * 判断本提供者是否属于指定 {@link IdentityProviderType}（默认基于接口，子类可检查配置）。
     *
     * Returns if this Identity Provider is of the passed type. By default it just returns
     * true when it implements the correct interface. Sub-classes like the OIDC
     * provider can check specific configuration options.
     * @param session The helper session
     * @param type The type to check
     * @return true if the provider is of the passed type, false otherwise
     */
    default boolean isType(KeycloakSession session, IdentityProviderType type) {
        return IdentityProviderTypeUtil.listTypesFromProvider(session, this).contains(type);
    }
}
