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

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.auth.authorization.enums.Decision;

/**
 * 单条策略条目：描述对某 {@link Resource} 在特定 {@link Environment} 下
 * 执行 {@link Action} 的 {@link Decision}。
 */
public class PolicyEntry {

    private Resource resource;

    private List<Action> actions;

    private Environment environment;

    private Decision decision;

    /** 构建包含资源、动作、环境与决策的策略条目。 */
    public static PolicyEntry of(Resource resource, List<Action> actions, Environment environment, Decision decision) {
        PolicyEntry policyEntry = new PolicyEntry();
        policyEntry.setResource(resource);
        policyEntry.setActions(actions);
        policyEntry.setEnvironment(environment);
        policyEntry.setDecision(decision);
        return policyEntry;
    }

    /** 更新动作、环境与决策字段。 */
    public void updateEntry(List<Action> actions, Environment environment,
        Decision decision) {
        this.setActions(actions);
        this.setEnvironment(environment);
        this.setDecision(decision);
    }

    /** 判断请求资源是否匹配本条目资源模式。 */
    public boolean isMatchResource(Resource resource) {
        return this.resource.isMatch(resource);
    }

    /** 判断请求动作是否被本条目允许；请求含 {@link Action#ANY} 时视为匹配。 */
    public boolean isMatchAction(List<Action> actions) {
        if (CollectionUtils.isEmpty(this.actions)) {
            return false;
        }
        if (actions.contains(Action.ANY)) {
            return true;
        }
        return actions.stream()
            .anyMatch(action -> this.actions.contains(action)
                || this.actions.contains(Action.ALL));
    }

    /** 判断请求环境是否满足本条目约束；未配置环境时视为匹配。 */
    public boolean isMatchEnvironment(Environment environment) {
        if (this.environment == null) {
            return true;
        }
        return this.environment.isMatch(environment);
    }

    /** 返回资源键字符串；资源为 null 时返回 null。 */
    public String toResourceStr() {
        if (resource == null) {
            return null;
        }
        return resource.getResourceKey();
    }

    /** 将动作列表转为名称字符串列表。 */
    public List<String> toActionsStr() {
        if (CollectionUtils.isEmpty(actions)) {
            return null;
        }
        return actions.stream().map(Action::getName)
            .collect(Collectors.toList());
    }

    /** 返回策略条目绑定的资源。 */
    public Resource getResource() {
        return resource;
    }

    /** 设置策略条目绑定的资源。 */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /** 返回允许的动作列表。 */
    public List<Action> getActions() {
        return actions;
    }

    /** 设置允许的动作列表。 */
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    /** 返回环境约束。 */
    public Environment getEnvironment() {
        return environment;
    }

    /** 设置环境约束。 */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /** 返回授权决策（允许或拒绝）。 */
    public Decision getDecision() {
        return decision;
    }

    /** 设置授权决策。 */
    public void setDecision(Decision decision) {
        this.decision = decision;
    }
}
