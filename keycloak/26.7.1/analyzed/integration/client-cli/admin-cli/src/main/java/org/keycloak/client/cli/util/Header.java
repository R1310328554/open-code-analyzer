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
 * HTTP 请求/响应头的名称-值对。
 * <p>
 * 由 {@link Headers} 集合持有，供 {@link HttpUtil} 组装 Apache HttpClient 请求。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Header {

    /** 头名称（保留原始大小写）。 */
    private String name;
    /** 头值。 */
    private String value;

    /**
     * 构造 HTTP 头。
     *
     * @param key 头名称
     * @param value 头值
     */
    public Header(String key, String value) {
        this.name = key;
        this.value = value;
    }

    /** 返回头名称。 */
    public String getName() {
        return name;
    }

    /** 返回头值。 */
    public String getValue() {
        return value;
    }
}
