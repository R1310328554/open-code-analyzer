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

package com.alibaba.nacos.logger.adapter.logback12;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import com.alibaba.nacos.common.logging.NacosLoggingAdapter;
import com.alibaba.nacos.common.logging.NacosLoggingProperties;
import com.alibaba.nacos.common.utils.ResourceUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * Logback 1.0.8～1.2.x 版 Nacos 日志适配器。
 *
 * <p>实现 {@link com.alibaba.nacos.common.logging.NacosLoggingAdapter}， 加载 {@code nacos-logback12.xml} 并在 LoggerContext 重置时自动恢复 Nacos 配置； 通过检测 {@code ch.qos.logback.core.model.Model} 排除 Logback 1.3+。</p>
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @author <a href="mailto:hujun3@xiaomi.com">hujun</a>
 * @author xiweng.yy
 * @since 0.9.0
 */
public class LogbackNacosLoggingAdapter implements NacosLoggingAdapter {
    
    /** Nacos 默认 Logback 1.2 配置文件 classpath 位置。 */
    private static final String NACOS_LOGBACK_LOCATION = "classpath:nacos-logback12.xml";
    
    /** Logback Classic Logger 实现类名，用于 classpath 探测。 */
    private static final String LOGBACK_CLASSES = "ch.qos.logback.classic.Logger";
    
    /** 自定义 Joran 配置器，支持 nacosClientProperty 且不污染用户 savepoint。 */
    private final NacosLogbackConfiguratorAdapterV1 configurator;
    
    /** 构造适配器并初始化 Logback 1.2 配置器。 */
    public LogbackNacosLoggingAdapter() {
        configurator = new NacosLogbackConfiguratorAdapterV1();
    }
    
    /** 判断 Logger 类是否为 Logback 1.2 Classic 且非 1.3+。 */
    @Override
    public boolean isAdaptedLogger(Class<?> loggerClass) {
        Class<?> expectedLoggerClass = getExpectedLoggerClass();
        if (null == expectedLoggerClass || !expectedLoggerClass.isAssignableFrom(loggerClass)) {
            return false;
        }
        return !isUpperLogback13();
    }
    
    private Class<?> getExpectedLoggerClass() {
        try {
            return Class.forName(LOGBACK_CLASSES);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Logback 自 1.3.0 起引入 {@code ch.qos.logback.core.model.Model}， 通过该类是否存在判断是否为 1.3 及以上版本。
     */
    private boolean isUpperLogback13() {
        try {
            Class.forName("ch.qos.logback.core.model.Model");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /** Logback 1.2 适配器无需检测重载，固定返回 false。 */
    @Override
    public boolean isNeedReloadConfiguration() {
        return false;
    }
    
    /** 返回默认 nacos-logback12.xml 位置。 */
    @Override
    public String getDefaultConfigLocation() {
        return NACOS_LOGBACK_LOCATION;
    }
    
    /** 加载指定位置 Logback 配置并注册 Context 重置监听器。 */
    @Override
    public void loadConfiguration(NacosLoggingProperties loggingProperties) {
        String location = loggingProperties.getLocation();
        configurator.setLoggingProperties(loggingProperties);
        LoggerContext loggerContext = loadConfigurationOnStart(location);
        if (hasNoListener(loggerContext)) {
            addListener(loggerContext, location);
        }
    }
    
    private boolean hasNoListener(LoggerContext loggerContext) {
        for (LoggerContextListener loggerContextListener : loggerContext.getCopyOfListenerList()) {
            if (loggerContextListener instanceof NacosLoggerContextListener) {
                return false;
            }
        }
        return true;
    }
    
    private LoggerContext loadConfigurationOnStart(final String location) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        configurator.setContext(loggerContext);
        if (StringUtils.isNotBlank(location)) {
            try {
                boolean isPackagingDataEnabled = loggerContext.isPackagingDataEnabled();
                configurator.configure(ResourceUtils.getResourceUrl(location));
                loggerContext.setPackagingDataEnabled(isPackagingDataEnabled);
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Could not initialize Logback Nacos logging from " + location, e);
            }
        }
        return loggerContext;
    }
    
    /** LoggerContext 监听器：在 reset 后重新加载 Nacos 日志配置。 */
    class NacosLoggerContextListener implements LoggerContextListener {
        
        private final String location;
        
        NacosLoggerContextListener(String location) {
            this.location = location;
        }
        
        /** 标记为 reset  resistant，避免被 Logback 自动移除。 */
        @Override
        public boolean isResetResistant() {
            return true;
        }
        
        /** Context 重置时按原 location 重新加载 Nacos 配置。 */
        @Override
        public void onReset(LoggerContext context) {
            loadConfigurationOnStart(location);
        }
        
        @Override
        public void onStart(LoggerContext context) {
        }
        
        @Override
        public void onStop(LoggerContext context) {
        }
        
        @Override
        public void onLevelChange(Logger logger, Level level) {
        }
    }
    
    private void addListener(LoggerContext loggerContext, String location) {
        loggerContext.addListener(new NacosLoggerContextListener(location));
    }
    
}
