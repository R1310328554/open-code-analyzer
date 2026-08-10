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

package com.alibaba.nacos.copilot.model;

/**
 * 流式响应分片类型枚举：区分模型思考、工具调用、正文内容与完成信号。
 * Stream response type enum.
 *
 * @author nacos
 */
public enum StreamResponseType {
    
    /** 模型推理/思考过程分片。 */
    THINKING("thinking", "模型思考过程"),
    
    /** 工具调用过程分片。 */
    TOOL_CALL("tool_call", "工具调用过程"),
    
    /** 正文内容分片。 */
    CONTENT("content", "内容片段"),
    
    /** 流式响应完成信号。 */
    DONE("done", "响应完成");
    
    /** 协议层类型编码字符串。 */
    private final String code;
    /** 中文可读描述。 */
    private final String description;
    
    /** 构造枚举常量并绑定编码与描述。 */
    StreamResponseType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /** 获取类型编码。 */
    public String getCode() {
        return code;
    }
    
    /** 获取中文描述。 */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据编码字符串解析枚举值。
     *
     * @param code 类型编码
     * @return 匹配的 {@link StreamResponseType}，未知时默认 {@link #CONTENT}
     */
    public static StreamResponseType fromCode(String code) {
        for (StreamResponseType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        // 未知编码时回退为 CONTENT
        return CONTENT;
    }
}
