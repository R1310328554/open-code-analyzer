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

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Nacos 专用 Log4j2 配置加载器。
 *
 * <p>以框架合规方式将 Nacos 日志配置增量合并到现有 {@link LoggerContext}， 不替换用户应用日志；设计模式与 Logback 版 {@code NacosLogbackConfiguratorAdapterV1} 一致。</p>
 *
 * <p>要点：</p>
 * <ul>
 *   <li>使用 {@link Configuration#initialize()} 而非 {@link Configuration#start()}， 避免 ClassUnload 问题（#13940）</li>
 *   <li>仅追加 Nacos Appender 与 {@code com.alibaba.nacos} 包 Logger</li>
 *   <li>非侵入式，保留用户原有日志配置</li>
 * </ul>
 *
 * @author xiweng.yy
 * @see <a href="https://github.com/alibaba/nacos/issues/13940">#13940</a>
 * @since 3.2.0
 */
public class NacosLog4j2Configurator {
    
    /** 仅合并该包前缀下的 Logger 配置。 */
    private static final String NACOS_LOGGER_PREFIX = "com.alibaba.nacos";
    
    /**
     * 从 URI 加载 Nacos Log4j2 配置并增量合并到现有 LoggerContext。
     *
     * @param loggerContext 待配置的 LoggerContext
     * @param configLocation Nacos log4j2 配置文件 URI
     * @throws IOException 配置文件无法读取时抛出
     */
    public void configure(LoggerContext loggerContext, URI configLocation) throws IOException {
        Configuration nacosConfig = loadConfiguration(loggerContext, configLocation);
        
        // 修复 #13940：使用 initialize() 而非 start() 避免插件重复初始化
        // initialize() sets up the configuration without triggering plugin reinitialization
        nacosConfig.initialize();
        
        // 获取当前活跃的 LoggerContext 配置
        Configuration currentConfig = loggerContext.getConfiguration();
        
        // 增量合并 Nacos Appender，中间件非侵入式接入
        // Appender 单独启动后注册到 currentConfig，不从 nacosConfig 移除
        // They are NOT removed from nacosConfig to avoid lifecycle issues
        nacosConfig.getAppenders().values().forEach(appender -> {
            if (!appender.isStarted()) {
                appender.start();
            }
            currentConfig.addAppender(appender);
        });
        
        // 仅追加 com.alibaba.nacos 包 Logger，避免覆盖用户配置
        nacosConfig.getLoggers().entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(NACOS_LOGGER_PREFIX))
            .forEach(entry -> currentConfig.addLogger(entry.getKey(), entry.getValue()));
        
        // 刷新 Logger 引用使合并生效
        loggerContext.updateLoggers();
        
        // 切勿调用 nacosConfig.stop()，Appender 已转移给 currentConfig
        // Appender 与 Logger 已归属 currentConfig，由 GC 回收 nacosConfig
        // Calling stop() would shut down the appenders that are now owned by currentConfig.
        // 仅 initialize 未 start，无后台线程需清理
        // (not start()), there are no active background threads or resources to clean up.
    }
    
    /**
     * 通过 {@link ConfigurationFactory} 从 URI 解析 Log4j2 配置。
     *
     * @param ctx LoggerContext 上下文
     * @param configLocation 配置文件 URI
     * @return 解析后的 Configuration 对象
     * @throws IOException 加载失败时抛出
     */
    private Configuration loadConfiguration(LoggerContext ctx, URI configLocation)
        throws IOException {
        try (InputStream stream = configLocation.toURL().openStream()) {
            ConfigurationSource source = new ConfigurationSource(stream, configLocation.toURL());
            return ConfigurationFactory.getInstance().getConfiguration(ctx, source);
        }
    }
}
