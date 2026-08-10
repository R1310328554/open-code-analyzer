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

package org.keycloak.sdjwt.consumer;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * HTTP 数据获取接口，用于从远程 URI 拉取 JSON 元数据。
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public interface HttpDataFetcher {

    /**
     * 对指定 URI 执行 HTTP GET 并将响应解析为 JSON。
     *
     * @param uri 目标 URI
     * @throws IOException 发生 I/O 错误或 HTTP 状态非 OK (200) 时
     */
    JsonNode fetchJsonData(String uri) throws IOException;
}
