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

package org.keycloak.representations.info;

import java.util.Map;

/**
 * 单个 SPI（Service Provider Interface）的 REST 表示，列出该 SPI 下全部 Provider 实现及其元数据。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class SpiInfoRepresentation {

    /** 是否为 Keycloak 内部 SPI（不对外暴露给扩展开发者）。 */
    private boolean internal;

    /** Provider 实现 ID 到 {@link ProviderRepresentation} 的映射。 */
    private Map<String, ProviderRepresentation> providers;

    /** @return 是否内部 SPI */
    public boolean isInternal() {
        return internal;
    }

    /** @param internal 是否内部 SPI */
    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    /** @return Provider 实现映射 */
    public Map<String, ProviderRepresentation> getProviders() {
        return providers;
    }

    /** @param providers Provider 实现映射 */
    public void setProviders(Map<String, ProviderRepresentation> providers) {
        this.providers = providers;
    }

}
