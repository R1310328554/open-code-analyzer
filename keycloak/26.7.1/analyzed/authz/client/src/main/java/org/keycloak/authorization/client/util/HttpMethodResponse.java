/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * <p>{@link HttpMethod} 的响应解析包装器，支持将响应体反序列化为指定 Java 类型。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class HttpMethodResponse<R> {

    private final HttpMethod<R> method;

    /** 绑定待解析响应的 HTTP 方法。 */
    public HttpMethodResponse(HttpMethod method) {
        this.method = method;
    }

    /** 执行请求并丢弃响应体。 */
    public R execute() {
        return this.method.execute(new HttpResponseProcessor<R>() {
            @Override
            public R process(byte[] entity) {
                return null;
            }
        });
    }

    /** 指定响应 JSON 反序列化的目标类后执行。 */
    public HttpMethodResponse<R> json(final Class<R> responseType) {
        return new HttpMethodResponse<R>(this.method) {
            @Override
            public R execute() {
                return method.execute(new HttpResponseProcessor<R>() {
                    @Override
                    public R process(byte[] entity) {
                        try {
                            return JsonSerialization.readValue(entity, responseType);
                        } catch (IOException e) {
                            throw new RuntimeException("Error parsing JSON response.", e);
                        }
                    }
                });
            }
        };
    }

    /** 指定 Jackson {@link TypeReference} 后执行，适用于泛型响应类型。 */
    public HttpMethodResponse<R> json(final TypeReference responseType) {
        return new HttpMethodResponse<R>(this.method) {
            @Override
            public R execute() {
                return method.execute(new HttpResponseProcessor<R>() {
                    @Override
                    public R process(byte[] entity) {
                        try {
                            return (R) JsonSerialization.readValue(new ByteArrayInputStream(entity), responseType);
                        } catch (IOException e) {
                            throw new RuntimeException("Error parsing JSON response.", e);
                        }
                    }
                });
            }
        };
    }
}
