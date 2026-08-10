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

package org.keycloak.protocol.saml.profile.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;

import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.saml.processing.core.saml.v2.util.DocumentUtil;
import org.keycloak.saml.processing.web.util.PostBindingUtil;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * SAML SOAP 工具类：构建/解析 SOAP 消息、提取 SAML Body、编码 HTTP POST 绑定及发起 SOAP 调用。
 * <p>供 ECP Profile 与 SOAP 绑定端点复用。</p>
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class Soap {

    /** 创建 SOAP Fault 构建器 */
    public static SoapFaultBuilder createFault() {
        return new SoapFaultBuilder();
    }

    /** 创建 SOAP 消息构建器 */
    public static SoapMessageBuilder createMessage() {
        return new SoapMessageBuilder();
    }

    /**
     * 将 SOAP Body 中的 SAML 文档按 HTTP POST 绑定规范 Base64 编码。
     * @param document 含 SAML 消息的 SOAP Body 文档
     * @return POST 绑定编码字符串
     */
    public static String toSamlHttpPostMessage(Document document) {
        try {
            return PostBindingUtil.base64Encode(DocumentUtil.asString(document));
        } catch (Exception e) {
            throw new RuntimeException("Error encoding SOAP document to String.", e);
        }
    }

    /**
     * 从输入流解析 SOAP 消息并提取 Body 中的 SAML 文档。
     * @param inputStream SOAP 消息输入流
     * @return Body 内 SAML 内容的 W3C Document
     */
    public static Document extractSoapMessage(InputStream inputStream) {
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage(null, inputStream);
            return extractSoapMessage(soapMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error creating fault message.", e);
        }
    }

    /**
     * 从 {@link SOAPMessage} 提取 Body 首个元素为 SAML Document。
     * @param soapMessage 已解析的 SOAP 消息
     * @return Body 内 SAML 文档
     */
    public static Document extractSoapMessage(SOAPMessage soapMessage) {
        try {
            SOAPBody soapBody = soapMessage.getSOAPBody();
            Node authnRequestNode = getFirstChild(soapBody);
            Document document = DocumentUtil.createDocument();
            document.appendChild(document.importNode(authnRequestNode, true));
            return document;
        } catch (Exception e) {
            throw new RuntimeException("Error creating fault message.", e);
        }
    }

    /**
     * 获取父节点下第一个 XML 元素子节点（跳过空白文本节点）。
     * @param parent 父 DOM 节点
     * @return 首个 Element 子节点，无则 null
     */
    public static Node getFirstChild(Node parent) {
        Node n = parent.getFirstChild();
        while (n != null && !(n instanceof Element)) {
            n = n.getNextSibling();
        }
        if (n == null) return null;
        return n;
    }

    /** SOAP 消息构建器：组装 Envelope、Header、Body 并输出 JAX-RS Response 或 HTTP POST */
    public static class SoapMessageBuilder {
        private final SOAPMessage message;
        private final SOAPBody body;
        private final SOAPEnvelope envelope;

        private SoapMessageBuilder() {
            try {
                this.message = MessageFactory.newInstance().createMessage();
                this.envelope = message.getSOAPPart().getEnvelope();
                this.body = message.getSOAPBody();
            } catch (Exception e) {
                throw new RuntimeException("Error creating fault message.", e);
            }
        }

        /** 将 W3C Document 追加到 SOAP Body */
        public SoapMessageBuilder addToBody(Document document) {
            try {
                this.body.addDocument(document);
            } catch (SOAPException e) {
                throw new RuntimeException("Could not add document to SOAP body.", e);
            }
            return this;
        }

        /** 在 Envelope 上声明 XML 命名空间 */
        public SoapMessageBuilder addNamespace(String prefix, String ns) {
            try {
                envelope.addNamespaceDeclaration(prefix, ns);
            } catch (SOAPException e) {
                throw new RuntimeException("Could not add namespace to SOAP Envelope.", e);
            }
            return this;
        }

        /** 添加 SOAP Header 元素（如 ECP 扩展头） */
        public SOAPHeaderElement addHeader(String name, String prefix) {
            try {
                return this.envelope.getHeader().addHeaderElement(envelope.createQName(name, prefix));
            } catch (SOAPException e) {
                throw new RuntimeException("Could not add SOAP Header.", e);
            }
        }

        public SoapMessageBuilder addMimeHeader(String name, String value) {
            this.message.getMimeHeaders().addHeader(name, value);
            return this;
        }

        public Name createName(String name) {
            try {
                return this.envelope.createName(name);
            } catch (SOAPException e) {
                throw new RuntimeException("Could not create Name.", e);
            }
        }

        public byte[] getBytes() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                this.message.writeTo(outputStream);
            } catch (Exception e) {
                throw new RuntimeException("Error while building SOAP Fault.", e);
            }
            return outputStream.toByteArray();
        }

        public Response build() {
            return build(Status.OK);
        }

        /**
         * 构建指定 HTTP 状态的 JAX-RS Response（Content-Type: text/xml）。
         * @param status HTTP 响应状态
         * @return 含 SOAP 字节的 Response
         */
        Response build(Status status) {
            return Response.status(status).entity(getBytes()).type(MediaType.TEXT_XML_TYPE).build();
        }

        /**
         * 构建 Apache HttpClient HttpPost（测试用）。
         * @param uri SOAP 端点 URI
         */
        public HttpPost buildHttpPost(URI uri) {
            HttpPost post = new HttpPost(uri);
            post.setEntity(new ByteArrayEntity(getBytes(), ContentType.TEXT_XML));
            return post;
        }

        /**
         * Performs a synchronous call, sending the current message to the given url
         * @param url a SOAP endpoint url
         * @return the SOAPMessage returned by the contacted SOAP server
         * @throws SOAPException Raised if there's a problem performing the SOAP call
         * @deprecated Use {@link #call(String,KeycloakSession)} to use SimpleHttp configuration
         */
        @Deprecated
        public SOAPMessage call(String url) throws SOAPException {
            SOAPMessage response;
            SOAPConnection soapConnection = null;
            try {
                SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
                soapConnection = soapConnectionFactory.createConnection();
                response = soapConnection.call(message, url);
            } finally {
                if (soapConnection != null) {
                    soapConnection.close();
                }
            }
            return response;
        }

        /**
         * Performs a synchronous call, sending the current message to the given url.
         * SimpleHttp is retrieved using the session parameter.
         * @param url The SOAP endpoint URL to connect
         * @param session The session to use to locate the SimpleHttp sender
         * @return the SOAPMessage returned by the contacted SOAP server
         * @throws SOAPException Raised if there's a problem performing the SOAP call
         */
        public SOAPMessage call(String url, KeycloakSession session) throws SOAPException {
            // 参考 Metro SAAJ HttpSOAPConnection 实现
            // 保存消息变更以写入 Content-Type 与 Content-Length
            if (message.saveRequired()) {
                message.saveChanges();
            }
            // 使用 session 中的 SimpleHttp 发送请求
            SimpleHttpRequest simpleHttp = SimpleHttp.create(session).doPost(url);
            // 将 MIME 头映射为 HTTP 头（Content-Type/Length 单独处理）
            Iterator<MimeHeader> reqHeaders = message.getMimeHeaders().getAllHeaders();
            ContentType contentType = null;
            int length = -1;
            boolean hasCacheControl = false;
            while (reqHeaders.hasNext()) {
                MimeHeader mimeHeader = reqHeaders.next();
                if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(mimeHeader.getName())) {
                    contentType = ContentType.parse(mimeHeader.getValue());
                } else if (HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(mimeHeader.getName())) {
                    length = Integer.parseInt(mimeHeader.getValue());
                } else {
                    if (HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(mimeHeader.getName())) {
                        hasCacheControl = true;
                    }
                    String currentValue = simpleHttp.getHeader(mimeHeader.getName());
                    simpleHttp.header(mimeHeader.getName(), currentValue == null
                            ? mimeHeader.getValue() : currentValue + "," + mimeHeader.getValue());
                }
            }
            if (!hasCacheControl) {
                // 未指定 Cache-Control 时默认 no-cache, no-store
                simpleHttp.header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
            }
            // 序列化 SOAP 消息并 POST 到目标 URL
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                message.writeTo(out);
                simpleHttp.entity(new ByteArrayEntity(out.toByteArray(), 0, length, contentType));
                try (SimpleHttpResponse res = simpleHttp.asResponse()) {
                    // 500/400/200 均按 SOAP 响应解析
                    if (res.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                            || res.getStatus() == HttpStatus.SC_BAD_REQUEST
                            || res.getStatus() == HttpStatus.SC_OK) {
                        MimeHeaders resHeaders = new MimeHeaders();
                        Header[] headers = res.getAllHeaders();
                        for (Header header : headers) {
                            resHeaders.addHeader(header.getName(), header.getValue());
                        }
                        String responseString = res.asString();
                        if (responseString == null || responseString.isEmpty()) {
                            // 空响应体时返回 null
                            return null;
                        }
                        return MessageFactory.newInstance().createMessage(resHeaders, new ByteArrayInputStream(responseString.getBytes(res.getContentTypeCharset())));
                    } else {
                        throw new SOAPException("Bad response (" + res.getStatus() + ") :" + res.asString());
                    }
                }
            } catch (IOException e) {
                throw new SOAPException(e);
            }
        }

        public SOAPMessage getMessage() {
            return this.message;
        }
    }

    /** SOAP Fault 构建器：设置 fault code/reason/detail 并返回 500 Response */
    public static class SoapFaultBuilder {

        private final SOAPFault fault;
        private final SoapMessageBuilder messageBuilder;

        private SoapFaultBuilder() {
            this.messageBuilder = createMessage();
            try {
                this.fault = messageBuilder.getMessage().getSOAPBody().addFault();
            } catch (SOAPException e) {
                throw new RuntimeException("Could not create SOAP Fault.", e);
            }
        }

        /** 设置 Fault detail 文本 */
        public SoapFaultBuilder detail(String detail) {
            try {
                this.fault.addDetail().setValue(detail);
            } catch (SOAPException e) {
                throw new RuntimeException("Error creating fault message.", e);
            }
            return this;
        }

        /** 设置 Fault 原因字符串 */
        public SoapFaultBuilder reason(String reason) {
            try {
                this.fault.setFaultString(reason);
            } catch (SOAPException e) {
                throw new RuntimeException("Error creating fault message.", e);
            }
            return this;
        }

        /** 设置 Fault 代码 */
        public SoapFaultBuilder code(String code) {
            try {
                this.fault.setFaultCode(code);
            } catch (SOAPException e) {
                throw new RuntimeException("Error creating fault message.", e);
            }
            return this;
        }

        public Response build() {
            return this.messageBuilder.build(Status.INTERNAL_SERVER_ERROR);
        }
    }
}
