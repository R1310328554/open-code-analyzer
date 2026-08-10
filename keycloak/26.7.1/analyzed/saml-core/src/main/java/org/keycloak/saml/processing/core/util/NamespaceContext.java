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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * XPath 命名空间上下文辅助类，用于 SAML 令牌签名解析。
 * <p>用法示例：</p>
 * <code>
 * xpath.setNamespaceContext(
 * NamespaceContext.create()
 * .addNsUriPair(xmlSignatureNSPrefix, JBossSAMLURIConstants.XMLDSIG_NSURI.get())
 * );
 * </code>
 *
 * @author Peter Skopek: pskopek at redhat dot com
 */

public class NamespaceContext implements javax.xml.namespace.NamespaceContext {

    /** 前缀到命名空间 URI 的映射。 */
    private Map<String, String> nsMap = new HashMap<>();

    /** 创建空的命名空间上下文。 */
    public NamespaceContext() {
    }

    /**
     * 创建含单个前缀-URI 对的命名空间上下文。
     *
     * @param prefix 命名空间前缀
     * @param uri 命名空间 URI
     */
    public NamespaceContext(String prefix, String uri) {
        nsMap.put(prefix, uri);
    }

    /** 根据前缀获取命名空间 URI。 */
    public String getNamespaceURI(String prefix) {
        return nsMap.get(prefix);
    }

    /** 根据命名空间 URI 获取前缀。 */
    public String getPrefix(String namespaceURI) {
        for (var entry : nsMap.entrySet()) {
            String value = entry.getValue();
            if (value.equals(namespaceURI)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** 获取绑定到指定 URI 的所有前缀迭代器。 */
    public Iterator<String> getPrefixes(String namespaceURI) {
        return nsMap.keySet().iterator();
    }

    /** 添加前缀-URI 映射并返回自身（链式调用）。 */
    public NamespaceContext addNsUriPair(String ns, String uri) {
        nsMap.put(ns, uri);
        return this;
    }

    /**
     * 创建新的 {@link NamespaceContext} 实例。
     *
     * @return 空的命名空间上下文
     */
    public static NamespaceContext create() {
        return new NamespaceContext();
    }
}
