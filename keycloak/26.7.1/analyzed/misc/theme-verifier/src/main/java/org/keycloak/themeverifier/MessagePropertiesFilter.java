/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.themeverifier;

import java.io.File;

import org.apache.commons.io.filefilter.AbstractFileFilter;

/**
 * 主题国际化资源文件过滤器：仅接受 {@code messages_*.properties} 命名规则的文件。
 */
public class MessagePropertiesFilter extends AbstractFileFilter {
    /** 单例实例，供 {@link ThemeVerifierMojo} 遍历资源目录时使用。 */
    public static MessagePropertiesFilter INSTANCE = new MessagePropertiesFilter();

    /**
     * 判断文件是否为待校验的消息属性文件。
     *
     * @param file 候选文件
     * @return 文件名以 {@code messages_} 开头且以 {@code .properties} 结尾时返回 true
     */
    @Override
    public boolean accept(File file) {
        return file.getName().startsWith("messages_") && file.getName().endsWith(".properties");
    }
}
