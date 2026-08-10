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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.keycloak.common.util.StreamUtil;
import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.processing.api.saml.v2.request.SAML2Request;
import org.keycloak.saml.processing.api.saml.v2.response.SAML2Response;
import org.keycloak.saml.processing.api.util.DeflateUtil;
import org.keycloak.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.keycloak.saml.processing.web.util.PostBindingUtil;
import org.keycloak.saml.processing.web.util.RedirectBindingUtil;

import org.jboss.logging.Logger;

/**
 * SAML 请求/响应消息解析工具，支持 HTTP-Redirect 与 HTTP-POST 绑定。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SAMLRequestParser {
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    /** JBoss 日志记录器，用于调试输出原始 SAML 报文。 */
    protected static Logger log = Logger.getLogger(SAMLRequestParser.class);

    /**
     * 解析 Redirect 绑定的 SAML 请求（使用默认最大解压尺寸）。
     *
     * @param samlMessage Base64+Deflate 编码的 SAML 消息
     * @return 解析结果；失败时返回 {@code null}
     */
    public static SAMLDocumentHolder parseRequestRedirectBinding(String samlMessage) {
        return parseRequestRedirectBinding(samlMessage, DeflateUtil.DEFAULT_MAX_INFLATING_SIZE);
    }

    /**
     * 解析 Redirect 绑定的 SAML 请求。
     *
     * @param samlMessage Base64+Deflate 编码的 SAML 消息
     * @param maxInflatingSize 解压上限（字节）
     * @return 解析结果；失败时返回 {@code null}
     */
    public static SAMLDocumentHolder parseRequestRedirectBinding(String samlMessage, long maxInflatingSize) {
        try (InputStream is = RedirectBindingUtil.base64DeflateDecode(samlMessage, maxInflatingSize)) {
            if (log.isDebugEnabled()) {
                String message = StreamUtil.readString(is, GeneralConstants.SAML_CHARSET);
                log.debug("SAML Redirect Binding");
                log.debug(message);
                return SAML2Request.getSAML2ObjectFromStream(new ByteArrayInputStream(message.getBytes(GeneralConstants.SAML_CHARSET)));
            }
            return SAML2Request.getSAML2ObjectFromStream(is);
        } catch (Exception e) {
            logger.samlBase64DecodingError(e);
        }
        return null;

    }

    /**
     * 解析 POST 绑定的 SAML 请求。
     *
     * @param samlMessage Base64 编码的 SAML 消息
     * @return 解析结果；失败时返回 {@code null}
     */
    public static SAMLDocumentHolder parseRequestPostBinding(String samlMessage) {
        InputStream is;
        byte[] samlBytes;
        try {
            samlBytes = PostBindingUtil.base64Decode(samlMessage);
        } catch (IllegalArgumentException e) {
            logger.samlBase64DecodingError(e);
            return null;
        }
        if (log.isDebugEnabled()) {
            String str = new String(samlBytes, GeneralConstants.SAML_CHARSET);
            log.debug("SAML POST Binding");
            log.debug(str);
        }
        is = new ByteArrayInputStream(samlBytes);
        try {
            return SAML2Request.getSAML2ObjectFromStream(is);
        } catch (Exception e) {
            logger.samlBase64DecodingError(e);
        }
        return null;
    }

    /**
     * 解析 POST 绑定的 SAML 响应。
     *
     * @param samlMessage Base64 编码的 SAML 响应
     * @return 解析结果；失败时返回 {@code null}
     */
    public static SAMLDocumentHolder parseResponsePostBinding(String samlMessage) {
        byte[] samlBytes;
        try {
            samlBytes = PostBindingUtil.base64Decode(samlMessage);
        } catch (IllegalArgumentException e) {
            logger.samlBase64DecodingError(e);
            return null;
        }
        log.debug("SAML POST Binding");
        return parseResponseDocument(samlBytes);
    }

    /**
     * 从原始字节解析 SAML 响应文档。
     *
     * @param samlBytes SAML XML 字节
     * @return 解析结果；失败时返回 {@code null}
     */
    public static SAMLDocumentHolder parseResponseDocument(byte[] samlBytes) {
        if (log.isDebugEnabled()) {
            String str = new String(samlBytes, GeneralConstants.SAML_CHARSET);
            log.debug(str);
        }
        InputStream is = new ByteArrayInputStream(samlBytes);
        SAML2Response response = new SAML2Response();
        try {
            response.getSAML2ObjectFromStream(is);
            return response.getSamlDocumentHolder();
        } catch (Exception e) {
            logger.samlBase64DecodingError(e);
        }
        return null;
    }

    /**
     * 解析 Redirect 绑定的 SAML 响应（使用默认最大解压尺寸）。
     *
     * @param samlMessage Base64+Deflate 编码的 SAML 响应
     * @return 解析结果；失败时返回 {@code null}
     */
    public static SAMLDocumentHolder parseResponseRedirectBinding(String samlMessage) {
        return parseResponseRedirectBinding(samlMessage, DeflateUtil.DEFAULT_MAX_INFLATING_SIZE);
    }

    /**
     * 解析 Redirect 绑定的 SAML 响应。
     *
     * @param samlMessage Base64+Deflate 编码的 SAML 响应
     * @param maxInflatingSize 解压上限（字节）
     * @return 解析结果；失败时返回 {@code null}
     */
    public static SAMLDocumentHolder parseResponseRedirectBinding(String samlMessage, long maxInflatingSize) {
        try (InputStream is = RedirectBindingUtil.base64DeflateDecode(samlMessage, maxInflatingSize)) {
            if (log.isDebugEnabled()) {
                String message = StreamUtil.readString(is, GeneralConstants.SAML_CHARSET);
                log.debug("SAML Redirect Binding");
                log.debug(message);
                SAML2Response response = new SAML2Response();
                response.getSAML2ObjectFromStream(new ByteArrayInputStream(message.getBytes(GeneralConstants.SAML_CHARSET)));
                return response.getSamlDocumentHolder();
            }
            SAML2Response response = new SAML2Response();
            response.getSAML2ObjectFromStream(is);
            return response.getSamlDocumentHolder();
        } catch (Exception e) {
            logger.samlBase64DecodingError(e);
        }
        return null;

    }


}
