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
package org.keycloak.saml.processing.core.saml.v2.factories;

import java.net.URI;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.EncryptedAssertionType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.dom.saml.v2.protocol.ResponseType.RTChoiceType;
import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.processing.core.saml.v2.holders.IssuerInfoHolder;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;

import org.w3c.dom.Element;

/**
 * SAML 2.0 认证响应（AuthnResponse）工厂类。
 * <p>负责构造 {@link ResponseType}、{@link StatusType} 等协议对象。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 9, 2008
 */
public class JBossSAMLAuthnResponseFactory {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * 根据状态码 URI 创建 {@link StatusType}。
     *
     * @param statusCodeURI SAML 状态码 URI
     *
     * @return 包含指定状态码的 StatusType
     */
    public static StatusType createStatusType(String statusCodeURI) {
        StatusCodeType sct = new StatusCodeType();
        sct.setValue(URI.create(statusCodeURI));

        StatusType statusType = new StatusType();
        statusType.setStatusCode(sct);
        return statusType;
    }

    /**
     * <p>创建顶层为 {@code STATUS_RESPONDER}、次级为给定 {@code statusCodeURI} 的两级 {@link StatusType}。</p>
     *
     * @param statusCodeURI 二级状态码 URI
     *
     * @return 两级嵌套的 StatusType
     */
    public static StatusType createStatusTypeForResponder(String statusCodeURI) {
        StatusCodeType topLevelCode = new StatusCodeType();

        topLevelCode.setValue(JBossSAMLURIConstants.STATUS_RESPONDER.getUri());

        StatusCodeType secondLevelCode = new StatusCodeType();

        secondLevelCode.setValue(URI.create(statusCodeURI));

        topLevelCode.setStatusCode(secondLevelCode);

        StatusType statusType = new StatusType();

        statusType.setStatusCode(topLevelCode);

        return statusType;
    }

    /**
     * 创建包含明文断言的 SAML 响应。
     *
     * @param ID 响应 ID
     * @param issuerInfo 签发者及状态码信息
     * @param assertionType 明文断言
     *
     * @return 构造完成的 {@link ResponseType}
     *
     * @throws ConfigurationException 配置错误时抛出
     */
    public static ResponseType createResponseType(String ID, IssuerInfoHolder issuerInfo, AssertionType assertionType) {
        XMLGregorianCalendar issueInstant = XMLTimeUtil.getIssueInstant();
        ResponseType responseType = new ResponseType(ID, issueInstant);

        // 设置 Issuer 元素
        NameIDType issuer = issuerInfo.getIssuer();
        responseType.setIssuer(issuer);

        // 设置 Status 元素
        String statusCode = issuerInfo.getStatusCode();
        if (statusCode == null)
            throw logger.issuerInfoMissingStatusCodeError();

        responseType.setStatus(createStatusType(statusCode));

        responseType.addAssertion(new RTChoiceType(assertionType));
        return responseType;
    }

    /**
     * 创建包含加密断言 DOM 元素的 SAML 响应。
     *
     * @param ID 响应 ID
     * @param issuerInfo 签发者及状态码信息
     * @param encryptedAssertion 表示加密断言的 DOM {@link Element}
     *
     * @return 构造完成的 {@link ResponseType}
     *
     * @throws ConfigurationException 配置错误时抛出
     */
    public static ResponseType createResponseType(String ID, IssuerInfoHolder issuerInfo, Element encryptedAssertion) {
        ResponseType responseType = new ResponseType(ID, XMLTimeUtil.getIssueInstant());

        // Issuer
        NameIDType issuer = issuerInfo.getIssuer();
        responseType.setIssuer(issuer);

        // Status
        String statusCode = issuerInfo.getStatusCode();
        if (statusCode == null)
            throw logger.issuerInfoMissingStatusCodeError();

        responseType.setStatus(createStatusType(statusCode));

        responseType.addAssertion(new RTChoiceType(new EncryptedAssertionType(encryptedAssertion)));
        return responseType;
    }
}