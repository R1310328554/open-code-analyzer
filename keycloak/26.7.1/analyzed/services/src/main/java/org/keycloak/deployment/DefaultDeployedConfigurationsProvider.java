/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.deployment;

import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.models.AuthenticatorConfigModel;

/**
 * 默认部署配置提供者：在内存映射中维护运行时注册的认证器配置。
 * <p>实现 {@link DeployedConfigurationsProvider}，供 {@link DeployedConfigurationsManager} 统一查询。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultDeployedConfigurationsProvider implements DeployedConfigurationsProvider {

    /** 已部署认证器配置 ID → 模型映射（共享引用，由工厂持有）。 */
    private final Map<String, AuthenticatorConfigModel> deployedAuthenticatorConfigs;
    /** @param deployedAuthenticatorConfigs 工厂级共享配置映射 */
    public DefaultDeployedConfigurationsProvider(Map<String, AuthenticatorConfigModel> deployedAuthenticatorConfigs) {
        this.deployedAuthenticatorConfigs = deployedAuthenticatorConfigs;
    }

    @Override
    /** 注册或覆盖一条部署型认证器配置。 */
    public void registerDeployedAuthenticatorConfig(AuthenticatorConfigModel model) {
        deployedAuthenticatorConfigs.put(model.getId(), model);
    }

    @Override
    /** @return 所有已注册部署配置的流 */
    public Stream<AuthenticatorConfigModel> getDeployedAuthenticatorConfigs() {
        return deployedAuthenticatorConfigs.values().stream();
    }

    @Override
    /** 无资源需释放。 */
    public void close() {

    }
}
