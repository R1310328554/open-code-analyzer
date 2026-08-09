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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * 热点参数流控规则实体，包装 {@link ParamFlowRule}。
 * <p>通过 {@link JsonIgnore} getter 暴露参数索引、阈值、例外项及集群配置等字段。
 *
 * @author Eric Zhao
 * @since 0.2.1
 */
public class ParamFlowRuleEntity extends AbstractRuleEntity<ParamFlowRule> {

    /** 无参构造，供 JSON 反序列化使用。 */
    public ParamFlowRuleEntity() {
    }

    /** @param rule 非空的热点参数流控规则 */
    public ParamFlowRuleEntity(ParamFlowRule rule) {
        AssertUtil.notNull(rule, "Authority rule should not be null");
        this.rule = rule;
    }

    /** 从客户端 {@link ParamFlowRule} 构建带机器绑定的 Dashboard 实体。 */
    public static ParamFlowRuleEntity fromParamFlowRule(String app, String ip, Integer port, ParamFlowRule rule) {
        ParamFlowRuleEntity entity = new ParamFlowRuleEntity(rule);
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        return entity;
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public String getLimitApp() {
        return rule.getLimitApp();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public String getResource() {
        return rule.getResource();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public int getGrade() {
        return rule.getGrade();
    }

    /** @return 热点参数在方法参数列表中的索引 */
    @JsonIgnore
    @JSONField(serialize = false)
    public Integer getParamIdx() {
        return rule.getParamIdx();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public double getCount() {
        return rule.getCount();
    }

    /** @return 参数例外项列表（特定参数值单独限流） */
    @JsonIgnore
    @JSONField(serialize = false)
    public List<ParamFlowItem> getParamFlowItemList() {
        return rule.getParamFlowItemList();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public int getControlBehavior() {
        return rule.getControlBehavior();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public int getMaxQueueingTimeMs() {
        return rule.getMaxQueueingTimeMs();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public int getBurstCount() {
        return rule.getBurstCount();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public long getDurationInSec() {
        return rule.getDurationInSec();
    }

    /** @return 是否启用集群热点参数流控 */
    @JsonIgnore
    @JSONField(serialize = false)
    public boolean isClusterMode() {
        return rule.isClusterMode();
    }

    @JsonIgnore
    @JSONField(serialize = false)
    public ParamFlowClusterConfig getClusterConfig() {
        return rule.getClusterConfig();
    }
}
