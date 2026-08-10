/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.ai.model.mcp.registry;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * MCP Registry 命令行参数联合类型（命名参数 | 位置参数）。
 *
 * <p>与 OpenAPI components.schemas.Argument 对齐，
 * 通过 Jackson 多态反序列化为 {@link NamedArgument} 或 {@link PositionalArgument}。</p>
 *
 * @author xinluo
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = NamedArgument.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PositionalArgument.class, name = "positional"),
    @JsonSubTypes.Type(value = NamedArgument.class, name = "named")
})
public interface Argument {
}
