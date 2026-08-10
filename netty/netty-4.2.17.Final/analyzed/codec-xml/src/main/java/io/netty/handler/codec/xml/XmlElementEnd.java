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
 * Specific {@link XmlElement} representing end of element.
 *
 * <p>元素结束标签（{@code </name>}）对应的消息对象，对应 StAX {@code END_ELEMENT} 事件。
 * 同样携带该标签上的 xmlns 命名空间声明列表。</p>
 */
public class XmlElementEnd extends XmlElement {

    public XmlElementEnd(String name, String namespace, String prefix) {
        super(name, namespace, prefix);
    }

    @Override
    public String toString() {
        return "XmlElementStart{" +
                super.toString() +
                "} ";
    }

}
