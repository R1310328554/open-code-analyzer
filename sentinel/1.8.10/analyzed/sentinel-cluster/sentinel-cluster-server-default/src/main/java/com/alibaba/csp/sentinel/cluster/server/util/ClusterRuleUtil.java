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
package com.alibaba.csp.sentinel.cluster.server.util;

/**
 * 集群规则工具类，提供规则 ID 等通用校验方法。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterRuleUtil {

    /** 校验规则 ID 是否有效（非 null 且大于 0）。
     *
     * @param id 规则 ID
     * @return 有效返回 true
     */
    public static boolean validId(Long id) {
        return id != null && id > 0;
    }

    private ClusterRuleUtil() {}
}
