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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.auth.authentication.model.Subject;
import org.apache.rocketmq.auth.authorization.enums.Decision;
import org.apache.rocketmq.auth.authorization.enums.PolicyType;
import org.apache.rocketmq.common.action.Action;

/**
 * 访问控制列表：绑定 {@link Subject} 与其 {@link Policy} 集合。
 */
public class Acl {

    private Subject subject;

    private List<Policy> policies;

    /** 以单条策略创建 ACL。 */
    public static Acl of(Subject subject, Policy policy) {
        return of(subject, Lists.newArrayList(policy));
    }

    /** 以策略列表创建 ACL。 */
    public static Acl of(Subject subject, List<Policy> policies) {
        Acl acl = new Acl();
        acl.setSubject(subject);
        acl.setPolicies(policies);
        return acl;
    }

    /** 从资源、动作、环境与决策快速构建单策略 ACL。 */
    public static Acl of(Subject subject, List<Resource> resources, List<Action> actions, Environment environment,
        Decision decision) {
        Acl acl = new Acl();
        acl.setSubject(subject);
        Policy policy = Policy.of(resources, actions, environment, decision);
        acl.setPolicies(Lists.newArrayList(policy));
        return acl;
    }

    /** 合并单条策略到现有 ACL。 */
    public void updatePolicy(Policy policy) {
        this.updatePolicy(Lists.newArrayList(policy));
    }

    /** 按策略类型合并多条策略；同类型则更新条目。 */
    public void updatePolicy(List<Policy> policies) {
        if (this.policies == null) {
            this.policies = new ArrayList<>();
        }
        policies.forEach(newPolicy -> {
            Policy oldPolicy = this.getPolicy(newPolicy.getPolicyType());
            if (oldPolicy == null) {
                this.policies.add(newPolicy);
            } else {
                oldPolicy.updateEntry(newPolicy.getEntries());
            }
        });
    }

    /** 删除指定类型下某资源的策略条目；策略为空时移除该策略。 */
    public void deletePolicy(PolicyType policyType, Resource resource) {
        Policy policy = getPolicy(policyType);
        if (policy == null) {
            return;
        }
        policy.deleteEntry(resource);
        if (CollectionUtils.isEmpty(policy.getEntries())) {
            this.policies.remove(policy);
        }
    }

    /** 按类型查找策略；不存在时返回 null。 */
    public Policy getPolicy(PolicyType policyType) {
        if (CollectionUtils.isEmpty(this.policies)) {
            return null;
        }
        for (Policy policy : this.policies) {
            if (policy.getPolicyType() == policyType) {
                return policy;
            }
        }
        return null;
    }

    /** 返回 ACL 所属主体。 */
    public Subject getSubject() {
        return subject;
    }

    /** 设置 ACL 所属主体。 */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    /** 返回策略列表。 */
    public List<Policy> getPolicies() {
        return policies;
    }

    /** 设置策略列表。 */
    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }
}
