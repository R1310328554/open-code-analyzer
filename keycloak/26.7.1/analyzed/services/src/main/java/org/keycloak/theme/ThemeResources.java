/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

import java.util.Collections;
import java.util.List;

/**
 * 主题静态资源集合。
 * <p>聚合样式表、公共样式、脚本与 favicon 四类 {@link ThemeResourceDescriptor} 列表，供登录页等模板渲染时引用。</p>
 */
public class ThemeResources {

    /** 空资源集合单例。 */
    private static final ThemeResources EMPTY = new ThemeResources(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
    );

    private final List<ThemeResourceDescriptor> styles;
    private final List<ThemeResourceDescriptor> stylesCommon;
    private final List<ThemeResourceDescriptor> scripts;
    private final List<ThemeResourceDescriptor> favicons;

    /** 构造资源集合；各列表会被 {@link List#copyOf} 防御性复制。 */
    public ThemeResources(
            List<ThemeResourceDescriptor> styles,
            List<ThemeResourceDescriptor> stylesCommon,
            List<ThemeResourceDescriptor> scripts,
            List<ThemeResourceDescriptor> favicons) {
        this.styles = List.copyOf(styles);
        this.stylesCommon = List.copyOf(stylesCommon);
        this.scripts = List.copyOf(scripts);
        this.favicons = List.copyOf(favicons);
    }

    /** 返回不含任何资源的空实例。 */
    public static ThemeResources empty() {
        return EMPTY;
    }

    /** 返回页面专用样式表列表。 */
    public List<ThemeResourceDescriptor> getStyles() {
        return styles;
    }

    /** 返回跨页面共享的公共样式表列表。 */
    public List<ThemeResourceDescriptor> getStylesCommon() {
        return stylesCommon;
    }

    /** 返回 JavaScript 脚本列表。 */
    public List<ThemeResourceDescriptor> getScripts() {
        return scripts;
    }

    /** 返回 favicon 链接列表。 */
    public List<ThemeResourceDescriptor> getFavicons() {
        return favicons;
    }
}
