/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter.Feature;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.MixAll;

/**
 * Proxy 配置管理单例入口：解析 RMQ_PROXY_HOME 并持有 {@link Configuration} 实例。
 */
public class ConfigurationManager {
    /** 环境变量/系统属性名：Proxy 安装根目录。 */
    public static final String RMQ_PROXY_HOME = "RMQ_PROXY_HOME";
    protected static final String DEFAULT_RMQ_PROXY_HOME = MixAll.ROCKETMQ_HOME_DIR;
    protected static String proxyHome;
    protected static Configuration configuration;

    /** 从环境变量或系统属性解析 Proxy 主目录，缺省为 RocketMQ 安装目录。 */
    public static void initEnv() {
        proxyHome = System.getenv(RMQ_PROXY_HOME);
        if (StringUtils.isEmpty(proxyHome)) {
            proxyHome = System.getProperty(RMQ_PROXY_HOME, DEFAULT_RMQ_PROXY_HOME);
        }

        // 仍未解析到主目录时使用当前工作目录
        if (proxyHome == null) {
            proxyHome = "./";
        }
    }

    /** 创建 {@link Configuration} 并加载 JSON 配置文件。 */
    public static void initConfig() throws Exception {
        configuration = new Configuration();
        configuration.init();
    }

    /** 返回已解析的 Proxy 主目录路径。 */
    public static String getProxyHome() {
        return proxyHome;
    }

    /** 便捷方法：获取 Proxy 配置。 */
    public static ProxyConfig getProxyConfig() {
        return configuration.getProxyConfig();
    }

    public static AuthConfig getAuthConfig() {
        return configuration.getAuthConfig();
    }

    /** 将当前 Proxy 配置格式化为可读 JSON 字符串（含 null 字段）。 */
    public static String formatProxyConfig() {
        return JSON.toJSONString(ConfigurationManager.getProxyConfig(),
                Feature.PrettyFormat, Feature.WriteMapNullValue, Feature.WriteNullListAsEmpty);
    }
}
