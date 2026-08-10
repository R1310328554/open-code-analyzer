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

package org.keycloak.testsuite.arquillian.wildfly.container;

import java.util.ArrayList;
import java.util.List;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.as.arquillian.container.managed.ManagedDeployableContainer;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;
import org.keycloak.testsuite.arquillian.container.AppServerContainerProvider;

/**
 * WildFly 应用服务器 Arquillian 容器提供者，构建 standalone 与 HA 集群配置。
 *
 * @author <a href="mailto:vramik@redhat.com">Vlasta Ramik</a>
 */
public class WildflyAppServerProvider implements AppServerContainerProvider {

    /** 当前正在构建的容器配置节点 */
    private Node configuration;
    /** WildFly 容器标识名称 */
    private static final String containerName = "wildfly";

    private final String appServerHome;
    private final String appServerJavaHome;
    private final String appServerPortOffset;
    private final String managementProtocol;
    private final String managementPort;
    private final String startupTimeoutInSeconds;

    /**
     * 从系统属性读取 WildFly 安装路径、管理端口等配置并校验必填项。
     */
    public WildflyAppServerProvider() {
        appServerHome = System.getProperty("app.server.home");
        appServerJavaHome = System.getProperty("app.server.java.home");
        appServerPortOffset = System.getProperty("app.server.port.offset");
        managementProtocol = System.getProperty("app.server.management.protocol");
        managementPort = System.getProperty("app.server.management.port");
        startupTimeoutInSeconds = System.getProperty("app.server.startup.timeout");

        Validate.notNullOrEmpty(appServerHome, "app.server.home is not set.");
        Validate.notNullOrEmpty(appServerJavaHome, "app.server.java.home is not set.");
        Validate.notNullOrEmpty(appServerPortOffset, "app.server.port.offset is not set.");
        Validate.notNullOrEmpty(managementProtocol, "app.server.management.protocol is not set.");
        Validate.notNullOrEmpty(managementPort, "app.server.management.port is not set.");
        Validate.notNullOrEmpty(startupTimeoutInSeconds, "app.server.startup.timeout is not set.");
    }

    /** {@inheritDoc} 返回 {@code wildfly}。 */
    @Override
    public String getName() {
        return containerName;
    }

    /**
     * 构建独立模式容器与 HA 集群组配置节点。
     *
     * @return 包含 standalone 与 cluster 组的容器节点列表
     */
    @Override
    public List<Node> getContainers() {
        List<Node> containers = new ArrayList<>();

        containers.add(standaloneContainer());
        containers.add(clusterGroup());

        return containers;
    }

    /** 在当前 configuration 节点下追加 name/text 属性子节点。 */
    private void createChild(String name, String text) {
        configuration.createChild("property").attribute("name", name).text(text);
    }

    /** 构建 WildFly 独立（standalone-test）模式 Arquillian 容器配置。 */
    private Node standaloneContainer() {
        Node container = new Node("container");
        container.attribute("mode", "manual");
        container.attribute("qualifier", AppServerContainerProvider.APP_SERVER + "-" + containerName);

        configuration = container.createChild("configuration");
        createChild("enabled", "true");
        createChild("adapterImplClass", ManagedDeployableContainer.class.getName());
        createChild("jbossHome", appServerHome);
        createChild("javaHome", appServerJavaHome);
        createChild("jbossArguments", 
                "-Djboss.server.base.dir=" + appServerHome + "/standalone-test " +
                "-Djboss.server.config.dir=" + appServerHome + "/standalone-test/configuration " +
                "-Djboss.server.log.dir=" + appServerHome + "/standalone-test/log " +
                "-Djboss.socket.binding.port-offset=" + appServerPortOffset + " " +
                System.getProperty("adapter.test.props", " ") +
                System.getProperty("kie.maven.settings", " ")
        );
        createChild("javaVmArguments", 
                System.getProperty("app.server.jboss.jvm.debug.args", "") + " " +
                System.getProperty("app.server.memory.settings", "") + " " +
                "-Djava.net.preferIPv4Stack=true" + " " +
                System.getProperty("app.server.jvm.args.extra")
        );
        createChild("managementProtocol", managementProtocol);
        createChild("managementPort", managementPort);
        createChild("startupTimeoutInSeconds", startupTimeoutInSeconds);

        return container;
    }

    /** 构建包含两个 HA 节点的 WildFly 集群容器组。 */
    private Node clusterGroup() {
        Node group = new Node("group");
        group.attribute("qualifier", "app-server-wildfly-clustered");
        addHaNodeContainer(group, 1);
        addHaNodeContainer(group, 2);
        return group;
    }

    /**
     * 向集群组添加指定编号的 HA 节点容器配置。
     *
     * @param group 集群组节点
     * @param number HA 节点编号（1 或 2）
     */
    private void addHaNodeContainer(Node group, int number) {
        String portOffset = System.getProperty("app.server." + number + ".port.offset");
        String managementPort = System.getProperty("app.server." + number + ".management.port");

        Validate.notNullOrEmpty(portOffset, "app.server." + number + ".port.offset is not set.");
        Validate.notNullOrEmpty(managementPort, "app.server." + number + ".management.port is not set.");

        Node container = group.createChild("container");
        container.attribute("mode", "manual");
        container.attribute("qualifier", AppServerContainerProvider.APP_SERVER + "-" + containerName + "-ha-node-" + number);

        configuration = container.createChild("configuration");
        createChild("enabled", "true");
        createChild("adapterImplClass", ManagedDeployableContainer.class.getName());
        createChild("jbossHome", appServerHome);
        createChild("javaHome", appServerJavaHome);
        // cleanServerBaseDir 在 WFARQ-44 修复前不可用
//        createChild("cleanServerBaseDir", appServerHome + "/standalone-ha-node-" + number);
        createChild("serverConfig", "standalone-ha.xml");
        createChild("jbossArguments", 
                "-Djboss.server.base.dir=" + appServerHome + "/standalone-ha-node-" + number + " " +
                "-Djboss.socket.binding.port-offset=" + portOffset + " " +
                "-Djboss.node.name=ha-node-" + number + " " +
                getCrossDCProperties(number, portOffset) +
                System.getProperty("adapter.test.props", " ") +
                System.getProperty("kie.maven.settings", " ")
        );
        createChild("javaVmArguments",
                System.getProperty("app.server." + number + ".jboss.jvm.debug.args") + " " +
                System.getProperty("app.server.memory.settings", "") + " " +
                "-Djava.net.preferIPv4Stack=true" + " " +
                System.getProperty("app.server.jvm.args.extra")
        );
        createChild("managementProtocol", managementProtocol);
        createChild("managementPort", managementPort);
        createChild("startupTimeoutInSeconds", startupTimeoutInSeconds);
    }
    
    /**
     * 当启用跨数据中心缓存时，生成 TCP 发现与 Hot Rod 端口系统属性。
     *
     * @param number HA 节点编号
     * @param portOffset 端口偏移量
     * @return JVM 参数字符串；未配置 cache.server 时返回空串
     */
    private String getCrossDCProperties(int number, String portOffset) {
        if (System.getProperty("cache.server") == null || System.getProperty("cache.server").equals("undefined")) {
            return "";
        }
        String cacheHotrodPortString = System.getProperty("cache.server." + number + ".port.offset");
        Validate.notNullOrEmpty(cacheHotrodPortString, "cache.server." + number + ".port.offset is not set.");

        int tcppingPort = 7600 + Integer.parseInt(portOffset);
        int cacheHotrodPort = 11222 + Integer.parseInt(cacheHotrodPortString);
        
        // 以下属性供 servers/app-server/jboss/common/cli/configure-crossdc-config.cli 使用
        return "-Dtcpping.port=" + tcppingPort + " -Dcache.hotrod.port=" + cacheHotrodPort + " ";
    }
}
