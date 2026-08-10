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

import javax.xml.namespace.QName;

/**
 * 为 SAML/XML 解析枚举提供 {@link QName} 访问能力的标记接口。
 * <p>实现类通常将 XML 元素本地名与命名空间 URI 封装为枚举常量。</p>
 *
 * @author hmlnarik
 */
public interface HasQName {

    /**
     * 返回此常量对应的 XML 元素 QName。
     *
     * @return 元素 QName
     */
    QName getQName();

}
