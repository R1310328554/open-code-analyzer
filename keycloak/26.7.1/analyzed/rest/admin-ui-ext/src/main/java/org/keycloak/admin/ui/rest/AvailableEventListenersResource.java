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

package org.keycloak.admin.ui.rest;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.keycloak.admin.ui.rest.model.EventListener;
import org.keycloak.admin.ui.rest.model.ProviderMapper;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * 列出当前可注册的非全局事件监听器 SPI 提供者，供 Admin UI 配置领域事件。
 */
public class AvailableEventListenersResource {

    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** 权限评估器。 */
    private final AdminPermissionEvaluator auth;

    public AvailableEventListenersResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
    }

    @GET
    @Path("/")
    @Produces({"application/json"})
    @Operation(
            summary = "List all available event listener providers",
            description = "This endpoint returns List all available event listener providers"
    )
    @APIResponse(
            responseCode = "200",
            description = "",
            content = {@Content(
                    schema = @Schema(
                            implementation = EventListener.class,
                            type = SchemaType.ARRAY
                    )
            )}
    )
    /** 返回所有非全局 {@link EventListenerProviderFactory} 的 UI 模型列表。 */
    public final List<EventListener> listAvailableEventListeners() {
        auth.realm().requireViewEvents();
        this.auth.adminAuth().getRealm().getEventsListenersStream();

        ArrayList<EventListener> result = new ArrayList<>();

        session.getKeycloakSessionFactory().getProviderFactoriesStream(EventListenerProvider.class).forEach(
               providerFactory -> {
                    // 排除内置全局监听器，仅展示可 per-realm 配置的工厂
                    if (!((EventListenerProviderFactory) providerFactory).isGlobal()) {
                        result.add(ProviderMapper.convertToModel((EventListenerProviderFactory) providerFactory));
                    }
                }
        );

        return result;
    }

}
