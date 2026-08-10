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

package com.alibaba.nacos.client.logging;

import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.logging.NacosLoggingAdapter;
import com.alibaba.nacos.common.logging.NacosLoggingAdapterBuilder;
import com.alibaba.nacos.common.logging.NacosLoggingProperties;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Nacos 客户端日志适配与配置加载。
 *
 * <p>通过 SPI 发现 {@link NacosLoggingAdapterBuilder}，匹配当前 SLF4J 实现并加载 Nacos 专用日志配置；支持定时热重载。</p>
 *
 * @author mai.jh
 */
public class NacosLogging {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosLogging.class);
    
    /** 已选中的日志适配器实现。 */
    private NacosLoggingAdapter loggingAdapter;
    
    /** 日志配置属性（路径与重载间隔等）。 */
    private NacosLoggingProperties loggingProperties;
    
    private NacosLogging() {
        initLoggingAdapter();
    }
    
    /** 扫描 SPI 构建器，选择首个适配当前 Logger 类型的适配器。 */
    private void initLoggingAdapter() {
        Class<? extends Logger> loggerClass = LOGGER.getClass();
        for (NacosLoggingAdapterBuilder each : NacosServiceLoader
            .load(NacosLoggingAdapterBuilder.class)) {
            LOGGER.info("Nacos Logging Adapter Builder: {}", each.getClass().getName());
            NacosLoggingAdapter tempLoggingAdapter = buildLoggingAdapterFromBuilder(each);
            if (isAdaptLogging(tempLoggingAdapter, loggerClass)) {
                LOGGER.info("Nacos Logging Adapter: {} match {} success.",
                    tempLoggingAdapter.getClass().getName(),
                    loggerClass.getName());
                loggingProperties =
                    new NacosLoggingProperties(tempLoggingAdapter.getDefaultConfigLocation(),
                        NacosClientProperties.PROTOTYPE.asProperties());
                loggingAdapter = tempLoggingAdapter;
            }
        }
        if (null == loggingAdapter) {
            LOGGER.warn(
                "Nacos Logging don't find adapter, logging will print into application logs.");
            return;
        }
        scheduleReloadTask();
    }
    
    /** 从构建器实例化适配器，失败时记录警告并返回 null。 */
    private NacosLoggingAdapter buildLoggingAdapterFromBuilder(NacosLoggingAdapterBuilder builder) {
        try {
            return builder.build();
        } catch (Throwable e) {
            LOGGER.warn("Build Nacos Logging Adapter failed: {}", e.getMessage());
            return null;
        }
    }
    
    /** 判断适配器是否启用且能适配给定 Logger 类型。 */
    private boolean isAdaptLogging(NacosLoggingAdapter loggingAdapter,
        Class<? extends Logger> loggerClass) {
        return null != loggingAdapter && loggingAdapter.isEnabled()
            && loggingAdapter.isAdaptedLogger(loggerClass);
    }
    
    /** 启动定时任务，按配置间隔热重载日志配置。 */
    private void scheduleReloadTask() {
        ScheduledExecutorService reloadContextService = ExecutorFactory.Managed
            .newSingleScheduledExecutorService("Nacos-Client",
                new NameThreadFactory("com.alibaba.nacos.client.logging"));
        reloadContextService.scheduleAtFixedRate(() -> {
            if (loggingAdapter.isNeedReloadConfiguration()) {
                loggingAdapter.loadConfiguration(loggingProperties);
            }
        }, 0, loggingProperties.getReloadInternal(), TimeUnit.SECONDS);
    }
    
    /** 静态内部类持有单例，实现懒加载。 */
    private static class NacosLoggingInstance {
        
        private static final NacosLogging INSTANCE = new NacosLogging();
    }
    
    /** 获取 Nacos 日志管理单例。 */
    public static NacosLogging getInstance() {
        return NacosLoggingInstance.INSTANCE;
    }
    
    /**
     * 加载 Nacos 日志配置到当前适配器。
     *
     * <p>无可用适配器时静默跳过；异常仅记录警告。</p>
     */
    public void loadConfiguration() {
        try {
            if (null != loggingAdapter) {
                loggingAdapter.loadConfiguration(loggingProperties);
            }
        } catch (Throwable t) {
            LOGGER.warn("Load {} Configuration of Nacos fail, message: {}",
                LOGGER.getClass().getName(),
                t.getMessage());
        }
    }
}
