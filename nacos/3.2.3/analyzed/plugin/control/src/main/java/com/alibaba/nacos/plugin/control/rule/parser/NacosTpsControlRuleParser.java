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

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.control.tps.rule.TpsControlRule;

/**
 * Nacos 默认 TPS 管控规则解析器，使用 JSON 反序列化为 {@link TpsControlRule}。
 *
 * <p>规则内容为空时返回默认空规则，由 TPS 管理器按无限制或默认策略处理。</p>
 *
 * @author xiweng.yy
 */
public class NacosTpsControlRuleParser implements TpsControlRuleParser {
    
    @Override
    public TpsControlRule parseRule(String ruleContent) {
        return StringUtils.isBlank(ruleContent) ? new TpsControlRule()
            : JacksonUtils.toObj(ruleContent, TpsControlRule.class);
    }
    
}
