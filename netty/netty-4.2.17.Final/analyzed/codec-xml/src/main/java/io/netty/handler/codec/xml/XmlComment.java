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
 * XML Comment
 *
 * <p>XML 注释内容（{@code <!-- ... -->} 内部文本）的不可变消息对象，
 * 继承 {@link XmlContent}，由 {@link XmlDecoder} 解析 {@code COMMENT} 事件时创建。</p>
 */
public class XmlComment extends XmlContent {

    /**
     * @param data 注释正文，不含 {@code <!-- -->} 定界符
     */
    public XmlComment(String data) {
        super(data);
    }

}
