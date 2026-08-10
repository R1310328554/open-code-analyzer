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
 * See the License for the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.arquillian.container;

import java.util.List;

import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * 应用服务器 Arquillian 容器提供者 SPI。
 * <p>
 * 各应用服务器适配模块实现此接口，向测试套件暴露可用的容器配置节点。
 * </p>
 *
 * @author <a href="mailto:vramik@redhat.com">Vlasta Ramik</a>
 */
public interface AppServerContainerProvider  {

    /** Arquillian 容器组名称常量：{@value}。 */
    public static final String APP_SERVER = "app-server";

    /**
     * 返回容器提供者的名称标识。
     *
     * @return 容器名称字符串
     */
    public String getName();

    /**
     * 返回该提供者下可用的 Arquillian 容器描述节点列表。
     *
     * @return 可用容器节点列表；无可用容器时返回 {@code null}
     */
    public List<Node> getContainers();
}
