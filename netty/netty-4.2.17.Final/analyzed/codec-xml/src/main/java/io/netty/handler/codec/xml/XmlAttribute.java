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
 * XML attributes, it is part of {@link XmlElement}
 *
 * <p>不可变值对象，表示元素开始标签上的单个属性（类型、本地名、前缀、命名空间 URI、值）。
 * 由 {@link XmlDecoder} 在解析 {@code START_ELEMENT} 事件时构造并加入 {@link XmlElementStart#attributes()}。</p>
 */
public class XmlAttribute {

    /** DTD 或模式声明中的属性类型（如 CDATA、ID 等）。 */
    private final String type;
    /** 属性本地名（不含前缀）。 */
    private final String name;
    /** 命名空间前缀，无前缀时为 null。 */
    private final String prefix;
    /** 属性所属命名空间 URI。 */
    private final String namespace;
    /** 属性字面值。 */
    private final String value;

    public XmlAttribute(String type, String name, String prefix, String namespace, String value) {
        this.type = type;
        this.name = name;
        this.prefix = prefix;
        this.namespace = namespace;
        this.value = value;
    }

    public String type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String prefix() {
        return prefix;
    }

    public String namespace() {
        return namespace;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlAttribute that = (XmlAttribute) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) {
            return false;
        }
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XmlAttribute{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", namespace='" + namespace + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
