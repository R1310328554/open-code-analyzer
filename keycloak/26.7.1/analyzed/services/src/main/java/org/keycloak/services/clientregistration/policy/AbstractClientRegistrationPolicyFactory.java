/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientregistration.policy;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 客户端注册策略工厂的抽象基类。
 * <p>提供 {@link ClientRegistrationPolicyFactory} 的默认生命周期与配置校验实现。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractClientRegistrationPolicyFactory implements ClientRegistrationPolicyFactory {

    /** Keycloak 会话工厂引用（postInit 后可用） */
    protected KeycloakSessionFactory sessionFactory;

    /** 工厂初始化（无全局配置） */
    @Override
    public void init(Config.Scope config) {
    }

    /** 保存会话工厂引用 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        this.sessionFactory = factory;
    }

    /** 工厂关闭钩子 */
    @Override
    public void close() {
    }

    /** 校验组件配置（默认无额外校验） */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
    }

    /** @param session 当前会话（含领域上下文） @return 可配置属性列表 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties(KeycloakSession session) {
        return getConfigProperties();
    }
}
