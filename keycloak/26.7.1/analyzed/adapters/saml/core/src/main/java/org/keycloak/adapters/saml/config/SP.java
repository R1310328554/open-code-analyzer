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
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * 服务提供方（SP）配置模型，对应 {@code keycloak-saml.xml} 中单个 {@code <SP>} 元素。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SP implements Serializable {
    /** 主体名称映射策略（NameID 或断言属性）。 */
    public static class PrincipalNameMapping implements Serializable {
        /** 策略名，对应 {@link org.keycloak.adapters.saml.SamlDeployment.PrincipalNamePolicy}。 */
        private String policy;
        /** FROM_ATTRIBUTE 策略下的属性名。 */
        private String attributeName;

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }
    }

    /**
     * {@link org.keycloak.adapters.saml.RoleMappingsProvider} 的配置容器。
     *
     * <p>包含提供者 ID 及 {@link Properties} 形式的扩展选项。</p>
     */
    public static class RoleMappingsProviderConfig implements Serializable {
        /** SPI 提供者标识。 */
        private String id;
        /** 提供者专属配置项。 */
        private Properties configuration;

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public Properties getConfiguration() {
            return this.configuration;
        }

        public void setConfiguration(final Properties configuration) {
            this.configuration = configuration;
        }

        public void addConfigurationProperty(final String name, final String value) {
            this.configuration.setProperty(name, value);
        }
    }

    /** SP 实体标识符（建议为 URI 格式）。 */
    private String entityID;
    /** SSL 策略（如 EXTERNAL、ALL）。 */
    private String sslPolicy;
    private boolean forceAuthentication;
    private boolean isPassive;
    private boolean turnOffChangeSessionIdOnLogin;
    private String logoutPage;
    /** SP 签名/加密密钥列表。 */
    private List<Key> keys;
    private String nameIDPolicyFormat;
    private PrincipalNameMapping principalNameMapping;
    /** 用于提取角色的断言属性名集合。 */
    private Set<String> roleAttributes;
    private RoleMappingsProviderConfig roleMappingsProviderConfig;
    private IDP idp;
    private boolean autodetectBearerOnly;
    /** 是否在会话中保留断言 DOM。 */
    private boolean keepDOMAssertion;

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getSslPolicy() {
        return sslPolicy;
    }

    public void setSslPolicy(String sslPolicy) {
        this.sslPolicy = sslPolicy;
    }

    public boolean isForceAuthentication() {
        return forceAuthentication;
    }

    public void setForceAuthentication(Boolean forceAuthentication) {
        this.forceAuthentication = forceAuthentication != null && forceAuthentication;
    }

    public boolean isIsPassive() {
        return isPassive;
    }

    public void setIsPassive(Boolean isPassive) {
        this.isPassive = isPassive != null && isPassive;
    }

    public boolean isTurnOffChangeSessionIdOnLogin() {
        return turnOffChangeSessionIdOnLogin;
    }

    public void setTurnOffChangeSessionIdOnLogin(Boolean turnOffChangeSessionIdOnLogin) {
        this.turnOffChangeSessionIdOnLogin = turnOffChangeSessionIdOnLogin != null && turnOffChangeSessionIdOnLogin;
    }

    public boolean isKeepDOMAssertion() {
        return keepDOMAssertion;
    }

    public void setKeepDOMAssertion(Boolean keepDOMAssertion) {
        this.keepDOMAssertion = keepDOMAssertion != null && keepDOMAssertion;
    }

    public List<Key> getKeys() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public String getNameIDPolicyFormat() {
        return nameIDPolicyFormat;
    }

    public void setNameIDPolicyFormat(String nameIDPolicyFormat) {
        this.nameIDPolicyFormat = nameIDPolicyFormat;
    }

    public PrincipalNameMapping getPrincipalNameMapping() {
        return principalNameMapping;
    }

    public void setPrincipalNameMapping(PrincipalNameMapping principalNameMapping) {
        this.principalNameMapping = principalNameMapping;
    }

    public Set<String> getRoleAttributes() {
        return roleAttributes;
    }

    public void setRoleAttributes(Set<String> roleAttributes) {
        this.roleAttributes = roleAttributes;
    }

    public RoleMappingsProviderConfig getRoleMappingsProviderConfig() {
        return this.roleMappingsProviderConfig;
    }

    public void setRoleMappingsProviderConfig(final RoleMappingsProviderConfig provider) {
        this.roleMappingsProviderConfig = provider;
    }

    public IDP getIdp() {
        return idp;
    }

    public void setIdp(IDP idp) {
        this.idp = idp;
    }

    public String getLogoutPage() {
        return logoutPage;
    }

    public void setLogoutPage(String logoutPage) {
        this.logoutPage = logoutPage;
    }

    public boolean isAutodetectBearerOnly() {
        return autodetectBearerOnly;
    }

    public void setAutodetectBearerOnly(Boolean autodetectBearerOnly) {
        this.autodetectBearerOnly = autodetectBearerOnly != null && autodetectBearerOnly;
    }
}
