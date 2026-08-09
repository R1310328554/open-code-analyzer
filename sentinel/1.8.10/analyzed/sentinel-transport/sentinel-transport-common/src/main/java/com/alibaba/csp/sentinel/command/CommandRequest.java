/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.command;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * 命令中心请求模型：封装 HTTP 查询参数、元数据与可选请求体。
 * 参数与元数据均使用字符串键值对，便于传输层透传。
 *
 * @author Eric Zhao
 */
public class CommandRequest {

    /** 请求元数据（如客户端地址、协议信息等）。 */
    private final Map<String, String> metadata = new HashMap<String, String>();
    /** URL 查询参数或表单参数。 */
    private final Map<String, String> parameters = new HashMap<String, String>();
    /** 可选请求体（如 POST 规则变更时的 JSON 载荷）。 */
    private byte[] body;

    public byte[] getBody() {
        return body;
    }

    public CommandRequest setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParam(String key) {
        return parameters.get(key);
    }

    /** 获取参数，空白时返回默认值。 */
    public String getParam(String key, String defaultValue) {
        String value = parameters.get(key);
        return StringUtil.isBlank(value) ? defaultValue : value;
    }

    public CommandRequest addParam(String key, String value) {
        if (StringUtil.isBlank(key)) {
            throw new IllegalArgumentException("参数键不能为空");
        }
        parameters.put(key, value);
        return this;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public CommandRequest addMetadata(String key, String value) {
        if (StringUtil.isBlank(key)) {
            throw new IllegalArgumentException("元数据键不能为空");
        }
        metadata.put(key, value);
        return this;
    }
}
