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

package com.alibaba.nacos.core.code;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.core.listener.NacosApplicationListener;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;
import java.util.Collection;

/**
 * Nacos 自定义 {@link org.springframework.boot.SpringApplicationRunListener}：在 Spring Boot 启动各阶段早于 {@code EventPublishingRunListener} 触发 {@link NacosApplicationListener} SPI。
 * {@link org.springframework.boot.SpringApplicationRunListener} before
 * {@see org.springframework.boot.context.event.EventPublishingRunListener} execution.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.2
 */
public class SpringApplicationRunListener
    implements org.springframework.boot.SpringApplicationRunListener, Ordered {
    
    /** 当前 Spring Boot 应用实例。 */
    private final SpringApplication application;
    
    /** 启动命令行参数。 */
    private final String[] args;
    
    /** 通过 SPI 加载的全部 Nacos 应用生命周期监听器。 */
    Collection<NacosApplicationListener> nacosApplicationListeners =
        NacosServiceLoader.load(NacosApplicationListener.class);
    
    /**
     * Spring Boot 工厂方法构造监听器。
     *
     * @param application Spring 应用
     * @param args 启动参数
     */
    public SpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }
    
    /** 启动最初阶段：通知所有 Nacos 监听器 starting。 */
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        for (NacosApplicationListener nacosApplicationListener : nacosApplicationListeners) {
            nacosApplicationListener.starting();
        }
    }
    
    /** 环境准备完成：通知监听器 environmentPrepared。 */
    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
        ConfigurableEnvironment environment) {
        for (NacosApplicationListener nacosApplicationListener : nacosApplicationListeners) {
            nacosApplicationListener.environmentPrepared(environment);
        }
    }
    
    /** ApplicationContext 创建后、refresh 前：通知 contextPrepared。 */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        for (NacosApplicationListener nacosApplicationListener : nacosApplicationListeners) {
            nacosApplicationListener.contextPrepared(context);
        }
    }
    
    /** Context refresh 后：通知 contextLoaded。 */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        for (NacosApplicationListener nacosApplicationListener : nacosApplicationListeners) {
            nacosApplicationListener.contextLoaded(context);
        }
    }
    
    /** 应用已启动：通知 started。 */
    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        for (NacosApplicationListener nacosApplicationListener : nacosApplicationListeners) {
            nacosApplicationListener.started(context);
        }
    }
    
    /** 应用就绪（含 Web 容器就绪）：通知 ready。 */
    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        for (NacosApplicationListener nacosApplicationListener : nacosApplicationListeners) {
            nacosApplicationListener.ready(context);
        }
    }
    
    /** 启动失败：通知 failed。 */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        for (NacosApplicationListener nacosApplicationListener : nacosApplicationListeners) {
            nacosApplicationListener.failed(context, exception);
        }
    }
    
    /**
     * 优先级设为最高，确保早于 {@code EventPublishingRunListener} 执行。
     *
     * @return HIGHEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
