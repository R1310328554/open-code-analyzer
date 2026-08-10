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

package com.alibaba.nacos.plugin.control;

import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.control.configs.ControlConfigs;
import com.alibaba.nacos.plugin.control.connection.ConnectionControlManager;
import com.alibaba.nacos.plugin.control.connection.DefaultConnectionControlManager;
import com.alibaba.nacos.plugin.control.event.ConnectionLimitRuleChangeEvent;
import com.alibaba.nacos.plugin.control.event.TpsControlRuleChangeEvent;
import com.alibaba.nacos.plugin.control.rule.storage.RuleStorageProxy;
import com.alibaba.nacos.plugin.control.spi.ControlManagerBuilder;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.DefaultTpsControlManager;

import java.util.Optional;

/**
 * 流量与连接管控中心。
 *
 * <p>单例入口，负责初始化 {@link TpsControlManager} 与 {@link ConnectionControlManager}，
 * 通过 SPI 加载 {@link ControlManagerBuilder} 构建具体实现；未配置或构建失败时降级为无限制默认实现。</p>
 *
 * @author shiyiyue
 */
public class ControlManagerCenter {
    
    /** 单例实例，双重检查锁延迟初始化。 */
    static volatile ControlManagerCenter instance = null;
    
    /** 规则存储代理，统一管理本地与外部规则持久化。 */
    private final RuleStorageProxy ruleStorageProxy;
    
    /** TPS 限流管理器。 */
    private TpsControlManager tpsControlManager;
    
    /** 连接数管控管理器。 */
    private ConnectionControlManager connectionControlManager;
    
    private ControlManagerCenter() {
        ruleStorageProxy = RuleStorageProxy.getInstance();
        Optional<ControlManagerBuilder> controlManagerBuilder = findTargetControlManagerBuilder();
        if (controlManagerBuilder.isPresent()) {
            initConnectionManager(controlManagerBuilder.get());
            initTpsControlManager(controlManagerBuilder.get());
        } else {
            buildNoLimitControlManagers();
        }
    }
    
    /** 通过 SPI 构建器初始化连接管控管理器，失败时降级为默认无限制实现。 */
    private void initConnectionManager(ControlManagerBuilder controlManagerBuilder) {
        try {
            connectionControlManager = controlManagerBuilder.buildConnectionControlManager();
            Loggers.CONTROL.info("Build connection control manager, class={}",
                connectionControlManager.getClass().getCanonicalName());
        } catch (Exception e) {
            Loggers.CONTROL
                .warn("Build connection control manager failed, use no limit manager replaced.", e);
            connectionControlManager = new DefaultConnectionControlManager();
        }
    }
    
    /** 通过 SPI 构建器初始化 TPS 限流管理器，失败时降级为默认无限制实现。 */
    private void initTpsControlManager(ControlManagerBuilder controlManagerBuilder) {
        try {
            tpsControlManager = controlManagerBuilder.buildTpsControlManager();
            Loggers.CONTROL
                .info("Build tps control manager, class={}",
                    tpsControlManager.getClass().getCanonicalName());
        } catch (Exception e) {
            Loggers.CONTROL.warn("Build tps control manager failed, use no limit manager replaced.",
                e);
            tpsControlManager = new DefaultTpsControlManager();
        }
    }
    
    /**
     * 按配置项 {@code controlManagerType} 查找匹配的 SPI 构建器。
     *
     * @return 匹配的构建器，未配置或未找到时为空
     */
    private Optional<ControlManagerBuilder> findTargetControlManagerBuilder() {
        String controlManagerType = ControlConfigs.getInstance().getControlManagerType();
        if (StringUtils.isEmpty(controlManagerType)) {
            Loggers.CONTROL
                .info("Not configure type of control plugin, no limit control for current node.");
            return Optional.empty();
        }
        for (ControlManagerBuilder each : NacosServiceLoader.load(ControlManagerBuilder.class)) {
            Loggers.CONTROL.info("Found control manager plugin of name={}", each.getName());
            if (controlManagerType.equalsIgnoreCase(each.getName())) {
                return Optional.of(each);
            }
        }
        Loggers.CONTROL.warn("Not found control manager plugin of name");
        return Optional.empty();
    }
    
    /** 使用默认无限制实现初始化连接与 TPS 管理器。 */
    private void buildNoLimitControlManagers() {
        connectionControlManager = new DefaultConnectionControlManager();
        tpsControlManager = new DefaultTpsControlManager();
    }
    
    /**
     * 获取规则存储代理。
     *
     * @return 规则存储代理实例
     */
    public RuleStorageProxy getRuleStorageProxy() {
        return ruleStorageProxy;
    }
    
    /**
     * 获取 TPS 限流管理器。
     *
     * @return TPS 管控管理器
     */
    public TpsControlManager getTpsControlManager() {
        return tpsControlManager;
    }
    
    /**
     * 获取连接数管控管理器。
     *
     * @return 连接管控管理器
     */
    public ConnectionControlManager getConnectionControlManager() {
        return connectionControlManager;
    }
    
    /**
     * 获取管控中心单例。
     *
     * @return 管控中心实例
     */
    public static ControlManagerCenter getInstance() {
        if (instance == null) {
            synchronized (ControlManagerCenter.class) {
                if (instance == null) {
                    instance = new ControlManagerCenter();
                }
            }
        }
        return instance;
    }
    
    /**
     * 发布 TPS 限流规则变更事件，触发规则热重载。
     *
     * @param pointName 限流点名称
     * @param external  是否来自外部存储
     */
    public void reloadTpsControlRule(String pointName, boolean external) {
        NotifyCenter.publishEvent(new TpsControlRuleChangeEvent(pointName, external));
    }
    
    /**
     * 发布连接数限制规则变更事件，触发规则热重载。
     *
     * @param external 是否来自外部存储
     */
    public void reloadConnectionControlRule(boolean external) {
        NotifyCenter.publishEvent(new ConnectionLimitRuleChangeEvent(external));
    }
    
}
