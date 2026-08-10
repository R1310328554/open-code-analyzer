/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.jpa;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.keycloak.Config;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.common.Profile;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.RealmAttributes;
import org.keycloak.protocol.saml.SamlConfigAttributes;

import static org.keycloak.models.jpa.JpaRealmProviderFactory.PROVIDER_ID;
import static org.keycloak.models.jpa.JpaRealmProviderFactory.PROVIDER_PRIORITY;

/**
 * JPA {@link ClientProvider} 工厂：创建共享 {@link JpaRealmProvider} 实例处理客户端 CRUD。
 * <p>
 * {@link #init} 合并 SPI/系统属性中的可搜索客户端属性，并强制包含 SAML/JWT/SSF 所需键。
 * {@link #postInit} 在启用 ADMIN_FINE_GRAINED_AUTHZ_V2 时监听 realm 属性变更以初始化 FGAP schema。
 */
public class JpaClientProviderFactory implements ClientProviderFactory {

    /** 客户端 Admin UI / 搜索 API 可索引的属性名集合。 */
    private Set<String> clientSearchableAttributes = null;

    /** 无论配置如何都必须可搜索的内置属性（SAML artifact、JWT issuer 等）。 */
    private static final List<String> REQUIRED_SEARCHABLE_ATTRIBUTES = Arrays.asList(
        "saml_idp_initiated_sso_url_name",
        SamlConfigAttributes.SAML_ARTIFACT_BINDING_IDENTIFIER,
        "jwt.credential.issuer",
        "jwt.credential.sub"
    );

    /**
     * 解析 searchableAttributes：SPI 数组 → 系统属性 → 默认仅 REQUIRED；
     * SSF 特性启用时追加 ssf.enabled / ssf.stream.id。
     */
    @Override
    public void init(Config.Scope config) {
        String[] searchableAttrsArr = config.getArray("searchableAttributes");
        if (searchableAttrsArr == null) {
            String s = System.getProperty("keycloak.client.searchableAttributes");
            searchableAttrsArr = s == null ? null : s.split("\\s*,\\s*");
        }
        HashSet<String> s = new HashSet<>(REQUIRED_SEARCHABLE_ATTRIBUTES);
        if (searchableAttrsArr != null) {
            s.addAll(Arrays.asList(searchableAttrsArr));
        }
        if (Profile.isFeatureEnabled(Profile.Feature.SSF)) {
            // SSF Receiver 客户端需按 stream 配置检索
            s.add("ssf.enabled");
            s.add("ssf.stream.id");
        }
        clientSearchableAttributes = Collections.unmodifiableSet(s);
    }

    /** admin-permissions-enabled 变为 true 时初始化 realm 的 FGAP schema。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ_V2)) {
            factory.register(event -> {
                if (event instanceof RealmModel.RealmAttributeUpdateEvent attrUpdateEvent) {
                    if (Objects.equals(attrUpdateEvent.getAttributeName(), RealmAttributes.ADMIN_PERMISSIONS_ENABLED) && Boolean.parseBoolean(attrUpdateEvent.getAttributeValue())) {
                        KeycloakSession keycloakSession = attrUpdateEvent.getKeycloakSession();
                        RealmModel realm = attrUpdateEvent.getRealm();
                        AdminPermissionsSchema.SCHEMA.init(keycloakSession, realm);
                    }
                }
            });
        }
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 委托 JpaRealmProvider，传入可搜索属性集（client scope 参数为 null）。 */
    @Override
    public ClientProvider create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return new JpaRealmProvider(session, em, clientSearchableAttributes, null);
    }

    @Override
    public void close() {
    }

    @Override
    public int order() {
        return PROVIDER_PRIORITY;
    }

}
