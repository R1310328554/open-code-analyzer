/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 可本地化消息的 JSON 表示：消息键与可选占位参数，由前端或主题 bundle 解析为最终文本。
 *
 * @author rmartinc
 */
public class LocalizedMessage {

    /** 消息资源 bundle 中的键名。 */
    private final String key;
    /** 消息格式化占位参数；空数组时存储为 null。 */
    private final String[] parameters;

    /**
     * Jackson 反序列化构造器。
     *
     * @param key 消息键
     * @param parameters 格式化参数（可变参数）
     */
    @JsonCreator
    public LocalizedMessage(@JsonProperty("key") String key, @JsonProperty("parameters") String... parameters) {
        this.key = key;
        this.parameters = parameters == null || parameters.length == 0? null : parameters;
    }

    /** @return 消息键 */
    public String getKey() {
        return key;
    }

    /** @return 格式化参数数组，可能为 null */
    public String[] getParameters() {
        return parameters;
    }
}
