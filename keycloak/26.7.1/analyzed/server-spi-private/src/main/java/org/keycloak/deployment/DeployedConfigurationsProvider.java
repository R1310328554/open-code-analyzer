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

import java.util.stream.Stream;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.provider.Provider;

/**
 * 部署配置提供者 SPI：在运行时注册并查询由扩展部署的认证器配置。
 * <p>此类配置不写入数据库，随部署包或启动逻辑注入。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface DeployedConfigurationsProvider extends Provider {

    /** 注册一条部署型 {@link AuthenticatorConfigModel}。 */
    void registerDeployedAuthenticatorConfig(AuthenticatorConfigModel model);

    /** @return 所有已注册部署配置的流 */
    Stream<AuthenticatorConfigModel> getDeployedAuthenticatorConfigs();

}
