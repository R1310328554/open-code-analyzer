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

package com.alibaba.nacos.core.listener;

import com.alibaba.nacos.core.code.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Nacos 应用生命周期监听器：对齐 Spring Boot 启动各阶段，供 {@link StartingApplicationListener} 等实现初始化逻辑。
 * Nacos Application Listener, execute init process.
 *
 * @author horizonzy
 * @since 1.4.1
 */
public interface NacosApplicationListener {
    
    /**
     * 应用开始启动，对应 {@link SpringApplicationRunListener#starting}。
     */
    default void starting() {
    }
    
    /**
     * Environment 准备完成，对应 {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#environmentPrepared}。
     *
     * @param environment environment
     */
    default void environmentPrepared(ConfigurableEnvironment environment) {
    }
    
    /**
     * ApplicationContext 创建完成，对应 contextPrepared 阶段。
     *
     * @param context context
     */
    default void contextPrepared(ConfigurableApplicationContext context) {
    }
    
    /**
     * Context 加载完成，对应 {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#contextLoaded}。
     *
     * @param context context
     */
    default void contextLoaded(ConfigurableApplicationContext context) {
    }
    
    /**
     * 应用已启动，对应 {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#started}。
     *
     * @param context context
     */
    default void started(ConfigurableApplicationContext context) {
    }
    
    /**
     * 应用就绪，对应 {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#ready}。
     *
     * @param context context
     */
    default void ready(ConfigurableApplicationContext context) {
    }
    
    /**
     * 启动失败回调，对应 {@link com.alibaba.nacos.core.code.SpringApplicationRunListener#failed}。
     *
     * @param context   context
     * @param exception exception
     */
    default void failed(ConfigurableApplicationContext context, Throwable exception) {
    }
}
