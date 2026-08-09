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
package com.alibaba.csp.sentinel.datasource.etcd;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Etcd 连接配置项，从 {@link com.alibaba.csp.sentinel.config.SentinelConfig} 读取。
 *
 * @author lianglin
 * @since 1.7.0
 */
public final class EtcdConfig {

    public final static String END_POINTS = "csp.sentinel.etcd.endpoints";
    public final static String USER = "csp.sentinel.etcd.user";
    public final static String PASSWORD = "csp.sentinel.etcd.password";
    public final static String CHARSET = "csp.sentinel.etcd.charset";
    public final static String AUTH_ENABLE = "csp.sentinel.etcd.auth.enable";
    public final static String AUTHORITY = "csp.sentinel.etcd.authority";

    private final static String ENABLED = "true";

    /** 获取 Etcd 端点列表（逗号分隔）。 */
    public static String getEndPoints() {
        return SentinelConfig.getConfig(END_POINTS);
    }

    /** 获取 Etcd 认证用户名。 */
    public static String getUser() {
        return SentinelConfig.getConfig(USER);
    }

    /** 获取 Etcd 认证密码。 */
    public static String getPassword() {
        return SentinelConfig.getConfig(PASSWORD);
    }

    /** 获取 Etcd 通信字符集，未配置则回退 Sentinel 全局 charset。 */
    public static String getCharset() {
        String etcdCharset = SentinelConfig.getConfig(CHARSET);
        if (StringUtil.isNotBlank(etcdCharset)) {
            return etcdCharset;
        }
        return SentinelConfig.charset();
    }

    /** 是否启用 Etcd 用户名密码认证。 */
    public static boolean isAuthEnable() {
        return ENABLED.equalsIgnoreCase(SentinelConfig.getConfig(AUTH_ENABLE));
    }

    /** 获取 Etcd authority 头（TLS/SNI 场景）。 */
    public static String getAuthority() {
        return SentinelConfig.getConfig(AUTHORITY);
    }

    private EtcdConfig() {}

}
