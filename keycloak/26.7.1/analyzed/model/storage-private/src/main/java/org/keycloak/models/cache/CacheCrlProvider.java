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

package org.keycloak.models.cache;

import org.keycloak.provider.Provider;

/**
 * CRL（证书吊销列表）缓存 Provider 接口，支持清空已缓存的 CRL 以触发重新加载。
 */
public interface CacheCrlProvider extends Provider {

    /**
     * 清空所有已缓存的 CRL，后续访问将重新从源加载。
     */
    void clearCache();

}
