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
 * XML Content is base class for XML CDATA, Comments, Characters and Space
 *
 * <p>带文本载荷的 XML 事件抽象基类。子类 {@link XmlCdata}、{@link XmlComment}、
 * {@link XmlCharacters}、{@link XmlSpace} 分别对应解析器中的不同文本类事件，
 * 均通过 {@link #data()} 暴露原始字符串。</p>
 */
public abstract class XmlContent {

    /** 该内容节点携带的文本数据。 */
    private final String data;

    protected XmlContent(String data) {
        this.data = data;
    }

    /** 返回内容文本。 */
    public String data() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlContent that = (XmlContent) o;

        if (data != null ? !data.equals(that.data) : that.data != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "XmlContent{" +
                "data='" + data + '\'' +
                '}';
    }
}
