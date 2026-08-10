/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.client.cli.util;

/**
 * 对象属性操作相关的运行时异常。
 * <p>
 * 携带出错的属性名称，便于 CLI 命令定位 JSON/属性编辑失败的具体字段。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AttributeException extends RuntimeException {

    /** 引发异常的属性名称。 */
    private final String attrName;

    /** 构造带属性名与消息的异常。 */
    public AttributeException(String attrName, String message) {
        super(message);
        this.attrName = attrName;
    }

    /** 构造带属性名、消息及原因的异常。 */
    public AttributeException(String attrName, String message, Throwable th) {
        super(message, th);
        this.attrName = attrName;
    }

    /** 返回引发异常的属性名称。 */
    public String getAttributeName() {
        return attrName;
    }
}
