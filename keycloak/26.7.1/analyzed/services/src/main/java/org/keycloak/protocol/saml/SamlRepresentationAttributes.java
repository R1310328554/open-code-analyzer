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

package org.keycloak.protocol.saml;

import java.util.Map;

/**
 * SAML 客户端表示属性读取器：从 {@link org.keycloak.representations.idm.ClientRepresentation} attributes 映射中提取 SAML 配置项。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SamlRepresentationAttributes {
    protected Map<String, String> attributes;

    /** @param attributes 客户端表示中的 attributes 映射 */
    public SamlRepresentationAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /** @return 签名规范化方法 URI */
    public String getCanonicalizationMethod() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_CANONICALIZATION_METHOD_ATTRIBUTE);
    }

    protected Map<String, String> getAttributes() {
        return attributes;
    }

    /** @return 签名算法标识 */
    public String getSignatureAlgorithm() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_SIGNATURE_ALGORITHM);
    }

    /** @return NameID 格式 */
    public String getNameIDFormat() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_NAME_ID_FORMAT_ATTRIBUTE);

    }

    /** @return 是否在 Assertion 中包含 AuthnStatement */
    public String getIncludeAuthnStatement() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_AUTHNSTATEMENT);

    }

    /** @return 是否强制使用配置的 NameID 格式 */
    public String getForceNameIDFormat() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_FORCE_NAME_ID_FORMAT_ATTRIBUTE);
    }

    /** @return 是否允许 ECP（PAOS）flow */
    public String getAllowEcpFlow() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_ALLOW_ECP_FLOW);
    }

    public String getSamlArtifactBinding() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_ARTIFACT_BINDING);
    }

    /** @return 是否要求 IdP/Realm 签名 */
    public String getSamlServerSignature() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_SERVER_SIGNATURE);
    }

    public String getAddExtensionsElementWithKeyInfo() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_SERVER_SIGNATURE_KEYINFO_EXT);
    }

    /** @return 是否强制 POST 绑定 */
    public String getForcePostBinding() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_FORCE_POST_BINDING);

    }
    /** @return 是否要求 SP 对请求签名 */
    public String getClientSignature() {
        if (getAttributes() == null) return null;
        return getAttributes().get(SamlConfigAttributes.SAML_CLIENT_SIGNATURE_ATTRIBUTE);

    }
}
