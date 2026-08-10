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

/**
 * 管控规则持久化 SPI，定义连接限流与 TPS 规则的读写契约。
 *
 * <p>实现包括 {@link LocalDiskRuleStorage} 与 {@link ExternalRuleStorage} 等。</p>
 *
 * @author shiyiyue
 * @date 2022-10-26 11:43:00
 */
public interface RuleStorage {
    
    /**
     * 获取存储实现名称，用于日志与 SPI 识别。
     *
     * @return 存储名称
     */
    String getName();
    
    /**
     * 将连接限流规则持久化到存储。
     *
     * @param ruleContent 规则内容（通常为 JSON 字符串）
     * @throws Exception 写入失败时抛出
     */
    void saveConnectionRule(String ruleContent) throws Exception;
    
    /**
     * 读取当前生效的连接限流规则文本。
     *
     * @return 规则内容，不存在时可为 null
     */
    String getConnectionRule();
    
    /**
     * 将指定 TPS 限流点的规则持久化到存储。
     *
     * @param pointName   TPS 限流点名称
     * @param ruleContent 规则内容；null 表示删除该点规则
     * @throws Exception 写入失败时抛出
     */
    void saveTpsRule(String pointName, String ruleContent) throws Exception;
    
    /**
     * 读取指定 TPS 限流点的规则文本。
     *
     * @param pointName TPS 限流点名称
     * @return 规则内容，不存在时可为 null
     */
    String getTpsRule(String pointName);
    
}
