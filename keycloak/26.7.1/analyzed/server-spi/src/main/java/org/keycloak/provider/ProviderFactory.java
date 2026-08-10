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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Provider 工厂接口：启动时发现所有工厂，依次调用 {@link #init}、{@link #postInit}，关闭时调用 {@link #close}。
 * 每台服务器每个工厂仅有一个实例。
 *
 * At boot time, keycloak discovers all factories.  For each discovered factory, the init() method is called.  After
 * all factories have been initialized, the postInit() method is called.  close() is called when the server shuts down.
 *
 * Only one instance of a factory exists per server.
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ProviderFactory<T extends Provider> {

    /** @param session 当前 Keycloak 会话
     * @return 新建的 Provider 实例 */
    T create(KeycloakSession session);

    /**
     * 工厂首次创建时仅调用一次。
     * Only called once when the factory is first created.
     *
     * @param config
     */
    void init(Config.Scope config);

    /**
     * 所有 Provider 工厂初始化完成后调用。
     * Called after all provider factories have been initialized
     */
    void postInit(KeycloakSessionFactory factory);

    /**
     * 服务器关闭时调用。
     * This is called when the server shuts down.
     *
     */
    void close();

    /** @return 工厂唯一标识符 */
    String getId();

    /** @return 工厂排序权重，数值越小越优先 */
    default int order() {
        return 0;
    }

    /**
     * 返回本工厂支持的各配置项元数据列表。
     * Returns the metadata for each configuration property supported by this factory.
     *
     * @return a list with the metadata for each configuration property supported by this factory
     */
    default List<ProviderConfigProperty> getConfigMetadata() {
        return Collections.emptyList();
    }

    /**
     * 可选：声明本工厂依赖的其他 Provider；保证依赖方 {@link #postInit}/{@link #close} 调用顺序。
     * Optional method used to declare that a ProviderFactory has a dependency on one or more Providers. If a Provider
     * is declared here, it is guaranteed that the dependencies {@link #postInit} method will be executed
     * before this ProviderFactory's {@link #postInit}. Similarly, it's guaranteed that {@link #close()} will be
     * called on this {@link ProviderFactory} before {@link #close()} is called on any of the dependent ProviderFactory
     * implementations.
     */
    default Set<Class<? extends Provider>> dependsOn() {
        return Collections.emptySet();
    }
}
