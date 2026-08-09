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

import java.util.Date;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.Rule;

/**
 * 规则实体抽象基类，封装 Dashboard 侧通用元数据与 {@link AbstractRule} 载荷。
 * <p>子类通过泛型 {@code T} 绑定具体规则类型，{@link #toRule()} 直接返回内存中的 rule 对象。
 *
 * @author Eric Zhao
 * @since 0.2.1
 */
public abstract class AbstractRuleEntity<T extends AbstractRule> implements RuleEntity {

    /** Dashboard 仓库中的规则主键 id。 */
    protected Long id;

    /** 规则所属应用名。 */
    protected String app;
    /** 规则绑定的客户端机器 IP。 */
    protected String ip;
    /** 规则绑定的客户端机器端口。 */
    protected Integer port;

    /** 具体 Sentinel 规则对象（流控/授权/热点参数等）。 */
    protected T rule;

    /** 记录创建时间。 */
    private Date gmtCreate;
    /** 记录最后修改时间。 */
    private Date gmtModified;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getApp() {
        return app;
    }

    public AbstractRuleEntity<T> setApp(String app) {
        this.app = app;
        return this;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public AbstractRuleEntity<T> setIp(String ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public AbstractRuleEntity<T> setPort(Integer port) {
        this.port = port;
        return this;
    }

    /** @return 内嵌的 Sentinel 规则对象 */
    public T getRule() {
        return rule;
    }

    /** @param rule 要绑定的规则对象 */
    public AbstractRuleEntity<T> setRule(T rule) {
        this.rule = rule;
        return this;
    }

    @Override
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public AbstractRuleEntity<T> setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public AbstractRuleEntity<T> setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    /** 将实体转换为客户端可用的 {@link Rule} 实例。 */
    @Override
    public T toRule() {
        return rule;
    }
}
