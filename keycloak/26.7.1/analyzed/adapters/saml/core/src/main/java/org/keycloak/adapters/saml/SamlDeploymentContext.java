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

package org.keycloak.adapters.saml;

import org.keycloak.adapters.spi.HttpFacade;

/**
 * SAML 部署配置上下文：持有固定部署或按请求动态解析的配置源。
 *
 * <p>单租户使用构造时注入的 {@link SamlDeployment}；多租户使用 {@link SamlConfigResolver}
 * 在每次请求时解析部署信息。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SamlDeploymentContext {
    /** 静态部署配置（单租户）。 */
    private SamlDeployment deployment = null;
    /** 动态部署解析器（多租户）。 */
    private SamlConfigResolver resolver = null;

    /**
     * 使用固定 SAML 部署配置创建上下文。
     *
     * @param deployment 预加载的部署配置
     */
    public SamlDeploymentContext(SamlDeployment deployment) {
        this.deployment = deployment;
    }

    /**
     * 使用配置解析器创建上下文，部署信息按请求解析。
     *
     * @param resolver 多租户部署解析器
     */
    public SamlDeploymentContext(SamlConfigResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * 解析当前请求对应的 SAML 部署配置。
     *
     * @param facade HTTP 门面
     * @return 静态或动态解析得到的 {@link SamlDeployment}
     */
    public SamlDeployment resolveDeployment(HttpFacade facade) {
        if (deployment != null) {
            return deployment;
        } else {
            return resolver.resolve(facade.getRequest());
        }
    }
}
