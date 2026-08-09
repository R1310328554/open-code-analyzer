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
package com.alibaba.csp.sentinel.command.handler;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;

/**
 * 按类型查询当前生效的规则列表（流控/降级/授权/系统），返回 JSON。
 * 请求参数 {@code type} 取值：flow、degrade、authority、system。
 *
 * @author jialiang.linjl
 */
@CommandMapping(name = "getRules", desc = "按类型获取生效规则，参数 type={ruleType}")
public class FetchActiveRuleCommandHandler implements CommandHandler<String> {

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String type = request.getParam("type");
        // 流控规则
        if ("flow".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(JSON.toJSONString(FlowRuleManager.getRules()));
        // 降级规则
        } else if ("degrade".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(JSON.toJSONString(DegradeRuleManager.getRules()));
        // 授权规则
        } else if ("authority".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(JSON.toJSONString(AuthorityRuleManager.getRules()));
        // 系统规则
        } else if ("system".equalsIgnoreCase(type)) {
            return CommandResponse.ofSuccess(JSON.toJSONString(SystemRuleManager.getRules()));
        } else {
            return CommandResponse.ofFailure(new IllegalArgumentException("无效的规则类型 type"));
        }
    }

}
