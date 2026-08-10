/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link GroupProvider} 的 {@link org.keycloak.provider.ProviderFactory} 工厂接口。
 * <p>管理 realm 组层次结构的持久化与查询。</p>
 */
public interface GroupProviderFactory<T extends GroupProvider> extends ProviderFactory<T> {

    /**
     * 组路径中斜杠是否转义存储。
     * <p>默认使用 {@link GroupProvider#DEFAULT_ESCAPE_SLASHES}。</p>
     *
     * @return 是否转义斜杠
     */
    default boolean escapeSlashesInGroupPath() {
        return GroupProvider.DEFAULT_ESCAPE_SLASHES;
    }
}
