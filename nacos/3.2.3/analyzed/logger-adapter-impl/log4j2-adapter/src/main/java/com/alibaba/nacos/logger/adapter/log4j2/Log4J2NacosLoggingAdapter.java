/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.logger.adapter.log4j2;

import com.alibaba.nacos.common.logging.NacosLoggingAdapter;
import com.alibaba.nacos.common.logging.NacosLoggingProperties;
import com.alibaba.nacos.common.utils.ResourceUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

import java.net.URI;
import java.util.Map;

/**
 * Log4j2 版 Nacos 日志适配器（支持 2.7+）。
 *
 * <p>实现 {@link com.alibaba.nacos.common.logging.NacosLoggingAdapter}， 将 classpath 下的 {@code nacos-log4j2.xml} 增量合并到现有 {@link LoggerContext}， 不覆盖用户应用日志配置。</p>
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @author xiweng.yy
 * @since 0.9.0
 */
public class Log4J2NacosLoggingAdapter implements NacosLoggingAdapter {
    
    /** Nacos 默认 Log4j2 配置文件 classpath 位置。 */
    private static final String NACOS_LOG4J2_LOCATION = "classpath:nacos-log4j2.xml";
    
    /** Nacos 包名前缀，用于识别 Nacos 专用 Logger。 */
    private static final String NACOS_LOGGER_PREFIX = "com.alibaba.nacos";
    
    /** 已加载 Nacos 配置的 Appender 标记名。 */
    private static final String APPENDER_MARK = "ASYNC_NAMING";
    
    /** SLF4J 桥接 Log4j2 的 Logger 实现类名。 */
    private static final String LOG4J2_CLASSES = "org.apache.logging.slf4j.Log4jLogger";
    
    /** 自定义 Log4j2 配置加载器。 */
    private final NacosLog4j2Configurator configurator;
    
    /** 构造适配器并初始化配置器。 */
    public Log4J2NacosLoggingAdapter() {
        this.configurator = new NacosLog4j2Configurator();
    }
    
    /** 判断 Logger 类是否为 Log4j2 SLF4J 桥接实现。 */
    @Override
    public boolean isAdaptedLogger(Class<?> loggerClass) {
        Class<?> expectedLoggerClass = getExpectedLoggerClass();
        return null != expectedLoggerClass && expectedLoggerClass.isAssignableFrom(loggerClass);
    }
    
    private Class<?> getExpectedLoggerClass() {
        try {
            return Class.forName(LOG4J2_CLASSES);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /** 检测 ASYNC_NAMING Appender 是否缺失，决定是否需重新加载。 */
    @Override
    public boolean isNeedReloadConfiguration() {
        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        final Configuration contextConfiguration = loggerContext.getConfiguration();
        for (Map.Entry<String, Appender> entry : contextConfiguration.getAppenders().entrySet()) {
            if (APPENDER_MARK.equals(entry.getValue().getName())) {
                return false;
            }
        }
        return true;
    }
    
    /** 返回默认 nacos-log4j2.xml 位置。 */
    @Override
    public String getDefaultConfigLocation() {
        return NACOS_LOG4J2_LOCATION;
    }
    
    /** 保存属性并加载指定位置的 Log4j2 配置。 */
    @Override
    public void loadConfiguration(NacosLoggingProperties loggingProperties) {
        Log4j2NacosLoggingPropertiesHolder.setProperties(loggingProperties);
        String location = loggingProperties.getLocation();
        loadConfiguration(location);
    }
    
    private void loadConfiguration(String location) {
        if (StringUtils.isBlank(location)) {
            return;
        }
        
        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        
        // 快速路径：Appender 已存在则跳过加载
        if (loggerContext.getConfiguration().getAppender(APPENDER_MARK) != null) {
            return;
        }
        
        // 双重检查锁，防止并发场景重复加载配置
        // Although normal usage is single-threaded (via ScheduledExecutorService), this ensures
        // robustness in edge cases like concurrent framework initialization or testing scenarios
        synchronized (loggerContext) {
            final Configuration config = loggerContext.getConfiguration();
            if (config.getAppender(APPENDER_MARK) != null) {
                return;
            }
            
            try {
                // 使用自定义配置器增量合并（与 Logback 适配器设计一致）
                URI configUri = ResourceUtils.getResourceUrl(location).toURI();
                configurator.configure(loggerContext, configUri);
                
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Could not initialize Log4J2 logging from " + location, e);
            }
        }
    }
}
