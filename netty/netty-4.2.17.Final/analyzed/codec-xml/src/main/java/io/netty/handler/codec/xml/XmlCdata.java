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
 * XML CDATA ... <![CDATA[&lt;sender&gt;John Smith&lt;/sender&gt;]]>
 *
 * <p>CDATA 区段内容的消息类型，对应 {@code <![CDATA[...]]>} 内的原始文本。
 * 内部可含 {@code <}、{@code &} 等无需转义的字符；由 {@link XmlDecoder} 在
 * {@code CDATA} 事件时产出。</p>
 */
public class XmlCdata extends XmlContent {

    /**
     * @param data CDATA 区段内的纯文本（不含 {@code <![CDATA[} 定界符）
     */
    public XmlCdata(String data) {
        super(data);
    }

}
