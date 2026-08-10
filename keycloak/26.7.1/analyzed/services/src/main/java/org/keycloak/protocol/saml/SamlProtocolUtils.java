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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.VerificationException;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;
import org.keycloak.dom.saml.v2.SAML2Object;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.ArtifactResponseType;
import org.keycloak.dom.saml.v2.protocol.AuthnContextComparisonType;
import org.keycloak.dom.saml.v2.protocol.ExtensionsType;
import org.keycloak.dom.saml.v2.protocol.RequestAbstractType;
import org.keycloak.dom.saml.v2.protocol.RequestedAuthnContextType;
import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.dom.saml.v2.protocol.StatusResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.keys.PublicKeyLoader;
import org.keycloak.keys.PublicKeyStorageProvider;
import org.keycloak.keys.PublicKeyStorageUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.AcrUtils;
import org.keycloak.rotation.HardcodedKeyLocator;
import org.keycloak.rotation.KeyLocator;
import org.keycloak.saml.BaseSAML2BindingBuilder;
import org.keycloak.saml.SignatureAlgorithm;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.common.util.StaxUtil;
import org.keycloak.saml.processing.api.saml.v2.request.SAML2Request;
import org.keycloak.saml.processing.api.saml.v2.sig.SAML2Signature;
import org.keycloak.saml.processing.core.saml.v2.common.IDGenerator;
import org.keycloak.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.keycloak.saml.processing.core.saml.v2.util.XMLTimeUtil;
import org.keycloak.saml.processing.core.saml.v2.writers.SAMLResponseWriter;
import org.keycloak.saml.processing.core.util.KeycloakKeySamlExtensionGenerator;
import org.keycloak.saml.processing.core.util.RedirectBindingSignatureUtil;
import org.keycloak.saml.processing.web.util.RedirectBindingUtil;
import org.keycloak.utils.StringUtil;

import org.apache.xml.security.encryption.XMLCipher;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SAML 协议工具类：文档/Redirect 签名验证、加密密钥解析、Artifact 响应构建及 LoA 选择。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SamlProtocolUtils {

    private static final Logger logger = Logger.getLogger(SamlProtocolUtils.class);

    /**
     * 使用客户端配置验证 SAML 文档签名；无效时抛出 {@link VerificationException}。
     *
     * @param session Keycloak 会话
     * @param client 客户端模型
     * @param document 待验证的 SAML XML 文档
     * @throws VerificationException 签名无效
     */
    public static void verifyDocumentSignature(KeycloakSession session, ClientModel client, Document document) throws VerificationException {
        verifyDocumentSignature(document, createKeyLocatorForClient(session, new SamlClient(client), KeyUse.SIG));
    }

    /**
     * 使用 {@link KeyLocator} 提供的密钥验证 SAML 文档签名。
     *
     * @param document 待验证文档
     * @param keyLocator 公钥定位器
     * @throws VerificationException 签名无效
     */
    public static void verifyDocumentSignature(Document document, KeyLocator keyLocator) throws VerificationException {
        SAML2Signature saml2Signature = new SAML2Signature();
        try {
            if (!saml2Signature.validate(document, keyLocator)) {
                throw new VerificationException("Invalid signature on document");
            }
        } catch (ProcessingException e) {
            throw new VerificationException("Error validating signature", e);
        }
    }

    /**
     * 从客户端配置获取 SAML 加密用 RSA 公钥。
     * @param session Keycloak 会话
     * @param client 客户端模型
     * @return 加密公钥
     * @throws VerificationException 无可用加密密钥
     */
    public static PublicKey getEncryptionKey(KeycloakSession session, ClientModel client) throws VerificationException {
        return getEncryptionKey(session, new SamlClient(client));
    }

    /**
     * Returns public part of SAML encryption key from the client settings.
     * @param session
     * @param samlClient
     * @return Public key for encryption.
     * @throws VerificationException
     */
    public static PublicKey getEncryptionKey(KeycloakSession session, SamlClient samlClient) throws VerificationException {
        KeyLocator locator = createKeyLocatorForClient(session, samlClient, KeyUse.ENC);
        // get the first one that is RSA
        for (Key key : locator) {
            if (KeyType.RSA.equals(key.getAlgorithm())) {
                return (PublicKey) key;
            }
        }
        throw new VerificationException("Client does not have a public key for encryption");
    }

    /** 按客户端配置为 SAML 绑定构建器设置加密算法与密钥 */
    public static void setupEncryption(KeycloakSession session, SamlClient samlClient, BaseSAML2BindingBuilder<?> bindingBuilder) throws VerificationException {
        PublicKey publicKey = getEncryptionKey(session, samlClient);
        bindingBuilder.encrypt(publicKey);
        if (samlClient.getClientEncryptingAlgorithm() != null) {
            bindingBuilder.encryptionAlgorithm(samlClient.getClientEncryptingAlgorithm());
        }
        if (samlClient.getClientEncryptingKeyAlgorithm() != null) {
            bindingBuilder.keyEncryptionAlgorithm(samlClient.getClientEncryptingKeyAlgorithm());
        }
        if (samlClient.getClientEncryptingDigestMethod() != null &&
                (XMLCipher.RSA_OAEP.equals(samlClient.getClientEncryptingKeyAlgorithm()) ||
                XMLCipher.RSA_OAEP_11.equals(samlClient.getClientEncryptingKeyAlgorithm()))) {
            // 摘要算法仅 RSA-OAEP 可用
            bindingBuilder.keyEncryptionDigestMethod(samlClient.getClientEncryptingDigestMethod());
        }
        if (samlClient.getClientEncryptingMaskGenerationFunction() != null &&
                XMLCipher.RSA_OAEP_11.equals(samlClient.getClientEncryptingKeyAlgorithm())) {
            // MGF 仅 RSA-OAEP-11 可用
            bindingBuilder.keyEncryptionMgfAlgorithm(samlClient.getClientEncryptingMaskGenerationFunction());
        }
    }

    public static PublicKey getPublicKey(ClientModel client, String attribute) throws VerificationException {
        String certPem = client.getAttribute(attribute);
        return getPublicKey(certPem);
    }

    public static KeyLocator createKeyLocatorForClient(KeycloakSession session, ClientModel client, KeyUse use) throws VerificationException {
        return createKeyLocatorForClient(session, new SamlClient(client), use);
    }

    public static KeyLocator createKeyLocatorForClient(KeycloakSession session, SamlClient samlClient, KeyUse use) throws VerificationException {
        if (StringUtil.isNotBlank(samlClient.getMetadataDescriptorUrl()) && samlClient.isUseMetadataDescriptorUrl()) {
            // 使用元数据 URL 加载客户端公钥
            String modelKey = PublicKeyStorageUtils.getClientModelCacheKey(samlClient.getClient().getRealm().getId(), samlClient.getClient().getClientId());
            PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
            PublicKeyLoader keyLoader = new SamlMetadataPublicKeyLoader(session, samlClient.getMetadataDescriptorUrl(), false);
            return new SamlMetadataKeyLocator(modelKey, keyLoader, use, keyStorage);
        } else if (KeyUse.SIG.equals(use)) {
            // 回退到客户端配置的签名证书
            return new HardcodedKeyLocator(getPublicKey(samlClient.getClientSigningCertificate()));
        } else if (KeyUse.ENC.equals(use)) {
            return new HardcodedKeyLocator(getPublicKey(samlClient.getClientEncryptingCertificate()));
        }
        throw new VerificationException("Client does not have a public key for use " + use);
    }

    private static PublicKey getPublicKey(String certPem) throws VerificationException {
        if (certPem == null) throw new VerificationException("Client does not have a public key.");
        X509Certificate cert = null;
        try {
            cert = PemUtils.decodeCertificate(certPem);
            cert.checkValidity();
        } catch (CertificateException ex) {
            throw new VerificationException("Certificate is not valid.");
        } catch (Exception e) {
            throw new VerificationException("Could not decode cert", e);
        }
        return cert.getPublicKey();
    }

    public static void verifyRedirectSignature(SAMLDocumentHolder documentHolder, KeyLocator locator, UriInfo uriInformation, String paramKey) throws VerificationException {
        MultivaluedMap<String, String> encodedParams = uriInformation.getQueryParameters(false);
        verifyRedirectSignature(documentHolder, locator, encodedParams, paramKey);
    }

    public static void verifyRedirectSignature(SAMLDocumentHolder documentHolder, KeyLocator locator, MultivaluedMap<String, String> encodedParams, String paramKey) throws VerificationException {
        String request = encodedParams.getFirst(paramKey);
        String algorithm = encodedParams.getFirst(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY);
        String signature = encodedParams.getFirst(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY);
        String relayState = encodedParams.getFirst(GeneralConstants.RELAY_STATE);

        if (request == null) throw new VerificationException("SAM was null");
        if (algorithm == null) throw new VerificationException("SigAlg was null");
        if (signature == null) throw new VerificationException("Signature was null");

        String keyId = getMessageSigningKeyId(documentHolder.getSamlObject());

        // Shibboleth Redirect 绑定可能不签名文档
        // todo maybe a flag?

        StringBuilder rawQueryBuilder = new StringBuilder().append(paramKey).append("=").append(request);
        if (encodedParams.containsKey(GeneralConstants.RELAY_STATE)) {
            rawQueryBuilder.append("&" + GeneralConstants.RELAY_STATE + "=").append(relayState);
        }
        rawQueryBuilder.append("&" + GeneralConstants.SAML_SIG_ALG_REQUEST_KEY + "=").append(algorithm);
        String rawQuery = rawQueryBuilder.toString();

        try {
            byte[] decodedSignature = RedirectBindingUtil.urlBase64Decode(signature);

            String decodedAlgorithm = RedirectBindingUtil.urlDecode(encodedParams.getFirst(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY));
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.getFromXmlMethod(decodedAlgorithm);
            if (!RedirectBindingSignatureUtil.validateRedirectBindingSignature(signatureAlgorithm,
                    rawQuery.getBytes(StandardCharsets.UTF_8), decodedSignature, locator, keyId)) {
                throw new VerificationException("Invalid query param signature");
            }
        } catch (Exception e) {
            throw new VerificationException(e);
        }
    }

    private static String getMessageSigningKeyId(SAML2Object doc) {
        final ExtensionsType extensions;
        if (doc instanceof RequestAbstractType) {
            extensions = ((RequestAbstractType) doc).getExtensions();
        } else if (doc instanceof StatusResponseType) {
            extensions = ((StatusResponseType) doc).getExtensions();
        } else {
            return null;
        }

        if (extensions == null) {
            return null;
        }

        for (Object ext : extensions.getAny()) {
            if (! (ext instanceof Element)) {
                continue;
            }

            String res = KeycloakKeySamlExtensionGenerator.getMessageSigningKeyIdFromElement((Element) ext);

            if (res != null) {
                return res;
            }
        }

        return null;
    }

    /**
     * 将 SAML 对象包装为指定状态码的 {@link ArtifactResponseType}。
     *
     * @param samlObject 待嵌入的 SAML 对象
     * @param issuer ArtifactResponse 签发者
     * @param statusCode SAML 状态码 URI
     * @return 含 SAML 对象的 ArtifactResponse
     */
    public static ArtifactResponseType buildArtifactResponse(SAML2Object samlObject, NameIDType issuer, URI statusCode) throws ConfigurationException, ProcessingException {
        ArtifactResponseType artifactResponse = new ArtifactResponseType(IDGenerator.create("ID_"),
                XMLTimeUtil.getIssueInstant());

        // Status
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(statusCode);
        statusType.setStatusCode(statusCodeType);

        artifactResponse.setStatus(statusType);
        artifactResponse.setIssuer(issuer);
        artifactResponse.setAny(samlObject);

        return artifactResponse;
    }

    /**
     * Takes a saml object (an object that will be part of resulting ArtifactResponse), and inserts it as the body of 
     * an ArtifactResponse. The ArtifactResponse is returned as ArtifactResponseType
     * 
     * @param samlObject a Saml object
     * @param issuer issuer of the resulting ArtifactResponse, should be the same as issuer of the samlObject
     * @return An ArtifactResponse containing the saml object.
     */
    public static ArtifactResponseType buildArtifactResponse(SAML2Object samlObject, NameIDType issuer) throws ConfigurationException, ProcessingException {
        return buildArtifactResponse(samlObject, issuer, JBossSAMLURIConstants.STATUS_SUCCESS.getUri());
    }

    /**
     * Takes a saml document and inserts it as a body of ArtifactResponseType
     * @param document the document
     * @return An ArtifactResponse containing the saml document.
     */
    public static ArtifactResponseType buildArtifactResponse(Document document) throws ParsingException, ProcessingException, ConfigurationException {
        SAML2Object samlObject = SAML2Request.getSAML2ObjectFromDocument(document).getSamlObject();

        if (samlObject instanceof StatusResponseType) {
            return buildArtifactResponse(samlObject, ((StatusResponseType)samlObject).getIssuer());
        } else if (samlObject instanceof RequestAbstractType) {
            return buildArtifactResponse(samlObject, ((RequestAbstractType)samlObject).getIssuer());
        }
        
        throw new ProcessingException("SAMLObject was not StatusResponseType or LogoutRequestType");
    }

    /**
     * 将 {@link ArtifactResponseType} 序列化为 W3C {@link Document}。
     * @param responseType Artifact 响应对象
     * @return XML 文档
     * @throws ParsingException 解析失败
     * @throws ConfigurationException 配置错误
     * @throws ProcessingException 处理失败
     */
    public static Document convert(ArtifactResponseType responseType) throws ProcessingException, ConfigurationException,
            ParsingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(bos));
        writer.write(responseType);
        return DocumentUtil.getDocument(new ByteArrayInputStream(bos.toByteArray()));
    }

    private static String checkLoAExact(String current, Map<String, Integer> acrLoaMap, int minLevel) {
        // EXACT：认证上下文须与请求列表中某项完全匹配
        Integer level = acrLoaMap.get(current);
        if (level == null) {
            return null;
        }
        return level >= minLevel ? current : null;
    }

    private static String checkLoAMinimum(String current, Map<String, Integer> acrLoaMap, String minLoa, int minLevel) {
        // authentication context in the authentication statement MUST be as strong as one of the authentication contexts specified
        Integer level = acrLoaMap.get(current);
        if (level == null) {
            return null;
        }
        // check if current value is OK, if not return minLoa which is valid because is greater than current
        return (level >= minLevel) ?  current : minLoa;
    }

    private static String checkLoAMaximum(String current, Map<String, Integer> acrLoaMap, int minLevel) {
        // authentication context in the authentication statement MUST be as strong as possible without exceeding the strength of at least one of the authentication contexts specified
        Integer level = acrLoaMap.get(current);
        if (level == null) {
            return null;
        }
        // only valid if it is better than minLoa
        return level >= minLevel ? current : null;
    }

    private static String checkLoABetter(String current, Map<String, Integer> acrLoaMap, String minLoa, int minLevel) {
        // authentication context in the authentication statement MUST be stronger than any one of the authentication contexts specified
        Integer level = acrLoaMap.get(current);
        if (level == null) {
            return null;
        }
        // if minLoa is valid return minLoa
        if (minLevel > level) {
            return minLoa;
        }
        // find any level that is better than level, get the min of them
        return acrLoaMap.entrySet().stream()
                .filter(e -> e.getValue() > level)
                .min((Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static String checkLoa(AuthnContextComparisonType comparison, String current, Map<String, Integer> acrLoaMap, String minLoa, int minLevel) {
        if (comparison == null) {
            comparison = AuthnContextComparisonType.EXACT;
        }
        return switch (comparison) {
            case EXACT -> checkLoAExact(current, acrLoaMap, minLevel);
            case MINIMUM -> checkLoAMinimum(current, acrLoaMap, minLoa, minLevel);
            case MAXIMUM -> checkLoAMaximum(current, acrLoaMap, minLevel);
            case BETTER -> checkLoABetter(current, acrLoaMap, minLoa, minLevel);
        };
    }

    /**
     * 按 RequestedAuthnContext 比较类型与客户端最低 ACR 选择认证上下文。
     * @param client 客户端模型
     * @param requestedAuthnContext SP 请求的认证上下文
     * @param acrLoaMap ACR → LoA 映射
     * @return 选中的 AuthnContextClassRef，无匹配则 null
     */
        String minLoa = AcrUtils.getMinimumAcrValue(client);
        if (minLoa != null && acrLoaMap.get(minLoa) == null) {
            logger.warnf("Invalid value '%s' for option '%s' in client '%s' in realm '%s', no minimum value used",
                        minLoa, Constants.MINIMUM_ACR_VALUE, client.getClientId(), client.getRealm().getName());
        }
        Integer minLevel = minLoa != null ? acrLoaMap.get(minLoa) : null;
        return requestedAuthnContext.getAuthnContextClassRef().stream()
                .map(current -> checkLoa(requestedAuthnContext.getComparison(), current, acrLoaMap,
                        minLoa, minLevel != null ? minLevel : Integer.MIN_VALUE))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
