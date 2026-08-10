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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Classpath 主题提供者。
 * <p>按类型与名称查找已注册的 {@link ClassLoaderTheme} 实例。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClasspathThemeProvider implements ThemeProvider {

    private Map<Theme.Type, Map<String, ClassLoaderTheme>> themes;

    /** 注入预加载的主题映射表。 */
    public ClasspathThemeProvider(Map<Theme.Type, Map<String, ClassLoaderTheme>> themes) {
        this.themes = themes;
    }

    /** 返回提供者优先级（classpath 主题默认为 0）。 */
    @Override
    public int getProviderPriority() {
        return 0;
    }

    /** 按名称与类型获取主题，不存在时返回 null。 */
    @Override
    public Theme getTheme(String name, Theme.Type type) throws IOException {
        return hasTheme(name, type) ? themes.get(type).get(name) : null;
    }

    /** 返回指定类型的所有已注册主题名称。 */
    @Override
    public Set<String> nameSet(Theme.Type type) {
        if (themes.containsKey(type)) {
            return themes.get(type).keySet();
        } else {
            return Collections.emptySet();
        }
    }

    /** 检查是否存在指定名称与类型的主题。 */
    @Override
    public boolean hasTheme(String name, Theme.Type type) {
        return themes.containsKey(type) && themes.get(type).containsKey(name);
    }

    @Override
    public void close() {
    }

}
