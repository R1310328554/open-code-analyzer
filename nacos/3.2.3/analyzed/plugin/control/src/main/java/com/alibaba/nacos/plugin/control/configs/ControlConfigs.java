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

package com.alibaba.nacos.plugin.control.configs;

import com.alibaba.nacos.common.spi.NacosServiceLoader;

import java.util.Collection;

/**
 * 管控插件全局配置参数容器。
 *
 * <p>单例持有连接运行时剔除器、规则外部存储、本地规则目录及管控管理器类型等配置项，
 * 首次获取时通过 SPI 加载 {@link ControlConfigsInitializer} 完成初始化。</p>
 *
 * @author shiyiyue
 */
public class ControlConfigs {
    
    /** 单例实例。 */
    private static volatile ControlConfigs instance = null;
    
    /**
     * 获取配置单例，首次调用时触发 SPI 初始化。
     *
     * @return 管控配置实例
     */
    public static ControlConfigs getInstance() {
        if (instance == null) {
            synchronized (ControlConfigs.class) {
                if (instance == null) {
                    instance = new ControlConfigs();
                    Collection<ControlConfigsInitializer> load = NacosServiceLoader
                        .load(ControlConfigsInitializer.class);
                    for (ControlConfigsInitializer controlConfigsInitializer : load) {
                        controlConfigsInitializer.initialize(instance);
                    }
                }
            }
        }
        
        return instance;
    }
    
    /**
     * 设置配置单例，主要用于测试注入。
     *
     * @param instance 配置实例
     */
    public static void setInstance(ControlConfigs instance) {
        ControlConfigs.instance = instance;
    }
    
    /** 连接运行时剔除器标识，默认 {@code nacos}。 */
    private String connectionRuntimeEjector = "nacos";
    
    /** 规则外部存储实现名称，空表示不使用外部存储。 */
    private String ruleExternalStorage = "";
    
    /** 本地规则文件存储根目录。 */
    private String localRuleStorageBaseDir = "";
    
    /** 管控管理器 SPI 实现类型名称。 */
    private String controlManagerType = "";
    
    /**
     * 获取规则外部存储实现名称。
     *
     * @return 外部存储名称
     */
    public String getRuleExternalStorage() {
        return ruleExternalStorage;
    }
    
    /**
     * 设置规则外部存储实现名称。
     *
     * @param ruleExternalStorage 外部存储名称
     */
    public void setRuleExternalStorage(String ruleExternalStorage) {
        this.ruleExternalStorage = ruleExternalStorage;
    }
    
    /**
     * 获取连接运行时剔除器标识。
     *
     * @return 剔除器名称
     */
    public String getConnectionRuntimeEjector() {
        return connectionRuntimeEjector;
    }
    
    /**
     * 设置连接运行时剔除器标识。
     *
     * @param connectionRuntimeEjector 剔除器名称
     */
    public void setConnectionRuntimeEjector(String connectionRuntimeEjector) {
        this.connectionRuntimeEjector = connectionRuntimeEjector;
    }
    
    /**
     * 获取本地规则存储根目录。
     *
     * @return 本地目录路径
     */
    public String getLocalRuleStorageBaseDir() {
        return localRuleStorageBaseDir;
    }
    
    /**
     * 设置本地规则存储根目录。
     *
     * @param localRuleStorageBaseDir 本地目录路径
     */
    public void setLocalRuleStorageBaseDir(String localRuleStorageBaseDir) {
        this.localRuleStorageBaseDir = localRuleStorageBaseDir;
    }
    
    /**
     * 获取管控管理器 SPI 实现类型。
     *
     * @return 管理器类型名称
     */
    public String getControlManagerType() {
        return controlManagerType;
    }
    
    /**
     * 设置管控管理器 SPI 实现类型。
     *
     * @param controlManagerType 管理器类型名称
     */
    public void setControlManagerType(String controlManagerType) {
        this.controlManagerType = controlManagerType;
    }
}
