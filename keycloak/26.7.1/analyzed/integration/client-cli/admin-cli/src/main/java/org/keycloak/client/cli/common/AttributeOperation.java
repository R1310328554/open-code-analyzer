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
package org.keycloak.client.cli.common;

/**
 * 对 JSON/配置属性的单次 SET 或 DELETE 操作。
 * <p>
 * 键经 {@link AttributeKey} 解析；DELETE 类型不允许携带 value。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AttributeOperation {

    /** 操作类型。 */
    private Type type;
    /** 目标属性路径。 */
    private AttributeKey key;
    /** SET 时的新值；DELETE 时为 null。 */
    private String value;

    public AttributeOperation(Type type, String key) {
        this(type, key, null);
    }

    public AttributeOperation(Type type, String key, String value) {
        if (type == Type.DELETE && value != null) {
            throw new IllegalArgumentException("When type is DELETE, value has to be null");
        }
        this.type = type;
        this.key = new AttributeKey(key);
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public AttributeKey getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }


    /** 属性操作类型：设置值或删除键。 */
    public enum Type {
        /** 设置或覆盖属性值。 */
        SET,
        /** 删除指定属性。 */
        DELETE
    }
}
