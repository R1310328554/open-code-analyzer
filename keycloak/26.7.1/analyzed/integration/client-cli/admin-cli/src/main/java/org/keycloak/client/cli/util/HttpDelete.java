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

import java.net.URI;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * 支持请求体的 HTTP DELETE 方法实现。
 * <p>
 * Apache HttpClient 默认 {@code HttpDelete} 不允许携带实体；本类继承
 * {@link HttpEntityEnclosingRequestBase} 以支持带 JSON 正文的 DELETE 调用。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
@Contract(threading = ThreadingBehavior.UNSAFE)
public class HttpDelete extends HttpEntityEnclosingRequestBase {

    /**
     * 创建指向指定 URI 的 DELETE 请求。
     *
     * @param uri 目标 URI 字符串
     */
    public HttpDelete(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return "DELETE";
    }
}
