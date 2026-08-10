/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.config;

import com.alibaba.nacos.common.event.ServerConfigChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.core.utils.Loggers;

/**
 * 动态配置抽象基类：订阅 {@link ServerConfigChangeEvent}，在服务端配置变更时从环境重新加载并记录日志。
 * Nacos abstract dynamic config.
 *
 * @author xiweng.yy
 */
public abstract class AbstractDynamicConfig extends Subscriber<ServerConfigChangeEvent> {
    
    /** 配置项名称，用于日志标识。 */
    private final String configName;
    
    /**
     * 注册为 NotifyCenter 订阅者并保存配置名。
     *
     * @param configName 配置模块名称
     */
    protected AbstractDynamicConfig(String configName) {
        this.configName = configName;
        NotifyCenter.registerSubscriber(this);
    }
    
    /** 收到服务端配置变更事件后触发 {@link #resetConfig()}。 */
    @Override
    public void onEvent(ServerConfigChangeEvent event) {
        resetConfig();
    }
    
    /** 订阅 {@link ServerConfigChangeEvent} 类型。 */
    @Override
    public Class<? extends Event> subscribeType() {
        return ServerConfigChangeEvent.class;
    }
    
    /** 从环境重新加载配置；失败时保留旧值并打 WARN 日志。 */
    protected void resetConfig() {
        try {
            getConfigFromEnv();
            Loggers.CORE.info("Get {} config from env, {}", configName, printConfig());
        } catch (Exception e) {
            Loggers.CORE.warn("Upgrade {} config from env failed, will use old value", configName,
                e);
        }
    }
    
    /**
     * 子类实现：从环境变量或配置文件实际读取并更新内存中的配置字段。
     */
    protected abstract void getConfigFromEnv();
    
    /**
     * 子类实现：返回当前配置的可读摘要，供日志输出。
     *
     * @return config content
     */
    protected abstract String printConfig();
}
