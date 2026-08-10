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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.keycloak.theme.ClasspathThemeProviderFactory;
import org.keycloak.theme.ClasspathThemeResourceProviderFactory;
import org.keycloak.theme.ThemeResourceSpi;
import org.keycloak.theme.ThemeSpi;

/**
 * 默认 SPI 提供者加载器。
 * <p>通过 {@link ServiceLoader} 从类路径加载 {@link Spi} 与 {@link ProviderFactory}；并在部署信息包含主题资源时注入 classpath 主题工厂。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultProviderLoader implements ProviderLoader {

    /** Keycloak 部署元信息（是否含 services/themes 等） */
    private KeycloakDeploymentInfo info;
    /** 用于 {@link ServiceLoader} 的类加载器 */
    private ClassLoader classLoader;

    /** @param info 部署信息 @param classLoader SPI 扫描类加载器 */
    public DefaultProviderLoader(KeycloakDeploymentInfo info, ClassLoader classLoader) {
        this.info = info;
        this.classLoader = classLoader;
    }

    /** 加载所有 SPI 定义；无 services 时返回空列表 @return SPI 列表 */
    @Override
    public List<Spi> loadSpis() {
        if (info.hasServices()) {
            LinkedList<Spi> list = new LinkedList<>();
            for (Spi spi : ServiceLoader.load(Spi.class, classLoader)) {
                list.add(spi);
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    /** 加载指定 SPI 的 {@link ProviderFactory}，含主题相关内置工厂 @param spi 目标 SPI @return 工厂列表 */
    @Override
    public List<ProviderFactory> load(Spi spi) {
        List<ProviderFactory> list = new LinkedList<>();
        if (info.hasServices()) {
            for (ProviderFactory f : ServiceLoader.load(spi.getProviderFactoryClass(), classLoader)) {
                list.add(f);
            }
        }

        if (spi.getClass().equals(ThemeResourceSpi.class) && info.hasThemeResources()) {
            ClasspathThemeResourceProviderFactory resourceProviderFactory = new ClasspathThemeResourceProviderFactory(info.getName(), classLoader);
            list.add(resourceProviderFactory);
        }

        if (spi.getClass().equals(ThemeSpi.class) && info.hasThemes()) {
            ClasspathThemeProviderFactory themeProviderFactory = new ClasspathThemeProviderFactory(info.getName(), classLoader);
            list.add(themeProviderFactory);
        }

        return list;
    }

}
