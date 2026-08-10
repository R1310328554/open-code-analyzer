/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.arquillian.galleon.container;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.keycloak.testsuite.utils.arquillian.DeploymentArchiveProcessorUtils;

/**
 * EAP 8 部署归档处理器，在测试部署前调整 web.xml 与 SAML 适配器配置。
 */
public class EAP8DeploymentArchiveProcessor implements ApplicationArchiveProcessor {

    /** 日志记录器 */
    private final Logger log = Logger.getLogger(EAP8DeploymentArchiveProcessor.class);

    /**
     * 处理测试部署归档：跳过服务端运行部署，否则修改 web.xml 与 SAML 配置。
     *
     * @param archive 待部署的 ShrinkWrap 归档
     * @param testClass 当前测试类
     */
    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (DeploymentArchiveProcessorUtils.checkRunOnServerDeployment(archive)) return;

        modifyWebXML(archive, testClass);

        modifySAMLAdapterConfig(archive, DeploymentArchiveProcessorUtils.SAML_ADAPTER_CONFIG_PATH);
        modifySAMLAdapterConfig(archive, DeploymentArchiveProcessorUtils.SAML_ADAPTER_CONFIG_PATH_TENANT1);
        modifySAMLAdapterConfig(archive, DeploymentArchiveProcessorUtils.SAML_ADAPTER_CONFIG_PATH_TENANT2);
    }

    /** 将 web.xml 中的 Servlet 类替换为 Jakarta EE 版本。 */
    private void modifyWebXML(Archive<?> archive, TestClass testClass) {
        if (!archive.contains(DeploymentArchiveProcessorUtils.WEBXML_PATH)) return;

        DeploymentArchiveProcessorUtils.useJakartaEEServletClass(archive, DeploymentArchiveProcessorUtils.WEBXML_PATH);

        if (!archive.contains(DeploymentArchiveProcessorUtils.JBOSS_DEPLOYMENT_XML_PATH)) return;
    }

    /**
     * 修改归档内指定路径的 SAML 适配器配置文件。
     *
     * @param archive 部署归档
     * @param adapterConfigPath SAML 适配器配置路径
     */
    private void modifySAMLAdapterConfig(Archive<?> archive, String adapterConfigPath) {
        if (!archive.contains(adapterConfigPath)) return;

        log.debug("Modifying adapter config " + adapterConfigPath + " in " + archive.getName());
        DeploymentArchiveProcessorUtils.modifySAMLAdapterConfig(archive, adapterConfigPath);
    }
}
