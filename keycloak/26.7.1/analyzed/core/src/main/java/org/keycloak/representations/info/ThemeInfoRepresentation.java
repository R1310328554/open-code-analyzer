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

/*
 */

package org.keycloak.representations.info;

/**
 * UI 主题的 REST 表示，描述主题名称、支持的语言区域及说明文字。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ThemeInfoRepresentation {

    /** 主题名称（目录名）。 */
    private String name;
    /** 该主题支持的语言/区域代码数组。 */
    private String[] locales;
    /** 主题的人类可读描述。 */
    private String description;

    /** @return 主题名称 */
    public String getName() {
        return name;
    }

    /** @param name 主题名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 支持的语言区域 */
    public String[] getLocales() {
        return locales;
    }

    /** @param locales 支持的语言区域 */
    public void setLocales(String[] locales) {
        this.locales = locales;
    }

    /** @return 主题描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 主题描述 */
    public void setDescription(String description) {
        this.description = description;
    }
}
