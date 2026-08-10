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

package org.keycloak.provider;

import java.util.List;

/**
 * 提供者加载器：从部署单元加载 SPI 定义与提供者工厂。
 * <p>由 {@link ProviderLoaderFactory} 创建，支持 JAR、目录等多种加载方式。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ProviderLoader {

    /**
     * 加载 SPI 定义列表。
     * Load the SPI definitions themselves.
     * @return a list of Spi definition objects
     */
    List<Spi> loadSpis();

    /**
     * 加载指定 SPI 的全部提供者工厂。
     * Load all provider factories of a specific SPI.
     * @param spi the Spi definition
     * @return a list of provider factories
     */
    List<ProviderFactory> load(Spi spi);
}
