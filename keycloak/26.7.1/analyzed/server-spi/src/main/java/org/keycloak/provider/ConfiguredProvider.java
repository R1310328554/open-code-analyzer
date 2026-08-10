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

package org.keycloak.provider;

import java.util.List;

/**
 * 可配置 Provider：提供帮助文本、配置属性与默认配置。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ConfiguredProvider {
    /** @return Provider 帮助文本（管理控制台展示） */
    String getHelpText();

    /** @return Provider 配置属性元数据列表 */
    List<ProviderConfigProperty> getConfigProperties();

    /**
     * 返回该 Provider 的默认配置对象。
     * Returns a default configuration for this provider.
     *
     * @param <C> the type of the configuration
     * @return the default configuration
     */
    default <C> C getConfig() {
        return null;
    }
}
