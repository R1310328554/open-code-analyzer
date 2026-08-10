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
package org.keycloak.saml.processing.api.saml.v2.response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.keycloak.dom.saml.v2.SAML2Object;
import org.keycloak.dom.saml.v2.assertion.ActionType;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AuthnContextClassRefType;
import org.keycloak.dom.saml.v2.assertion.AuthnContextType;
import org.keycloak.dom.saml.v2.assertion.AuthnStatementType;
import org.keycloak.dom.saml.v2.assertion.AuthzDecisionStatementType;
import org.keycloak.dom.saml.v2.assertion.ConditionsType;
import org.keycloak.dom.saml.v2.assertion.DecisionType;
import org.keycloak.dom.saml.v2.assertion.EncryptedAssertionType;
import org.keycloak.dom.saml.v2.assertion.EncryptedElementType;
import org.keycloak.dom.saml.v2.assertion.EvidenceType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.assertion.StatementAbstractType;
import org.keycloak.dom.saml.v2.assertion.SubjectConfirmationDataType;
import org.keycloak.dom.saml.v2.assertion.SubjectConfirmationType;
import org.keycloak.dom.saml.v2.assertion.SubjectType;
import org.keycloak.dom.saml.v2.protocol.ArtifactResponseType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.exceptions.fed.IssueInstantMissingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.common.util.StaxUtil;
import org.keycloak.saml.processing.core.parsers.saml.SAMLParser;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;
import org.keycloak.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.keycloak.saml.processing.core.saml.v2.factories.JBossSAMLAuthnResponseFactory;
import org.keycloak.saml.processing.core.saml.v2.factories.SAMLAssertionFactory;
import org.keycloak.saml.processing.core.saml.v2.holders.IDPInfoHolder;
import org.keycloak.saml.processing.core.saml.v2.holders.IssuerInfoHolder;
import org.keycloak.saml.processing.core.saml.v2.holders.SPInfoHolder;
import org.keycloak.saml.processing.core.saml.v2.util.AssertionUtil;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;
import org.keycloak.saml.processing.core.saml.v2.writers.SAMLResponseWriter;
import org.keycloak.saml.processing.core.util.JAXPValidationUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.keycloak.saml.common.constants.JBossSAMLURIConstants.PROTOCOL_NSURI;

/**
 * SAML 2.0 响应（Response）的构建、解析与序列化 API。
 * API for dealing with SAML2 Response objects
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 5, 2009
 */
public class SAML2Response {

    /** 日志实例。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    /** 断言默认有效时长（毫秒）。 */
    private final long ASSERTION_VALIDITY = 5000; // 5secs in milis

    /** 时钟偏差容忍（毫秒）。 */
    private final long CLOCK_SKEW = 2000; // 2secs

    /** 最近一次解析得到的 SAML 文档持有者。 */
    private SAMLDocumentHolder samlDocumentHolder = null;

    /**
     * 创建 SAML 断言（Assertion）。
     *
     * Create an assertion
     *
     * @param id 断言 ID
     * @param issuer 签发者 NameID
     *
     * @return AssertionType 实例
     */
    public AssertionType createAssertion(String id, NameIDType issuer) {
        return AssertionUtil.createAssertion(id, issuer);
    }

    /**
     * 创建认证声明（AuthnStatement）。
     *
     * Create an AuthnStatement
     *
     * @param authnContextDeclRef 认证上下文声明引用，如 JBossSAMLURIConstants.AC_PASSWORD_PROTECTED_TRANSPORT
     * @param issueInstant 签发时间
     *
     * @return AuthnStatementType 实例
     */
    public AuthnStatementType createAuthnStatement(String authnContextDeclRef, XMLGregorianCalendar issueInstant) {
        AuthnStatementType authnStatement = new AuthnStatementType(issueInstant);
        AuthnContextType act = new AuthnContextType();
        String authContextDeclRef = JBossSAMLURIConstants.AC_PASSWORD_PROTECTED_TRANSPORT.get();
        act.addAuthenticatingAuthority(URI.create(authContextDeclRef));

        AuthnContextType.AuthnContextTypeSequence sequence = new AuthnContextType.AuthnContextTypeSequence();
        sequence.setClassRef(new AuthnContextClassRefType(JBossSAMLURIConstants.AC_PASSWORD.getUri()));
        act.setSequence(sequence);

        authnStatement.setAuthnContext(act);
        return authnStatement;
    }

    /**
     * 创建授权决策声明（AuthzDecisionStatement）。
     *
     * Create an Authorization Decision Statement Type
     *
     * @param resource 受保护资源 URI
     * @param decision 决策结果
     * @param evidence 证据
     * @param actions 允许/拒绝的操作
     *
     * @return AuthzDecisionStatementType 实例
     */
    public AuthzDecisionStatementType createAuthzDecisionStatementType(String resource, DecisionType decision,
                                                                       EvidenceType evidence, ActionType... actions) {
        AuthzDecisionStatementType authzDecST = new AuthzDecisionStatementType();
        authzDecST.setResource(resource);
        authzDecST.setDecision(decision);
        if (evidence != null)
            authzDecST.setEvidence(evidence);

        if (actions != null) {
            authzDecST.getAction().addAll(Arrays.asList(actions));
        }

        return authzDecST;
    }

    /**
     * 根据 SP/IdP 信息自动构建完整 Response（含断言）。
     *
     * Create a ResponseType
     *
     * <b>NOTE:</b>: The PicketLink STS is used to issue/update the assertion
     *
     * If you want to control over the assertion being issued, then use
     * {@link #createResponseType(String, SPInfoHolder, IDPInfoHolder, IssuerInfoHolder, AssertionType)}
     *
     * @param ID id of the response
     * @param sp holder with the information about the Service Provider
     * @param idp holder with the information on the Identity Provider
     * @param issuerInfo holder with information on the issuer
     *
     * @return
     *
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    public ResponseType createResponseType(String ID, SPInfoHolder sp, IDPInfoHolder idp, IssuerInfoHolder issuerInfo)
            throws ProcessingException {
        String responseDestinationURI = sp.getResponseDestinationURI();

        XMLGregorianCalendar issueInstant = XMLTimeUtil.getIssueInstant();

        // 创建断言主体
        SubjectType subjectType = new SubjectType();

        // 设置 NameID
        NameIDType nameIDType = new NameIDType();
        nameIDType.setFormat(idp.getNameIDFormat() == null ? null : URI.create(idp.getNameIDFormat()));
        nameIDType.setValue(idp.getNameIDFormatValue());

        SubjectType.STSubType subType = new SubjectType.STSubType();
        subType.addBaseID(nameIDType);
        subjectType.setSubType(subType);

        SubjectConfirmationType subjectConfirmation = new SubjectConfirmationType();
        subjectConfirmation.setMethod(idp.getSubjectConfirmationMethod());

        SubjectConfirmationDataType subjectConfirmationData = new SubjectConfirmationDataType();
        subjectConfirmationData.setInResponseTo(sp.getRequestID());
        subjectConfirmationData.setRecipient(responseDestinationURI);
        //subjectConfirmationData.setNotBefore(issueInstant);
        subjectConfirmationData.setNotOnOrAfter(issueInstant);

        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

        subjectType.addConfirmation(subjectConfirmation);

        AssertionType assertionType;
        NameIDType issuerID = issuerInfo.getIssuer();
        issueInstant = XMLTimeUtil.getIssueInstant();
        ConditionsType conditions = null;
        List<StatementAbstractType> statements = new LinkedList<>();

        // generate an id for the new assertion.
        String assertionID = IDGenerator.create("ID_");

        assertionType = SAMLAssertionFactory.createAssertion(assertionID, issuerID, issueInstant, conditions,
                subjectType, statements);

        try {
            AssertionUtil.createTimedConditions(assertionType, ASSERTION_VALIDITY, CLOCK_SKEW);
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        } catch (IssueInstantMissingException e) {
            throw logger.processingError(e);
        }

        ResponseType responseType = createResponseType(ID, issuerInfo, assertionType);
        // InResponseTo ID
        responseType.setInResponseTo(sp.getRequestID());
        // Destination
        responseType.setDestination(responseDestinationURI);

        return responseType;
    }

    /**
     * 创建仅含 ID 与 issueInstant 的空 Response。
     *
     * Create an empty response type
     *
     * @param ID 响应 ID
     * @return ResponseType 实例
     */
    public ResponseType createResponseType(String ID) {
        return new ResponseType(ID, XMLTimeUtil.getIssueInstant());
    }

    /**
     * 用已有断言创建 ResponseType。
     *
     * Create a ResponseType
     *
     * @param ID 响应 ID
     * @param issuerInfo 签发者信息
     * @param assertion 断言对象
     *
     * @return ResponseType 实例
     *
     * @throws ConfigurationException
     */
    public ResponseType createResponseType(String ID, IssuerInfoHolder issuerInfo, AssertionType assertion){
        return JBossSAMLAuthnResponseFactory.createResponseType(ID, issuerInfo, assertion);
    }

    /**
     * 用加密断言 DOM 元素创建 ResponseType。
     *
     * Create a ResponseType
     *
     * @param ID 响应 ID
     * @param issuerInfo 签发者信息
     * @param encryptedAssertion a DOM {@link Element} that represents an encrypted assertion
     *
     * @return
     *
     * @throws ConfigurationException
     */
    public ResponseType createResponseType(String ID, IssuerInfoHolder issuerInfo, Element encryptedAssertion)
            throws ConfigurationException {
        return JBossSAMLAuthnResponseFactory.createResponseType(ID, issuerInfo, encryptedAssertion);
    }

    /**
     * 为断言添加带时效的 Conditions（NotBefore/NotOnOrAfter）。
     *
     * Add validity conditions to the SAML2 Assertion
     *
     * @param assertion 目标断言
     * @param durationInMilis 有效时长（毫秒）
     *
     * @throws ConfigurationException
     * @throws IssueInstantMissingException
     */
    public void createTimedConditions(AssertionType assertion, long durationInMilis) throws ConfigurationException,
            IssueInstantMissingException {
        AssertionUtil.createTimedConditions(assertion, durationInMilis);
    }

    /**
     * 从输入流解析加密断言（EncryptedAssertion）。
     *
     * Get an encrypted assertion from the stream
     *
     * @param is 输入流
     *
     * @return
     *
     * @throws org.keycloak.saml.common.exceptions.ParsingException
     * @throws ProcessingException
     * @throws ConfigurationException
     */
    public EncryptedAssertionType getEncryptedAssertion(InputStream is) throws ParsingException, ConfigurationException,
            ProcessingException {
        if (is == null)
            throw logger.nullArgumentError("InputStream");

        Document samlDocument = DocumentUtil.getDocument(is);
        SAMLParser samlParser = SAMLParser.getInstance();
        JAXPValidationUtil.checkSchemaValidation(samlDocument);

        return (EncryptedAssertionType) samlParser.parse(samlDocument);

    }

    /**
     * 从输入流解析明文断言。
     *
     * Read an assertion from an input stream
     *
     * @param is 输入流
     *
     * @return
     *
     * @throws ParsingException
     * @throws ProcessingException
     * @throws ConfigurationException
     */
    public AssertionType getAssertionType(InputStream is) throws ParsingException, ConfigurationException, ProcessingException {
        if (is == null)
            throw logger.nullArgumentError("InputStream");
        Document samlDocument = DocumentUtil.getDocument(is);

        SAMLParser samlParser = SAMLParser.getInstance();
        JAXPValidationUtil.checkSchemaValidation(samlDocument);
        return (AssertionType) samlParser.parse(samlDocument);
    }

    /**
     * 获取最近一次解析得到的 {@code SAMLDocumentHolder}。
     *
     * Get the parsed {@code SAMLDocumentHolder}
     *
     * @return 文档持有者，可能为 null
     */
    public SAMLDocumentHolder getSamlDocumentHolder() {
        return samlDocumentHolder;
    }

    /**
     * 从输入流解析 SAML 2.0 Response。
     *
     * Read a ResponseType from an input stream
     *
     * @param is 输入流
     *
     * @return
     *
     * @throws ParsingException
     * @throws ConfigurationException
     */
    public ResponseType getResponseType(InputStream is) throws ParsingException, ConfigurationException, ProcessingException {
        if (is == null)
            throw logger.nullArgumentError("InputStream");

        Document samlResponseDocument = DocumentUtil.getDocument(is);

        SAMLParser samlParser = SAMLParser.getInstance();
        JAXPValidationUtil.checkSchemaValidation(samlResponseDocument);

        ResponseType responseType = (ResponseType) samlParser.parse(samlResponseDocument);

        samlDocumentHolder = new SAMLDocumentHolder(responseType, samlResponseDocument);
        return responseType;
    }

    /**
     * 从输入流解析任意 {@code SAML2Object}（Response/Assertion 等）。
     *
     * Read a {@code SAML2Object} from an input stream
     *
     * @param is 输入流
     *
     * @return
     *
     * @throws ParsingException
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    public SAML2Object getSAML2ObjectFromStream(InputStream is) throws ParsingException, ConfigurationException,
            ProcessingException {
        if (is == null)
            throw logger.nullArgumentError("InputStream");

        Document samlResponseDocument = DocumentUtil.getDocument(is);

        if (logger.isTraceEnabled()) {
            logger.trace("SAML Response Document: " + DocumentUtil.asString(samlResponseDocument));
        }

        SAMLParser samlParser = SAMLParser.getInstance();
        JAXPValidationUtil.checkSchemaValidation(samlResponseDocument);

        SAML2Object responseType = (SAML2Object) samlParser.parse(samlResponseDocument);

        samlDocumentHolder = new SAMLDocumentHolder(responseType, samlResponseDocument);
        return responseType;

    }

    /**
     * 从 DOM 文档解析底层 SAML2Object。
     * Get the Underlying SAML2Object from a document
     * @param samlDocument a Document containing a SAML2Object
     * @return a SAMLDocumentHolder 解析结果
     * @throws ProcessingException
     * @throws ParsingException
     */
    public static SAMLDocumentHolder getSAML2ObjectFromDocument(Document samlDocument) throws ProcessingException, ParsingException {
        SAMLParser samlParser = SAMLParser.getInstance();
        JAXPValidationUtil.checkSchemaValidation(samlDocument);
        SAML2Object responseType = (SAML2Object) samlParser.parse(samlDocument);

        return new SAMLDocumentHolder(responseType, samlDocument);
    }

    /**
     * 将 EncryptedElement 转为独立 DOM 文档。
     *
     * Convert an EncryptedElement into a Document
     *
     * @param encryptedElementType 加密元素
     *
     * @return
     *
     * @throws ConfigurationException
     */
    public Document convert(EncryptedElementType encryptedElementType) throws ConfigurationException {
        if (encryptedElementType == null)
            throw logger.nullArgumentError("encryptedElementType");
        Document doc = DocumentUtil.createDocument();
        Node importedNode = doc.importNode(encryptedElementType.getEncryptedElement(), true);
        doc.appendChild(importedNode);

        return doc;
    }

    /**
     * 将 StatusResponseType 序列化为 DOM 文档。
     *
     * Convert a SAML2 Response into a Document
     *
     * @param responseType 响应对象
     *
     * @return
     *
     * @throws ParsingException
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    public static Document convert(StatusResponseType responseType) throws ProcessingException, ConfigurationException,
            ParsingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(bos));

        if (responseType instanceof ArtifactResponseType) {
            ArtifactResponseType response = (ArtifactResponseType) responseType;
            writer.write(response);
        } else if (responseType instanceof ResponseType) {
            ResponseType response = (ResponseType) responseType;
            writer.write(response);
        } else {
            writer.write(responseType, new QName(PROTOCOL_NSURI.get(), JBossSAMLConstants.LOGOUT_RESPONSE.get(), "samlp"));
        }

        return DocumentUtil.getDocument(new ByteArrayInputStream(bos.toByteArray()));
    }

    /**
     * 将 ResponseType 序列化到输出流。
     *
     * Marshall the response type to the output stream
     *
     * @param responseType 响应对象
     * @param os 目标输出流
     *
     * @throws ProcessingException
     */
    public void marshall(ResponseType responseType, OutputStream os) throws ProcessingException {
        SAMLResponseWriter samlWriter = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(os));
        samlWriter.write(responseType);
    }

    /**
     * 将 ResponseType 序列化到 Writer。
     *
     * Marshall the ResponseType into a writer
     *
     * @param responseType 响应对象
     * @param writer 目标 Writer
     *
     * @throws ProcessingException
     */
    public void marshall(ResponseType responseType, Writer writer) throws ProcessingException {
        SAMLResponseWriter samlWriter = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(writer));
        samlWriter.write(responseType);
    }
}