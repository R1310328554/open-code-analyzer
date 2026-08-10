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
package org.keycloak.saml.processing.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.common.util.SecurityActions;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.common.util.SystemPropertiesUtil;

import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

import static org.keycloak.saml.common.util.DocumentUtil.feature_disallow_doctype_decl;
import static org.keycloak.saml.common.util.DocumentUtil.feature_external_general_entities;
import static org.keycloak.saml.common.util.DocumentUtil.feature_external_parameter_entities;

/**
 * JAXP Schema 校验工具类。
 * <p>提供 SAML 文档的 Schema 校验及 Validator 单例管理。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 30, 2011
 */
public class JAXPValidationUtil {

    /** 日志记录器。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /** 缓存的 Validator 实例。 */
    protected static Validator validator;

    /** 缓存的 SchemaFactory 实例。 */
    protected static SchemaFactory schemaFactory;

    /** 校验输入流中的 SAML XML 文档。 */
    public static void validate(InputStream stream) throws SAXException, IOException {
        try {
            validator().validate(new StAXSource(StaxParserUtil.getXMLEventReader(stream)));
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * 根据系统属性 {@code picketlink.schema.validate} 决定是否执行 Schema 校验。
     *
     * @param samlDocument SAML DOM 节点
     *
     * @throws org.keycloak.saml.common.exceptions.ProcessingException 校验失败时抛出
     */
    public static void checkSchemaValidation(Node samlDocument) throws ProcessingException {
        if (SecurityActions.getSystemProperty("picketlink.schema.validate", "false").equalsIgnoreCase("true")) {
            try {
                JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(samlDocument));
            } catch (Exception e) {
                throw logger.processingError(e);
            }
        }
    }

    /** 获取或初始化单例 Validator，并禁用外部实体访问。 */
    public static Validator validator() throws SAXException, IOException {
        SystemPropertiesUtil.ensure();

        if (validator == null) {
            Schema schema = getSchema();
            if (schema == null)
                throw logger.nullValueError("schema");

            validator = schema.newValidator();
            // 不可简化为 && 短路：首个 setProperty 失败时仍需尝试后续属性
            boolean successful1 = setProperty(validator, FixXMLConstants.ACCESS_EXTERNAL_DTD, "");
            successful1 &= setProperty(validator, FixXMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            boolean successful2 = setFeature(validator, feature_disallow_doctype_decl, true);
            successful2 &= setFeature(validator, feature_external_general_entities, false);
            successful2 &= setFeature(validator, feature_external_parameter_entities, false);
            if (! successful1 && ! successful2) {
                logger.warn("Cannot disable external access in XML validator");
            }
            validator.setErrorHandler(new CustomErrorHandler());
        }
        return validator;
    }

    private static boolean setProperty(Validator v, String property, String value) {
        try {
            v.setProperty(property, value);
        } catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
            logger.debug("Cannot set " + property);
            return false;
        }
        return true;
    }

    private static boolean setFeature(Validator v, String feature, boolean value) {
        try {
            v.setFeature(feature, value);
        } catch (SAXNotRecognizedException | SAXNotSupportedException ex) {
            logger.debug("Cannot set " + feature);
            return false;
        }
        return true;
    }

    private static Schema getSchema() throws IOException {
        boolean tccl_jaxp = SystemPropertiesUtil.getSystemProperty(GeneralConstants.TCCL_JAXP, "false").equalsIgnoreCase("true");

        ClassLoader prevTCCL = SecurityActions.getTCCL();
        try {
            if (tccl_jaxp) {
                SecurityActions.setTCCL(JAXPValidationUtil.class.getClassLoader());
            }
            schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            schemaFactory.setResourceResolver(new IDFedLSInputResolver());
            schemaFactory.setErrorHandler(new CustomErrorHandler());
        } finally {
            if (tccl_jaxp) {
                SecurityActions.setTCCL(prevTCCL);
            }
        }
        Schema schemaGrammar = null;
        try {
            schemaGrammar = schemaFactory.newSchema(sources());
        } catch (SAXException e) {
            logger.xmlCouldNotGetSchema(e);
        }
        return schemaGrammar;
    }

    private static Source[] sources() throws IOException {
        List<String> schemas = SchemaManagerUtil.getSchemas();

        Source[] sourceArr = new Source[schemas.size()];

        int i = 0;
        for (String schema : schemas) {
            URL url = SecurityActions.loadResource(JAXPValidationUtil.class, schema);
            if (url == null)
                throw logger.nullValueError("schema url:" + schema);
            sourceArr[i++] = new StreamSource(url.openStream());
        }
        return sourceArr;
    }

    /** 自定义 SAX 错误处理器。 */
    private static class CustomErrorHandler implements ErrorHandler {

        public void error(SAXParseException ex) throws SAXException {
            logException(ex);
            if (!ex.getMessage().contains("null")) {
                throw ex;
            }
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            logException(ex);
            throw ex;
        }

        public void warning(SAXParseException ex) throws SAXException {
            logException(ex);
        }

        private void logException(SAXParseException sax) {
            StringBuilder builder = new StringBuilder();

            if (logger.isTraceEnabled()) {
                builder.append("[line:").append(sax.getLineNumber()).append(",").append("::col=").append(sax.getColumnNumber())
                        .append("]");
                builder.append("[publicID:").append(sax.getPublicId()).append(",systemId=").append(sax.getSystemId())
                        .append("]");
                builder.append(":").append(sax.getLocalizedMessage());
                logger.trace(builder.toString());
            }
        }
    }

    ;
}