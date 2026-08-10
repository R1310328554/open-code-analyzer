/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.keycloak.authorization.client.resource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.Throwables;
import org.keycloak.authorization.client.util.TokenCallable;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;
import org.keycloak.representations.idm.authorization.PermissionTicketRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 通过 Protection API 管理权限票据（permission ticket）的入口。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PermissionResource {

    private final Http http;
    private final ServerConfiguration serverConfiguration;
    private final TokenCallable pat;

    public PermissionResource(Http http, ServerConfiguration serverConfiguration, TokenCallable pat) {
        this.http = http;
        this.serverConfiguration = serverConfiguration;
        this.pat = pat;
    }

    /**
     * @deprecated 请使用 {@link #create(PermissionRequest)}
     * @param request 权限请求
     * @return 权限响应
     */
    @Deprecated
    public PermissionResponse forResource(PermissionRequest request) {
        return create(request);
    }

    /** 按条件统计匹配的权限票据数量。 */
    public Long count(final String resourceId,
                      final String scopeId,
                      final String owner,
                      final String requester,
                      final Boolean granted,
                      final Boolean returnNames) {
        Callable<Long> callable = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return http.<Long>get(serverConfiguration.getPermissionEndpoint()+"/ticket/count")
                        .authorizationBearer(pat.call())
                        .param("resourceId", resourceId)
                        .param("scopeId", scopeId)
                        .param("owner", owner)
                        .param("requester", requester)
                        .param("granted", granted == null ? null : granted.toString())
                        .param("returnNames", returnNames == null ? null : returnNames.toString())
                        .response().json(new TypeReference<Long>(){}).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error querying permission ticket count", cause);
        }
    }

    /**
     * 为单个资源及其 scope 创建权限票据。
     *
     * @param request 表示资源与 scope 的 {@link PermissionRequest}（不可为 {@code null}）
     * @return 含所请求权限的权限票据响应
     */
    public PermissionResponse create(PermissionRequest request) {
        return create(Arrays.asList(request));
    }

    /**
     * 为一组资源及其 scope 批量创建权限票据。
     *
     * @param requests 表示资源与 scope 的 {@link PermissionRequest} 列表（不可为 {@code null}）
     * @return 含所请求权限的权限票据响应
     */
    public PermissionResponse create(final List<PermissionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Permission request must not be null or empty");
        }
        Callable<PermissionResponse> callable = new Callable<PermissionResponse>() {
            @Override
            public PermissionResponse call() throws Exception {
                return http.<PermissionResponse>post(serverConfiguration.getPermissionEndpoint())
                        .authorizationBearer(pat.call())
                        .json(JsonSerialization.writeValueAsBytes(requests))
                        .response().json(PermissionResponse.class).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error creating permission ticket", cause);
        }
    }

    /**
     * 创建 UMA 权限票据（完整 {@link PermissionTicketRepresentation}）。
     *
     * @param ticket 表示资源与 scope 的 {@link PermissionTicketRepresentation}（不可为 {@code null}）
     * @return 创建后的权限票据表示
     */
    public PermissionTicketRepresentation create(final PermissionTicketRepresentation ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Permission ticket must not be null or empty");
        }
        if (ticket.getRequester() == null && ticket.getRequesterName() == null) {
            throw new IllegalArgumentException("Permission ticket must have a requester");
        }
        if (ticket.getResource() == null && ticket.getResourceName() == null) {
            throw new IllegalArgumentException("Permission ticket must have a resource");
        }
        if (ticket.getScope() == null && ticket.getScopeName() == null) {
            throw new IllegalArgumentException("Permission ticket must have a scope");
        }
        Callable<PermissionTicketRepresentation> callable = new Callable<PermissionTicketRepresentation>() {
            @Override
            public PermissionTicketRepresentation call() throws Exception {
                return http.<PermissionTicketRepresentation>post(serverConfiguration.getPermissionEndpoint()+"/ticket")
                        .json(JsonSerialization.writeValueAsBytes(ticket))
                        .authorizationBearer(pat.call())
                        .response().json(new TypeReference<PermissionTicketRepresentation>(){}).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error updating permission ticket", cause);
        }
    }
    
    /**
     * 查询与给定 <code>scopeId</code> 关联的全部权限票据。
     *
     * @param scopeId scope ID（不可为 {@code null}）
     * @return 关联的权限票据列表
     */
    public List<PermissionTicketRepresentation> findByScope(final String scopeId) {
        if (scopeId == null) {
            throw new IllegalArgumentException("Scope id must not be null");
        }
        Callable<List<PermissionTicketRepresentation>> callable = new Callable<List<PermissionTicketRepresentation>>() {
            @Override
            public List<PermissionTicketRepresentation> call() throws Exception {
                return http.<List<PermissionTicketRepresentation>>get(serverConfiguration.getPermissionEndpoint()+"/ticket")
                        .authorizationBearer(pat.call())
                        .param("scopeId", scopeId)
                        .response().json(new TypeReference<List<PermissionTicketRepresentation>>(){}).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error querying permission ticket by scope", cause);
        }
    }

    /**
     * 查询与给定 <code>resourceId</code> 关联的全部权限票据。
     *
     * @param resourceId 资源 ID（不可为 {@code null}）
     * @return 关联的权限票据列表
     */
    public List<PermissionTicketRepresentation> findByResource(final String resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("Resource id must not be null");
        }
        Callable<List<PermissionTicketRepresentation>> callable = new Callable<List<PermissionTicketRepresentation>>() {
            @Override
            public List<PermissionTicketRepresentation> call() throws Exception {
                return http.<List<PermissionTicketRepresentation>>get(serverConfiguration.getPermissionEndpoint()+"/ticket")
                        .authorizationBearer(pat.call())
                        .param("resourceId", resourceId)
                        .response().json(new TypeReference<List<PermissionTicketRepresentation>>(){}).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error querying permission ticket by resource", cause);
        }
    }

    /**
     * 按多条件组合查询权限票据。
     *
     * @param resourceId 资源 ID 或名称
     * @param scopeId scope ID 或名称
     * @param owner 所有者 ID 或名称
     * @param requester 请求方 ID 或名称
     * @param granted 为 true 时仅返回已授予的票据
     * @param returnNames 响应是否包含资源、scope 与所有者名称
     * @param firstResult 分页起始位置
     * @param maxResult 最大返回条数
     * @return 匹配的权限票据列表
     */
    public List<PermissionTicketRepresentation> find(final String resourceId,
                                                     final String scopeId,
                                                     final String owner,
                                                     final String requester,
                                                     final Boolean granted,
                                                     final Boolean returnNames,
                                                     final Integer firstResult,
                                                     final Integer maxResult) {
        Callable<List<PermissionTicketRepresentation>> callable = new Callable<List<PermissionTicketRepresentation>>() {
            @Override
            public List<PermissionTicketRepresentation> call() throws Exception {
                return http.<List<PermissionTicketRepresentation>>get(serverConfiguration.getPermissionEndpoint()+"/ticket")
                        .authorizationBearer(pat.call())
                        .param("resourceId", resourceId)
                        .param("scopeId", scopeId)
                        .param("owner", owner)
                        .param("requester", requester)
                        .param("granted", granted == null ? null : granted.toString())
                        .param("returnNames", returnNames == null ? null : returnNames.toString())
                        .param("first", firstResult == null ? null : firstResult.toString())
                        .param("max", maxResult == null ? null : maxResult.toString())
                        .response().json(new TypeReference<List<PermissionTicketRepresentation>>(){}).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error querying permission ticket", cause);
        }
    }

    /**
     * 更新权限票据。
     *
     * @param ticket 待更新的权限票据
     */
    public void update(final PermissionTicketRepresentation ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Permission ticket must not be null or empty");
        }
        if (ticket.getId() == null) {
            throw new IllegalArgumentException("Permission ticket must have an id");
        }
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                http.<Void>put(serverConfiguration.getPermissionEndpoint()+"/ticket")
                        .json(JsonSerialization.writeValueAsBytes(ticket))
                        .authorizationBearer(pat.call())
                        .response()
                        .execute();
                return null;
            }
        };
        try {
            callable.call();
        } catch (Exception cause) {
            Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error updating permission ticket", cause);
        }
    }

    /**
     * 按 ID 删除权限票据。
     * @param ticketId 权限票据 ID
     */
    public void delete(final String ticketId) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission ticket ID must not be null or empty");
        }
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                http.<Void>delete(serverConfiguration.getPermissionEndpoint() + "/ticket/" + ticketId)
                        .authorizationBearer(pat.call())
                        .response()
                        .execute();
                return null;
            }
        };
        try {
            callable.call();
        } catch (Exception cause) {
            Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error updating permission ticket", cause);
        }
    }
}
