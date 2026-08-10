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

package org.keycloak.services.resources.admin;

import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.ComponentTypeRepresentation;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyFactory;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 客户端注册策略 REST 资源。
 * <p>列出可用的 {@link ClientRegistrationPolicy} SPI 提供者及其配置属性。</p>
 *
 * @resource Client Registration Policy
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientRegistrationPolicyResource {

    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 当前领域 */
    private final RealmModel realm;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 构造客户端注册策略资源。 */
    public ClientRegistrationPolicyResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.adminEvent = adminEvent.resource(ResourceType.CLIENT_INITIAL_ACCESS_MODEL);

    }


    /**
     * 获取所有客户端注册策略提供者及其配置属性描述。
     *
     * @return {@link ComponentTypeRepresentation} 流
     */
    @Path("providers")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENT_REGISTRATION_POLICY)
    @Operation( summary="Base path for retrieve providers with the configProperties properly filled")
    public Stream<ComponentTypeRepresentation> getProviders() {
        auth.realm().requireViewRealm();
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(ClientRegistrationPolicy.class)
                .map((ProviderFactory factory) -> {
                    ClientRegistrationPolicyFactory clientRegFactory = (ClientRegistrationPolicyFactory) factory;
                    List<ProviderConfigProperty> configProps = clientRegFactory.getConfigProperties(session);

                    ComponentTypeRepresentation rep = new ComponentTypeRepresentation();
                    rep.setId(clientRegFactory.getId());
                    rep.setHelpText(clientRegFactory.getHelpText());
                    rep.setProperties(ModelToRepresentation.toRepresentation(configProps));
                    return rep;
                });
    }
}
