/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.policy.provider.aggregated;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.policy.provider.PolicyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.authorization.AggregatePolicyRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * <p>聚合（aggregate）策略类型的 {@link PolicyProviderFactory}。
 *
 * <p>创建/更新/导入时校验关联策略无循环引用。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AggregatePolicyProviderFactory implements PolicyProviderFactory<AggregatePolicyRepresentation> {

    /** 策略类型标识符。 */
    public static final String ID = "aggregate";

    private AggregatePolicyProvider provider = new AggregatePolicyProvider();

    @Override
    /** 管理控制台显示名称。 */
    public String getName() {
        return "Aggregated";
    }

    @Override
    /** 策略分组：Others。 */
    public String getGroup() {
        return "Others";
    }

    @Override
    /** 基于 {@link AuthorizationProvider} 创建单例 {@link AggregatePolicyProvider}。 */
    public PolicyProvider create(AuthorizationProvider authorization) {
        return provider;
    }

    @Override
    /** 基于 {@link KeycloakSession} 创建单例 {@link AggregatePolicyProvider}。 */
    public PolicyProvider create(KeycloakSession session) {
        return provider;
    }

    @Override
    /** 创建时检测关联策略是否存在循环引用。 */
    public void onCreate(Policy policy, AggregatePolicyRepresentation representation, AuthorizationProvider authorization) {
        verifyCircularReference(policy, new ArrayList<>());
    }

    @Override
    /** 更新时检测关联策略是否存在循环引用。 */
    public void onUpdate(Policy policy, AggregatePolicyRepresentation representation, AuthorizationProvider authorization) {
        verifyCircularReference(policy, new ArrayList<>());
    }

    @Override
    /** 导入时检测关联策略是否存在循环引用。 */
    public void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {
        verifyCircularReference(policy, new ArrayList<>());
    }

    @Override
    /** 转为 {@link AggregatePolicyRepresentation}（聚合策略无额外字段）。 */
    public AggregatePolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        return new AggregatePolicyRepresentation();
    }

    @Override
    /** 返回策略表示类型。 */
    public Class<AggregatePolicyRepresentation> getRepresentationType() {
        return AggregatePolicyRepresentation.class;
    }

    /** 深度优先检测 aggregate 关联链中的循环引用。 */
    private void verifyCircularReference(Policy policy, List<String> ids) {
        if (!policy.getType().equals("aggregate")) {
            return;
        }

        if (ids.contains(policy.getId())) {
            throw new RuntimeException("Circular reference found [" + policy.getName() + "].");
        }

        ids.add(policy.getId());

        for (Policy associated : policy.getAssociatedPolicies()) {
            verifyCircularReference(associated, ids);
        }
    }

    @Override
    /** 策略删除时无额外清理。 */
    public void onRemove(Policy policy, AuthorizationProvider authorization) {

    }

    @Override
    /** SPI 初始化（无配置项）。 */
    public void init(Config.Scope config) {

    }

    @Override
    /** 会话工厂后置初始化（无操作）。 */
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    /** 关闭工厂（无资源释放）。 */
    public void close() {

    }

    @Override
    /** 返回策略类型 ID {@value #ID}。 */
    public String getId() {
        return ID;
    }
}
