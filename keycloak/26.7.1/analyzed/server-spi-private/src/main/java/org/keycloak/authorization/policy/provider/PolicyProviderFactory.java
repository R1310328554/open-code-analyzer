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

package org.keycloak.authorization.policy.provider;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.authorization.AbstractPolicyRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * 策略提供者工厂 SPI：注册策略类型、创建 {@link PolicyProvider} 并处理策略 CRUD 生命周期。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface PolicyProviderFactory<R extends AbstractPolicyRepresentation> extends ProviderFactory<PolicyProvider> {

    /** 策略类型唯一名称（如 role、js、aggregate）。 */
    String getName();

    /** 管理控制台分组标识。 */
    String getGroup();

    /** 策略类型描述，供管理 UI 展示。 */
    default String getDescription() {
        return null;
    }

    /** 可选代码片段或模板标识。 */
    default String getCode() {
        return null;
    }

    /** 是否为内部策略类型（不在 UI 暴露）。 */
    default boolean isInternal() {
        return false;
    }

    /** 创建策略提供者实例。 */
    PolicyProvider create(AuthorizationProvider authorization);

    /** 将持久化 {@link Policy} 转为 REST 表示对象。 */
    R toRepresentation(Policy policy, AuthorizationProvider authorization);

    /** 返回该策略类型的表示类。 */
    Class<R> getRepresentationType();

    /** 策略创建后的回调。 */
    default void onCreate(Policy policy, R representation, AuthorizationProvider authorization) {

    }

    /** 策略更新后的回调。 */
    default void onUpdate(Policy policy, R representation, AuthorizationProvider authorization) {

    }

    /** 策略删除后的回调。 */
    default void onRemove(Policy policy, AuthorizationProvider authorization) {

    }

    /** 策略导入时的回调。 */
    default void onImport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorization) {

    }

    /** 策略导出时的回调。 */
    default void onExport(Policy policy, PolicyRepresentation representation, AuthorizationProvider authorizationProvider) {
    }

    /** 返回该策略类型的管理 REST 扩展，默认无。 */
    default PolicyProviderAdminService getAdminResource(ResourceServer resourceServer, AuthorizationProvider authorization) {
        return null;
    }
}