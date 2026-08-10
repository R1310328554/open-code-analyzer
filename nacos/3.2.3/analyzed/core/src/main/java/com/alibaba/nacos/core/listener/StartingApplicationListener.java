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

import com.alibaba.nacos.core.listener.startup.NacosStartUp;
import com.alibaba.nacos.core.listener.startup.NacosStartUpManager;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.nio.file.Paths;

/**
 * Nacos 启动主监听器：委托 {@link com.alibaba.nacos.core.listener.startup.NacosStartUpManager} 在各生命周期阶段完成工作目录、环境注入、属性加载与启动日志。
 * init environment config.
 *
 * @author <a href="mailto:huangxiaoyu1018@gmail.com">hxy1991</a>
 * @since 0.5.0
 */
public class StartingApplicationListener implements NacosApplicationListener {
    
    /** 启动过程日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(StartingApplicationListener.class);
    
    /** {@inheritDoc} 触发当前 {@link com.alibaba.nacos.core.listener.startup.NacosStartUp} 的 starting 钩子。 */
    @Override
    public void starting() {
        NacosStartUpManager.getCurrentStartUp().starting();
    }
    
    /** {@inheritDoc} 创建工作目录、注入环境、加载预置属性并初始化系统属性。 */
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        NacosStartUp currentStartUp = NacosStartUpManager.getCurrentStartUp();
        currentStartUp.makeWorkDir();
        currentStartUp.injectEnvironment(environment);
        currentStartUp.loadPreProperties(environment);
        currentStartUp.initSystemProperty();
    }
    
    /** {@inheritDoc} 输出启动前信息日志。 */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        NacosStartUpManager.getCurrentStartUp().logStartingInfo(LOGGER);
    }
    
    /** {@inheritDoc} 自定义 Environment 配置。 */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        NacosStartUpManager.getCurrentStartUp().customEnvironment();
    }
    
    /** {@inheritDoc} 完成启动并记录 started 日志。 */
    @Override
    public void started(ConfigurableApplicationContext context) {
        NacosStartUp currentStartUp = NacosStartUpManager.getCurrentStartUp();
        currentStartUp.started();
        currentStartUp.logStarted(LOGGER);
    }
    
    /** {@inheritDoc} 逆序通知已启动模块失败，并提示查看 nacos.log。 */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        for (NacosStartUp each : NacosStartUpManager.getReverseStartedList()) {
            each.failed(exception, context);
        }
        LOGGER.error("Startup errors : ", exception);
        LOGGER.error("Nacos failed to start, please see {} for more details.",
            Paths.get(EnvUtil.getNacosHome(), "logs/nacos.log"));
    }
}
