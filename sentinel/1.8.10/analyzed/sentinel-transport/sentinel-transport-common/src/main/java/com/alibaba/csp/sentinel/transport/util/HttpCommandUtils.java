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
package com.alibaba.csp.sentinel.transport.util;

import com.alibaba.csp.sentinel.command.CommandRequest;

/**
 * HTTP 命令中心工具类：从 {@link CommandRequest} 元数据提取路由目标命令名。
 *
 * @author Eric Zhao
 */
public final class HttpCommandUtils {

    /** 元数据键：目标命令名称。 */
    public static final String REQUEST_TARGET = "command-target";

    /** 从请求元数据读取 command-target，即待执行的命令名。 */
    public static String getTarget(CommandRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为 null");
        }
        return request.getMetadata().get(REQUEST_TARGET);
    }

    private HttpCommandUtils() {}
}
