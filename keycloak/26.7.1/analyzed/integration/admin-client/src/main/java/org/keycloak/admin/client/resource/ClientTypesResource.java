/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.admin.client.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.ClientTypesRepresentation;

/**
 * 领域客户端类型（Client Types）的管理 REST 资源。
 * <p>
 * 客户端类型定义客户端的默认配置模板，简化同类客户端的批量创建与管理。
 * 自 Keycloak 25 起可用，需启用 {@link org.keycloak.common.Profile.Feature#CLIENT_TYPES} 特性。
 *
 * @since Keycloak 25. All the child endpoints are also available since that version<p>
 *
 *  @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientTypesResource {

    /** 获取领域内配置的客户端类型（含全局类型）。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientTypesRepresentation getClientTypes();


    /**
     * 更新领域内的客户端类型配置。
     * <p>
     * {@code global-client-types} 字段会被忽略，全局类型不可通过此接口修改。
     *
     * @param clientTypes 待更新的客户端类型
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateClientTypes(final ClientTypesRepresentation clientTypes);
}
