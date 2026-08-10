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

package com.alibaba.nacos.console.config;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 控制台 CORS 配置值对象：从 {@code nacos.console.cors.*} 环境属性解析跨域策略。
 * Nacos console cors configurations.
 *
 * @author zhan7236
 */
public class ConsoleCorsConfig {
    
    /** CORS 相关配置项前缀 */
    private static final String CONSOLE_CORS_PREFIX = "nacos.console.cors.";
    
    /** 是否允许携带凭证的配置键 */
    private static final String ALLOW_CREDENTIALS_KEY = CONSOLE_CORS_PREFIX + "allow-credentials";
    
    /** 允许的请求头列表配置键（逗号分隔） */
    private static final String ALLOWED_HEADERS_KEY = CONSOLE_CORS_PREFIX + "allowed-headers";
    
    /** 预检请求缓存时间（秒）配置键 */
    private static final String MAX_AGE_KEY = CONSOLE_CORS_PREFIX + "max-age";
    
    /** 允许的 HTTP 方法列表配置键 */
    private static final String ALLOWED_METHODS_KEY = CONSOLE_CORS_PREFIX + "allowed-methods";
    
    /** 允许的来源列表配置键 */
    private static final String ALLOWED_ORIGINS_KEY = CONSOLE_CORS_PREFIX + "allowed-origins";
    
    /** 未配置时默认允许携带 Cookie 等凭证 */
    private static final boolean DEFAULT_ALLOW_CREDENTIALS = true;
    
    /** 未配置时预检缓存默认 18000 秒 */
    private static final long DEFAULT_MAX_AGE = 18000L;
    
    /** 是否允许跨域凭证 */
    private final boolean allowCredentials;
    
    /** 允许的请求头；空列表表示由 Web 层回退为 * */
    private final List<String> allowedHeaders;
    
    /** 预检 OPTIONS 响应的 Max-Age（秒） */
    private final long maxAge;
    
    /** 允许的 HTTP 方法列表 */
    private final List<String> allowedMethods;
    
    /** 允许的来源列表 */
    private final List<String> allowedOrigins;
    
    /** 从 {@link EnvUtil} 读取并解析全部 CORS 配置项 */
    public ConsoleCorsConfig() {
        this.allowCredentials =
            EnvUtil.getProperty(ALLOW_CREDENTIALS_KEY, Boolean.class, DEFAULT_ALLOW_CREDENTIALS);
        this.allowedHeaders = parseListProperty(ALLOWED_HEADERS_KEY);
        this.maxAge = EnvUtil.getProperty(MAX_AGE_KEY, Long.class, DEFAULT_MAX_AGE);
        this.allowedMethods = parseListProperty(ALLOWED_METHODS_KEY);
        this.allowedOrigins = parseListProperty(ALLOWED_ORIGINS_KEY);
    }
    
    /** 将逗号分隔的配置字符串解析为列表；空值返回空列表 */
    private List<String> parseListProperty(String key) {
        String value = EnvUtil.getProperty(key, "");
        if (StringUtils.isNotBlank(value)) {
            return Arrays.asList(value.split(","));
        }
        return Collections.emptyList();
    }
    
    /** 是否允许跨域携带凭证 */
    public boolean isAllowCredentials() {
        return allowCredentials;
    }
    
    /** 获取允许的请求头列表 */
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }
    
    /** 获取预检缓存时长（秒） */
    public long getMaxAge() {
        return maxAge;
    }
    
    /** 获取允许的 HTTP 方法列表 */
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }
    
    /** 获取允许的来源列表 */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
    
    /** 调试用字符串表示 */
    @Override
    public String toString() {
        return "ConsoleCorsConfig{" + "allowCredentials=" + allowCredentials + ", allowedHeaders="
            + allowedHeaders
            + ", maxAge=" + maxAge + ", allowedMethods=" + allowedMethods + ", allowedOrigins="
            + allowedOrigins
            + '}';
    }
}
