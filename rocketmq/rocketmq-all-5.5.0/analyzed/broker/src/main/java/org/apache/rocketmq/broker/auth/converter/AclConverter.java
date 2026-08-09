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
package org.apache.rocketmq.broker.auth.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.auth.authentication.model.Subject;
import org.apache.rocketmq.auth.authorization.enums.Decision;
import org.apache.rocketmq.auth.authorization.enums.PolicyType;
import org.apache.rocketmq.auth.authorization.model.Acl;
import org.apache.rocketmq.auth.authorization.model.Environment;
import org.apache.rocketmq.auth.authorization.model.Policy;
import org.apache.rocketmq.auth.authorization.model.PolicyEntry;
import org.apache.rocketmq.auth.authorization.model.Resource;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.remoting.protocol.body.AclInfo;

/**
 * ACL 模型转换器：在 Remoting 协议体 {@link AclInfo} 与授权域模型 {@link Acl} 之间双向转换。
 */
public class AclConverter {

    /** 将 {@link AclInfo} 转为内存 {@link Acl}；入参为 null 时返回 null。 */
    public static Acl convertAcl(AclInfo aclInfo) {
        if (aclInfo == null) {
            return null;
        }
        Subject subject = Subject.of(aclInfo.getSubject());
        List<Policy> policies = new ArrayList<>();
        for (AclInfo.PolicyInfo policy : aclInfo.getPolicies()) {
            PolicyType policyType = PolicyType.getByName(policy.getPolicyType());

            List<AclInfo.PolicyEntryInfo> entryInfos = policy.getEntries();
            if (CollectionUtils.isEmpty(entryInfos)) {
                continue;
            }
            List<PolicyEntry> entries = new ArrayList<>();
            for (AclInfo.PolicyEntryInfo entryInfo : entryInfos) {
                Resource resource = Resource.of(entryInfo.getResource());

                List<Action> actions = new ArrayList<>();
                for (String a : entryInfo.getActions()) {
                    Action action = Action.getByName(a);
                    if (action == null) {
                        continue;
                    }
                    actions.add(action);
                }

                Environment environment = new Environment();
                if (CollectionUtils.isNotEmpty(entryInfo.getSourceIps())) {
                    environment.setSourceIps(entryInfo.getSourceIps());
                }

                Decision decision = Decision.getByName(entryInfo.getDecision());

                entries.add(PolicyEntry.of(resource, actions, environment, decision));
            }

            policies.add(Policy.of(policyType, entries));
        }

        return Acl.of(subject, policies);
    }

    /** 批量将 {@link Acl} 列表转为 {@link AclInfo} 列表。 */
    public static List<AclInfo> convertAcls(List<Acl> acls) {
        if (CollectionUtils.isEmpty(acls)) {
            return null;
        }
        return acls.stream().map(AclConverter::convertAcl)
            .collect(Collectors.toList());
    }

    /** 将 {@link Acl} 转为 {@link AclInfo}；入参为 null 时返回 null。 */
    public static AclInfo convertAcl(Acl acl) {
        if (acl == null) {
            return null;
        }
        AclInfo aclInfo = new AclInfo();
        aclInfo.setSubject(acl.getSubject().getSubjectKey());
        if (CollectionUtils.isEmpty(acl.getPolicies())) {
            return aclInfo;
        }
        List<AclInfo.PolicyInfo> policyInfos = acl.getPolicies().stream()
            .map(AclConverter::convertPolicy)
            .collect(Collectors.toList());
        aclInfo.setPolicies(policyInfos);
        return aclInfo;
    }

    /** 将 {@link Policy} 转为 {@link AclInfo.PolicyInfo}。 */
    private static AclInfo.PolicyInfo convertPolicy(Policy policy) {
        AclInfo.PolicyInfo policyInfo = new AclInfo.PolicyInfo();
        if (policy.getPolicyType() != null) {
            policyInfo.setPolicyType(policy.getPolicyType().getName());
        }
        if (CollectionUtils.isEmpty(policy.getEntries())) {
            return policyInfo;
        }
        List<AclInfo.PolicyEntryInfo> entryInfos = policy.getEntries().stream()
            .map(AclConverter::convertPolicyEntry).collect(Collectors.toList());
        policyInfo.setEntries(entryInfos);
        return policyInfo;
    }

    /** 将 {@link PolicyEntry} 转为 {@link AclInfo.PolicyEntryInfo}。 */
    private static AclInfo.PolicyEntryInfo convertPolicyEntry(PolicyEntry entry) {
        AclInfo.PolicyEntryInfo entryInfo = new AclInfo.PolicyEntryInfo();
        entryInfo.setResource(entry.toResourceStr());
        entryInfo.setActions(entry.toActionsStr());
        if (entry.getEnvironment() != null) {
            entryInfo.setSourceIps(entry.getEnvironment().getSourceIps());
        }
        entryInfo.setDecision(entry.getDecision().getName());
        return entryInfo;
    }
}
