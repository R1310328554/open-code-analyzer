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

import org.keycloak.adapters.spi.HttpFacade.Request;

/**
 * 多租户场景下，在请求阶段由应用解析 {@link SamlDeployment} 的解析器接口。
 *
 * <p>Keycloak 将 {@link Request} 传入解析器，调用方需返回完整的部署配置，
 * 随后适配器据此继续认证与授权。可使用 {@link org.keycloak.adapters.saml.config.parsers.DeploymentBuilder}
 * 读取 {@code keycloak-saml.xml} 的 {@code InputStream} 并调用 {@code build()} 构建部署对象。</p>
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public interface SamlConfigResolver {

    /**
     * 根据当前 HTTP 请求解析并返回 SAML 部署配置。
     *
     * @param facade 当前请求门面
     * @return 与租户/上下文匹配的 {@link SamlDeployment}
     */
    public SamlDeployment resolve(Request facade);

}
