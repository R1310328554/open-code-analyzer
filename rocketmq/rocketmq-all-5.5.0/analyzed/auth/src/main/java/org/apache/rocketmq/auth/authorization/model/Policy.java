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
package org.apache.rocketmq.auth.authorization.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.auth.authorization.enums.Decision;
import org.apache.rocketmq.auth.authorization.enums.PolicyType;

/**
 * ACL 策略：按 {@link PolicyType} 分组的一组 {@link PolicyEntry}。
 */
public class Policy {

    private PolicyType policyType;

    private List<PolicyEntry> entries;

    /** 以 {@link PolicyType#CUSTOM} 从资源与动作列表构建策略。 */
    public static Policy of(List<Resource> resources, List<Action> actions, Environment environment,
        Decision decision) {
        return of(PolicyType.CUSTOM, resources, actions, environment, decision);
    }

    /** 指定策略类型，为每个资源生成一条 {@link PolicyEntry}。 */
    public static Policy of(PolicyType policyType, List<Resource> resources, List<Action> actions,
        Environment environment,
        Decision decision) {
        Policy policy = new Policy();
        policy.setPolicyType(policyType);
        List<PolicyEntry> entries = resources.stream()
            .map(resource -> PolicyEntry.of(resource, actions, environment, decision))
            .collect(Collectors.toList());
        policy.setEntries(entries);
        return policy;
    }

    /** 直接以条目列表构建策略。 */
    public static Policy of(PolicyType type, List<PolicyEntry> entries) {
        Policy policy = new Policy();
        policy.setPolicyType(type);
        policy.setEntries(entries);
        return policy;
    }

    /** 合并新条目；同资源则更新动作、环境与决策。 */
    public void updateEntry(List<PolicyEntry> newEntries) {
        if (this.entries == null) {
            this.entries = new ArrayList<>();
        }
        newEntries.forEach(newEntry -> {
            PolicyEntry entry = getEntry(newEntry.getResource());
            if (entry == null) {
                this.entries.add(newEntry);
            } else {
                entry.updateEntry(newEntry.getActions(), newEntry.getEnvironment(), newEntry.getDecision());
            }
        });
    }

    /** 删除指定资源对应的策略条目。 */
    public void deleteEntry(Resource resources) {
        PolicyEntry entry = getEntry(resources);
        if (entry != null) {
            this.entries.remove(entry);
        }
    }

    private PolicyEntry getEntry(Resource resource) {
        if (CollectionUtils.isEmpty(this.entries)) {
            return null;
        }
        for (PolicyEntry entry : this.entries) {
            if (Objects.equals(entry.getResource(), resource)) {
                return entry;
            }
        }
        return null;
    }

    /** 返回策略类型。 */
    public PolicyType getPolicyType() {
        return policyType;
    }

    /** 设置策略类型。 */
    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    /** 返回策略条目列表。 */
    public List<PolicyEntry> getEntries() {
        return entries;
    }

    /** 设置策略条目列表。 */
    public void setEntries(List<PolicyEntry> entries) {
        this.entries = entries;
    }
}
