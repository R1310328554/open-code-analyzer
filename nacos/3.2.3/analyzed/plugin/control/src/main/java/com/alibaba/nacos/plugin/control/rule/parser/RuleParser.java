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

package com.alibaba.nacos.plugin.control.rule.parser;

/**
 * 管控规则通用解析器接口，将原始字符串解析为强类型规则对象。
 *
 * @param <R> 解析后的规则类型
 * @author shiyiyue
 */
public interface RuleParser<R> {
    
    /**
     * 解析管控规则文本。
     *
     * @param ruleContent 规则原始字符串（通常为 JSON）
     * @return 解析后的规则对象
     */
    R parseRule(String ruleContent);
}
