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

/**
 * XML namespace is part of XML element.
 *
 * <p>XML 命名空间声明，通常来自元素上的 {@code xmlns:prefix="uri"} 属性。
 * 由 {@link XmlElement#namespaces()} 收集，供解码器解析前缀与 URI 的绑定关系。</p>
 */
public class XmlNamespace {

    /** 命名空间前缀；默认命名空间时可能为 null 或空串。 */
    private final String prefix;
    /** 命名空间 URI。 */
    private final String uri;

    public XmlNamespace(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    /** 返回前缀。 */
    public String prefix() {
        return prefix;
    }

    /** 返回 URI。 */
    public String uri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlNamespace that = (XmlNamespace) o;

        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) {
            return false;
        }
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = prefix != null ? prefix.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XmlNamespace{" +
                "prefix='" + prefix + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

}
