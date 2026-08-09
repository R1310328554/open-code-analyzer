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
package com.alibaba.csp.sentinel.util;

import java.lang.management.ManagementFactory;

/**
 * 提供当前进程 PID 的工具类。
 */
public final class PidUtil {

    /**
     * 解析并获取当前进程 ID。
     *
     * @return 当前进程 ID
     */
    public static int getPid() {
        // 注意：此操作会触发本机解析，可能较慢。
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.split("@")[0]);
    }

    private PidUtil() {}
}
