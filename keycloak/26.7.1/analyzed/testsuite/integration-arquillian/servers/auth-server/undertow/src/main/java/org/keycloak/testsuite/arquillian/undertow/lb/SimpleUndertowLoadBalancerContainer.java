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

package org.keycloak.testsuite.arquillian.undertow.lb;

import org.keycloak.testsuite.arquillian.LoadBalancerController;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * 基于 {@link SimpleUndertowLoadBalancer} 的 Arquillian 可部署容器，用于集成测试中的负载均衡场景。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SimpleUndertowLoadBalancerContainer implements DeployableContainer<SimpleUndertowLoadBalancerConfiguration>, LoadBalancerController {

    private static final Logger log = Logger.getLogger(SimpleUndertowLoadBalancerContainer.class);

    /** 负载均衡器容器配置。 */
    private SimpleUndertowLoadBalancerConfiguration configuration;
    /** 底层 Undertow 负载均衡实例。 */
    private SimpleUndertowLoadBalancer container;

    /** {@inheritDoc} 返回 {@link SimpleUndertowLoadBalancerConfiguration} 配置类型。 */
    @Override
    public Class<SimpleUndertowLoadBalancerConfiguration> getConfigurationClass() {
        return SimpleUndertowLoadBalancerConfiguration.class;
    }

    /** {@inheritDoc} 保存容器启动所需的配置。 */
    @Override
    public void setup(SimpleUndertowLoadBalancerConfiguration configuration) {
        this.configuration = configuration;
    }

    /** {@inheritDoc} 根据配置创建并启动负载均衡器。 */
    @Override
    public void start() throws LifecycleException {
        this.container = new SimpleUndertowLoadBalancer(configuration.getBindAddress(), configuration.getBindHttpPort(), configuration.getBindHttpsPort(), configuration.getNodes());
        this.container.start();
    }

    /** {@inheritDoc} 停止负载均衡器并释放资源。 */
    @Override
    public void stop() throws LifecycleException {
        log.info("Going to stop loadbalancer");
        this.container.stop();
    }

    /** {@inheritDoc} 默认使用 Servlet 3.1 协议描述。 */
    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.1");
    }

    /** {@inheritDoc} 当前容器不支持直接部署归档。 */
    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} 当前容器不支持撤销归档部署。 */
    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} 当前容器不支持基于描述符的部署。 */
    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} 当前容器不支持撤销描述符部署。 */
    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} 启用所有后端节点。 */
    @Override
    public void enableAllBackendNodes() {
        this.container.enableAllBackendNodes();
    }

    /** {@inheritDoc} 禁用所有后端节点。 */
    @Override
    public void disableAllBackendNodes() {
        this.container.disableAllBackendNodes();
    }

    /** {@inheritDoc} 按名称启用指定后端节点。 */
    @Override
    public void enableBackendNodeByName(String nodeName) {
        this.container.enableBackendNodeByName(nodeName);
    }

    /** {@inheritDoc} 按名称禁用指定后端节点。 */
    @Override
    public void disableBackendNodeByName(String nodeName) {
        this.container.disableBackendNodeByName(nodeName);
    }
}
