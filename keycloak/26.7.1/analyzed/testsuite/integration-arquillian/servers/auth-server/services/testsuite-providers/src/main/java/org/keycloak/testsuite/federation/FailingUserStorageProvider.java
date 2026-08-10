/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.testsuite.federation;

import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserCountMethodsProvider;
import org.keycloak.storage.user.UserQueryMethodsProvider;

/**
 * 模拟多种失败场景的用户存储提供者，用于测试 {@code UserStorageManager} 的优雅降级行为。
 */
public class FailingUserStorageProvider implements UserStorageProvider, UserQueryMethodsProvider, UserCountMethodsProvider {
    
    /** 组件配置项：搜索操作时是否抛出异常。 */
    public static final String FAIL_ON_SEARCH = "failOnSearch";
    /** 组件配置项：计数操作时是否抛出异常。 */
    public static final String FAIL_ON_COUNT = "failOnCount";
    
    /** 关联的组件模型，用于读取失败开关配置。 */
    private final ComponentModel model;
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    
    /**
     * @param session Keycloak 会话
     * @param model 用户存储组件模型
     */
    public FailingUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }
    
    /** {@inheritDoc} 关闭提供者；当前实现无需额外清理。 */
    @Override
    public void close() {
    }
    
    /** {@inheritDoc} 按参数搜索用户；配置失败时模拟 LDAP 连接超时异常。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        if (Boolean.parseBoolean(model.getConfig().getFirst(FAIL_ON_SEARCH))) {
            throw new RuntimeException("Simulated user search failure - LDAP connection timeout");
        }
        // 未配置失败时返回空流
        return Stream.empty();
    }
    
    /** {@inheritDoc} 按用户属性搜索；配置失败时模拟 LDAP 连接超时异常。 */
    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        if (Boolean.parseBoolean(model.getConfig().getFirst(FAIL_ON_SEARCH))) {
            throw new RuntimeException("Simulated user attribute search failure - LDAP connection timeout");
        }
        return Stream.empty();
    }
    
    /** {@inheritDoc} 查询组成员；配置失败时模拟 LDAP 连接超时异常。 */
    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        if (Boolean.parseBoolean(model.getConfig().getFirst(FAIL_ON_SEARCH))) {
            throw new RuntimeException("Simulated group member search failure - LDAP connection timeout");
        }
        return Stream.empty();
    }
    
    /** {@inheritDoc} 统计用户数；配置失败时模拟 LDAP 连接超时异常。 */
    @Override
    public int getUsersCount(RealmModel realm, Map<String, String> params) {
        if (Boolean.parseBoolean(model.getConfig().getFirst(FAIL_ON_COUNT))) {
            throw new RuntimeException("Simulated user count failure - LDAP connection timeout");
        }
        return 0;
    }
}
