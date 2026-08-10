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

package org.keycloak.saml;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AudienceRestrictionType;
import org.keycloak.dom.saml.v2.assertion.AuthnStatementType;
import org.keycloak.dom.saml.v2.assertion.ConditionsType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.assertion.OneTimeUseType;
import org.keycloak.dom.saml.v2.assertion.SubjectConfirmationDataType;
import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.processing.api.saml.v2.response.SAML2Response;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;
import org.keycloak.saml.processing.core.saml.v2.holders.IDPInfoHolder;
import org.keycloak.saml.processing.core.saml.v2.holders.IssuerInfoHolder;
import org.keycloak.saml.processing.core.saml.v2.holders.SPInfoHolder;
import org.keycloak.saml.processing.core.saml.v2.util.StatementUtil;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;

import org.w3c.dom.Document;

import static org.keycloak.saml.common.util.StringUtil.isNotNull;

/**
 * <p>处理 SAML 2.0 登录成功响应（含 Assertion）的构建逻辑。</p>
 * <p>配置项包括主体/断言/会话过期时间、NameID、AuthnStatement 及一次性使用条件等。</p>
 *
 * @author bburke@redhat.com
 */
public class SAML2LoginResponseBuilder implements SamlProtocolExtensionsAwareBuilder<SAML2LoginResponseBuilder> {
    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /** 响应 Destination（通常为 SP ACS URL）。 */
    protected String destination;
    /** 响应 Issuer（IdP 实体 ID）。 */
    protected NameIDType issuer;
    /** SubjectConfirmation 有效时长（秒），参见 SAML Core 2.4.1.2 NotOnOrAfter。 */
    protected int subjectExpiration;
    /** Assertion Conditions 有效时长（秒），参见 SAML Core 2.5.1.2 NotOnOrAfter。 */
    protected int assertionExpiration;
    /** IdP 会话有效时长（秒），参见 SAML Core 2.7.2 SessionNotOnOrAfter。 */
    protected int sessionExpiration;
    /** 主体 NameID 值。 */
    protected String nameId;
    /** NameID 格式 URI。 */
    protected String nameIdFormat;
    /** 是否支持多值角色属性。 */
    protected boolean multiValuedRoles;
    /** 是否在 Assertion 中包含 AuthnStatement。 */
    protected boolean disableAuthnStatement;
    /** 原始 AuthnRequest 的 ID（InResponseTo）。 */
    protected String requestID;
    /** 认证上下文类引用（AuthnContextClassRef）。 */
    protected String authMethod;
    /** 请求发起方（SP）实体 ID，用作 Audience。 */
    protected String requestIssuer;
    /** IdP 会话索引（SessionIndex）。 */
    protected String sessionIndex;
    /** 协议扩展节点生成器列表。 */
    protected final List<NodeGenerator> extensions = new LinkedList<>();
    /** 是否在 Conditions 中加入 OneTimeUse 条件。 */
    protected boolean includeOneTimeUseCondition;

    /**
     * 设置 IdP 会话索引。
     *
     * @param sessionIndex 会话标识
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder sessionIndex(String sessionIndex) {
        this.sessionIndex = sessionIndex;
        return this;
    }

    /**
     * 设置响应目标地址。
     *
     * @param destination SP ACS URL
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    /**
     * 设置 Issuer。
     *
     * @param issuer IdP 标识
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder issuer(NameIDType issuer) {
        this.issuer = issuer;
        return this;
    }

    /**
     * 设置 Issuer（字符串形式）。
     *
     * @param issuer IdP 实体 ID
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder issuer(String issuer) {
        return issuer(SAML2NameIDBuilder.value(issuer).build());
    }

    /**
     * 设置 SubjectConfirmation 有效时长（秒）。
     * 参见 SAML Core 规范 2.4.1.2 NotOnOrAfter。
     *
     * @param subjectExpiration 主体确认有效秒数
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder subjectExpiration(int subjectExpiration) {
        this.subjectExpiration = subjectExpiration;
        return this;
    }

    /**
     * 设置 IdP 会话有效时长（秒）。
     * 参见 SAML Core 规范 2.7.2 SessionNotOnOrAfter。
     *
     * @param sessionExpiration 会话有效秒数
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder sessionExpiration(int sessionExpiration) {
        this.sessionExpiration = sessionExpiration;
        return this;
    }

    /**
     * 设置 Assertion 有效时长（秒）。
     * 参见 SAML Core 规范 2.5.1.2 NotOnOrAfter。
     *
     * @param assertionExpiration 断言有效秒数
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder assertionExpiration(int assertionExpiration) {
        this.assertionExpiration = assertionExpiration;
        return this;
    }

    /**
     * 设置原始 AuthnRequest ID（InResponseTo）。
     *
     * @param requestID 请求 ID
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder requestID(String requestID) {
        this.requestID = requestID;
        return this;
    }

    /**
     * 设置请求发起方实体 ID（用作 AudienceRestriction）。
     *
     * @param requestIssuer SP 实体 ID
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder requestIssuer(String requestIssuer) {
        this.requestIssuer = requestIssuer;
        return this;
    }

    /**
     * 设置认证方法 / AuthnContextClassRef。
     *
     * @param authMethod 认证上下文 URI
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder authMethod(String authMethod) {
        this.authMethod = authMethod;
        return this;
    }

    /**
     * 设置主体 NameID 及其格式。
     *
     * @param nameIdFormat NameID 格式 URI
     * @param nameId NameID 值
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder nameIdentifier(String nameIdFormat, String nameId) {
        this.nameIdFormat = nameIdFormat;
        this.nameId = nameId;
        return this;
    }

    /**
     * 是否以多值形式输出角色属性。
     *
     * @param multiValuedRoles 多值标志
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder multiValuedRoles(boolean multiValuedRoles) {
        this.multiValuedRoles = multiValuedRoles;
        return this;
    }

    /**
     * 是否禁用 AuthnStatement（{@code true} 表示不生成认证语句）。
     *
     * @param disableAuthnStatement 禁用标志
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder disableAuthnStatement(boolean disableAuthnStatement) {
        this.disableAuthnStatement = disableAuthnStatement;
        return this;
    }

    /**
     * 是否在 Conditions 中加入 OneTimeUse 条件。
     *
     * @param includeOneTimeUseCondition 是否包含
     * @return 当前构建器
     */
    public SAML2LoginResponseBuilder includeOneTimeUseCondition(boolean includeOneTimeUseCondition) {
        this.includeOneTimeUseCondition = includeOneTimeUseCondition;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SAML2LoginResponseBuilder addExtension(NodeGenerator extension) {
        this.extensions.add(extension);
        return this;
    }

    /**
     * 将 {@link ResponseType} 模型序列化为 DOM 文档。
     *
     * @param responseType 响应模型
     * @return SAML 响应 XML 文档
     * @throws ConfigurationException 配置错误
     * @throws ProcessingException 序列化失败
     */
    public Document buildDocument(ResponseType responseType) throws ConfigurationException, ProcessingException {
        Document samlResponseDocument = null;

        try {
            SAML2Response docGen = new SAML2Response();
            samlResponseDocument = docGen.convert(responseType);

            if (logger.isTraceEnabled()) {
                logger.trace("SAML Response Document: " + DocumentUtil.asString(samlResponseDocument));
            }
        } catch (Exception e) {
            throw logger.samlAssertionMarshallError(e);
        }

        return samlResponseDocument;
    }

    /**
     * 构建完整的 {@link ResponseType} 模型（含 Assertion、Conditions、AuthnStatement 等）。
     *
     * @return 填充完毕的 SAML 响应对象
     * @throws ConfigurationException 配置错误
     * @throws ProcessingException 处理失败
     */
    public ResponseType buildModel() throws ConfigurationException, ProcessingException {
        ResponseType responseType = null;

        SAML2Response saml2Response = new SAML2Response();

        // 创建响应类型
        String id = IDGenerator.create("ID_");

        IssuerInfoHolder issuerHolder = new IssuerInfoHolder(issuer);
        issuerHolder.setStatusCode(JBossSAMLURIConstants.STATUS_SUCCESS.get());

        IDPInfoHolder idp = new IDPInfoHolder();
        idp.setNameIDFormatValue(nameId);
        idp.setNameIDFormat(nameIdFormat);

        SPInfoHolder sp = new SPInfoHolder();
        sp.setResponseDestinationURI(destination);
        sp.setRequestID(requestID);
        sp.setIssuer(requestIssuer);
        responseType = saml2Response.createResponseType(id, sp, idp, issuerHolder);

        AssertionType assertion = responseType.getAssertions().get(0).getAssertion();

        // 将请求发起方作为 AudienceRestriction
        AudienceRestrictionType audience = new AudienceRestrictionType();
        audience.addAudience(URI.create(requestIssuer));
        assertion.getConditions().addCondition(audience);

        // 更新 Conditions 的 NotOnOrAfter
        if(assertionExpiration > 0) {
            ConditionsType conditions = assertion.getConditions();
            conditions.setNotOnOrAfter(XMLTimeUtil.add(conditions.getNotBefore(), assertionExpiration * 1000L));
        }

        // 更新 SubjectConfirmationData 的 NotOnOrAfter
        if(subjectExpiration > 0) {
            SubjectConfirmationDataType subjectConfirmationData = assertion.getSubject().getConfirmation().get(0).getSubjectConfirmationData();
            subjectConfirmationData.setNotOnOrAfter(XMLTimeUtil.add(assertion.getConditions().getNotBefore(), subjectExpiration * 1000L));
        }

        // 创建 AuthnStatementType
        if (!disableAuthnStatement) {
            String authContextRef = JBossSAMLURIConstants.AC_UNSPECIFIED.get();
            if (isNotNull(authMethod))
                authContextRef = authMethod;

            AuthnStatementType authnStatement = StatementUtil.createAuthnStatement(XMLTimeUtil.getIssueInstant(),
                    authContextRef);

            if (sessionExpiration > 0)
                authnStatement.setSessionNotOnOrAfter(XMLTimeUtil.add(authnStatement.getAuthnInstant(), sessionExpiration * 1000L));

            if (sessionIndex != null) authnStatement.setSessionIndex(sessionIndex);
            else authnStatement.setSessionIndex(assertion.getID());

            assertion.addStatement(authnStatement);
        }

        if (includeOneTimeUseCondition) {
            assertion.getConditions().addCondition(new OneTimeUseType());
        }

        if (!this.extensions.isEmpty()) {
            ExtensionsType extensionsType = new ExtensionsType();
            for (NodeGenerator extension : this.extensions) {
                extensionsType.addExtension(extension);
            }
            responseType.setExtensions(extensionsType);
        }

        return responseType;
    }

}
