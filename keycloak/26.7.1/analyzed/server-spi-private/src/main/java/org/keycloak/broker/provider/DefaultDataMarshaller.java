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

package org.keycloak.broker.provider;

import java.io.IOException;
import java.util.List;

import org.keycloak.common.util.Base64Url;
import org.keycloak.util.JsonSerialization;

/**
 * {@link IdentityProviderDataMarshaller} 默认实现：字符串直传，其他类型 JSON 序列化后 Base64Url 编码。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultDataMarshaller implements IdentityProviderDataMarshaller {

    /** 序列化对象为存储字符串（{@link String} 原样返回，其余 JSON+Base64Url）。 */
    @Override
    public String serialize(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else {
            try {
                byte[] bytes = JsonSerialization.writeValueAsBytes(value);
                return Base64Url.encode(bytes);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    /** 从存储字符串反序列化为指定类型。 */
    @Override
    public <T> T deserialize(String serialized, Class<T> clazz) {
        try {
            if (clazz.equals(String.class)) {
                return clazz.cast(serialized);
            } else {
                byte[] bytes = Base64Url.decode(serialized);
                if (List.class.isAssignableFrom(clazz)) {
                    List list = JsonSerialization.readValue(bytes, List.class);
                    return clazz.cast(list);
                } else {
                    return JsonSerialization.readValue(bytes, clazz);
                }
            }
        }  catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
