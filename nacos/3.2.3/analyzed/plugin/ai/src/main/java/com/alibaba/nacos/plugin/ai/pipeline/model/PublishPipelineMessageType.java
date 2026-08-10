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

package com.alibaba.nacos.plugin.ai.pipeline.model;

/**
 * {@link PublishPipelineResult#getMessage()} 的语义类型，供客户端按格式渲染审核输出。
 *
 * @author qiacheng.cxy
 * @since 3.2.0
 */
public enum PublishPipelineMessageType {
    
    /**
     * 纯文本消息。
     */
    TEXT("text"),
    
    /**
     * JSON 结构化载荷。
     */
    JSON("json"),
    
    /**
     * Markdown 格式（例如 skill-scanner {@code --format markdown} 标准输出）。
     */
    MARKDOWN("markdown"),
    
    /**
     * HTML 片段或完整文档。
     */
    HTML("html");
    
    /** 对外 API 使用的 wire 值（小写）。 */
    private final String code;
    
    /** @param code 对外 wire 值 */
    PublishPipelineMessageType(String code) {
        this.code = code;
    }
    
    /**
     * 返回对外 API / 序列化使用的 wire 值（小写），例如 {@code markdown}。
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 根据 API wire 值解析枚举；未知或空值时返回 {@code null}。
     *
     * @param code wire 字符串
     * @return 匹配的枚举，或 {@code null}
     */
    public static PublishPipelineMessageType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (PublishPipelineMessageType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        return null;
    }
}
