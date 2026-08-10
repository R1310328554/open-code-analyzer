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

package com.alibaba.nacos.plugin.control.rule.storage;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.configs.ControlConfigs;
import com.alibaba.nacos.plugin.control.rule.ControlRuleChangeActivator;
import com.alibaba.nacos.plugin.control.spi.ExternalRuleStorageBuilder;
import org.slf4j.Logger;

import java.util.Collection;

/**
 * 规则存储代理，统一管理本地磁盘与外部持久化两种规则存储后端。
 *
 * <p>构造时按配置决定是否通过 SPI 加载外部存储，并始终初始化本地磁盘存储；
 * 同时创建 {@link ControlRuleChangeActivator} 以监听规则变更。</p>
 *
 * @author shiyiyue
 */
public class RuleStorageProxy {
    
    private static final Logger LOGGER = Loggers.CONTROL;
    
    /** 单例实例。 */
    private static final RuleStorageProxy INSTANCE = new RuleStorageProxy();
    
    /** 本地磁盘规则存储，始终可用。 */
    private LocalDiskRuleStorage localDiskRuleStorage = null;
    
    /** 外部规则存储，按配置通过 SPI 构建，可为空。 */
    private ExternalRuleStorage externalRuleStorage = null;
    
    /** 规则变更激活器，负责规则热更新通知。 */
    ControlRuleChangeActivator controlRuleChangeActivator = null;
    
    private RuleStorageProxy() {
        String externalStorageType = ControlConfigs.getInstance().getRuleExternalStorage();
        if (StringUtils.isNotEmpty(externalStorageType)) {
            buildExternalStorage(externalStorageType);
        }
        initLocalStorage();
        controlRuleChangeActivator = new ControlRuleChangeActivator();
    }
    
    /** 通过 SPI 按名称匹配并构建外部规则存储实现。 */
    private void buildExternalStorage(String externalStorageType) {
        Collection<ExternalRuleStorageBuilder> externalRuleStorageBuilders = NacosServiceLoader
            .load(ExternalRuleStorageBuilder.class);
        for (ExternalRuleStorageBuilder each : externalRuleStorageBuilders) {
            LOGGER.info("Found persist rule storage of name : {}", externalStorageType);
            if (externalStorageType.equalsIgnoreCase(each.getName())) {
                try {
                    externalRuleStorage = each.buildExternalRuleStorage();
                } catch (Exception e) {
                    LOGGER.warn(
                        "Build external rule storage failed, the rules will not be persisted", e);
                }
                LOGGER.info("Build external rule storage of name {} finished", externalStorageType);
                break;
            }
        }
        if (externalRuleStorage == null && StringUtils.isNotBlank(externalStorageType)) {
            LOGGER.error("Fail to found persist rule storage of name : {}", externalStorageType);
        }
    }
    
    /** 初始化本地磁盘存储，并按配置设置规则根目录。 */
    private void initLocalStorage() {
        localDiskRuleStorage = new LocalDiskRuleStorage();
        if (StringUtils.isNotBlank(ControlConfigs.getInstance().getLocalRuleStorageBaseDir())) {
            localDiskRuleStorage
                .setLocalRuleBaseDir(ControlConfigs.getInstance().getLocalRuleStorageBaseDir());
        }
    }
    
    /**
     * 获取本地磁盘规则存储。
     *
     * @return 本地磁盘存储实例
     */
    public RuleStorage getLocalDiskStorage() {
        return localDiskRuleStorage;
    }
    
    /**
     * 获取外部规则存储，未配置或未构建成功时返回 {@code null}。
     *
     * @return 外部存储实例，可能为空
     */
    public RuleStorage getExternalStorage() {
        return externalRuleStorage;
    }
    
    /**
     * 获取规则存储代理单例。
     *
     * @return 代理实例
     */
    public static RuleStorageProxy getInstance() {
        return INSTANCE;
    }
}
