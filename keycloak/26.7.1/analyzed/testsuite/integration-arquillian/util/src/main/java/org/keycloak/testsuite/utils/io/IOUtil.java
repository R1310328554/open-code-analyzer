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
package org.keycloak.testsuite.utils.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 集成测试 IO 与 XML/JSON 操作工具类。
 *
 * @author tkyjovsk
 */
public class IOUtil {

    private static final Logger log = Logger.getLogger(IOUtil.class);

    /** Maven 构建输出目录，默认为 target。 */
    public static final File PROJECT_BUILD_DIRECTORY = new File(System.getProperty("project.build.directory", "target"));

    /**
     * 从输入流反序列化 JSON 为指定类型。
     *
     * @param is JSON 输入流
     * @param type 目标类型
     * @param <T> 泛型类型
     * @return 反序列化结果
     */
    public static <T> T loadJson(InputStream is, Class<T> type) {
        try {
            return JsonSerialization.readValue(is, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load json.", e);
        }
    }

    /** 从类路径资源加载 Realm 表示。 */
    public static RealmRepresentation loadRealm(String realmConfig) {
        return loadRealm(IOUtil.class.getResourceAsStream(realmConfig));
    }

    /** 从文件加载 Realm 表示。 */
    public static RealmRepresentation loadRealm(File realmFile) {
        try {
            return loadRealm(new FileInputStream(realmFile));
        } catch (FileNotFoundException ex) {
            throw new IllegalStateException("Test realm file not found: " + realmFile, ex);
        }
    }

    /** 从输入流加载 Realm 表示并打印 realm 名称。 */
    public static RealmRepresentation loadRealm(InputStream is) {
        RealmRepresentation realm = loadJson(is, RealmRepresentation.class);
        System.out.println("Loaded realm " + realm.getRealm());
        return realm;
    }

    /** 解析 XML 输入流为 DOM 文档。 */
    public static Document loadXML(InputStream is) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 将 DOM 文档序列化为字符串。 */
    public static String documentToString(Document newDoc) {
        try {
            DOMSource domSource = new DOMSource(newDoc);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            return sw.toString();
        } catch (TransformerException e) {
            log.error("Can't transform document to String");
            throw new RuntimeException(e);
        }
    }

    /** 将 DOM 文档转换为可重复读取的输入流。 */
    public static InputStream documentToInputStream(Document doc) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException e) {
            log.error("Can't transform document to InputStream");
            throw new RuntimeException(e);
        }
    }

    /**
     * 按正则替换 XML 元素属性值（仅当恰好找到一个匹配节点且属性存在时生效）。
     *
     * @param doc XML 文档
     * @param tagName 元素标签名
     * @param attributeName 属性名
     * @param regex 待替换的正则表达式
     * @param replacement 替换文本
     */
    public static void modifyDocElementAttribute(Document doc, String tagName, String attributeName, String regex, String replacement) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() != 1) {
            log.warn("Not able or ambiguous to find element: " + tagName);
            return;
        }

        Node node = nodes.item(0).getAttributes().getNamedItem(attributeName);
        if (node == null || node.getTextContent() == null) {
            log.warn("Not able to find attribute " + attributeName + " within element: " + tagName);
            return;
        }
        node.setTextContent(node.getTextContent().replaceFirst(regex, replacement));
    }

    /**
     * 按属性值删除指定父节点下的子元素。
     *
     * @param doc XML 文档
     * @param parentTag 父元素标签
     * @param tagName 待删除子元素标签
     * @param attributeName 匹配属性名
     * @param value 匹配属性值
     */
    public static void removeNodeByAttributeValue(Document doc, String parentTag, String tagName, String attributeName, String value){
        NodeList parentNodes = doc.getElementsByTagName(parentTag);
        if (parentNodes.getLength() != 1) {
            log.warn("Not able or ambiguous to find element: " + parentTag);
            return;
        }

        Element parentElement = (Element) parentNodes.item(0);
        if (parentElement == null) {
            log.warn("Not able to find element: " + parentTag);
            return;
        }

        NodeList nodes = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++){
            Node node = nodes.item(i).getAttributes().getNamedItem(attributeName);
            if (node.getTextContent().equals(value)){
                parentElement.removeChild(nodes.item(i));
                return;
            }
        }
    }

    /**
     * 按正则替换 XML 元素文本内容（仅当恰好找到一个匹配节点时生效）。
     *
     * @param doc XML 文档
     * @param tagName 元素标签名
     * @param regex 待替换的正则表达式
     * @param replacement 替换文本
     */
    public static void modifyDocElementValue(Document doc, String tagName, String regex, String replacement) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() != 1) {
            log.warn("Not able or ambiguous to find element: " + tagName);
            return;
        }

        Node node = nodes.item(0);
        if (node == null) {
            log.warn("Not able to find element: " + tagName);
            return;
        }

        node.setTextContent(node.getTextContent().replaceFirst(regex, replacement));
    }

    /** 设置指定标签唯一匹配元素的属性值。 */
    public static void setDocElementAttributeValue(Document doc, String tagName, String attributeName, String value) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() != 1) {
            log.warn("Not able or ambiguous to find element: " + tagName);
            return;
        }

        Element node = (Element) nodes.item(0);
        if (node == null) {
            log.warn("Not able to find element: " + tagName);
            return;
        }

        node.setAttribute(attributeName, value);
    }

    /** 从父元素下移除所有指定标签名的子节点。 */
    public static void removeElementsFromDoc(Document doc, String parentTag, String removeNode) {
        NodeList nodes = doc.getElementsByTagName(parentTag);
        if (nodes.getLength() != 1) {
            log.warn("Not able or ambiguous to find element: " + parentTag);
            return;
        }

        Element parentElement = (Element) nodes.item(0);
        if (parentElement == null) {
            log.warn("Not able to find element: " + parentTag);
            return;
        }

        NodeList removeNodes = parentElement.getElementsByTagName(removeNode);
        if (removeNodes == null) {
            log.warn("Not able to find element: " + removeNode + " within node " + parentTag);
            return;
        }

        for (int i = 0; i < removeNodes.getLength();){
            Element removeElement = (Element) removeNodes.item(i);
            if (removeElement == null) {
                log.warn("Not able to find element: " + removeNode + " within node " + parentTag);
                return;
            }

            log.trace("Removing node " + removeNode);
            parentElement.removeChild(removeElement);

        }

    }

    /**
     * 按斜杠分隔路径获取元素文本内容。
     *
     * @param doc XML 文档
     * @param path 元素路径，如 root/child/grandchild
     * @return 文本内容，路径不存在时返回 null
     */
    public static String getElementTextContent(Document doc, String path) {
        String[] pathSegments = path.split("/");

        Element currentElement = (Element) doc.getElementsByTagName(pathSegments[0]).item(0);
        if (currentElement == null) {
            log.warn("Not able to find element: " + pathSegments[0] + " in document");
            return null;
        }

        for (int i = 1; i < pathSegments.length; i++) {
            currentElement = (Element) currentElement.getElementsByTagName(pathSegments[i]).item(0);

            if (currentElement == null) {
                log.warn("Not able to find element: " + pathSegments[i] + " in " + pathSegments[i - 1]);
                return null;
            }
        }

        return currentElement.getTextContent();
    }

    /** 在指定路径的父元素下追加子节点。 */
    public static void appendChildInDocument(Document doc, String parentPath, Element node) {
        String[] pathSegments = parentPath.split("/");

        Element currentElement = (Element) doc.getElementsByTagName(pathSegments[0]).item(0);
        if (currentElement == null) {
            log.warn("Not able to find element: " + pathSegments[0] + " in document");
            return;
        }

        for (int i = 1; i < pathSegments.length; i++) {
            currentElement = (Element) currentElement.getElementsByTagName(pathSegments[i]).item(0);

            if (currentElement == null) {
                log.warn("Not able to find element: " + pathSegments[i] + " in " + pathSegments[i - 1]);
                return;
            }
        }

        currentElement.appendChild(node);
    }

    /** 按路径删除文档中的元素节点。 */
    public static void removeElementFromDoc(Document doc, String path) {
        String[] pathSegments = path.split("/");

        Element currentElement = (Element) doc.getElementsByTagName(pathSegments[0]).item(0);
        if (currentElement == null) {
            log.warn("Not able to find element: " + pathSegments[0] + " in document");
            return;
        }

        for (int i = 1; i < pathSegments.length; i++) {
            currentElement = (Element) currentElement.getElementsByTagName(pathSegments[i]).item(0);

            if (currentElement == null) {
                log.warn("Not able to find element: " + pathSegments[i] + " in " + pathSegments[i - 1]);
                return;
            }
        }

        currentElement.getParentNode().removeChild(currentElement);
    }

    /**
     * 在指定目录执行 shell 命令，超时 10 秒。
     *
     * @param command 命令字符串
     * @param dir 工作目录
     */
    public static void execCommand(String command, File dir) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command, null, dir);
        if (process.waitFor(10, TimeUnit.SECONDS)) {
            if (process.exitValue() != 0) {
                getOutput("ERROR", process.getErrorStream());
                throw new RuntimeException("Adapter installation failed. Process exitValue: "
                        + process.exitValue());
            }
            getOutput("OUTPUT", process.getInputStream());
            log.debug("process.isAlive(): " + process.isAlive());
        } else {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
            throw new RuntimeException("Timeout after 10 seconds.");
        }
    }

    /** 读取并打印子进程标准输出或错误流内容。 */
    public static void getOutput(String type, InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        builder.append("<").append(type).append(">");
        System.out.println(builder);
        builder = new StringBuilder();
        while (reader.ready()) {
            System.out.println(reader.readLine());
        }
        builder.append("</").append(type).append(">");
        System.out.println(builder);
    }

}
