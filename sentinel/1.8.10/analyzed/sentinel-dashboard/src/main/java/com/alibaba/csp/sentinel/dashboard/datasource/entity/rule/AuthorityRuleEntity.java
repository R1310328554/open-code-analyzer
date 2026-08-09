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

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 授权（黑白名单）规则实体，包装 {@link AuthorityRule}。
 * <p>提供从客户端规则到 Dashboard 实体的工厂方法；
 * 带 {@link JsonIgnore} 的 getter 便于 API 层读取规则字段而不重复序列化 rule 对象。
 *
 * @author Eric Zhao
 * @since 0.2.1
 */
public class AuthorityRuleEntity extends AbstractRuleEntity<AuthorityRule> {

    /** 无参构造，供 JSON 反序列化使用。 */
    public AuthorityRuleEntity() {
    }

    /** @param authorityRule 非空的授权规则对象 */
    public AuthorityRuleEntity(AuthorityRule authorityRule) {
        AssertUtil.notNull(authorityRule, "Authority rule should not be null");
        this.rule = authorityRule;
    }

    /** 从客户端 {@link AuthorityRule} 构建带机器绑定的 Dashboard 实体。 */
    public static AuthorityRuleEntity fromAuthorityRule(String app, String ip, Integer port, AuthorityRule rule) {
        AuthorityRuleEntity entity = new AuthorityRuleEntity(rule);
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        return entity;
    }

    /** @return 受控来源应用（limitApp） */
    @JsonIgnore
    @JSONField(serialize = false)
    public String getLimitApp() {
        return rule.getLimitApp();
    }

    /** @return 受保护资源名 */
    @JsonIgnore
    @JSONField(serialize = false)
    public String getResource() {
        return rule.getResource();
    }

    /** @return 授权策略（白名单/黑名单） */
    @JsonIgnore
    @JSONField(serialize = false)
    public int getStrategy() {
        return rule.getStrategy();
    }
}
