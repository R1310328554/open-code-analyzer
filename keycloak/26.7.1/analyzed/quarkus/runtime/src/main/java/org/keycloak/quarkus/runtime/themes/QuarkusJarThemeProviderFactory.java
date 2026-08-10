/*
 * Copyright ${YEAR} Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.themes;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.theme.JarThemeProviderFactory;

/**
 * Quarkus JAR 主题工厂：构建期注入主题列表，运行时从 classpath 加载。
 */
public class QuarkusJarThemeProviderFactory extends JarThemeProviderFactory {

    /** 由 Quarkus 扩展在启动前注入已解析的主题元数据。 */
    public void setThemes(List<ThemesRepresentation> themes) {
        for (ThemesRepresentation theme : themes) {
            loadThemes(Thread.currentThread().getContextClassLoader(), theme);
        }
    }

    @Override
    public void init(Config.Scope config) {
        // Quarkus 构建期已完成主题注册，此处无需额外配置
    }
}
