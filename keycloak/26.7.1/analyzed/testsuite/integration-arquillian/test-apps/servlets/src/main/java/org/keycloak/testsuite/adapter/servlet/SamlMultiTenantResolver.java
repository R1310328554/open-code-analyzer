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

package org.keycloak.testsuite.adapter.servlet;

import java.io.InputStream;

import org.keycloak.adapters.saml.SamlConfigResolver;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.config.parsers.DeploymentBuilder;
import org.keycloak.adapters.saml.config.parsers.ResourceLoader;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.saml.common.exceptions.ParsingException;

/**
 * 多租户 SAML 配置解析器，根据请求中的 realm 参数加载对应 SAML 部署描述。
 *
 * @author rmartinc
 */
public class SamlMultiTenantResolver implements SamlConfigResolver {

    /**
     * 根据请求 realm 查询参数解析 SAML 部署配置。
     *
     * @param request HTTP 门面请求
     * @return 对应 realm 的 {@link SamlDeployment}
     */
    @Override
    public SamlDeployment resolve(HttpFacade.Request request) {
        String realm = request.getQueryParamValue("realm");
        if (realm == null) {
            throw new IllegalStateException("Not able to resolve realm from the request path!");
        }

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + realm + "-keycloak-saml.xml");
        if (is == null) {
            throw new IllegalStateException("Not able to find the file /" + realm + "-keycloak-saml.xml");
        }

        ResourceLoader loader = new ResourceLoader() {
            @Override
            public InputStream getResourceAsStream(String path) {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            }
        };

        try {
            return new DeploymentBuilder().build(is, loader);
        } catch (ParsingException e) {
            throw new IllegalStateException("Cannot load SAML deployment", e);
        }
    }

}
