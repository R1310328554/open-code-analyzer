/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.protection.permission;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.common.KeycloakIdentity;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.representations.idm.authorization.PermissionRequest;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
/**
 * UMA 权限端点：接收 {@link PermissionRequest} 列表并返回权限票据或 RPT。
 */
public class PermissionService extends AbstractPermissionService {

    /** 授权 Provider 实例。 */
    private final AuthorizationProvider authorization;
    /** 目标资源服务器。 */
    private final ResourceServer resourceServer;

    /**
     * @param identity 调用方身份
     * @param resourceServer 资源服务器
     * @param authorization 授权上下文
     */
    public PermissionService(KeycloakIdentity identity, ResourceServer resourceServer, AuthorizationProvider authorization) {
        super(identity, resourceServer, authorization);
        this.resourceServer = resourceServer;
        this.authorization = authorization;
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    /** 批量提交权限请求并委托父类处理 UMA 授权流程。 */
    public Response create(List<PermissionRequest> request) {
        return super.create(request);
    }

}