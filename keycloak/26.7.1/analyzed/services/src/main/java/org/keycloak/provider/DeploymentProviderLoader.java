/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.provider;

import java.util.Collections;
import java.util.List;

/**
 * 部署内嵌提供者加载器。
 * <p>不从类路径扫描 SPI，而是从 {@link KeycloakDeploymentInfo} 预注册的提供者映射加载 {@link ProviderFactory}（Quarkus/嵌入式部署场景）。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
final class DeploymentProviderLoader implements ProviderLoader {

    /** 含预注册 ProviderFactory 的部署信息 */
    private final KeycloakDeploymentInfo info;

    /** @param info 部署信息 */
    DeploymentProviderLoader(KeycloakDeploymentInfo info) {
        this.info = info;
    }

    /** 不通过此加载器发现 SPI @return 空列表 */
    @Override
    public List<Spi> loadSpis() {
        return Collections.emptyList();
    }

    /** 从部署信息获取指定 SPI 的工厂列表 @param spi 目标 SPI @return 预注册的工厂列表 */
    @Override
    public List<ProviderFactory> load(Spi spi) {
        return info.getProviders().getOrDefault(spi.getClass(), Collections.emptyList());
    }
}
