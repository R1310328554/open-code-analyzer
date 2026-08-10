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
package org.keycloak.representations.idm.authorization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 授权策略的抽象 REST 表示，封装策略 ID、类型、关联资源/Scope、决策策略及逻辑条件等公共字段。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AbstractPolicyRepresentation {

    /** 策略持久化 ID。 */
    private String id;
    /** 策略名称。 */
    private String name;
    /** 策略描述。 */
    private String description;
    /** 策略类型标识（如 role、group、aggregate 等）。 */
    private String type;
    /** 关联的子策略 ID 集合（用于聚合策略）。 */
    private Set<String> policies;
    /** 关联的资源 ID 集合。 */
    private Set<String> resources;
    /** 关联的 Scope ID 集合。 */
    private Set<String> scopes;
    /** 策略逻辑（POSITIVE/NEGATIVE）。 */
    private Logic logic = Logic.POSITIVE;
    /** 子策略组合时的决策策略。 */
    private DecisionStrategy decisionStrategy = DecisionStrategy.UNANIMOUS;
    /** 策略所有者 ID。 */
    private String owner;
    /** 资源类型过滤条件。 */
    private String resourceType;
    
    /** 关联资源的完整表示（序列化时非空才输出）。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<ResourceRepresentation> resourcesData;

    /** 关联 Scope 的完整表示（序列化时非空才输出）。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<ScopeRepresentation> scopesData;

    /** @return 策略 ID */
    public String getId() {
        return this.id;
    }

    /** @param id 策略 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 策略类型 */
    public String getType() {
        return this.type;
    }

    /** @param type 策略类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 决策策略 */
    public DecisionStrategy getDecisionStrategy() {
        return this.decisionStrategy;
    }

    /** @param decisionStrategy 决策策略 */
    public void setDecisionStrategy(DecisionStrategy decisionStrategy) {
        this.decisionStrategy = decisionStrategy;
    }

    /** @return 策略逻辑 */
    public Logic getLogic() {
        return logic;
    }

    /** @param logic 策略逻辑 */
    public void setLogic(Logic logic) {
        this.logic = logic;
    }

    /** @return 策略名称 */
    public String getName() {
        return name;
    }

    /** @param name 策略名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 策略描述 */
    public String getDescription() {
        return this.description;
    }

    /** @param description 策略描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 关联子策略 ID 集合 */
    public Set<String> getPolicies() {
        return policies;
    }

    /** @param policies 关联子策略 ID 集合 */
    public void setPolicies(Set<String> policies) {
        this.policies = policies;
    }

    /** 添加一个或多个子策略 ID。 */
    public void addPolicy(String... id) {
        if (this.policies == null) {
            this.policies = new HashSet<>();
        }
        this.policies.addAll(Arrays.asList(id));
    }

    /** 移除指定子策略 ID。 */
    public void removePolicy(String policy) {
        if (policies != null) {
            policies.remove(policy);
        }
    }

    /** @return 关联资源 ID 集合 */
    public Set<String> getResources() {
        return resources;
    }

    /** @param resources 关联资源 ID 集合 */
    public void setResources(Set<String> resources) {
        this.resources = resources;
    }

    /** 添加一个资源 ID。 */
    public void addResource(String id) {
        if (this.resources == null) {
            this.resources = new HashSet<>();
        }
        this.resources.add(id);
    }

    /** @return 关联 Scope ID 集合 */
    public Set<String> getScopes() {
        return scopes;
    }

    /** @param scopes 关联 Scope ID 集合 */
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    /** 添加一个或多个 Scope ID。 */
    public void addScope(String... id) {
        if (this.scopes == null) {
            this.scopes = new HashSet<>();
        }
        this.scopes.addAll(Arrays.asList(id));
    }

    /** 移除指定 Scope ID。 */
    public void removeScope(String scope) {
        if (scopes != null) {
            scopes.remove(scope);
        }
    }

    /** @return 策略所有者 ID */
    public String getOwner() {
        return owner;
    }

    /** @param owner 策略所有者 ID */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AbstractPolicyRepresentation policy = (AbstractPolicyRepresentation) o;
        return Objects.equals(getId(), policy.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /** @param resources 关联资源的完整表示集合 */
    public <R> void setResourcesData(Set<ResourceRepresentation> resources) {
        this.resourcesData = resources;
    }

    /** @return 关联资源的完整表示集合 */
    public Set<ResourceRepresentation> getResourcesData() {
        return resourcesData;
    }

    /** @param scopesData 关联 Scope 的完整表示集合 */
    public void setScopesData(Set<ScopeRepresentation> scopesData) {
        this.scopesData = scopesData;
    }

    /** @return 关联 Scope 的完整表示集合 */
    public Set<ScopeRepresentation> getScopesData() {
        return scopesData;
    }

    /** @param resourceType 资源类型过滤条件 */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /** @return 资源类型过滤条件 */
    public String getResourceType() {
        return resourceType;
    }

}
