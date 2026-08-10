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

package com.alibaba.nacos.plugin.control.spi;

import com.alibaba.nacos.plugin.control.rule.storage.ExternalRuleStorage;

/**
 * Nacos 管控插件外部规则存储构建 SPI。
 *
 * <p>允许将 TPS 与连接限制规则持久化到外部介质（如数据库、配置中心），
 * 由 {@link com.alibaba.nacos.plugin.control.rule.storage.RuleStorageProxy} 按配置加载。</p>
 *
 * @author xiweng.yy
 */
public interface ExternalRuleStorageBuilder {
    
    /**
     * 获取外部存储插件名称，与配置项 {@code ruleExternalStorage} 匹配。
     *
     * @return 插件名称
     */
    String getName();
    
    /**
     * 构建当前插件的 {@link ExternalRuleStorage} 实现。
     *
     * @return 外部规则存储实例
     */
    ExternalRuleStorage buildExternalRuleStorage();
}
