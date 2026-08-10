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
 * Beginning of the XML document ... i.e. XML header
 *
 * <p>文档开始事件（{@code START_DOCUMENT}）的元数据：编码、版本、standalone 声明及
 * 字符编码方案。通常对应 XML 声明 {@code <?xml version="1.0" encoding="UTF-8"?>} 中的信息。</p>
 */
public class XmlDocumentStart {

    /** 文档字符编码（解析器检测或声明值）。 */
    private final String encoding;
    /** XML 版本号（如 "1.0"），未声明时为 null。 */
    private final String version;
    /** 是否为 standalone 文档。 */
    private final boolean standalone;
    /** 字符编码方案标识。 */
    private final String encodingScheme;

    public XmlDocumentStart(String encoding, String version, boolean standalone, String encodingScheme) {
        this.encoding = encoding;
        this.version = version;
        this.standalone = standalone;
        this.encodingScheme = encodingScheme;
    }

    /** Return defined or guessed XML encoding **/
    /** 返回声明或推断的 XML 字符编码。 */
    public String encoding() {
        return encoding;
    }

    /** Return defined XML version or null **/
    /** 返回 XML 版本；未声明时返回 null。 */
    public String version() {
        return version;
    }

    /** Return standalonity of the document **/
    /** 文档是否 standalone（不依赖外部 DTD/实体）。 */
    public boolean standalone() {
        return standalone;
    }

    /** Return defined encoding or null **/
    /** 返回字符编码方案名称；未定义时为 null。 */
    public String encodingScheme() {
        return encodingScheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XmlDocumentStart that = (XmlDocumentStart) o;

        if (standalone != that.standalone) {
            return false;
        }
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) {
            return false;
        }
        if (encodingScheme != null ? !encodingScheme.equals(that.encodingScheme) : that.encodingScheme != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = encoding != null ? encoding.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (standalone ? 1 : 0);
        result = 31 * result + (encodingScheme != null ? encodingScheme.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "XmlDocumentStart{" +
                "encoding='" + encoding + '\'' +
                ", version='" + version + '\'' +
                ", standalone=" + standalone +
                ", encodingScheme='" + encodingScheme + '\'' +
                '}';
    }
}
