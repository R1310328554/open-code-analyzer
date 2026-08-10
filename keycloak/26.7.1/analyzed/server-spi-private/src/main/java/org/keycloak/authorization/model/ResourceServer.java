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

import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.PolicyEnforcementMode;

/**
 * 资源服务器模型：其资源受授权策略管理与保护。
 * <p>通常对应 Keycloak 中同时充当资源服务器的客户端应用。</p>
 *
 * Represents a resource server, whose resources are managed and protected. A resource server is basically an existing
 * client application in Keycloak that will also act as a resource server.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ResourceServer {

    /**
     * 返回本实例的唯一标识。
     *
     * Returns the unique identifier for this instance.
     *
     * @return the unique identifier for this instance
     */
    String getId();

    /**
     * 是否允许通过 Protection API 远程管理资源。
     *
     * Indicates if the resource server is allowed to manage its own resources remotely using the Protection API.
     *
     * @return {@code true} if the resource server is allowed to managed them remotely
     */
    boolean isAllowRemoteResourceManagement();

    /**
     * Indicates if the resource server is allowed to manage its own resources remotely using the Protection API.
     *
     * @param allowRemoteResourceManagement {@code true} if the resource server is allowed to managed them remotely
     */
    void setAllowRemoteResourceManagement(boolean allowRemoteResourceManagement);

    /**
     * 返回已配置的策略强制模式。
     *
     * Returns the {@code PolicyEnforcementMode} configured for this instance.
     *
     * @return the {@code PolicyEnforcementMode} configured for this instance.
     */
    PolicyEnforcementMode getPolicyEnforcementMode();

    /**
     * 设置策略强制模式。
     *
     * Defines a {@code PolicyEnforcementMode} for this instance.
     *
     * @param enforcementMode one of the available options in {@code PolicyEnforcementMode}
     */
    void setPolicyEnforcementMode(PolicyEnforcementMode enforcementMode);

    /**
     * 设置决策策略，决定权限如何根据策略结果授予。
     *
     * Defines a {@link DecisionStrategy} for this instance, indicating how permissions should be granted depending on the given
     * {@code decisionStrategy}.
     * 
     * @param decisionStrategy the decision strategy
     */
    void setDecisionStrategy(DecisionStrategy decisionStrategy);

    /**
     * 返回已配置的 {@link DecisionStrategy}。
     *
     * Returns the {@link DecisionStrategy} configured for this instance.
     * 
     * @return the decision strategy
     */
    DecisionStrategy getDecisionStrategy();

    /**
     * 返回关联客户端 ID。
     *
     * Returns id of a client that this {@link ResourceServer} is associated with
     * @return id of client
     */
    String getClientId();
}
