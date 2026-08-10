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
import java.util.Set;

import org.keycloak.provider.Provider;

/**
 * 主题提供者：按名称与类型加载主题资源。
 * <p>多个 {@link ThemeProvider} 按 {@link #getProviderPriority()} 优先级合并。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ThemeProvider extends Provider {

    /** @return 提供者优先级，数值越大优先级越高 */
    int getProviderPriority();

    /**
     * 按名称与类型获取主题。
     * @param name 主题名称
     * @param type 主题类型
     * @throws IOException 读取主题资源失败时
     */
    Theme getTheme(String name, Theme.Type type) throws IOException;

    /** @param type 主题类型
     * @return 该类型下可用主题名称集合 */
    Set<String> nameSet(Theme.Type type);

    /** @param name 主题名称
     * @param type 主题类型
     * @return 是否存在指定主题 */
    boolean hasTheme(String name, Theme.Type type);

}
