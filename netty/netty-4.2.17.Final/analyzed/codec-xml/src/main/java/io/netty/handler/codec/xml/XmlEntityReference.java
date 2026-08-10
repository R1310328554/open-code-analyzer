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
 * XML entity reference ... {@code &#nnnn;}
 *
 * <p>实体引用事件（如 {@code &amp;lt;} 或 {@code &amp;name;}）的消息表示。
 * {@link #name()} 为实体名或数字引用标识，{@link #text()} 为解析器给出的替换文本
 * （{@link XmlDecoder} 默认不自动展开外部实体）。</p>
 */
public class XmlEntityReference {

    /** 实体本地名或数字引用键。 */
    private final String name;
    /** 实体对应的文本（可能为空或未展开）。 */
    private final String text;

    public XmlEntityReference(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String name() {
        return name;
    }

    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlEntityReference that = (XmlEntityReference) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return text != null ? text.equals(that.text) : that.text == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XmlEntityReference{" +
                "name='" + name + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
