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

package org.keycloak.models.cache.infinispan.authorization.entities;

import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.models.cache.infinispan.entities.AbstractRevisioned;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.PolicyEnforcementMode;

/**
 * 资源服务器（ResourceServer）的 Infinispan 缓存快照实体。
 * <p>
 * 缓存远程资源管理开关、策略执行模式与决策策略等配置字段。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class CachedResourceServer extends AbstractRevisioned {

    /** 是否允许远程资源管理。 */
    private final boolean allowRemoteResourceManagement;
    /** 策略执行模式（ENFORCING/PERMISSIVE 等）。 */
    private final PolicyEnforcementMode policyEnforcementMode;
    /** 决策策略（一致/多数/否定等）。 */
    private final DecisionStrategy decisionStrategy;

    /** 从 ResourceServer 模型构造缓存快照。 */
    public CachedResourceServer(long revision, ResourceServer resourceServer) {
        super(revision, resourceServer.getId());
        this.allowRemoteResourceManagement = resourceServer.isAllowRemoteResourceManagement();
        this.policyEnforcementMode = resourceServer.getPolicyEnforcementMode();
        this.decisionStrategy = resourceServer.getDecisionStrategy();
    }

    public boolean isAllowRemoteResourceManagement() {
        return this.allowRemoteResourceManagement;
    }

    public PolicyEnforcementMode getPolicyEnforcementMode() {
        return this.policyEnforcementMode;
    }

    public DecisionStrategy getDecisionStrategy() {
        return decisionStrategy;
    }
}
