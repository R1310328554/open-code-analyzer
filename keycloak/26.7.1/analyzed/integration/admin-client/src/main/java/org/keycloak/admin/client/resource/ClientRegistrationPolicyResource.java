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

package org.keycloak.admin.client.resource;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.ComponentTypeRepresentation;

/**
 * 客户端注册策略（Client Registration Policy）的管理 REST 资源。
 * <p>
 * 用于查询可用的客户端注册策略提供程序类型，
 * 这些策略在动态客户端注册时校验注册请求的合规性。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientRegistrationPolicyResource {

    /** 列出所有可用的客户端注册策略提供程序类型。 */
    @Path("providers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ComponentTypeRepresentation> getProviders();
}
