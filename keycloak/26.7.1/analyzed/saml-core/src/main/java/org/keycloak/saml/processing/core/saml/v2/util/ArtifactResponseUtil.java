/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.saml.processing.core.saml.v2.util;

import java.io.StringWriter;
import java.util.Optional;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.keycloak.saml.common.constants.JBossSAMLConstants;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.util.TransformerUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 操作 SAML ArtifactResponse 及其内嵌 Response 元素的工具类。
 * <p>从 Artifact 响应文档中提取并序列化内嵌的 SAML Response。</p>
 * @author Thibault Morin (https://tmorin.github.io)
 */
public final class ArtifactResponseUtil {

    /** 工具类私有构造器，禁止实例化。 */
    private ArtifactResponseUtil() {
    }

    /**
     * 将文档中的内嵌 Response 元素转换为 XML 字符串。
     * <p>
     * Response 须位于命名空间 {@code urn:oasis:names:tc:SAML:2.0:protocol} 且本地名为 {@code Response}。
     *
     * @param document 含 ArtifactResponse 的 DOM 文档
     * @return 内嵌 Response 的 XML 字符串，未找到时为空
     */
    public static Optional<String> convertResponseToString(Document document) {
        return extractResponseElement(document).map(ArtifactResponseUtil::nodeToString);
    }

    /**
     * 将 DOM 节点序列化为 XML 字符串。
     *
     * @param node 待序列化的节点
     * @return XML 字符串
     */
    static String nodeToString(Node node) {
        try {
            final StringWriter writer = new StringWriter();
            TransformerUtil.getTransformer().transform(new DOMSource(node), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (ConfigurationException | TransformerException e) {
            throw new IllegalStateException("Error converting node to string", e);
        }
    }

    /**
     * 从 ArtifactResponse 文档中提取内嵌的 Response 元素。
     *
     * @param document ArtifactResponse DOM 文档
     * @return Response 元素，未找到或格式不符时为空
     */
    static Optional<Element> extractResponseElement(Document document) {
        // 从 ArtifactResponse 中提取内嵌的 Response 元素
        final NodeList responseNodeList = document.getElementsByTagNameNS(
                JBossSAMLConstants.RESPONSE__PROTOCOL.getNsUri().get(),
                JBossSAMLConstants.RESPONSE__PROTOCOL.get()
        );

        // 未找到唯一内嵌 Response 时提前返回
        if (responseNodeList.getLength() != 1) {
            return Optional.empty();
        }

        // 获取内嵌 Response 节点以便后续序列化
        final Node responseNode = responseNodeList.item(0);

        // 节点非 Element 类型时提前返回
        if (responseNode.getNodeType() != Node.ELEMENT_NODE) {
            return Optional.empty();
        }

        // 将 Response 节点作为 Element 返回
        return Optional.of((Element) responseNode);
    }

}
