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

import javax.xml.stream.XMLStreamWriter;

import org.keycloak.saml.common.exceptions.ProcessingException;

/**
 * 支持注册 {@code &lt;samlp:Extensions&gt;} 内容提供者的 SAML 协议消息构建器接口。
 *
 * @author hmlnarik
 */
public interface SamlProtocolExtensionsAwareBuilder<T> {

    /**
     * 扩展节点生成器：向 {@code &lt;samlp:Extensions&gt;} 写入自定义 XML 子树。
     */
    public interface NodeGenerator {
        /**
         * 生成 {@code &lt;samlp:Extensions&gt;} 内部内容。
         * 调用时 writer 已输出 Extensions 起始标签。
         *
         * @param writer 用于输出 XML 的 StAX 写入器
         * @throws ProcessingException 写入失败时抛出
         */
        void write(XMLStreamWriter writer) throws ProcessingException;
    }

    /**
     * 将扩展节点生成器加入 SAML 协议消息。
     *
     * @param extension 扩展内容生成器
     * @return 当前构建器（支持链式调用）
     */
    T addExtension(NodeGenerator extension);
}
