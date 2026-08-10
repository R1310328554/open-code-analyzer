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

package com.alibaba.nacos.core.listener.startup;

import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Nacos 多阶段启动 SPI 接口，定义 core/web/console/ai-registry 各阶段的启动生命周期钩子。
 * <p>通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 发现实现并由 {@link NacosStartUpManager} 按阶段调度。</p>
 * Nacos start up phases.
 *
 * @author xiweng.yy
 */
public interface NacosStartUp {
    
    /** 核心模块启动阶段标识。 */
    String CORE_START_UP_PHASE = "core";
    
    /** Web API 模块启动阶段标识。 */
    String WEB_START_UP_PHASE = "web";
    
    /** 控制台模块启动阶段标识。 */
    String CONSOLE_START_UP_PHASE = "console";
    
    /** AI Registry 模块启动阶段标识。 */
    String AI_REGISTRY_START_UP_PHASE = "ai-registry";
    
    /**
     * 返回本实现负责的启动阶段名称。
     *
     * @return {@link #CORE_START_UP_PHASE} or {@link #WEB_START_UP_PHASE} or {@link #CONSOLE_START_UP_PHASE}.
     */
    String startUpPhase();
    
    /**
     * 阶段开始启动：初始化启动状态与日志调度。
     */
    void starting();
    
    /**
     * 按需创建工作目录（如 logs、conf、data）。
     * @return created work dirs
     */
    default String[] makeWorkDir() {
        return new String[0];
    }
    
    /**
     * 将 Spring {@link ConfigurableEnvironment} 注入 Nacos 环境工具。
     *
     * @param environment environment
     */
    default void injectEnvironment(ConfigurableEnvironment environment) {
    }
    
    /**
     * 预加载配置文件到 Environment（如 application.properties）。
     *
     * @param environment environment
     */
    default void loadPreProperties(ConfigurableEnvironment environment) {
    }
    
    /**
     * 初始化 JVM 系统属性（模式、本机 IP 等）。
     */
    default void initSystemProperty() {
    }
    
    /**
     * 启动过程中输出周期性或阶段性日志。
     *
     * @param logger logger for print info
     */
    void logStartingInfo(Logger logger);
    
    /**
     * 若需自定义环境插件，在此方法中扩展 Environment。
     */
    default void customEnvironment() {
    }
    
    /**
     * 本阶段启动完成，清理启动中状态。
     */
    void started();
    
    /**
     * 启动成功后输出汇总信息（模式、耗时等）。
     *
     * @param logger logger for print info
     */
    void logStarted(Logger logger);
    
    /**
     * 启动失败时关闭相关资源并关闭 Spring 上下文。
     *
     * @param exception exception during start up
     * @param context current application context
     */
    void failed(Throwable exception, ConfigurableApplicationContext context);
}
