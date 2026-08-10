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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.util.JsonSerialization;

/**
 * Classpath 主题提供者工厂。
 * <p>从 {@link #KEYCLOAK_THEMES_JSON} 加载主题清单并注册 {@link ClassLoaderTheme}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClasspathThemeProviderFactory implements ThemeProviderFactory {

    /** classpath 主题清单 JSON 资源路径。 */
    public static final String KEYCLOAK_THEMES_JSON = "META-INF/keycloak-themes.json";
    protected static Map<Theme.Type, Map<String, ClassLoaderTheme>> themes = new HashMap<>();

    private String id;

    /** 仅设置工厂 id（延迟加载主题）。 */
    public ClasspathThemeProviderFactory(String id) {
        this.id = id;
    }

    /** 设置 id 并从 ClassLoader 加载主题清单。 */
    public ClasspathThemeProviderFactory(String id, ClassLoader classLoader) {
        this.id = id;
        loadThemes(classLoader, classLoader.getResourceAsStream(KEYCLOAK_THEMES_JSON));
    }

    /** keycloak-themes.json 中单个主题的 JSON 表示。 */
    public static class ThemeRepresentation {
        private String name;
        private String[] types;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[] getTypes() {
            return types;
        }

        public void setTypes(String[] types) {
            this.types = types;
        }
    }

    /** keycloak-themes.json 根结构，包含主题数组。 */
    public static class ThemesRepresentation {
        private ThemeRepresentation[] themes;

        public ThemeRepresentation[] getThemes() {
            return themes;
        }

        public void setThemes(ThemeRepresentation[] themes) {
            this.themes = themes;
        }
    }

    /** 创建共享静态主题表的 ThemeProvider 实例。 */
    @Override
    public ThemeProvider create(KeycloakSession session) {
        return new ClasspathThemeProvider(themes);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return id;
    }

    /** 从输入流解析 JSON 并加载主题。 */
    protected void loadThemes(ClassLoader classLoader, InputStream themesInputStream) {
        try {
            loadThemes(classLoader, JsonSerialization.readValue(themesInputStream, ThemesRepresentation.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load themes", e);
        }
    }

    /** 遍历主题清单并为每种类型注册 ClassLoaderTheme。 */
    protected void loadThemes(ClassLoader classLoader, ThemesRepresentation themesRep) {
        try {
            for (ThemeRepresentation themeRep : themesRep.getThemes()) {
                for (String t : themeRep.getTypes()) {
                    Theme.Type type = Theme.Type.valueOf(t.toUpperCase());
                    if (!themes.containsKey(type)) {
                        themes.put(type, new HashMap<>());
                    }
                    themes.get(type).put(themeRep.getName(), new ClassLoaderTheme(themeRep.getName(), type, classLoader));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load themes", e);
        }
    }

}
