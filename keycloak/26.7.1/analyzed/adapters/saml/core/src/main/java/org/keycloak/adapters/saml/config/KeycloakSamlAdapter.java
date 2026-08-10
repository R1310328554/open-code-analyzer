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

package org.keycloak.adapters.saml.config;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * {@code keycloak-saml.xml} 根配置对象，包含一个或多个服务提供方（SP）定义。
 *
 * <p>由 {@link org.keycloak.adapters.saml.config.parsers.KeycloakSamlAdapterParser} 解析生成，
 * 再经 {@link org.keycloak.adapters.saml.config.parsers.DeploymentBuilder} 转换为运行时 {@link org.keycloak.adapters.saml.SamlDeployment}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeycloakSamlAdapter implements Serializable {
    /** 已注册的 SP 配置列表。 */
    private final List<SP> sps = new LinkedList<>();

    /**
     * 返回所有 SP 配置。
     *
     * @return SP 列表（通常取首个元素构建部署）
     */
    public List<SP> getSps() {
        return sps;
    }

    /**
     * 向适配器配置追加一个 SP。
     *
     * @param sp 服务提供方配置
     */
    public void addSp(SP sp) {
        sps.add(sp);
    }

}
