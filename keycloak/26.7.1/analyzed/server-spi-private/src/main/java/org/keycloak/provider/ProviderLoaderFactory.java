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

/**
 * 提供者加载器工厂：根据部署类型创建 {@link ProviderLoader} 实例。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ProviderLoaderFactory {

    /** @param type 部署类型标识
     * @return 是否支持该部署类型 */
    boolean supports(String type);

    /** 创建提供者加载器。
     * @param info 部署信息
     * @param baseClassLoader 基础类加载器
     * @param resource 资源路径或标识
     * @return {@link ProviderLoader} 实例 */
    ProviderLoader create(KeycloakDeploymentInfo info, ClassLoader baseClassLoader, String resource);

}
