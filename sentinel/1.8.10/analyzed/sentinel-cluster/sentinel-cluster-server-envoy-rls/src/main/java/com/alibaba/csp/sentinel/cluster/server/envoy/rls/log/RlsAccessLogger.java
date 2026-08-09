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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls.log;

import com.alibaba.csp.sentinel.cluster.server.envoy.rls.SentinelEnvoyRlsConstants;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Envoy RLS 访问日志工具类。
 * <p>通过环境变量 {@link com.alibaba.csp.sentinel.cluster.server.envoy.rls.SentinelEnvoyRlsConstants#ENABLE_ACCESS_LOG_ENV_KEY}
 * 控制是否输出限流检查访问日志。</p>
 *
 * @author Eric Zhao
 */
public final class RlsAccessLogger {

    private static boolean enabled = false;

    static {
        try {
            enabled = "on".equalsIgnoreCase(System.getenv(SentinelEnvoyRlsConstants.ENABLE_ACCESS_LOG_ENV_KEY));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** 返回访问日志是否已启用。 */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * 在启用且 info 非空时输出访问日志。
     *
     * @param info 日志内容
     */
    public static void log(String info) {
        if (enabled && StringUtil.isNotEmpty(info)) {
            System.out.println(info);
        }
    }
}
