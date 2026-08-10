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
package org.keycloak.saml.processing.core.saml.v2.holders;

import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;

/**
 * 创建 SAML 消息时所需的签发者（Issuer）及响应状态信息容器。
 * <p>默认状态码为成功，SAML 版本为 2.0。</p>
 *
 * @param <JBossSAMLConstants>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2008
 */
public class IssuerInfoHolder {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /** 签发者的 NameID 表示。 */
    private NameIDType issuer;

    /** 响应状态码 URI，默认为成功。 */
    private String statusCodeURI = JBossSAMLURIConstants.STATUS_SUCCESS.get();

    /** SAML 协议版本，默认为 2.0。 */
    private String samlVersion = JBossSAMLConstants.VERSION_2_0.get();

    /** 使用 NameID 构造签发者信息容器。 */
    public IssuerInfoHolder(NameIDType issuer) {
        if (issuer == null)
            throw logger.nullArgumentError("issuer");
        this.issuer = issuer;
    }

    /** 使用字符串形式的 Issuer 值构造容器。 */
    public IssuerInfoHolder(String issuerAsString) {
        if (issuerAsString == null)
            throw logger.nullArgumentError("issuerAsString");
        issuer = new NameIDType();
        issuer.setValue(issuerAsString);
    }

    /** 返回签发者 NameID。 */
    public NameIDType getIssuer() {
        return issuer;
    }

    /** 设置签发者 NameID。 */
    public void setIssuer(NameIDType issuer) {
        this.issuer = issuer;
    }

    /** 返回响应状态码 URI。 */
    public String getStatusCode() {
        return statusCodeURI;
    }

    /** 设置响应状态码 URI。 */
    public void setStatusCode(String statusCode) {
        this.statusCodeURI = statusCode;
    }

    /** 返回 SAML 版本字符串。 */
    public String getSamlVersion() {
        return samlVersion;
    }

    /** 设置 SAML 版本字符串。 */
    public void setSamlVersion(String samlVersion) {
        this.samlVersion = samlVersion;
    }
}