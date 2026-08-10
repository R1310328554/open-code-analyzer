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

import java.util.List;
import java.util.concurrent.Callable;

import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.ServerConfiguration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.HttpMethod;
import org.keycloak.authorization.client.util.Throwables;
import org.keycloak.authorization.client.util.TokenCallable;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;

import static org.keycloak.common.util.Encode.encodePathAsIs;

/**
 * 通过 Protection API 管理 UMA 资源（resource）的入口。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ProtectedResource {

    private final Http http;
    private ServerConfiguration serverConfiguration;
    private final Configuration configuration;
    private final TokenCallable pat;

    ProtectedResource(Http http, ServerConfiguration serverConfiguration, Configuration configuration, TokenCallable pat) {
        this.http = http;
        this.serverConfiguration = serverConfiguration;
        this.configuration = configuration;
        this.pat = pat;
    }

    /**
     * 注册新资源。
     *
     * @param resource 资源数据
     * @return 创建后的 {@link ResourceRepresentation}
     */
    public ResourceRepresentation create(final ResourceRepresentation resource) {
        Callable<ResourceRepresentation> callable = new Callable<ResourceRepresentation>() {
            @Override
            public ResourceRepresentation call() throws Exception {
                return http.<ResourceRepresentation>post(serverConfiguration.getResourceRegistrationEndpoint())
                        .authorizationBearer(pat.call())
                        .json(JsonSerialization.writeValueAsBytes(resource))
                        .response().json(ResourceRepresentation.class).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Could not create resource", cause);
        }
    }

    /**
     * 更新已有资源。
     *
     * @param resource 资源数据（须含 ID）
     */
    public void update(final ResourceRepresentation resource) {
        if (resource.getId() == null) {
            throw new IllegalArgumentException("You must provide the resource id");
        }

        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                http.<ResourceRepresentation>put(serverConfiguration.getResourceRegistrationEndpoint() + "/" + encodePathAsIs(resource.getId()))
                        .authorizationBearer(pat.call())
                        .json(JsonSerialization.writeValueAsBytes(resource)).execute();
                return null;
            }
        };
        try {
            callable.call();
        } catch (Exception cause) {
            Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Could not update resource", cause);
        }
    }

    /**
     * 按 <code>id</code> 查询资源。
     *
     * @param id 资源 ID
     * @return {@link ResourceRepresentation}
     */
    public ResourceRepresentation findById(final String id) {
        Callable<ResourceRepresentation> callable = new Callable<ResourceRepresentation>() {
            @Override
            public ResourceRepresentation call() throws Exception {
                return http.<ResourceRepresentation>get(serverConfiguration.getResourceRegistrationEndpoint() + "/" + encodePathAsIs(id))
                        .authorizationBearer(pat.call())
                        .response().json(ResourceRepresentation.class).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Could not find resource", cause);
        }
    }

    /**
     * 按 <code>name</code> 查询资源，所有者为资源服务器自身。
     *
     * @param name 资源名称
     * @return {@link ResourceRepresentation}；无匹配时返回 {@code null}
     */
    public ResourceRepresentation findByName(String name) {
        List<ResourceRepresentation> representations = find(null, name, null, configuration.getResource(), null, null, false, true, true, null, null);

        if (representations.isEmpty()) {
            return null;
        }

        return representations.get(0);
    }

    /**
     * 按 <code>name</code> 与 <code>ownerId</code> 查询资源。
     *
     * @param name 资源名称
     * @param ownerId 所有者 ID
     * @return {@link ResourceRepresentation}；无匹配时返回 {@code null}
     */
    public ResourceRepresentation findByName(String name, String ownerId) {
        List<ResourceRepresentation> representations = find(null, name, null, ownerId, null, null, false, true, true, null, null);

        if (representations.isEmpty()) {
            return null;
        }

        return representations.get(0);
    }

    /**
     * 按条件查询资源，返回 ID 字符串数组。
     *
     * @param id 资源 ID
     * @param name 资源名称
     * @param uri 资源 URI
     * @param owner 资源所有者
     * @param type 资源类型
     * @param scope 资源 scope
     * @param matchingUri 为 true 时按 URI 最佳匹配查询
     * @param firstResult 分页起始位置
     * @param maxResult 最大返回条数
     * @return 资源 ID 数组
     */
    public String[] find(final String id, final String name, final String uri, final String owner, final String type, final String scope, final boolean matchingUri, final Integer firstResult, final Integer maxResult) {
        Callable<String[]> callable = new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                return (String[]) createFindRequest(id, name, uri, owner, type, scope, matchingUri, false, false, firstResult, maxResult).response().json(String[].class).execute();
            }
        };
        try {
            return callable.call();
        } catch (Exception cause) {
            return Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Could not find resource", cause);
        }
    }

    /**
     * <p>按条件查询资源；名称查询默认为部分匹配。
     * 
     * @param id 资源 ID
     * @param name 资源名称
     * @param uri 资源 URI
     * @param owner 资源所有者
     * @param type 资源类型
     * @param scope 资源 scope
     * @param matchingUri 为 true 时按 URI 最佳匹配查询
     * @param deep 为 true 时返回完整 {@link ResourceRepresentation} 列表，否则仅 ID
     * @param firstResult 分页起始位置
     * @param maxResult 最大返回条数
     * @return 资源表示列表或 ID 数组（取决于泛型与 {@code deep}）
     */
    public <R> R find(final String id, final String name, final String uri, final String owner, final String type, final String scope, final boolean matchingUri, final boolean deep, final Integer firstResult, final Integer maxResult) {
        return find(id, name, uri, owner, type, scope, matchingUri, false, deep, firstResult, maxResult);
    }
    
    /**
     * 按条件查询资源，可指定名称精确匹配与深度结果。
     *
     * @param id 资源 ID
     * @param name 资源名称
     * @param uri 资源 URI
     * @param owner 资源所有者
     * @param type 资源类型
     * @param scope 资源 scope
     * @param matchingUri 为 true 时按 URI 最佳匹配查询
     * @param exactName {@code name} 是否精确匹配
     * @param deep 为 true 时返回完整 {@link ResourceRepresentation} 列表，否则仅 ID
     * @param firstResult 分页起始位置
     * @param maxResult 最大返回条数
     * @return 资源表示列表或 ID 数组（取决于泛型与 {@code deep}）
     */
    public <R> R find(final String id, final String name, final String uri, final String owner, final String type, final String scope, final boolean matchingUri, final boolean exactName, final boolean deep, final Integer firstResult, final Integer maxResult) {
        if (deep) {
            Callable<List<ResourceRepresentation>> callable = new Callable<List<ResourceRepresentation>>() {
                @Override
                public List<ResourceRepresentation> call() {
                    return (List<ResourceRepresentation>) createFindRequest(id, name, uri, owner, type, scope, matchingUri, exactName, deep, firstResult, maxResult).response().json(new TypeReference<List<ResourceRepresentation>>() {
                    }).execute();
                }
            };
            try {
                return (R) callable.call();
            } catch (Exception cause) {
                return (R) Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "Could not find resource", cause);
            }
        }

        return (R) find(id, name, uri, owner, type, scope, matchingUri, firstResult, maxResult);
    }

    /**
     * 查询服务器上全部资源的 ID。
     *
     * @return 资源 ID 字符串数组
     */
    public String[] findAll() {
        try {
            return find(null,null , null, null, null, null, false, null, null);
        } catch (Exception cause) {
            throw Throwables.handleWrapException("Could not find resource", cause);
        }
    }

    /**
     * 删除指定 <code>id</code> 的资源。
     *
     * @param id 资源 ID
     */
    public void delete(final String id) {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                http.delete(serverConfiguration.getResourceRegistrationEndpoint() + "/" + encodePathAsIs(id))
                        .authorizationBearer(pat.call())
                        .execute();
                return null;
            }
        };
        try {
            callable.call();
        } catch (Exception cause) {
            Throwables.retryAndWrapExceptionIfNecessary(callable, pat, "", cause);
        }
    }

    /**
     * 查询与给定 URI 完全匹配的全部资源。
     *
     * @param uri 资源 URI
     */
    public List<ResourceRepresentation> findByUri(String uri) {
        return find(null, null, uri, null, null, null, false, false, true, null, null);
    }

    /**
     * 返回与给定 {@code uri} 最佳匹配的资源列表。
     * 查询条件基于 {@link ResourceRepresentation#uri} 与请求 URI 的匹配度。
     *
     * @param uri 待匹配的 URI
     * @return 匹配的资源列表
     */
    public List<ResourceRepresentation> findByMatchingUri(String uri) {
        return find(null, null, uri, null, null, null, true, false, true,null, null);
    }

    /** 构造带查询参数的 GET 请求（内部复用）。 */
    private HttpMethod createFindRequest(String id, String name, String uri, String owner, String type, String scope, boolean matchingUri, boolean exactName, boolean deep, Integer firstResult, Integer maxResult) {
        return http.get(serverConfiguration.getResourceRegistrationEndpoint())
                .authorizationBearer(pat.call())
                .param("_id", id)
                .param("name", name)
                .param("uri", uri)
                .param("owner", owner)
                .param("type", type)
                .param("scope", scope)
                .param("matchingUri", Boolean.valueOf(matchingUri).toString())
                .param("exactName", Boolean.valueOf(exactName).toString())
                .param("deep", Boolean.toString(deep))
                .param("first", firstResult != null ? firstResult.toString() : null)
                .param("max", maxResult != null ? maxResult.toString() : Integer.toString(-1));
    }
}
