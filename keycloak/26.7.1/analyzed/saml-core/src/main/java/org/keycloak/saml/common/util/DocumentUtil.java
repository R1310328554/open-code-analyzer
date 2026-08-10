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
package org.keycloak.saml.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.keycloak.saml.common.PicketLinkLogger;
import org.keycloak.saml.common.PicketLinkLoggerFactory;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * DOM 文档创建、解析与序列化工具类。
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 14, 2009
 */
public class DocumentUtil {

    /** 日志实例。 */
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /** 缓存的 {@link DocumentBuilderFactory}，线程安全延迟初始化。 */
    private static volatile DocumentBuilderFactory documentBuilderFactory;

    /** SAX 特性：是否解析外部一般实体。 */
    public static final String feature_external_general_entities = "http://xml.org/sax/features/external-general-entities";
    /** SAX 特性：是否解析外部参数实体。 */
    public static final String feature_external_parameter_entities = "http://xml.org/sax/features/external-parameter-entities";
    /** Apache 特性：是否禁止 DOCTYPE 声明（防 XXE）。 */
    public static final String feature_disallow_doctype_decl = "http://apache.org/xml/features/disallow-doctype-decl";

    /**
     * 创建空白 DOM 文档。
     *
     * @return 新文档实例
     *
     * @throws ParserConfigurationException
     */
    public static Document createDocument() throws ConfigurationException {
        DocumentBuilder builder;
        try {
            builder = getDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException(e);
        }
        return builder.newDocument();
    }

    /**
     * 创建带默认命名空间根元素的文档（形如 &lt;element xmlns="..."/&gt;）。
     *
     * @param baseNamespace 根元素默认命名空间 URI
     *
     * @return 含指定根元素的文档
     *
     * @throws org.keycloak.saml.common.exceptions.ProcessingException
     */
    public static Document createDocumentWithBaseNamespace(String baseNamespace, String localPart) throws ProcessingException {
        try {
            DocumentBuilder builder = getDocumentBuilder();
            return builder.getDOMImplementation().createDocument(baseNamespace, localPart, null);
        } catch (DOMException e) {
            throw logger.processingError(e);
        } catch (ParserConfigurationException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * 从字符串解析 DOM 文档。
     *
     * @param docString XML 文本
     *
     * @return 解析后的文档
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Document getDocument(String docString) throws ConfigurationException, ParsingException, ProcessingException {
        return getDocument(new StringReader(docString));
    }

    /**
     * Parse a document from a reader
     *
     * @param reader
     *
     * @return
     *
     * @throws ParsingException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document getDocument(Reader reader) throws ConfigurationException, ProcessingException, ParsingException {
        try {
            DocumentBuilder builder = getDocumentBuilder();
            return builder.parse(new InputSource(reader));
        } catch (ParserConfigurationException e) {
            throw logger.configurationError(e);
        } catch (SAXException e) {
            throw logger.parserError(e);
        } catch (IOException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Get Document from a file
     *
     * @param file
     *
     * @return
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document getDocument(File file) throws ConfigurationException, ProcessingException, ParsingException {
        try {
            DocumentBuilder builder = getDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException e) {
            throw logger.configurationError(e);
        } catch (SAXException e) {
            throw logger.parserError(e);
        } catch (IOException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * Get Document from an inputstream
     *
     * @param is
     *
     * @return
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document getDocument(InputStream is) throws ConfigurationException, ProcessingException, ParsingException {
        try {
            DocumentBuilder builder = getDocumentBuilder();
            return builder.parse(is);
        } catch (ParserConfigurationException e) {
            throw logger.configurationError(e);
        } catch (SAXException e) {
            throw logger.parserError(e);
        } catch (IOException e) {
            throw logger.processingError(e);
        }
    }

    /**
     * 将 DOM 文档序列化为字符串。
     *
     * @param signedDoc 待序列化文档
     *
     * @return XML 字符串
     *
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static String getDocumentAsString(Document signedDoc) throws ProcessingException, ConfigurationException {
        return getNodeAsString(signedDoc);
    }

    /**
     * Marshall a DOM Node into a String
     *
     * @param node
     *
     * @return
     *
     * @throws ProcessingException
     * @throws ConfigurationException
     */
    public static String getNodeAsString(Node node) throws ProcessingException, ConfigurationException {
        Source source = new DOMSource(node);
        StringWriter sw = new StringWriter();

        Result streamResult = new StreamResult(sw);
        // Write the DOM document to the stream
        Transformer xformer = TransformerUtil.getTransformer();
        try {
            xformer.transform(source, streamResult);
        } catch (TransformerException e) {
            throw logger.processingError(e);
        }

        return sw.toString();
    }

    /**
     * <p>按 {@link QName} 在文档中查找元素。</p>
     * <p>优先按命名空间匹配；失败则忽略命名空间仅匹配 localPart。</p>
     *
     * @param doc 文档根
     * @param elementQName 目标元素 QName
     *
     * @return 匹配的首个元素，未找到返回 null
     */
    public static Element getElement(Document doc, QName elementQName) {
        NodeList nl = doc.getElementsByTagNameNS(elementQName.getNamespaceURI(), elementQName.getLocalPart());
        if (nl.getLength() == 0) {
            nl = doc.getElementsByTagNameNS("*", elementQName.getLocalPart());
            if (nl.getLength() == 0)
                nl = doc.getElementsByTagName(elementQName.getPrefix() + ":" + elementQName.getLocalPart());
            if (nl.getLength() == 0)
                return null;
        }
        return (Element) nl.item(0);
    }

    /**
     * <p> Get an child element from the parent element given its {@link QName} </p> <p> First an attempt to get the
     * element based on its namespace is made, failing which an element with the localpart ignoring any namespace is
     * returned. </p>
     *
     * @param doc
     * @param elementQName
     *
     * @return
     */
    public static Element getChildElement(Element doc, QName elementQName) {
        NodeList nl = doc.getElementsByTagNameNS(elementQName.getNamespaceURI(), elementQName.getLocalPart());
        if (nl.getLength() == 0) {
            nl = doc.getElementsByTagNameNS("*", elementQName.getLocalPart());
            if (nl.getLength() == 0)
                nl = doc.getElementsByTagName(elementQName.getPrefix() + ":" + elementQName.getLocalPart());
            if (nl.getLength() == 0)
                return null;
        }
        return (Element) nl.item(0);
    }

    /**
     * Stream a DOM Node as an input stream
     *
     * @param node
     *
     * @return
     *
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    public static InputStream getNodeAsStream(Node node) throws ConfigurationException, ProcessingException {
        return getSourceAsStream(new DOMSource(node));
    }

    /**
     * Get the {@link Source} as an {@link InputStream}
     *
     * @param source
     *
     * @return
     *
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    public static InputStream getSourceAsStream(Source source) throws ConfigurationException, ProcessingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result streamResult = new StreamResult(baos);
        // Write the DOM document to the stream
        Transformer transformer = TransformerUtil.getTransformer();

        if (DOMSource.class.isInstance(source)) {
            Node node = ((DOMSource) source).getNode();
            if (Document.class.isInstance(node)) {
                String xmlEncoding = ((Document) node).getXmlEncoding();
                if (xmlEncoding != null) {
                    transformer.setOutputProperty(OutputKeys.ENCODING, xmlEncoding);
                    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                }
            }
        }

        try {
            transformer.transform(source, streamResult);
        } catch (TransformerException e) {
            throw logger.processingError(e);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Get a {@link Source} given a {@link Document}
     *
     * @param doc
     *
     * @return
     */
    public static Source getXMLSource(Document doc) {
        return new DOMSource(doc);
    }

    /**
     * 将文档转为字符串，异常时静默返回 null。
     *
     * @param doc DOM 文档
     *
     * @return XML 字符串或 null
     */
    public static String asString(Document doc) {
        String str = null;

        try {
            str = getDocumentAsString(doc);
        } catch (Exception ignore) {
        }
        return str;
    }

    private static void visit(Node node, int level) {
        // Visit each child
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            // Get child node
            Node childNode = list.item(i);

            logger.trace("Node=" + childNode.getNamespaceURI() + "::" + childNode.getLocalName());

            // Visit child node
            visit(childNode, level + 1);
        }
    }

    private static final ThreadLocal<DocumentBuilder> XML_DOCUMENT_BUILDER = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            DocumentBuilderFactory factory = getDocumentBuilderFactory();
            try {
                return factory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new RuntimeException(ex);
            }
        }

    };

    /** 获取线程本地 {@link DocumentBuilder} 并重置后返回。 */
    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder res = XML_DOCUMENT_BUILDER.get();
        res.reset();
        return res;
    }

    /**
     * <p>创建启用命名空间且禁用外部实体的 {@link DocumentBuilderFactory}，实例跨线程缓存共享。</p>
     *
     * @return 工厂实例
     */
    private static DocumentBuilderFactory getDocumentBuilderFactory() {
        if (documentBuilderFactory == null) {
            synchronized(DocumentUtil.class) {
                if (documentBuilderFactory == null) {
                    ClassLoader prevTCCL = SecurityActions.getTCCL();
                    boolean tccl_jaxp = SystemPropertiesUtil.getSystemProperty(GeneralConstants.TCCL_JAXP, "false")
                            .equalsIgnoreCase("true");
                    try {
                        if (tccl_jaxp) {
                            SecurityActions.setTCCL(DocumentUtil.class.getClassLoader());
                        }
                        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                        documentBuilderFactory.setNamespaceAware(true);
                        documentBuilderFactory.setXIncludeAware(false);
                        String feature = "";
                        try {
                            feature = feature_disallow_doctype_decl;
                            documentBuilderFactory.setFeature(feature, true);
                            feature = feature_external_general_entities;
                            documentBuilderFactory.setFeature(feature, false);
                            feature = feature_external_parameter_entities;
                            documentBuilderFactory.setFeature(feature, false);
                        } catch (ParserConfigurationException e) {
                            throw logger.parserFeatureNotSupported(feature);
                        }
                        // 工厂完全配置后再写入静态字段
                        DocumentUtil.documentBuilderFactory = documentBuilderFactory;
                    } finally {
                        if (tccl_jaxp) {
                            SecurityActions.setTCCL(prevTCCL);
                        }
                    }
                }
            }
        }

        return documentBuilderFactory;
    }

    /**
     * 在父元素下查找匹配命名空间与 localName 的直接子 {@linkplain Element}。
     *
     * @param parent 父元素
     * @param targetNamespace 目标命名空间 URI
     * @param targetLocalName 目标 localName
     * @return 满足条件的直接子元素，否则 null
     */
    
    public static Element getDirectChildElement(Element parent, String targetNamespace, String targetLocalName) {
        Node child = parent.getFirstChild();
        
        while(child != null) {
            if(child instanceof Element) {
                Element childElement = (Element)child;
                
                String ns = childElement.getNamespaceURI();
                String localName = childElement.getLocalName();
                
                if(Objects.equals(targetNamespace, ns) && Objects.equals(targetLocalName, localName)) {
                    return childElement;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }
}
