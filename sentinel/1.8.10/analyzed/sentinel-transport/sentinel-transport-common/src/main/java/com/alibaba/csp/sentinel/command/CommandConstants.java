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

/**
 * Sentinel 命令中心 API 常量：版本命令名与通用响应消息。
 *
 * @author Eric Zhao
 * @since 1.4.1
 */
public final class CommandConstants {

    /** 查询 Sentinel 版本号的命令名。 */
    public static final String VERSION_COMMAND = "version";

    /** 非法命令响应文本。 */
    public static final String MSG_INVALID_COMMAND = "Invalid command";
    /** 未知命令响应前缀。 */
    public static final String MSG_UNKNOWN_COMMAND_PREFIX = "Unknown command";

    /** 命令执行成功响应文本。 */
    public static final String MSG_SUCCESS = "success";
    /** 命令执行失败响应文本。 */
    public static final String MSG_FAIL = "failed";

    private CommandConstants() {}
}
