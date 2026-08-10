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

package org.keycloak.authorization.model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;

/**
 * 授权策略模型，包含类型、决策策略、逻辑、配置及关联资源/范围。
 *
 * Represents an authorization policy and all the configuration associated with it.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface Policy {

    /** 策略查询过滤选项。 */
    enum FilterOption {
        ID("id"),
        PERMISSION("permission"),
        OWNER("owner"),
        ANY_OWNER("owner.any"),
        RESOURCE_ID("resources.id"),
        SCOPE_ID("scopes.id"),
        CONFIG("config"),
        TYPE("type"),
        NAME("name");

        public static final String[] EMPTY_FILTER = new String[0];
        private final String name;

        FilterOption(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    
    /** 策略配置项分隔符。 */
    public static final String CONFIG_SEPARATOR = "##";

    /**
     * 返回本实例的唯一标识。
     *
     * Returns the unique identifier for this instance.
     *
     * @return the unique identifier for this instance
     */
    String getId();

    /**
     * 返回策略类型。
     *
     * Returns the type of this policy.
     *
     * @return the type of this policy
     */
    String getType();

    /**
     * 返回本策略的 {@link DecisionStrategy}。
     *
     * Returns the {@link DecisionStrategy} for this policy.
     *
     * @return the decision strategy defined for this policy
     */
    DecisionStrategy getDecisionStrategy();

    /**
     * 设置本策略的决策策略。
     *
     * Sets the {DecisionStrategy} for this policy.
     *
     * @param decisionStrategy for this policy
     */
    void setDecisionStrategy(DecisionStrategy decisionStrategy);

    /**
     * 返回本策略的 {@link Logic}。
     *
     * Returns the {@link Logic} for this policy.
     *
     * @return the decision strategy defined for this policy
     */
    Logic getLogic();

    /**
     * 设置本策略的逻辑运算方式。
     *
     * Sets the {Logic} for this policy.
     *
     * @param logic for this policy
     */
    void setLogic(Logic logic);

    /**
     * 返回策略附加配置的键值映射。
     *
     * Returns a {@link Map} holding string-based key/value pairs representing any additional configuration for this policy.
     *
     * @return a unmodifiable map with any additional configuration defined for this policy.
     */
    Map<String, String> getConfig();


    /**
     * 设置策略附加配置。
     *
     * Sets a {@link Map} with string-based key/value pairs representing any additional configuration for this policy.
     *
     * @param config a map with any additional configuration for this policy.
     */
    void setConfig(Map<String, String> config);

    void removeConfig(String name);
    void putConfig(String name, String value);

    /**
     * 返回策略名称。
     *
     * Returns the name of this policy.
     *
     * @return the name of this policy
     */
    String getName();

    /**
     * 设置策略唯一名称。
     *
     * Sets an unique name to this policy.
     *
     * @param name an unique name
     */
    void setName(String name);

    /**
     * 返回策略描述。
     *
     * Returns the description of this policy.
     *
     * @return a description or null of there is no description
     */
    String getDescription();

    /**
     * 设置策略描述。
     *
     * Sets the description for this policy.
     *
     * @param description a description
     */
    void setDescription(String description);

    /**
     * 返回所属 {@link ResourceServer}。
     *
     * Returns the {@link ResourceServer} where this policy belongs to.
     *
     * @return a resource server
     */
     ResourceServer getResourceServer();

    /**
     * 返回关联子策略，用于本策略生效时的授权决策评估。
     *
     * Returns the {@link Policy} instances associated with this policy and used to evaluate authorization decisions when
     * this policy applies.
     *
     * @return the associated policies or an empty set if no policy is associated with this policy
     */
    Set<Policy> getAssociatedPolicies();

    /**
     * 返回本策略适用的 {@link Resource} 集合。
     *
     * Returns the {@link Resource} instances where this policy applies.
     *
     * @return a set with all resource instances where this policy applies. Or an empty set if there is no resource associated with this policy
     */
    Set<Resource> getResources();

    /**
     * 返回本策略适用的资源名称集合。
     *
     * Returns the name of the {@link Resource} instances where this policy applies.
     *
     * @return a set with all names of resource instances where this policy applies. Or an empty set if there is no resource associated with this policy
     */
    default Set<String> getResourceNames() {
        return getResources().stream().map(Resource::getName).collect(Collectors.toSet());
    }

    /**
     * 返回本策略适用的 {@link Scope} 集合。
     *
     * Returns the {@link Scope} instances where this policy applies.
     *
     * @return a set with all scope instances where this policy applies. Or an empty set if there is no scope associated with this policy
     */
    Set<Scope> getScopes();

    String getOwner();

    void setOwner(String owner);

    void addScope(Scope scope);

    void removeScope(Scope scope);

    void addAssociatedPolicy(Policy associatedPolicy);

    void removeAssociatedPolicy(Policy associatedPolicy);

    void addResource(Resource resource);

    void removeResource(Resource resource);

    default String getResourceType() {
        return Optional.ofNullable(getConfig()).orElse(Map.of()).get("defaultResourceType");
    }

    default void setResourceType(String resourceType) {
        Map<String, String> config = getConfig();

        if (config != null) {
            putConfig("defaultResourceType", resourceType);
        }
    }
}
