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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.representations.AccessToken;

/**
 * 策略评估响应，返回各资源的评估结果、整体决策及可选的 RPT 令牌。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyEvaluationResponse {

    /** 各资源的评估结果列表。 */
    private List<EvaluationResultRepresentation> results;
    /** 是否为 Entitlements 响应。 */
    private boolean entitlements;
    /** 整体决策效果（PERMIT / DENY）。 */
    private DecisionEffect status;
    /** 签发的请求方令牌（RPT）。 */
    private AccessToken rpt;

    /** @return 评估结果列表 */
    public List<EvaluationResultRepresentation> getResults() {
        return results;
    }

    /** @return 整体决策效果 */
    public DecisionEffect getStatus() {
        return status;
    }

    /** @return 是否为 Entitlements 响应 */
    public boolean isEntitlements() {
        return entitlements;
    }

    /** @return RPT 访问令牌 */
    public AccessToken getRpt() {
        return rpt;
    }

    /** @param results 评估结果列表 */
    public void setResults(List<EvaluationResultRepresentation> results) {
        this.results = results;
    }

    /** @param entitlements 是否为 Entitlements 响应 */
    public void setEntitlements(boolean entitlements) {
        this.entitlements = entitlements;
    }

    /** @param status 整体决策效果 */
    public void setStatus(DecisionEffect status) {
        this.status = status;
    }

    /** @param rpt RPT 访问令牌 */
    public void setRpt(AccessToken rpt) {
        this.rpt = rpt;
    }

    /**
     * 单个资源的策略评估结果。
     */
    public static class EvaluationResultRepresentation {

        /** 被评估的资源。 */
        private ResourceRepresentation resource;
        /** 涉及的作用域列表。 */
        private List<ScopeRepresentation> scopes;
        /** 参与评估的策略结果集合。 */
        private Set<PolicyResultRepresentation> policies;
        /** 该资源的最终决策效果。 */
        private DecisionEffect status;
        /** 已允许的作用域集合。 */
        private Set<ScopeRepresentation> allowedScopes = new HashSet<>();
        /** 已拒绝的作用域集合。 */
        private Set<ScopeRepresentation> deniedScopes = new HashSet<>();

        /** @param resource 被评估资源 */
        public void setResource(final ResourceRepresentation resource) {
            this.resource = resource;
        }

        /** @return 被评估资源 */
        public ResourceRepresentation getResource() {
            return resource;
        }

        /** @param scopes 作用域列表 */
        public void setScopes(List<ScopeRepresentation> scopes) {
            this.scopes = scopes;
        }

        /** @return 作用域列表 */
        public List<ScopeRepresentation> getScopes() {
            return scopes;
        }

        /** @param policies 策略结果集合 */
        public void setPolicies(final Set<PolicyResultRepresentation> policies) {
            this.policies = policies;
        }

        /** @return 策略结果集合 */
        public Set<PolicyResultRepresentation> getPolicies() {
            return policies;
        }

        /** @param status 决策效果 */
        public void setStatus(final DecisionEffect status) {
            this.status = status;
        }

        /** @return 决策效果 */
        public DecisionEffect getStatus() {
            return status;
        }

        /** @param allowedScopes 已允许的作用域 */
        public void setAllowedScopes(Set<ScopeRepresentation> allowedScopes) {
            this.allowedScopes = allowedScopes;
        }

        /** @return 已允许的作用域 */
        public Set<ScopeRepresentation> getAllowedScopes() {
            return allowedScopes;
        }

        /** @param deniedScopes 已拒绝的作用域 */
        public void setDeniedScopes(Set<ScopeRepresentation> deniedScopes) {
            this.deniedScopes = deniedScopes;
        }

        /** @return 已拒绝的作用域 */
        public Set<ScopeRepresentation> getDeniedScopes() {
            return deniedScopes;
        }
    }

    /**
     * 单条策略的评估结果，可嵌套关联子策略。
     */
    public static class PolicyResultRepresentation {

        /** 被评估的策略定义。 */
        private PolicyRepresentation policy;
        /** 该策略的决策效果。 */
        private DecisionEffect status;
        /** 关联子策略的评估结果列表。 */
        private List<PolicyResultRepresentation> associatedPolicies;
        /** 策略覆盖的作用域名称集合。 */
        private Set<String> scopes = new HashSet<>();
        /** 适用的资源类型。 */
        private String resourceType;

        /** @return 策略定义 */
        public PolicyRepresentation getPolicy() {
            return policy;
        }

        /** @param policy 策略定义 */
        public void setPolicy(final PolicyRepresentation policy) {
            this.policy = policy;
        }

        /** @return 决策效果 */
        public DecisionEffect getStatus() {
            return status;
        }

        /** @param status 决策效果 */
        public void setStatus(final DecisionEffect status) {
            this.status = status;
        }

        /** @return 关联子策略结果列表 */
        public List<PolicyResultRepresentation> getAssociatedPolicies() {
            return associatedPolicies;
        }

        /** @param associatedPolicies 关联子策略结果列表 */
        public void setAssociatedPolicies(final List<PolicyResultRepresentation> associatedPolicies) {
            this.associatedPolicies = associatedPolicies;
        }

        /** 基于策略名称计算哈希。 */
        @Override
        public int hashCode() {
            return this.policy.getName().hashCode();
        }

        /** 基于策略名称比较相等性。 */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PolicyResultRepresentation policy = (PolicyResultRepresentation) o;
            return this.policy.getName().equals(policy.getPolicy().getName());
        }

        /** @param scopes 作用域名称集合 */
        public void setScopes(Set<String> scopes) {
            this.scopes = scopes;
        }

        /** @return 作用域名称集合 */
        public Set<String> getScopes() {
            return scopes;
        }

        /** @param resourceType 资源类型 */
        public void setResourceType(String resourceType) {
            this.resourceType = resourceType;
        }

        /** @return 资源类型 */
        public String getResourceType() {
            return resourceType;
        }
    }
}
