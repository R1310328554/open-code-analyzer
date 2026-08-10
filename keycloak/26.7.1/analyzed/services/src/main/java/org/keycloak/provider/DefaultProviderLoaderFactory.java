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
 * 默认 {@link ProviderLoaderFactory} 实现。
 * <p>作为兜底工厂创建 {@link DefaultProviderLoader}；{@link #supports(String)} 恒为 false，由 {@link ProviderManager} 直接注册默认加载器。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultProviderLoaderFactory implements ProviderLoaderFactory {

    /** 不支持任何外部资源类型（默认加载器由 ProviderManager 直接添加） @return 始终 false */
    @Override
    public boolean supports(String type) {
        return false;
    }

    /** 创建默认 classpath 提供者加载器 @param info 部署信息 @param baseClassLoader 基类加载器 @return DefaultProviderLoader 实例 */
    @Override
    public ProviderLoader create(KeycloakDeploymentInfo info, ClassLoader baseClassLoader, String resource) {
        return new DefaultProviderLoader(info, baseClassLoader);
    }

}
