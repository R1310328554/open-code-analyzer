/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.saml.processing.core.parsers.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * 基于 {@link HasQName} 枚举的 QName 反向查找表。
 * <p>构建时同时注册带命名空间与仅本地名的 QName，便于宽松匹配 StAX 事件中的元素名。</p>
 *
 * @author hmlnarik
 */
public class QNameEnumLookup<E extends Enum<E> & HasQName> {

    /** QName 到枚举常量的不可变映射。 */
    private final Map<QName, E> qNameConstants;

    /**
     * 从枚举常量数组构建查找表。
     *
     * @param e 所有枚举常量
     * @throws IllegalStateException 若两个常量绑定相同 QName
     */
    public QNameEnumLookup(E[] e) {
        Map<QName, E> q = new HashMap<>(e.length);
        E old;
        for (E c : e) {
            QName qName = c.getQName();
            if ((old = q.put(qName, c)) != null) {
                throw new IllegalStateException("Same name " + qName + " used for two distinct constants: " + c + ", " + old);
            }

            // 同时注册无命名空间的宽松版本
            if (qName.getNamespaceURI() != null && ! Objects.equals(qName.getNamespaceURI(), XMLConstants.NULL_NS_URI)) {
                qName = new QName(qName.getLocalPart());
                if (q.containsKey(qName)) {
                    q.put(qName, null);
                } else {
                    q.put(qName, c);
                }
            }
        }
        this.qNameConstants = Collections.unmodifiableMap(q);
    }

    /**
     * 根据 {@code name} 查找对应枚举常量。
     * <p>精确匹配失败时回退为仅本地名匹配。</p>
     *
     * @param name 待查找的 QName
     * @return 匹配的枚举常量，未找到时返回 {@code null}
     */
    public E from(QName name) {
        E c = qNameConstants.get(name);
        if (c == null) {
            name = new QName(name.getLocalPart());
            c = qNameConstants.get(name);
        }
        return c;
    }
}
