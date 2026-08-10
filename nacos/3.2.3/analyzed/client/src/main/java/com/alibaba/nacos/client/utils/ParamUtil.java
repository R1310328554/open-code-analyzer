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

package com.alibaba.nacos.client.utils;

import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;

/**
 * manage param tool.
 * <p>客户端全局 HTTP 与配置拉取参数：静态块从 {@link NacosClientProperties#PROTOTYPE} 读取连接/读超时及单任务配置大小，并提供环境名截断与 MD5 后缀工具。</p>
 *
 * @author nacos
 */
public class ParamUtil {
    
    private static final Logger LOGGER = LogUtils.logger(ParamUtil.class);
    
    /** HTTP 连接超时（毫秒） */
    private static int connectTimeout;
    
    /** HTTP 读超时（毫秒） */
    private static int readTimeout;
    
    /** 单次配置拉取任务允许的最大配置体量（KB） */
    private static double perTaskConfigSize;
    
    private static final String NACOS_CONNECT_TIMEOUT_KEY = "NACOS.CONNECT.TIMEOUT";
    
    private static final String NACOS_READ_TIMEOUT_KEY = "NACOS.READ.TIMEOUT";
    
    private static final String DEFAULT_NACOS_CONNECT_TIMEOUT = "1000";
    
    private static final String DEFAULT_NACOS_READ_TIMEOUT = "3000";
    
    private static final String PER_TASK_CONFIG_SIZE_KEY = "PER_TASK_CONFIG_SIZE";
    
    private static final String DEFAULT_PER_TASK_CONFIG_SIZE_KEY = "3000";
    
    static {
        // 静态初始化：加载 HTTP 超时与单任务配置大小
        connectTimeout = initConnectionTimeout();
        LOGGER.info("[settings] [http-client] connect timeout:{}", connectTimeout);
        
        readTimeout = initReadTimeout();
        LOGGER.info("[settings] [http-client] read timeout:{}", readTimeout);
        
        perTaskConfigSize = initPerTaskConfigSize();
        LOGGER.info("PER_TASK_CONFIG_SIZE: {}", perTaskConfigSize);
    }
    
    /** 从属性或默认值解析连接超时，非法数字抛 {@link IllegalArgumentException} */
    private static int initConnectionTimeout() {
        String tmp = DEFAULT_NACOS_CONNECT_TIMEOUT;
        try {
            tmp = NacosClientProperties.PROTOTYPE.getProperty(NACOS_CONNECT_TIMEOUT_KEY,
                DEFAULT_NACOS_CONNECT_TIMEOUT);
            return Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            final String msg = "[http-client] invalid connect timeout:" + tmp;
            LOGGER.error("[settings] " + msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }
    
    /** 从属性或默认值解析读超时 */
    private static int initReadTimeout() {
        String tmp = DEFAULT_NACOS_READ_TIMEOUT;
        try {
            tmp = NacosClientProperties.PROTOTYPE.getProperty(NACOS_READ_TIMEOUT_KEY,
                DEFAULT_NACOS_READ_TIMEOUT);
            return Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            final String msg = "[http-client] invalid read timeout:" + tmp;
            LOGGER.error("[settings] " + msg, e);
            throw new IllegalArgumentException(msg, e);
        }
    }
    
    /** 从属性解析 PER_TASK_CONFIG_SIZE（double） */
    private static double initPerTaskConfigSize() {
        try {
            return Double.parseDouble(
                NacosClientProperties.PROTOTYPE.getProperty(PER_TASK_CONFIG_SIZE_KEY,
                    DEFAULT_PER_TASK_CONFIG_SIZE_KEY));
        } catch (NumberFormatException e) {
            LOGGER.error("[PER_TASK_CONFIG_SIZE] PER_TASK_CONFIG_SIZE invalid", e);
            throw new IllegalArgumentException(
                "invalid PER_TASK_CONFIG_SIZE, expected value type double", e);
        }
    }
    
    /** 返回当前连接超时毫秒数 */
    public static int getConnectTimeout() {
        return connectTimeout;
    }
    
    /** 运行时覆盖连接超时 */
    public static void setConnectTimeout(int connectTimeout) {
        ParamUtil.connectTimeout = connectTimeout;
    }
    
    /** 返回当前读超时毫秒数 */
    public static int getReadTimeout() {
        return readTimeout;
    }
    
    /** 运行时覆盖读超时 */
    public static void setReadTimeout(int readTimeout) {
        ParamUtil.readTimeout = readTimeout;
    }
    
    /** 返回单任务配置大小上限 */
    public static double getPerTaskConfigSize() {
        return perTaskConfigSize;
    }
    
    /** 运行时覆盖单任务配置大小 */
    public static void setPerTaskConfigSize(double perTaskConfigSize) {
        ParamUtil.perTaskConfigSize = perTaskConfigSize;
    }
    
    /** 环境名超过该长度时触发截断+MD5 后缀 */
    public static final int MAX_ENV_NAME_LENGTH = 50;
    
    /**
     * simply env name if name is too long.
     * <p>环境名过长时保留前 {@link #MAX_ENV_NAME_LENGTH} 字符并追加 UTF-8 MD5 十六进制，避免存储/传输超限。</p>
     *
     * @param envName env name.
     * @return env name.
     */
    public static String simplyEnvNameIfOverLimit(String envName) {
        if (StringUtils.isNotBlank(envName) && envName.length() > MAX_ENV_NAME_LENGTH) {
            return envName.substring(0, MAX_ENV_NAME_LENGTH) + MD5Utils.md5Hex(envName, "UTF-8");
        }
        return envName;
    }
    
}
