/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.httpclient;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 * 限制 HTTP 响应体读取字节数，防止超大响应导致 {@link OutOfMemoryError}。
 * <p>通过包装实体为 {@link SafeHttpEntity} 实现上限控制。</p>
 *
 * @author Alexander Schwartz
 */
class SafeBasicResponseHandler extends BasicResponseHandler {
    /** 允许读取的最大响应体字节数。 */
    private final long maxConsumedResponseSize;

    /** @param maxConsumedResponseSize 响应体读取上限（字节） */
    SafeBasicResponseHandler(long maxConsumedResponseSize) {
        this.maxConsumedResponseSize = maxConsumedResponseSize;
    }

    @Override
    /** 将实体包装为安全实体后再转为字符串。 */
    public String handleEntity(HttpEntity entity) throws IOException {
        return super.handleEntity(new SafeHttpEntity(entity, maxConsumedResponseSize));
    }
}
