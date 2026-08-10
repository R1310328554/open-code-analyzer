/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.client.resource;

import java.util.List;
import java.util.concurrent.Callable;

import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.Throwables;
import org.keycloak.authorization.client.util.TokenCallable;
import org.keycloak.representations.idm.authorization.UmaPermissionRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.keycloak.common.util.Encode.encodePathAsIs;

/**
 * 针对特定资源的用户托管权限（user-managed permission）管理入口。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyResource {

    private String resourceId;
    private final Http http;
    private final ServerConfiguration serverConfiguration;
    private final TokenCallable pat;

    public PolicyResource(String resourceId, Http http, ServerConfiguration serverConfiguration, TokenCallable pat) {
        this.resourceId = resourceId;
        this.http = http;
        this.serverConfiguration = serverConfiguration;
        this.pat = pat;
    }

    /**
     * 创建 {@code permission} 所表示的用户托管权限。
     *
     * @param permission 待创建的权限表示
     * @return 成功时返回已创建的权限
     */
    public UmaPermissionRepresentation create(final UmaPermissionRepresentation permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }

        Callable<UmaPermissionRepresentation> callable = new Callable<UmaPermissionRepresentation>() {
            @Override
            public UmaPermissionRepresentation call() throws Exception {
                return http.<UmaPermissionRepresentation>post(serverConfiguration.getPolicyEndpoint() + "/" + encodePathAsIs(resourceId))
                        .authorizationBearer(pat.call())
                        .json(JsonSerialization.writeValueAsBytes(permission))
                        .response().json(UmaPermissionRepresentation.class).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error creating policy for resource [" + resourceId + "]", cause);
        }
    }

    /**
     * 更新已有用户托管权限。
     *
     * @param permission 待更新的权限
     */
    public void update(final UmaPermissionRepresentation permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }

        if (permission.getId() == null) {
            throw new IllegalArgumentException("Permission id must not be null");
        }

        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                http.<Void>put(serverConfiguration.getPolicyEndpoint() + "/"+ encodePathAsIs(permission.getId()))
                        .authorizationBearer(pat.call())
                        .json(JsonSerialization.writeValueAsBytes(permission)).execute();
                return null;
            }
        };
        try {
            callable.call();
        } catch (Exception cause) {
            Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error updating policy for resource [" + resourceId + "]", cause);
        }
    }

    /**
     * 删除指定 ID 的用户托管权限。
     *
     * @param id 权限 ID
     */
    public void delete(final String id) {
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() {
                http.<UmaPermissionRepresentation>delete(serverConfiguration.getPolicyEndpoint() + "/" + encodePathAsIs(id))
                        .authorizationBearer(pat.call())
                        .response().execute();
                return null;
            }
        };
        try {
            callable.call();
        } catch (Exception cause) {
            Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error updating policy for resource [" + resourceId + "]", cause);
        }
    }

    /**
     * 按条件查询匹配的用户托管权限。
     *
     * @param id 权限 ID
     * @param name 权限名称
     * @param scope 关联 scope
     * @param firstResult 分页起始位置
     * @param maxResult 最大返回条数
     * @return 满足条件的权限列表
     */
    public List<UmaPermissionRepresentation> find(final String name,
                                                  final String scope,
                                                  final Integer firstResult,
                                                  final Integer maxResult) {
        Callable<List<UmaPermissionRepresentation>> callable = new Callable<List<UmaPermissionRepresentation>>() {
            @Override
            public List<UmaPermissionRepresentation> call() {
                return http.<List<UmaPermissionRepresentation>>get(serverConfiguration.getPolicyEndpoint())
                        .authorizationBearer(pat.call())
                        .param("name", name)
                        .param("resource", resourceId)
                        .param("scope", scope)
                        .param("first", firstResult == null ? null : firstResult.toString())
                        .param("max", maxResult == null ? null : maxResult.toString())
                        .response().json(new TypeReference<List<UmaPermissionRepresentation>>(){}).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error querying policies for resource [" + resourceId + "]", cause);
        }
    }

    /**
     * 按 {@code id} 查询单条用户托管权限。
     *
     * @param id 权限 ID
     * @return 对应权限；不存在时由 HTTP 层处理
     */
    public UmaPermissionRepresentation findById(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Permission id must not be null");
        }

        Callable<UmaPermissionRepresentation> callable = new Callable<UmaPermissionRepresentation>() {
            @Override
            public UmaPermissionRepresentation call() {
                return http.<UmaPermissionRepresentation>get(serverConfiguration.getPolicyEndpoint() + "/" + encodePathAsIs(id))
                        .authorizationBearer(pat.call())
                        .response().json(UmaPermissionRepresentation.class).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Error creating policy for resource [" + resourceId + "]", cause);
        }
    }
}
