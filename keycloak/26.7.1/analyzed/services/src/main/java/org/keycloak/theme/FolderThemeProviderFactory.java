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

package org.keycloak.theme;

import java.io.File;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 文件夹主题 Provider 工厂。
 * <p>从配置项 {@code dir} 初始化 {@link FolderThemeProvider}，id 为 {@code folder}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FolderThemeProviderFactory implements ThemeProviderFactory {

    private FolderThemeProvider themeProvider;

    /** 返回共享的文件夹主题 Provider 单例。 */
    @Override
    public ThemeProvider create(KeycloakSession sessions) {
        return themeProvider;
    }

    /** 读取 {@code dir} 配置并创建 Provider。 */
    @Override
    public void init(Config.Scope config) {
        String d = config.get("dir");
        File rootDir = null;
        if (d != null) {
            rootDir = new File(d);
        }
        themeProvider = new FolderThemeProvider(rootDir);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** 工厂标识符。 */
    @Override
    public String getId() {
        return "folder";
    }

}
