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

package org.keycloak.representations.idm.authorization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 策略评估请求，携带用户、角色、资源及上下文信息供授权引擎模拟决策。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyEvaluationRequest {

    /** 评估上下文属性（分类 → 键值对）。 */
    private Map<String, Map<String, String>> context = new HashMap<>();
    /** 待评估的资源列表。 */
    private List<ResourceRepresentation> resources = new LinkedList<>();
    /** 资源类型过滤条件。 */
    private String resourceType;
    /** 客户端 ID。 */
    private String clientId;
    /** 用户 ID。 */
    private String userId;
    /** 用户所属角色 ID 列表。 */
    private List<String> roleIds = new LinkedList<>();
    /** 是否请求完整权限清单（Entitlements）而非单次决策。 */
    private boolean entitlements;

    /** @return 评估上下文 */
    public Map<String, Map<String, String>> getContext() {
        return this.context;
    }

    /** @param context 评估上下文 */
    public void setContext(Map<String, Map<String, String>> context) {
        this.context = context;
    }

    /** @return 待评估资源列表 */
    public List<ResourceRepresentation> getResources() {
        return this.resources;
    }

    /** @param resources 待评估资源列表 */
    public void setResources(List<ResourceRepresentation> resources) {
        this.resources = resources;
    }

    /** @return 资源类型 */
    public String getResourceType() {
        return resourceType;
    }

    /** @param resourceType 资源类型 */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /** @return 客户端 ID */
    public String getClientId() {
        return this.clientId;
    }

    /** @param clientId 客户端 ID */
    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    /** @return 用户 ID */
    public String getUserId() {
        return this.userId;
    }

    /** @param userId 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return 角色 ID 列表 */
    public List<String> getRoleIds() {
        return this.roleIds;
    }

    /** @param roleIds 角色 ID 列表 */
    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    /** @return 是否请求 Entitlements */
    public boolean isEntitlements() {
        return entitlements;
    }

    /** @param entitlements 是否请求 Entitlements */
    public void setEntitlements(boolean entitlements) {
        this.entitlements = entitlements;
    }

    /**
     * 追加一个待评估资源（流式构建）。
     *
     * @param name 资源名称
     * @param scopes 作用域名称（可变参数）
     * @return 当前请求实例，支持链式调用
     */
    public PolicyEvaluationRequest addResource(String name, String... scopes) {
        if (resources == null) {
            resources = new LinkedList<>();
        }
        resources.add(new ResourceRepresentation(name, scopes));
        return this;
    }


}
