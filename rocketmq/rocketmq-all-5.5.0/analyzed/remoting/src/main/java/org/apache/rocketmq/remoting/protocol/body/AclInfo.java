/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.remoting.protocol.body;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ACL 访问控制信息：主体（Subject）及其策略列表，用于 Plain ACL 管理接口。
 */
public class AclInfo {

    /** ACL 主体（用户名或角色）。 */
    private String subject;

    /** 绑定的策略集合。 */
    private List<PolicyInfo> policies;

    /** 便捷构造：单策略、多资源条目、统一决策（Allow/Deny）。 */
    public static AclInfo of(String subject, List<String> resources, List<String> actions,
        List<String> sourceIps,
        String decision) {
        AclInfo aclInfo = new AclInfo();
        aclInfo.setSubject(subject);
        PolicyInfo policyInfo = PolicyInfo.of(resources, actions, sourceIps, decision);
        aclInfo.setPolicies(Collections.singletonList(policyInfo));
        return aclInfo;
    }

    /** 单条 ACL 策略：类型 + 多条资源条目。 */
    public static class PolicyInfo {

        /** 策略类型标识。 */
        private String policyType;

        /** 资源级策略条目列表。 */
        private List<PolicyEntryInfo> entries;

        /** 按资源列表展开为多条 {@link PolicyEntryInfo}。 */
        public static PolicyInfo of(List<String> resources, List<String> actions,
            List<String> sourceIps, String decision) {
            PolicyInfo policyInfo = new PolicyInfo();
            List<PolicyEntryInfo> entries = resources.stream()
                .map(resource -> PolicyEntryInfo.of(resource, actions, sourceIps, decision))
                .collect(Collectors.toList());
            policyInfo.setEntries(entries);
            return policyInfo;
        }

        public String getPolicyType() {
            return policyType;
        }

        public void setPolicyType(String policyType) {
            this.policyType = policyType;
        }

        public List<PolicyEntryInfo> getEntries() {
            return entries;
        }

        public void setEntries(List<PolicyEntryInfo> entries) {
            this.entries = entries;
        }
    }

    /** 单资源 ACL 条目：资源、操作、来源 IP 与 Allow/Deny 决策。 */
    public static class PolicyEntryInfo {
        /** 资源名（Topic/Group/Cluster 等）。 */
        private String resource;

        /** 允许的操作列表（Pub/Sub 等）。 */
        private List<String> actions;

        /** 来源 IP 白名单。 */
        private List<String> sourceIps;

        /** 决策：Allow 或 Deny。 */
        private String decision;

        /** 构造单条策略条目。 */
        public static PolicyEntryInfo of(String resource, List<String> actions, List<String> sourceIps,
            String decision) {
            PolicyEntryInfo policyEntryInfo = new PolicyEntryInfo();
            policyEntryInfo.setResource(resource);
            policyEntryInfo.setActions(actions);
            policyEntryInfo.setSourceIps(sourceIps);
            policyEntryInfo.setDecision(decision);
            return policyEntryInfo;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public List<String> getActions() {
            return actions;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }

        public List<String> getSourceIps() {
            return sourceIps;
        }

        public void setSourceIps(List<String> sourceIps) {
            this.sourceIps = sourceIps;
        }

        public String getDecision() {
            return decision;
        }

        public void setDecision(String decision) {
            this.decision = decision;
        }
    }

    /** 返回 ACL 主体。 */
    public String getSubject() {
        return subject;
    }

    /** 设置 ACL 主体。 */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /** 返回策略列表。 */
    public List<PolicyInfo> getPolicies() {
        return policies;
    }

    /** 设置策略列表。 */
    public void setPolicies(List<PolicyInfo> policies) {
        this.policies = policies;
    }
}
