/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic XML element in document, {@link XmlElementStart} represents open element and provides access to attributes,
 * {@link XmlElementEnd} represents closing element.
 *
 * <p>文档中元素的抽象基类，保存本地名、命名空间 URI 与前缀，以及该标签上声明的
 * {@link XmlNamespace} 列表。开始标签见 {@link XmlElementStart}，结束标签见 {@link XmlElementEnd}。</p>
 */
public abstract class XmlElement {

    /** 元素本地名。 */
    private final String name;
    /** 元素命名空间 URI。 */
    private final String namespace;
    /** 元素命名空间前缀。 */
    private final String prefix;

    /** 该开始/结束标签上绑定的 xmlns 声明（可写列表，解码器直接追加）。 */
    private final List<XmlNamespace> namespaces = new ArrayList<XmlNamespace>();

    protected XmlElement(String name, String namespace, String prefix) {
        this.name = name;
        this.namespace = namespace;
        this.prefix = prefix;
    }

    public String name() {
        return name;
    }

    public String namespace() {
        return namespace;
    }

    public String prefix() {
        return prefix;
    }

    public List<XmlNamespace> namespaces() {
        return namespaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlElement that = (XmlElement) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) {
            return false;
        }
        if (!namespaces.equals(that.namespaces)) {
            return false;
        }
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + namespaces.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return ", name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", prefix='" + prefix + '\'' +
                ", namespaces=" + namespaces;
    }

}
