/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.credentialbuilder;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.VCFormat;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link LDCredentialBuilder} 的组件工厂，注册 LDP-VC 格式构建器。
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class LDCredentialBuilderFactory implements CredentialBuilderFactory {

    /** 工厂配置属性列表（当前为空）。 */
    protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /** {@inheritDoc} LDP-VC 格式。 */
    @Override
    public String getSupportedFormat() {
        return VCFormat.LDP_VC;
    }

    /** {@inheritDoc} LDP-VC 格式构建器说明。 */
    @Override
    public String getHelpText() {
        return "Builds verifiable credentials on the LDP-VC format (https://www.w3.org/TR/vc-data-model).";
    }

    /** {@inheritDoc} 返回空配置列表。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** {@inheritDoc} 创建 LDP 构建器实例。 */
    @Override
    public CredentialBuilder create(KeycloakSession session, ComponentModel model) {
        return new LDCredentialBuilder();
    }
}
