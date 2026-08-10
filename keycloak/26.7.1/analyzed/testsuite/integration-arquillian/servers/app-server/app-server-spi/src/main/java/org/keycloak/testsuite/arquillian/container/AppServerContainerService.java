/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.arquillian.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * 应用服务器容器服务门面，通过 SPI 加载 {@link AppServerContainerProvider} 实现。
 * <p>
 * 按应用服务器名称查找并返回 Arquillian 容器配置节点列表。
 *
 * @author <a href="mailto:vramik@redhat.com">Vlasta Ramik</a>
 */
public class AppServerContainerService  {

    /** 单例服务实例 */
    private static AppServerContainerService service;
    /** SPI 加载的应用服务器容器提供者 */
    private final ServiceLoader<AppServerContainerProvider> loader;

    /** 私有构造，通过 {@link ServiceLoader} 初始化提供者加载器。 */
    private AppServerContainerService() {
        loader = ServiceLoader.load(AppServerContainerProvider.class);
    }

    /**
     * 获取容器服务单例实例。
     *
     * @return 容器服务单例
     */
    public static synchronized AppServerContainerService getInstance() {
        if (service == null) {
            service = new AppServerContainerService();
        }
        return service;
    }

    /**
     * 根据应用服务器名称获取 Arquillian 容器配置节点。
     *
     * @param appServerName 应用服务器名称（如 wildfly、eap8）
     * @return 匹配的容器节点列表；未找到时可能为 {@code null}
     */
    public List<Node> getContainers(String appServerName) {
        List<Node> containers = null;
        try {
            // 遍历 SPI 发现的所有容器提供者
            Iterator<AppServerContainerProvider> definitions = loader.iterator();

            List<AppServerContainerProvider> availableDefinitions = new ArrayList<>();
            while (definitions != null && definitions.hasNext()) {
                availableDefinitions.add(definitions.next());
            }
            // 按名称匹配目标应用服务器提供者
            for (AppServerContainerProvider def : availableDefinitions) {
                if (def.getName().equals(appServerName)) {
                    containers = def.getContainers();
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            containers = null;
            throw serviceError;
        }
        return containers;
    }
}
