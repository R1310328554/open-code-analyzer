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

package org.keycloak.representations.idm.authorization;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.json.StringListMapDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 权限申请请求，用于向授权服务请求对指定资源与作用域的访问许可。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PermissionRequest {

    /** 目标资源 ID。 */
    private String resourceId;
    /** 请求的作用域集合。 */
    private Set<String> scopes;
    /** 资源服务器（客户端）ID。 */
    private String resourceServerId;

    /** 附加声明，键对应字符串值列表。 */
    @JsonDeserialize(using = StringListMapDeserializer.class)
    private Map<String, List<String>> claims;

    /**
     * @param resourceId 资源 ID
     * @param scopes 可变参数作用域名称
     */
    public PermissionRequest(String resourceId, String... scopes) {
        this.resourceId = resourceId;
        if (scopes != null) {
            this.scopes = new HashSet<>(Arrays.asList(scopes));
        }
    }

    /** 创建空请求。 */
    public PermissionRequest() {
        this(null, null);
    }

    /** @return 资源 ID */
    public String getResourceId() {
        return resourceId;
    }

    /** @param resourceSetId 资源 ID（JSON 字段 {@code resource_id}） */
    @JsonProperty("resource_id")
    public void setResourceId(String resourceSetId) {
        this.resourceId = resourceSetId;
    }

    /** @return 作用域集合 */
    public Set<String> getScopes() {
        return scopes;
    }

    /** @param scopes 作用域集合（JSON 字段 {@code resource_scopes}） */
    @JsonProperty("resource_scopes")
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    /** @param resourceServerId 资源服务器 ID（JSON 字段 {@code resource_server_id}） */
    @JsonProperty("resource_server_id")
    public void setResourceServerId(String resourceServerId) {
        this.resourceServerId = resourceServerId;
    }

    /** @return 资源服务器 ID */
    public String getResourceServerId() {
        return resourceServerId;
    }

    /** @return 声明映射 */
    public Map<String, List<String>> getClaims() {
        return claims;
    }

    /** @param claims 声明映射 */
    public void setClaims(Map<String, List<String>> claims) {
        this.claims = claims;
    }

    /**
     * @param name 声明名称
     * @param value 声明值（可变参数）
     */
    public void setClaim(String name, String... value) {
        if (claims == null) {
            claims = new HashMap<>();
        }

        claims.put(name, Arrays.asList(value));
    }

    /** @param name 要追加的作用域名称（可变参数） */
    public void addScope(String... name) {
        if (scopes == null) {
            scopes = new HashSet<>();
        }

        scopes.addAll(Arrays.asList(name));
    }
}
